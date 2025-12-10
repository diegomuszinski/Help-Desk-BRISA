# =============================================================================
# HelpDesk Database Backup Script
# =============================================================================
# Este script realiza backup automático do banco de dados PostgreSQL do HelpDesk
# e mantém um histórico de backups com rotação automática.
#
# Uso:
#   .\backup-database.ps1
#   .\backup-database.ps1 -KeepDays 30
#   .\backup-database.ps1 -BackupDir "D:\Backups"
# =============================================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$BackupDir = ".\backups",
    
    [Parameter(Mandatory=$false)]
    [int]$KeepDays = 7,
    
    [Parameter(Mandatory=$false)]
    [string]$DatabaseName = "helpdesk",
    
    [Parameter(Mandatory=$false)]
    [string]$DatabaseHost = "localhost",
    
    [Parameter(Mandatory=$false)]
    [int]$DatabasePort = 5432,
    
    [Parameter(Mandatory=$false)]
    [string]$DatabaseUser = "postgres",
    
    [Parameter(Mandatory=$false)]
    [switch]$Compress,
    
    [Parameter(Mandatory=$false)]
    [switch]$Verbose
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
    
    # Log to file
    $logFile = Join-Path $BackupDir "backup.log"
    Add-Content -Path $logFile -Value $logMessage
}

function Test-PostgreSQLConnection {
    param(
        [string]$Host,
        [int]$Port,
        [string]$User,
        [string]$Database
    )
    
    try {
        $env:PGPASSWORD = $env:DB_PASSWORD
        $testQuery = "SELECT 1"
        $result = & psql -h $Host -p $Port -U $User -d $Database -c $testQuery 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            return $true
        }
        return $false
    }
    catch {
        return $false
    }
}

function Get-BackupFileName {
    param(
        [string]$Database,
        [bool]$Compress
    )
    
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $fileName = "${Database}_backup_${timestamp}"
    
    if ($Compress) {
        return "${fileName}.sql.gz"
    }
    return "${fileName}.sql"
}

function Invoke-DatabaseBackup {
    param(
        [string]$Host,
        [int]$Port,
        [string]$User,
        [string]$Database,
        [string]$OutputFile,
        [bool]$Compress
    )
    
    try {
        $env:PGPASSWORD = $env:DB_PASSWORD
        
        Write-Log "Starting backup of database '$Database'..."
        
        if ($Compress) {
            # Backup with compression using pg_dump | gzip
            $tempFile = [System.IO.Path]::GetTempFileName()
            
            & pg_dump -h $Host -p $Port -U $User -d $Database -F p -f $tempFile
            
            if ($LASTEXITCODE -ne 0) {
                throw "pg_dump failed with exit code $LASTEXITCODE"
            }
            
            # Compress using .NET compression
            $fileStream = [System.IO.File]::OpenRead($tempFile)
            $outputStream = [System.IO.File]::Create($OutputFile)
            $gzipStream = New-Object System.IO.Compression.GZipStream($outputStream, [System.IO.Compression.CompressionMode]::Compress)
            
            $fileStream.CopyTo($gzipStream)
            
            $gzipStream.Close()
            $outputStream.Close()
            $fileStream.Close()
            
            Remove-Item $tempFile -Force
        }
        else {
            # Backup without compression
            & pg_dump -h $Host -p $Port -U $User -d $Database -F p -f $OutputFile
            
            if ($LASTEXITCODE -ne 0) {
                throw "pg_dump failed with exit code $LASTEXITCODE"
            }
        }
        
        return $true
    }
    catch {
        Write-Log "Backup failed: $($_.Exception.Message)" -Level "ERROR"
        return $false
    }
}

function Remove-OldBackups {
    param(
        [string]$BackupDir,
        [int]$KeepDays
    )
    
    Write-Log "Cleaning up old backups (keeping last $KeepDays days)..."
    
    $cutoffDate = (Get-Date).AddDays(-$KeepDays)
    $backupFiles = Get-ChildItem -Path $BackupDir -Filter "*_backup_*.sql*" | Where-Object { $_.LastWriteTime -lt $cutoffDate }
    
    foreach ($file in $backupFiles) {
        try {
            Remove-Item $file.FullName -Force
            Write-Log "Deleted old backup: $($file.Name)"
        }
        catch {
            Write-Log "Failed to delete $($file.Name): $($_.Exception.Message)" -Level "WARN"
        }
    }
    
    if ($backupFiles.Count -eq 0) {
        Write-Log "No old backups to clean up"
    }
}

