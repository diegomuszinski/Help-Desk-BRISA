# =============================================================================
# HelpDesk Database Restore Script
# =============================================================================
# Este script restaura um backup do banco de dados PostgreSQL do HelpDesk
#
# Uso:
#   .\restore-database.ps1 -BackupFile "backups\helpdesk_backup_20250101_120000.sql.gz"
#   .\restore-database.ps1 -BackupFile "backups\helpdesk_backup_20250101_120000.sql" -Force
# =============================================================================

param(
    [Parameter(Mandatory=$true)]
    [string]$BackupFile,
    
    [Parameter(Mandatory=$false)]
    [string]$DatabaseName = "helpdesk",
    
    [Parameter(Mandatory=$false)]
    [string]$DatabaseHost = "localhost",
    
    [Parameter(Mandatory=$false)]
    [int]$DatabasePort = 5432,
    
    [Parameter(Mandatory=$false)]
    [string]$DatabaseUser = "postgres",
    
    [Parameter(Mandatory=$false)]
    [switch]$Force = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$CreateDatabase = $false
)

# =============================================================================
# Functions
# =============================================================================

function Write-Log {
    param(
        [string]$Message,
        [string]$Level = "INFO"
    )
    
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [$Level] $Message"
    
    switch ($Level) {
        "ERROR" { Write-Host $logMessage -ForegroundColor Red }
        "WARN"  { Write-Host $logMessage -ForegroundColor Yellow }
        "SUCCESS" { Write-Host $logMessage -ForegroundColor Green }
        default { Write-Host $logMessage }
    }
}

function Invoke-DatabaseRestore {
    param(
        [string]$HostName,
        [int]$Port,
        [string]$User,
        [string]$Database,
        [string]$BackupFile
    )
    
    try {
        $env:PGPASSWORD = $env:DB_PASSWORD
        
        Write-Log "Starting restore of database '$Database'..."
        
        # Check if file is compressed
        if ($BackupFile -match "\.gz$") {
            Write-Log "Decompressing backup file..."
            
            $fileStream = [System.IO.File]::OpenRead($BackupFile)
            $gzipStream = New-Object System.IO.Compression.GZipStream($fileStream, [System.IO.Compression.CompressionMode]::Decompress)
            
            $tempFile = [System.IO.Path]::GetTempFileName()
            $outputStream = [System.IO.File]::Create($tempFile)
            
            $gzipStream.CopyTo($outputStream)
            
            $outputStream.Close()
            $gzipStream.Close()
            $fileStream.Close()
            
            $sqlFile = $tempFile
        }
        else {
            $sqlFile = $BackupFile
        }
        
        # Restore database
        & psql -h $HostName -p $Port -U $User -d $Database -f $sqlFile
        
        if ($LASTEXITCODE -ne 0) {
            throw "psql restore failed with exit code $LASTEXITCODE"
        }
        
        # Clean up temp file
        if ($BackupFile -match "\.gz$") {
            Remove-Item $tempFile -Force
        }
        
        return $true
    }
    catch {
        Write-Log "Restore failed: $($_.Exception.Message)" -Level "ERROR"
        return $false
    }
}

# =============================================================================
# Main Script
# =============================================================================

Write-Log "==================================================" -Level "SUCCESS"
Write-Log "HelpDesk Database Restore Script" -Level "SUCCESS"
Write-Log "==================================================" -Level "SUCCESS"

# Check if DB_PASSWORD environment variable is set
if (-not $env:DB_PASSWORD) {
    Write-Log "ERROR: DB_PASSWORD environment variable not set!" -Level "ERROR"
    exit 1
}

# Check if backup file exists
if (-not (Test-Path $BackupFile)) {
    Write-Log "ERROR: Backup file not found: $BackupFile" -Level "ERROR"
    exit 1
}

# Confirm restore
if (-not $Force) {
    Write-Log "WARNING: This will REPLACE all data in database '$DatabaseName'!" -Level "WARN"
    $confirmation = Read-Host "Are you sure you want to continue? (yes/no)"
    if ($confirmation -ne "yes") {
        Write-Log "Restore cancelled by user"
        exit 0
    }
}

# Perform restore
$startTime = Get-Date
$success = Invoke-DatabaseRestore -Host $DatabaseHost -Port $DatabasePort -User $DatabaseUser -Database $DatabaseName -BackupFile $BackupFile
$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds

if ($success) {
    Write-Log "Restore completed successfully!" -Level "SUCCESS"
    Write-Log "Duration: ${duration} seconds"
    exit 0
}
else {
    Write-Log "Restore failed!" -Level "ERROR"
    exit 1
}
