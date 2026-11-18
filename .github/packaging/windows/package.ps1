$ErrorActionPreference = 'Stop'
$work = Join-Path $env:BUILD_DIR ('portable-windows-' + [guid]::NewGuid().ToString())
$inputDir = Join-Path $work 'input'
$imageDir = Join-Path $work 'image'
$dist = Join-Path $env:BUILD_DIR 'dist'
New-Item -ItemType Directory -Force $inputDir, $imageDir, $dist | Out-Null
Copy-Item (Join-Path $env:BUILD_DIR 'portable-dependencies\*.jar') $inputDir
Copy-Item (Join-Path $env:BUILD_DIR $env:APP_JAR) (Join-Path $inputDir 'omni-viewer.jar')

# Convert the shared PNG into a Windows icon without requiring ImageMagick.
Add-Type -AssemblyName System.Drawing
$source = [System.Drawing.Image]::FromFile((Resolve-Path '.github/assets/icon.png').Path)
$bitmap = New-Object System.Drawing.Bitmap 256, 256
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$graphics.DrawImage($source, 0, 0, 256, 256)
$png = New-Object System.IO.MemoryStream
$bitmap.Save($png, [System.Drawing.Imaging.ImageFormat]::Png)
$graphics.Dispose(); $bitmap.Dispose(); $source.Dispose()
$bytes = $png.ToArray(); $png.Dispose()
$ico = Join-Path $work 'icon.ico'
$writer = New-Object System.IO.BinaryWriter ([System.IO.File]::Create($ico))
$writer.Write([uint16]0); $writer.Write([uint16]1); $writer.Write([uint16]1)
$writer.Write([byte]0); $writer.Write([byte]0); $writer.Write([byte]0); $writer.Write([byte]0)
$writer.Write([uint16]1); $writer.Write([uint16]32); $writer.Write([uint32]$bytes.Length); $writer.Write([uint32]22); $writer.Write($bytes); $writer.Dispose()

& (Join-Path $env:JDK_HOME 'bin\jpackage.exe') --type app-image --name OmniViewer --icon $ico --input $inputDir --dest $imageDir --main-jar omni-viewer.jar --main-class com.cgvsu.Main --java-options=--enable-native-access=ALL-UNNAMED
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$archive = Join-Path $work 'nsis.zip'
$expectedHash = 'ba63dffc4410ee89193e1cb5a41989991bd77c61068da17e3156d136b7b0b3d8'
$downloadUrls = @(
    'https://downloads.sourceforge.net/project/nsis/NSIS%203/3.13/nsis-3.13.zip',
    'https://prdownloads.sourceforge.net/nsis/nsis-3.13.zip?download'
)
$downloaded = $false
for ($attempt = 1; $attempt -le 5; $attempt++) {
    Remove-Item $archive -Force -ErrorAction SilentlyContinue
    try {
        $downloadUrl = $downloadUrls[($attempt - 1) % $downloadUrls.Count]
        Invoke-WebRequest $downloadUrl -OutFile $archive -TimeoutSec 120
        $hashOutput = & certutil.exe -hashfile $archive SHA256
        if ($LASTEXITCODE -ne 0) { throw 'certutil could not calculate the archive SHA-256' }
        $actualHash = ($hashOutput | Where-Object { $_ -match '^([0-9a-fA-F]{2}\s*){32}$' } | Select-Object -First 1) -replace '\s', ''
        if ($actualHash -eq $expectedHash) {
            $downloaded = $true
            break
        }
        Write-Warning "NSIS download attempt $attempt had unexpected SHA-256 '$actualHash'; retrying."
    }
    catch {
        Write-Warning "NSIS download attempt $attempt failed: $_"
    }
    if ($attempt -lt 5) { Start-Sleep -Seconds ([Math]::Min(5 * $attempt, 20)) }
}
if (-not $downloaded) { throw 'Could not download a valid NSIS 3.13 archive after 5 attempts.' }
Expand-Archive $archive -DestinationPath $work
$exe = Join-Path $work 'OmniViewer-windows-x64.exe'
& (Join-Path $work 'nsis-3.13\makensis.exe') "/DAPP_IMAGE=$imageDir\OmniViewer" "/DOUTPUT=$exe" "/DICON=$ico" '.github/packaging/windows/portable.nsi'
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Move-Item -Force $exe (Join-Path $dist 'OmniViewer-windows-x64.exe')