function Get-BackupStatistics {
    param(
        [string]$BackupDir
    )
    
    $backupFiles = Get-ChildItem -Path $BackupDir -Filter "*_backup_*.sql*"
    $totalSize = ($backupFiles | Measure-Object -Property Length -Sum).Sum
    $totalSizeGB = [math]::Round($totalSize / 1GB, 2)
    
    Write-Log "=== Backup Statistics ==="
    Write-Log "Total backups: $($backupFiles.Count)"
    Write-Log "Total size: ${totalSizeGB} GB"
    Write-Log "Oldest backup: $(($backupFiles | Sort-Object LastWriteTime | Select-Object -First 1).LastWriteTime)"
    Write-Log "Newest backup: $(($backupFiles | Sort-Object LastWriteTime -Descending | Select-Object -First 1).LastWriteTime)"
}

# =============================================================================
# Main Script
# =============================================================================

Write-Log "==================================================" -Level "SUCCESS"
Write-Log "HelpDesk Database Backup Script" -Level "SUCCESS"
Write-Log "==================================================" -Level "SUCCESS"

# Check if DB_PASSWORD environment variable is set
if (-not $env:DB_PASSWORD) {
    Write-Log "ERROR: DB_PASSWORD environment variable not set!" -Level "ERROR"
    Write-Log "Please set DB_PASSWORD before running this script:" -Level "ERROR"
    Write-Log '  $env:DB_PASSWORD = "your_password"' -Level "ERROR"
    exit 1
}

# Create backup directory if it doesn't exist
if (-not (Test-Path $BackupDir)) {
    try {
        New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
        Write-Log "Created backup directory: $BackupDir" -Level "SUCCESS"
    }
    catch {
        Write-Log "Failed to create backup directory: $($_.Exception.Message)" -Level "ERROR"
        exit 1
    }
}

# Check PostgreSQL connection
Write-Log "Testing database connection..."
if (-not (Test-PostgreSQLConnection -Host $DatabaseHost -Port $DatabasePort -User $DatabaseUser -Database $DatabaseName)) {
    Write-Log "Failed to connect to database!" -Level "ERROR"
    Write-Log "Please verify:" -Level "ERROR"
    Write-Log "  - PostgreSQL is running" -Level "ERROR"
    Write-Log "  - Database '$DatabaseName' exists" -Level "ERROR"
    Write-Log "  - DB_PASSWORD is correct" -Level "ERROR"
    Write-Log "  - pg_dump and psql are in PATH" -Level "ERROR"
    exit 1
}
Write-Log "Database connection successful" -Level "SUCCESS"

# Generate backup file name
$backupFileName = Get-BackupFileName -Database $DatabaseName -Compress $Compress
$backupFilePath = Join-Path $BackupDir $backupFileName

Write-Log "Backup file: $backupFilePath"

# Perform backup
$startTime = Get-Date
$success = Invoke-DatabaseBackup -Host $DatabaseHost -Port $DatabasePort -User $DatabaseUser -Database $DatabaseName -OutputFile $backupFilePath -Compress $Compress
$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds

if ($success) {
    $fileSize = (Get-Item $backupFilePath).Length
    $fileSizeMB = [math]::Round($fileSize / 1MB, 2)
    
    Write-Log "Backup completed successfully!" -Level "SUCCESS"
    Write-Log "File: $backupFilePath"
    Write-Log "Size: ${fileSizeMB} MB"
    Write-Log "Duration: ${duration} seconds"
    
    # Clean up old backups
    Remove-OldBackups -BackupDir $BackupDir -KeepDays $KeepDays
    
    # Show statistics
    Get-BackupStatistics -BackupDir $BackupDir
    
    Write-Log "==================================================" -Level "SUCCESS"
    Write-Log "Backup process completed successfully!" -Level "SUCCESS"
    Write-Log "==================================================" -Level "SUCCESS"
    
    exit 0
}
else {
    Write-Log "Backup failed!" -Level "ERROR"
    
    # Clean up failed backup file
    if (Test-Path $backupFilePath) {
        Remove-Item $backupFilePath -Force
        Write-Log "Cleaned up incomplete backup file"
    }
    
    exit 1
}
