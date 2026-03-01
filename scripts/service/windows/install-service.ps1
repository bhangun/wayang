Param(
    [string]$ServiceName = "WayangService",
    [string]$WayangHome = "$env:USERPROFILE\.wayang",
    [string]$GollekHome = "",
    [string]$BinaryPath = "$env:USERPROFILE\.wayang\wayang.exe"
)

$ErrorActionPreference = "Stop"

if (-not $GollekHome) {
    $GollekHome = "$WayangHome\gollek"
}

New-Item -ItemType Directory -Force -Path "$WayangHome\config" | Out-Null
New-Item -ItemType Directory -Force -Path "$WayangHome\logs" | Out-Null
New-Item -ItemType Directory -Force -Path "$WayangHome\plugins" | Out-Null
New-Item -ItemType Directory -Force -Path "$WayangHome\secrets" | Out-Null
New-Item -ItemType Directory -Force -Path "$GollekHome\models" | Out-Null
New-Item -ItemType Directory -Force -Path "$GollekHome\storage" | Out-Null

if (!(Test-Path $BinaryPath)) {
    throw "Binary not found at $BinaryPath"
}

$env:WAYANG_HOME = $WayangHome
$env:WAYANG_GOLLEK_HOME = $GollekHome
$env:GOLLEK_HOME = $GollekHome

if (Get-Service -Name $ServiceName -ErrorAction SilentlyContinue) {
    sc.exe stop $ServiceName | Out-Null
    sc.exe delete $ServiceName | Out-Null
    Start-Sleep -Seconds 1
}

$bin = '"' + $BinaryPath + '"'
sc.exe create $ServiceName binPath= $bin start= auto DisplayName= "Wayang Standalone Service" | Out-Null
sc.exe failure $ServiceName reset= 0 actions= restart/5000/restart/5000/restart/5000 | Out-Null
sc.exe start $ServiceName | Out-Null

Write-Host "Wayang Windows service installed: $ServiceName"
Write-Host "Check status with: Get-Service -Name $ServiceName"
