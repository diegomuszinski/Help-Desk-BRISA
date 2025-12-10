# =============================================================================
# HelpDesk K6 Load Testing Runner
# =============================================================================

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet('load', 'spike', 'stress', 'soak', 'all')]
    [string]$TestType = 'load',
    
    [Parameter(Mandatory=$false)]
    [string]$BaseUrl = 'http://localhost:8080',
    
    [Parameter(Mandatory=$false)]
    [string]$OutputDir = '.\results'
)

function Write-TestHeader {
    param([string]$TestName)
    
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "Running $TestName Test" -ForegroundColor Cyan
    Write-Host "========================================`n" -ForegroundColor Cyan
}

function Run-K6Test {
    param(
        [string]$TestFile,
        [string]$TestName,
        [string]$OutputFile
    )
    
    Write-TestHeader $TestName
    
    $env:BASE_URL = $BaseUrl
    
    k6 run `
        --out json="$OutputFile" `
        --summary-export="$OutputDir\${TestName}-summary.json" `
        $TestFile
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n✅ $TestName test completed successfully" -ForegroundColor Green
    } else {
        Write-Host "`n❌ $TestName test failed" -ForegroundColor Red
    }
}

# Create output directory
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

Write-Host "HelpDesk K6 Load Testing Suite" -ForegroundColor Yellow
Write-Host "Base URL: $BaseUrl" -ForegroundColor Yellow
Write-Host "Output Directory: $OutputDir`n" -ForegroundColor Yellow

# Check if k6 is installed
if (-not (Get-Command k6 -ErrorAction SilentlyContinue)) {
    Write-Host "❌ K6 is not installed!" -ForegroundColor Red
    Write-Host "Install K6 from: https://k6.io/docs/getting-started/installation/" -ForegroundColor Yellow
    Write-Host "Or using Chocolatey: choco install k6" -ForegroundColor Yellow
    exit 1
}

switch ($TestType) {
    'load' {
        Run-K6Test -TestFile ".\load-test.js" -TestName "Load" -OutputFile "$OutputDir\load-test-results.json"
    }
    'spike' {
        Run-K6Test -TestFile ".\spike-test.js" -TestName "Spike" -OutputFile "$OutputDir\spike-test-results.json"
    }
    'stress' {
        Run-K6Test -TestFile ".\stress-test.js" -TestName "Stress" -OutputFile "$OutputDir\stress-test-results.json"
    }
    'soak' {
        Run-K6Test -TestFile ".\soak-test.js" -TestName "Soak" -OutputFile "$OutputDir\soak-test-results.json"
    }
    'all' {
        Run-K6Test -TestFile ".\load-test.js" -TestName "Load" -OutputFile "$OutputDir\load-test-results.json"
        Start-Sleep -Seconds 30
        Run-K6Test -TestFile ".\spike-test.js" -TestName "Spike" -OutputFile "$OutputDir\spike-test-results.json"
        Start-Sleep -Seconds 30
        Run-K6Test -TestFile ".\stress-test.js" -TestName "Stress" -OutputFile "$OutputDir\stress-test-results.json"
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Load Testing Complete!" -ForegroundColor Cyan
Write-Host "Results saved to: $OutputDir" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan
