# HelpDesk Database Backup Guide

## Scripts de Backup

Este diretório contém scripts PowerShell para backup e restore automático do banco de dados PostgreSQL.

## Scripts Disponíveis

### 1. backup-database.ps1
Script para criar backups do banco de dados com compressão e rotação automática.

### 2. restore-database.ps1
Script para restaurar backups do banco de dados.

### 3. scheduled-backup-task.xml
Arquivo de configuração para agendar backups automáticos no Windows Task Scheduler.

## Como Usar

### Backup Manual

```powershell
# Backup simples (mantém últimos 7 dias)
.\backup-database.ps1

# Backup mantendo 30 dias
.\backup-database.ps1 -KeepDays 30

# Backup em diretório específico
.\backup-database.ps1 -BackupDir "D:\Backups\HelpDesk"

# Backup sem compressão
.\backup-database.ps1 -Compress:$false

# Backup de banco em servidor remoto
.\backup-database.ps1 -DatabaseHost "192.168.1.100" -DatabasePort 5432
```

### Restore Manual

```powershell
# Restaurar backup (com confirmação)
.\restore-database.ps1 -BackupFile "backups\helpdesk_backup_20250115_020000.sql.gz"

# Restaurar sem confirmação (força)
.\restore-database.ps1 -BackupFile "backups\helpdesk_backup_20250115_020000.sql.gz" -Force

# Restaurar em banco diferente
.\restore-database.ps1 -BackupFile "backups\helpdesk_backup_20250115_020000.sql.gz" -DatabaseName "helpdesk_test"
```

## Configuração de Backup Automático

### Windows Task Scheduler

1. **Abrir Task Scheduler**:
   ```powershell
   taskschd.msc
   ```

2. **Importar tarefa**:
   - File → Import Task
   - Selecionar `scheduled-backup-task.xml`
   - Ajustar caminhos se necessário
   - Configurar credenciais do usuário

3. **Ou criar via PowerShell**:
   ```powershell
   # Registrar tarefa agendada
   Register-ScheduledTask -Xml (Get-Content .\scheduled-backup-task.xml | Out-String) -TaskName "HelpDesk Database Backup"
   
   # Verificar tarefa
   Get-ScheduledTask -TaskName "HelpDesk Database Backup"
   
   # Executar manualmente
   Start-ScheduledTask -TaskName "HelpDesk Database Backup"
   ```

### Configuração Personalizada

Edite `scheduled-backup-task.xml`:

- **Horário**: Altere `<StartBoundary>` (formato: YYYY-MM-DDTHH:mm:ss)
- **Frequência**: Ajuste `<DaysInterval>` (1 = diário, 7 = semanal)
- **Parâmetros**: Modifique `<Arguments>` para adicionar `-KeepDays`, `-BackupDir`, etc.

## Variáveis de Ambiente

Configure antes de usar os scripts:

```powershell
# Senha do PostgreSQL
$env:DB_PASSWORD = "sua_senha"

# Ou configure permanentemente (usuário)
[System.Environment]::SetEnvironmentVariable("DB_PASSWORD", "sua_senha", "User")

# Ou configure permanentemente (sistema)
[System.Environment]::SetEnvironmentVariable("DB_PASSWORD", "sua_senha", "Machine")
```

## Parâmetros dos Scripts

### backup-database.ps1

| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| `BackupDir` | string | `.\backups` | Diretório para armazenar backups |
| `KeepDays` | int | `7` | Dias de retenção de backups antigos |
| `DatabaseName` | string | `helpdesk` | Nome do banco de dados |
| `DatabaseHost` | string | `localhost` | Host do PostgreSQL |
| `DatabasePort` | int | `5432` | Porta do PostgreSQL |
| `DatabaseUser` | string | `postgres` | Usuário do PostgreSQL |
| `Compress` | switch | `$true` | Comprimir backup (gzip) |
| `Verbose` | switch | `$false` | Modo verbose |

### restore-database.ps1

| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| `BackupFile` | string | *obrigatório* | Caminho do arquivo de backup |
| `DatabaseName` | string | `helpdesk` | Nome do banco de dados |
| `DatabaseHost` | string | `localhost` | Host do PostgreSQL |
| `DatabasePort` | int | `5432` | Porta do PostgreSQL |
| `DatabaseUser` | string | `postgres` | Usuário do PostgreSQL |
| `Force` | switch | `$false` | Pular confirmação |
| `CreateDatabase` | switch | `$false` | Criar banco se não existir |

## Estrutura de Backups

```
helpdesk-backend/
├── backups/
│   ├── helpdesk_backup_20250115_020000.sql.gz  (compressed)
│   ├── helpdesk_backup_20250114_020000.sql.gz
│   ├── helpdesk_backup_20250113_020000.sql.gz
│   └── backup.log  (log de todas as operações)
├── backup-database.ps1
├── restore-database.ps1
└── scheduled-backup-task.xml
```

## Monitoramento

### Verificar Logs

```powershell
# Ver últimas 50 linhas do log
Get-Content .\backups\backup.log -Tail 50

# Filtrar apenas erros
Get-Content .\backups\backup.log | Select-String "ERROR"

# Ver backups criados hoje
Get-ChildItem .\backups\*_backup_*.sql* | Where-Object { $_.LastWriteTime -gt (Get-Date).Date }
```

### Estatísticas de Backups

```powershell
# Total de backups
(Get-ChildItem .\backups\*_backup_*.sql*).Count

# Tamanho total
(Get-ChildItem .\backups\*_backup_*.sql* | Measure-Object -Property Length -Sum).Sum / 1GB

# Backup mais recente
Get-ChildItem .\backups\*_backup_*.sql* | Sort-Object LastWriteTime -Descending | Select-Object -First 1
```

