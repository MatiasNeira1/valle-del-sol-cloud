param(
  [Parameter(Mandatory=$true)][string]$ApimUrl,
  [string]$AccessToken = $env:ACCESS_TOKEN,
  [string]$TokenWithoutRole = $env:TOKEN_WITHOUT_ROLE,
  [string]$AdminToken = $env:ADMIN_TOKEN,
  [string]$AllowedOrigin = 'http://localhost:5173'
)
$ErrorActionPreference = 'Stop'
$base = $ApimUrl.TrimEnd('/')

function Assert-Status([string]$Name, [string]$Method, [string]$Uri, [hashtable]$Headers, [int[]]$Expected, [string]$Body = $null) {
  try {
    $params = @{ Method=$Method; Uri=$Uri; Headers=$Headers; SkipHttpErrorCheck=$true }
    if ($Body) { $params.ContentType='application/json'; $params.Body=$Body }
    $response = Invoke-WebRequest @params
    if ($Expected -notcontains [int]$response.StatusCode) { throw "$Name returned $($response.StatusCode); expected $($Expected -join '/')" }
    Write-Host "PASS $Name -> $($response.StatusCode)"
    return $response
  } catch { Write-Error "FAIL $Name: $($_.Exception.Message)" }
}

Assert-Status 'missing token' GET "$base/api/reportes" @{} @(401)
Assert-Status 'invalid token' GET "$base/api/reportes" @{Authorization='Bearer invalid.jwt.value'} @(401)
Assert-Status 'CORS preflight' OPTIONS "$base/api/reportes" @{Origin=$AllowedOrigin;'Access-Control-Request-Method'='GET';'Access-Control-Request-Headers'='authorization,content-type'} @(200,204)
if ($AccessToken) { Assert-Status 'valid token' GET "$base/api/reportes" @{Authorization="Bearer $AccessToken"} @(200) }
else { Write-Warning 'ACCESS_TOKEN not provided; valid-token test skipped.' }
if ($TokenWithoutRole) { Assert-Status 'valid token without role' DELETE "$base/api/reportes/eliminar/0" @{Authorization="Bearer $TokenWithoutRole"} @(403) }
else { Write-Warning 'TOKEN_WITHOUT_ROLE not provided; 403 test skipped.' }
if ($AdminToken) {
  $payload = @{titulo='SMOKE TEST';descripcion='Registro temporal de prueba';latitud=-33.45;longitud=-70.66} | ConvertTo-Json
  $created = Assert-Status 'create report' POST "$base/api/reportes/crear" @{Authorization="Bearer $AdminToken"} @(201) $payload
  $id = ($created.Content | ConvertFrom-Json).id
  if ($id) { Assert-Status 'delete report' DELETE "$base/api/reportes/eliminar/$id" @{Authorization="Bearer $AdminToken"} @(204) }
} else { Write-Warning 'ADMIN_TOKEN not provided; CRUD test skipped.' }
