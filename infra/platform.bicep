param location string
param suffix string
param publisherEmail string
param apimSkuName string
param acrSkuName string
param postgresSkuName string
@secure() param postgresAdminPassword string
param tenantId string
@description('Application (client) ID GUID de la aplicación API; valor esperado en aud para access tokens v2.')
param apiAudience string
param entraIssuerUri string
param corsAllowedOrigins string
param gatewayImage string
param alertasImage string
param mapaImage string
param notificacionesImage string

var acrName = 'acrvalledelsol${suffix}'
var keyVaultName = 'kv-valle-${suffix}'
var postgresName = 'psql-valle-${suffix}'
var commonEnv = [
  { name: 'DB_HOST', value: '${postgresName}.postgres.database.azure.com' }
  { name: 'DB_PORT', value: '5432' }
  { name: 'DB_NAME', value: 'valle_del_sol' }
  { name: 'DB_USERNAME', value: 'valleadmin' }
  { name: 'DB_SSL_MODE', value: 'require' }
  { name: 'DB_PASSWORD', secretRef: 'db-password' }
  { name: 'AZURE_TENANT_ID', value: tenantId }
  { name: 'AZURE_API_AUDIENCE', value: apiAudience }
  { name: 'SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI', value: entraIssuerUri }
  { name: 'CORS_ALLOWED_ORIGINS', value: corsAllowedOrigins }
  { name: 'SPRING_PROFILES_ACTIVE', value: 'prod' }
  { name: 'APPLICATIONINSIGHTS_CONNECTION_STRING', value: appInsights.properties.ConnectionString }
]

resource logs 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: 'log-valle-${suffix}'
  location: location
  properties: { retentionInDays: 30, features: { enableLogAccessUsingOnlyResourcePermissions: true } }
  sku: { name: 'PerGB2018' }
}

resource appInsights 'Microsoft.Insights/components@2020-02-02' = {
  name: 'appi-valle-${suffix}'
  location: location
  kind: 'web'
  properties: { Application_Type: 'web', WorkspaceResourceId: logs.id }
}

resource identity 'Microsoft.ManagedIdentity/userAssignedIdentities@2023-01-31' = {
  name: 'id-valle-${suffix}'
  location: location
}

resource acr 'Microsoft.ContainerRegistry/registries@2023-07-01' = {
  name: acrName
  location: location
  sku: { name: acrSkuName }
  properties: { adminUserEnabled: false }
}

resource acrPull 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(acr.id, identity.id, 'acrpull')
  scope: acr
  properties: {
    principalId: identity.properties.principalId
    principalType: 'ServicePrincipal'
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '7f951dda-4ed3-4680-a7ca-43fe172d538d')
  }
}

resource keyVault 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: keyVaultName
  location: location
  properties: {
    tenantId: tenantId
    enableRbacAuthorization: true
    enableSoftDelete: true
    softDeleteRetentionInDays: 7
    sku: { family: 'A', name: 'standard' }
    accessPolicies: []
  }
}

resource dbSecret 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: keyVault
  name: 'postgres-admin-password'
  properties: { value: postgresAdminPassword }
}

resource kvSecretsUser 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(keyVault.id, identity.id, 'secrets-user')
  scope: keyVault
  properties: {
    principalId: identity.properties.principalId
    principalType: 'ServicePrincipal'
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '4633458b-17de-408a-b874-0445c86b69e6')
  }
}

resource postgres 'Microsoft.DBforPostgreSQL/flexibleServers@2024-08-01' = {
  name: postgresName
  location: location
  sku: { name: postgresSkuName, tier: 'Burstable' }
  properties: {
    administratorLogin: 'valleadmin'
    administratorLoginPassword: postgresAdminPassword
    version: '16'
    storage: { storageSizeGB: 32, autoGrow: 'Enabled' }
    backup: { backupRetentionDays: 7, geoRedundantBackup: 'Disabled' }
    highAvailability: { mode: 'Disabled' }
  }
}

resource database 'Microsoft.DBforPostgreSQL/flexibleServers/databases@2024-08-01' = {
  parent: postgres
  name: 'valle_del_sol'
  properties: { charset: 'UTF8', collation: 'en_US.utf8' }
}

resource postgresAzureAccess 'Microsoft.DBforPostgreSQL/flexibleServers/firewallRules@2024-08-01' = {
  parent: postgres
  name: 'AllowAzureServices'
  properties: { startIpAddress: '0.0.0.0', endIpAddress: '0.0.0.0' }
}

resource environment 'Microsoft.App/managedEnvironments@2024-03-01' = {
  name: 'cae-valle-${suffix}'
  location: location
  properties: {
    appLogsConfiguration: { destination: 'log-analytics', logAnalyticsConfiguration: { customerId: logs.properties.customerId, sharedKey: logs.listKeys().primarySharedKey } }
  }
}

