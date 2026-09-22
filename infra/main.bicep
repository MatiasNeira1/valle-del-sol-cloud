targetScope = 'subscription'

@description('Azure region, for example eastus2.')
param location string = 'eastus2'
@description('Short globally-unique suffix, lowercase letters and numbers only.')
param suffix string
param publisherEmail string
@allowed(['Consumption', 'Developer']) param apimSkuName string = 'Consumption'
@allowed(['Basic', 'Standard']) param acrSkuName string = 'Basic'
param postgresSkuName string = 'Standard_B1ms'
@secure()
param postgresAdminPassword string
param tenantId string
@description('Application (client) ID GUID de la aplicación API. Para tokens v2 no anteponer api://.')
param apiAudience string
param entraIssuerUri string
param corsAllowedOrigins string
param gatewayImage string
param alertasImage string
param mapaImage string
param notificacionesImage string

var resourceGroupName = 'rg-valle-sol-${suffix}'

resource resourceGroup 'Microsoft.Resources/resourceGroups@2024-03-01' = {
  name: resourceGroupName
  location: location
}

module platform './platform.bicep' = {
  name: 'valle-del-sol-platform'
  scope: resourceGroup
  params: {
    location: location
    suffix: suffix
    publisherEmail: publisherEmail
    apimSkuName: apimSkuName
    acrSkuName: acrSkuName
    postgresSkuName: postgresSkuName
    postgresAdminPassword: postgresAdminPassword
    tenantId: tenantId
    apiAudience: apiAudience
    entraIssuerUri: entraIssuerUri
    corsAllowedOrigins: corsAllowedOrigins
    gatewayImage: gatewayImage
    alertasImage: alertasImage
    mapaImage: mapaImage
    notificacionesImage: notificacionesImage
  }
}

output resourceGroupName string = resourceGroup.name
output apimGatewayUrl string = platform.outputs.apimGatewayUrl
output staticWebAppHostName string = platform.outputs.staticWebAppHostName
