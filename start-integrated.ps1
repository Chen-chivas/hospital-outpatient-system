param(
  [switch]$BuildFrontend
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backend = Join-Path $root 'backend'
$frontend = Join-Path $root 'frontend'
$jdk21 = 'D:\javaJDK\Java\jdk-21'
$preferredJavaExe = Join-Path $jdk21 'bin\java.exe'

if (Test-Path $preferredJavaExe) {
  $javaExe = $preferredJavaExe
  $env:JAVA_HOME = $jdk21
  $env:Path = (Join-Path $jdk21 'bin') + ';' + $env:Path
} else {
  $javaExe = 'java.exe'
}

$javaVersionProcess = New-Object System.Diagnostics.Process
$javaVersionProcess.StartInfo.FileName = $javaExe
$javaVersionProcess.StartInfo.Arguments = '-version'
$javaVersionProcess.StartInfo.UseShellExecute = $false
$javaVersionProcess.StartInfo.RedirectStandardOutput = $true
$javaVersionProcess.StartInfo.RedirectStandardError = $true
$javaVersionProcess.StartInfo.CreateNoWindow = $true
[void]$javaVersionProcess.Start()
$javaVersionStdout = $javaVersionProcess.StandardOutput.ReadToEnd()
$javaVersionStderr = $javaVersionProcess.StandardError.ReadToEnd()
$javaVersionProcess.WaitForExit()
$javaExitCode = $javaVersionProcess.ExitCode
$javaVersionOutput = @($javaVersionStdout, $javaVersionStderr) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }

if ($javaExitCode -ne 0 -or ($javaVersionOutput -join "`n") -notmatch 'version "(17|18|19|20|21|22|23|24|25)\.') {
  throw "Java 17+ is required. Current java is:`n$($javaVersionOutput -join "`n")"
}

if ($BuildFrontend) {
  Push-Location $frontend
  try {
    npm.cmd run build
  } finally {
    Pop-Location
  }

  $static = Join-Path $backend 'src\main\resources\static'
  New-Item -ItemType Directory -Force $static | Out-Null
  Copy-Item -Recurse -Force (Join-Path $frontend 'dist\*') $static
}

Write-Host 'Starting single integrated project on http://localhost:8080'
Write-Host "  Java home:     $env:JAVA_HOME"
Write-Host '  Database:      H2 file ./.data/ocs'
Write-Host '  Vue console:   http://localhost:8080'
Write-Host '  Outpatient:    http://localhost:8080/outpatient'
Write-Host '  Smart medical: http://localhost:8080/smart'
Write-Host ''

Push-Location $backend
try {
  mvn.cmd -q -DskipTests package
  $jar = Join-Path $backend 'target\ocs-backend-0.1.0-SNAPSHOT.jar'
  & $javaExe -jar $jar
} finally {
  Pop-Location
}
