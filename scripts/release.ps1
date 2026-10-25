<#
  Builds a signed APK and publishes it as a GitHub release.
  Phones running the app see the new version in Settings / on the home screen.

  Usage (from the project folder):
    .\scripts\release.ps1 -Version 1.1.0 -Notes "New aartis and a darker night mode"

  Needs: keystore.properties + the .jks file it points to, and `gh auth login` done once.
#>
param(
    [Parameter(Mandatory = $true)][string]$Version,
    [string]$Notes = "Improvements and fixes"
)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

if ($Version -notmatch '^\d+\.\d+\.\d+$') { throw "Version must look like 1.2.3" }
if (-not (Test-Path "keystore.properties")) { throw "keystore.properties is missing - see README (Signing)" }

# Bump versionCode and versionName in gradle.properties (UTF-8, no BOM).
$propsPath = Join-Path $root "gradle.properties"
$utf8 = New-Object System.Text.UTF8Encoding($false)
$props = [IO.File]::ReadAllText($propsPath, $utf8)
$code = [int]([regex]::Match($props, 'aarti\.versionCode=(\d+)').Groups[1].Value) + 1
$props = [regex]::Replace($props, 'aarti\.versionCode=\d+', "aarti.versionCode=$code")
$props = [regex]::Replace($props, 'aarti\.versionName=[^\r\n]+', "aarti.versionName=$Version")
[IO.File]::WriteAllText($propsPath, $props, $utf8)
Write-Host "Version $Version (code $code)"

python scripts/validate_content.py
if ($LASTEXITCODE -ne 0) { throw "content.json has problems" }

& .\gradlew.bat assembleRelease
if ($LASTEXITCODE -ne 0) { throw "Gradle build failed" }

New-Item -ItemType Directory -Force release | Out-Null
$apk = "release\AartiSangrah-v$Version.apk"
Copy-Item "app\build\outputs\apk\release\app-release.apk" $apk -Force

git add -A
git commit -m "Release v$Version"
git tag "v$Version"
git push origin main --tags
if ($LASTEXITCODE -ne 0) { throw "git push failed" }

gh release create "v$Version" $apk --title "Aarti Sangrah v$Version" --notes $Notes
if ($LASTEXITCODE -ne 0) { throw "gh release failed" }
Write-Host "Done: $apk published."
