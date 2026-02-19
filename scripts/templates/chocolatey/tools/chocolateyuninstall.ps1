# Chocolatey uninstallation script for Wayang

$ErrorActionPreference = 'Continue'

$packageName = 'wayang'

# Remove shims
$shimPath = Join-Path $env:ChocolateyInstall "bin\wayang.exe"
if (Test-Path $shimPath) {
    Remove-Item $shimPath -Force
}

# Remove installation directory
$installDir = Join-Path $env:ChocolateyInstall "lib\$packageName"
if (Test-Path $installDir) {
    Remove-Item $installDir -Recurse -Force
}

Write-Host "Wayang has been uninstalled successfully!"
