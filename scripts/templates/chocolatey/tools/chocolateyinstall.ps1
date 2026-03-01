# Chocolatey installation script for Wayang

$ErrorActionPreference = 'Stop'

$packageName = 'wayang'
$version = '{{projectVersion}}'
$arch = 'x86_64'

# Download URL
$url = "https://github.com/bhangun/wayang/releases/download/v${version}/wayang-standalone-windows-${arch}.exe"

# SHA256 checksum
$checksum = '{{sha256_windows_x86_64}}'

# Installation directory
$installDir = Join-Path $env:ChocolateyInstall "lib\$packageName\tools"
$binDir = Join-Path $env:ChocolateyInstall "bin"

# Create installation directory
if (!(Test-Path $installDir)) {
    New-Item -ItemType Directory -Path $installDir | Out-Null
}

# Download and install
$wayangHome = Join-Path $env:USERPROFILE ".wayang"
$gollekHome = Join-Path $wayangHome "gollek"
$exePath = Join-Path $wayangHome "wayang.exe"

$packageArgs = @{
    PackageName    = $packageName
    FileType       = 'exe'
    File64Bit      = $url
    Url64Bit       = $url
    Checksum       = $checksum
    ChecksumType   = 'sha256'
    SilentArgs     = ''
    ValidExitCodes = @(0)
}

# Ensure default local state directories exist
New-Item -ItemType Directory -Force -Path (Join-Path $wayangHome "config") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $wayangHome "logs") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $wayangHome "plugins") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $wayangHome "secrets") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $gollekHome "models") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $gollekHome "storage") | Out-Null

# Download the binary
Get-ChocolateyWebFile -PackageName $packageName `
    -FileFullPath $exePath `
    -Url $url `
    -Checksum $checksum `
    -ChecksumType 'sha256'

# Create shim for easy access
Install-ChocolateyPath -PathToInstall $installDir -FileType 'exe'

# Create shims
Write-Host "Creating shim for wayang.exe"
$shimPath = Join-Path $binDir "wayang.exe"
if (Test-Path $shimPath) {
    Remove-Item $shimPath -Force
}

# Use Chocolatey's shim creation
Install-BinFile -Name "wayang" -Path $exePath

Write-Host "Wayang v$version has been installed successfully!"
Write-Host ""
Write-Host "Default data directory: $wayangHome"
Write-Host "To get started, run: wayang --help"
