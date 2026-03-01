Param(
    [string]$ServiceName = "WayangService",
    [string]$WayangHome = "$env:USERPROFILE\.wayang",
    [string]$GollekHome = "",
    [string]$BinaryPath = ""
)

$ErrorActionPreference = "Stop"

function Ensure-GollekHome {
    param(
        [string]$PrimaryGollekHome,
        [string]$FallbackGollekHome
    )

    try {
        New-Item -ItemType Directory -Force -Path "$PrimaryGollekHome\models" | Out-Null
        New-Item -ItemType Directory -Force -Path "$PrimaryGollekHome\storage" | Out-Null
        return $PrimaryGollekHome
    } catch {
        New-Item -ItemType Directory -Force -Path "$FallbackGollekHome\models" | Out-Null
        New-Item -ItemType Directory -Force -Path "$FallbackGollekHome\storage" | Out-Null
        return $FallbackGollekHome
    }
}

New-Item -ItemType Directory -Force -Path "$WayangHome\config" | Out-Null
New-Item -ItemType Directory -Force -Path "$WayangHome\logs" | Out-Null
New-Item -ItemType Directory -Force -Path "$WayangHome\plugins" | Out-Null
New-Item -ItemType Directory -Force -Path "$WayangHome\secrets" | Out-Null

if (-not $GollekHome) {
    $GollekHome = "$WayangHome\gollek"
}
$legacyGollekHome = if ($env:GOLLEK_HOME) { $env:GOLLEK_HOME } else { "$env:USERPROFILE\.gollek" }
$GollekHome = Ensure-GollekHome -PrimaryGollekHome $GollekHome -FallbackGollekHome $legacyGollekHome

if (-not $BinaryPath) {
    $candidates = @(
        "$WayangHome\bin\wayang.exe",
        "$WayangHome\wayang.exe",
        "$WayangHome\bin\wayang-standalone-windows-x86_64.exe",
        "$WayangHome\wayang-standalone-windows-x86_64.exe",
        "$env:LOCALAPPDATA\Microsoft\WindowsApps\wayang.exe"
    )
    $BinaryPath = $candidates | Where-Object { Test-Path $_ } | Select-Object -First 1
}

if (-not $BinaryPath -or !(Test-Path $BinaryPath)) {
    throw "Binary not found. Set -BinaryPath explicitly."
}

if (Get-Service -Name $ServiceName -ErrorAction SilentlyContinue) {
    sc.exe stop $ServiceName | Out-Null
    sc.exe delete $ServiceName | Out-Null
    Start-Sleep -Seconds 1
}

$bin = '"' + $BinaryPath + '"'
sc.exe create $ServiceName binPath= $bin start= auto DisplayName= "Wayang Standalone Service" | Out-Null
sc.exe failure $ServiceName reset= 0 actions= restart/5000/restart/5000/restart/5000 | Out-Null

$serviceRegistryPath = "HKLM:\SYSTEM\CurrentControlSet\Services\$ServiceName"
$serviceEnvironment = @(
    "WAYANG_HOME=$WayangHome",
    "WAYANG_GOLLEK_HOME=$GollekHome",
    "GOLLEK_HOME=$GollekHome"
)
New-ItemProperty -Path $serviceRegistryPath -Name Environment -PropertyType MultiString -Value $serviceEnvironment -Force | Out-Null

sc.exe start $ServiceName | Out-Null

Write-Host "Wayang Windows service installed: $ServiceName"
Write-Host "Check status with: Get-Service -Name $ServiceName"