resource alertas 'Microsoft.App/containerApps@2024-03-01' = {
  name: 'ca-alertas-${suffix}'
  location: location
  identity: { type: 'UserAssigned', userAssignedIdentities: { '${identity.id}': {} } }
  properties: {
    managedEnvironmentId: environment.id
    configuration: {
      activeRevisionsMode: 'Single'
      ingress: { external: false, targetPort: 8081, transport: 'http' }
      registries: [{ server: acr.properties.loginServer, identity: identity.id }]
      secrets: [{ name: 'db-password', keyVaultUrl: dbSecret.properties.secretUriWithVersion, identity: identity.id }]
    }
    template: { containers: [{ name: 'alertas', image: alertasImage, env: commonEnv, resources: { cpu: json('0.5'), memory: '1Gi' } }], scale: { minReplicas: 0, maxReplicas: 2 } }
  }
  dependsOn: [acrPull, kvSecretsUser]
}

resource mapa 'Microsoft.App/containerApps@2024-03-01' = {
  name: 'ca-mapa-${suffix}'
  location: location
  identity: { type: 'UserAssigned', userAssignedIdentities: { '${identity.id}': {} } }
  properties: {
    managedEnvironmentId: environment.id
    configuration: { activeRevisionsMode: 'Single', ingress: { external: false, targetPort: 8082, transport: 'http' }, registries: [{ server: acr.properties.loginServer, identity: identity.id }], secrets: [{ name: 'db-password', keyVaultUrl: dbSecret.properties.secretUriWithVersion, identity: identity.id }] }
    template: { containers: [{ name: 'mapa', image: mapaImage, env: concat(commonEnv, [{ name: 'MS_ALERTA_URL', value: 'https://${alertas.properties.configuration.ingress.fqdn}' }]), resources: { cpu: json('0.5'), memory: '1Gi' } }], scale: { minReplicas: 0, maxReplicas: 2 } }
  }
  dependsOn: [acrPull, kvSecretsUser]
}

resource notificaciones 'Microsoft.App/containerApps@2024-03-01' = {
  name: 'ca-notificaciones-${suffix}'
  location: location
  identity: { type: 'UserAssigned', userAssignedIdentities: { '${identity.id}': {} } }
  properties: {
    managedEnvironmentId: environment.id
    configuration: { activeRevisionsMode: 'Single', ingress: { external: false, targetPort: 8083, transport: 'http' }, registries: [{ server: acr.properties.loginServer, identity: identity.id }], secrets: [{ name: 'db-password', keyVaultUrl: dbSecret.properties.secretUriWithVersion, identity: identity.id }] }
    template: { containers: [{ name: 'notificaciones', image: notificacionesImage, env: commonEnv, resources: { cpu: json('0.5'), memory: '1Gi' } }], scale: { minReplicas: 0, maxReplicas: 2 } }
  }
  dependsOn: [acrPull, kvSecretsUser]
}

resource gateway 'Microsoft.App/containerApps@2024-03-01' = {
  name: 'ca-gateway-${suffix}'
  location: location
  identity: { type: 'UserAssigned', userAssignedIdentities: { '${identity.id}': {} } }
  properties: {
    managedEnvironmentId: environment.id
    configuration: { activeRevisionsMode: 'Single', ingress: { external: true, targetPort: 8080, transport: 'http', allowInsecure: false }, registries: [{ server: acr.properties.loginServer, identity: identity.id }] }
    template: {
      containers: [{ name: 'gateway', image: gatewayImage, env: [
        { name: 'AZURE_TENANT_ID', value: tenantId }, { name: 'AZURE_API_AUDIENCE', value: apiAudience }, { name: 'SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI', value: entraIssuerUri }, { name: 'CORS_ALLOWED_ORIGINS', value: corsAllowedOrigins },
        { name: 'MS_ALERTA_URL', value: 'https://${alertas.properties.configuration.ingress.fqdn}' }, { name: 'MS_MONITOREO_URL', value: 'https://${mapa.properties.configuration.ingress.fqdn}' }, { name: 'MS_NOTIFICACIONES_URL', value: 'https://${notificaciones.properties.configuration.ingress.fqdn}' },
        { name: 'APPLICATIONINSIGHTS_CONNECTION_STRING', value: appInsights.properties.ConnectionString }
      ], resources: { cpu: json('0.5'), memory: '1Gi' } }]
      scale: { minReplicas: 0, maxReplicas: 2 }
    }
  }
  dependsOn: [acrPull]
}

resource apim 'Microsoft.ApiManagement/service@2024-05-01' = {
  name: 'apim-valle-${suffix}'
  location: location
  sku: { name: apimSkuName, capacity: apimSkuName == 'Consumption' ? 0 : 1 }
  properties: { publisherEmail: publisherEmail, publisherName: 'Municipalidad Valle del Sol' }
}

resource api 'Microsoft.ApiManagement/service/apis@2024-05-01' = {
  parent: apim
  name: 'valle-del-sol'
  properties: { displayName: 'Valle del Sol Emergency API', path: '', protocols: ['https'], serviceUrl: 'https://${gateway.properties.configuration.ingress.fqdn}', subscriptionRequired: false }
}

resource staticWebApp 'Microsoft.Web/staticSites@2023-12-01' = {
  name: 'swa-valle-${suffix}'
  location: location
  sku: { name: 'Free', tier: 'Free' }
  properties: { allowConfigFileUpdates: true }
}

output apimGatewayUrl string = 'https://${apim.name}.azure-api.net'
output gatewayOrigin string = 'https://${gateway.properties.configuration.ingress.fqdn}'
output staticWebAppHostName string = staticWebApp.properties.defaultHostname
output registryName string = acr.name