## Alertas e Notificações

### Email em Caso de Falha

Adicione ao final de `backup-database.ps1`:

```powershell
if (-not $success) {
    Send-MailMessage `
        -To "admin@example.com" `
        -From "backup@helpdesk.local" `
        -Subject "HelpDesk Backup Failed" `
        -Body "Backup failed at $(Get-Date). Check logs." `
        -SmtpServer "smtp.gmail.com" `
        -Port 587 `
        -UseSsl `
        -Credential (Get-Credential)
}
```

## Segurança

### Proteger Senha

1. **Usar Windows Credential Manager**:
   ```powershell
   cmdkey /generic:PostgreSQL /user:postgres /pass:sua_senha
   ```

2. **Ou usar arquivo criptografado**:
   ```powershell
   # Criar arquivo de senha criptografado
   "sua_senha" | ConvertTo-SecureString -AsPlainText -Force | ConvertFrom-SecureString | Out-File password.enc
   
   # Usar no script
   $securePassword = Get-Content password.enc | ConvertTo-SecureString
   $credential = New-Object System.Management.Automation.PSCredential("postgres", $securePassword)
   ```

### Permissões

Restrinja acesso aos backups:

```powershell
# Remover herança
$acl = Get-Acl .\backups
$acl.SetAccessRuleProtection($true, $false)

# Adicionar apenas administradores
$rule = New-Object System.Security.AccessControl.FileSystemAccessRule("Administrators", "FullControl", "ContainerInherit,ObjectInherit", "None", "Allow")
$acl.SetAccessRule($rule)

# Aplicar ACL
Set-Acl .\backups $acl
```

## Troubleshooting

### Erro: pg_dump não encontrado

```powershell
# Adicionar PostgreSQL ao PATH
$env:Path += ";C:\Program Files\PostgreSQL\15\bin"

# Ou especificar caminho completo
& "C:\Program Files\PostgreSQL\15\bin\pg_dump.exe" ...
```

### Erro: Permissão negada

```powershell
# Verificar permissões do usuário PostgreSQL
psql -U postgres -d helpdesk -c "\du"

# Grant necessário
GRANT ALL PRIVILEGES ON DATABASE helpdesk TO postgres;
```

### Backup muito grande

```powershell
# Usar compressão customizada do PostgreSQL
pg_dump -U postgres -d helpdesk -F c -Z 9 -f backup.dump

# Ou dividir em chunks
pg_dump -U postgres -d helpdesk | split -b 100M - backup_part_
```

## Restore em Produção

### Checklist de Restore

1. **Backup atual antes de restore**:
   ```powershell
   .\backup-database.ps1 -BackupDir ".\backups-before-restore"
   ```

2. **Parar aplicação**:
   ```powershell
   Stop-Service "HelpDesk API"
   ```

3. **Verificar integridade do backup**:
   ```powershell
   # Testar descompressão
   Test-Archive -Path "backup.sql.gz"
   ```

4. **Restore em banco de teste primeiro**:
   ```powershell
   .\restore-database.ps1 -BackupFile "backup.sql.gz" -DatabaseName "helpdesk_test" -Force
   ```

5. **Restore em produção**:
   ```powershell
   .\restore-database.ps1 -BackupFile "backup.sql.gz" -Force
   ```

6. **Iniciar aplicação**:
   ```powershell
   Start-Service "HelpDesk API"
   ```

7. **Verificar funcionamento**:
   ```powershell
   Invoke-WebRequest http://localhost:8080/actuator/health
   ```

## Backup para Nuvem

### Upload para Azure Blob Storage

```powershell
# Instalar módulo Az
Install-Module -Name Az -AllowClobber -Scope CurrentUser

# Login
Connect-AzAccount

# Upload backup
$backupFile = "backups\helpdesk_backup_20250115_020000.sql.gz"
Set-AzStorageBlobContent `
    -File $backupFile `
    -Container "helpdesk-backups" `
    -Blob (Split-Path $backupFile -Leaf) `
    -Context (New-AzStorageContext -StorageAccountName "youraccount" -StorageAccountKey "yourkey")
```

### Upload para AWS S3

```powershell
# Instalar AWS Tools
Install-Module -Name AWSPowerShell.NetCore

# Configure credenciais
Set-AWSCredential -AccessKey "your_key" -SecretKey "your_secret"

# Upload
Write-S3Object -BucketName "helpdesk-backups" -File "backups\helpdesk_backup_20250115_020000.sql.gz"
```

## Automação Avançada

### Notificação Slack

```powershell
$slackWebhook = "https://hooks.slack.com/services/YOUR/WEBHOOK/URL"
$message = @{
    text = "HelpDesk backup completed successfully at $(Get-Date)"
} | ConvertTo-Json

Invoke-RestMethod -Uri $slackWebhook -Method Post -Body $message -ContentType 'application/json'
```

### Integração com Monitoring

```powershell
# Prometheus Pushgateway
$metrics = @"
helpdesk_backup_success{job="database_backup"} 1
helpdesk_backup_duration_seconds{job="database_backup"} $duration
helpdesk_backup_size_bytes{job="database_backup"} $fileSize
"@

Invoke-RestMethod -Uri "http://localhost:9091/metrics/job/helpdesk_backup" -Method Post -Body $metrics
```

## Referências

- [PostgreSQL pg_dump Documentation](https://www.postgresql.org/docs/current/app-pgdump.html)
- [PowerShell Task Scheduler Cmdlets](https://docs.microsoft.com/powershell/module/scheduledtasks/)
- [Windows Task Scheduler XML Format](https://docs.microsoft.com/windows/win32/taskschd/task-scheduler-schema)
