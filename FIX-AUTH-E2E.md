# 🔒 Correção de Autenticação E2E - Usuário de Teste

## ❌ Problema Identificado

Os testes E2E estavam falhando com erro **HTTP 401 "Credenciais inválidas"** para o usuário `usuario@teste.net` porque havia **inconsistência nos hashes BCrypt das senhas** entre:

- O arquivo [`init-database.sql`](init-database.sql) (local)
- O workflow CI/CD [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml)

### Hashes Anteriores (INCORRETOS):
```sql
-- Hash antigo no init-database.sql
'$2a$10$N9qo8uLOickgx2ZMRZoMye6J4Qf8mKvjeyELCtU3xbGQEUzLe6T7e'
```

### Hashes Corretos (BCrypt para senha "123456"):
```sql
-- Hash correto usado no CI/CD
'$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
```

## ✅ Correções Aplicadas

### 1. Arquivo `init-database.sql` ✓
Atualizado com os hashes BCrypt corretos para:
- `admin@admin.net` → senha: 123456
- `mariana@tecnico.net` → senha: 123456  
- `usuario@teste.net` → senha: 123456
- `sonia.lima@gestor.net` → senha: 1234546 (inalterada)

### 2. Scripts de Correção Criados

#### **Opção A: Usar o Backend** (Recomendado)
Execute [`CORRIGIR-SENHAS.ps1`](CORRIGIR-SENHAS.ps1):
```powershell
.\CORRIGIR-SENHAS.ps1
```

Este script usa o endpoint `/api/test/reset-all-passwords` do backend para gerar hashes BCrypt corretos automaticamente.

#### **Opção B: Atualizar Diretamente no Banco**
Execute [`fix-senha-usuarios.sql`](fix-senha-usuarios.sql) no pgAdmin:
```sql
-- Conecte ao banco 'helpdesk' e execute o script
```

## 🧪 Como Testar

### 1. Testar Login via API
```powershell
$login = @{ email = "usuario@teste.net"; senha = "123456" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body $login
```

### 2. Testar nos E2E
```powershell
cd help-desk-frontend
npm run test:e2e
```

## 📋 Credenciais de Teste Válidas

| Email | Senha | Perfil | Hash BCrypt |
|-------|-------|--------|-------------|
| `admin@admin.net` | `123456` | ADMIN | `$2a$10$92I...` ✓ |
| `sonia.lima@gestor.net` | `1234546` | MANAGER | `$2a$10$3Lk...` ✓ |
| `mariana@tecnico.net` | `123456` | TECHNICIAN | `$2a$10$92I...` ✓ |
| `usuario@teste.net` | `123456` | USER | `$2a$10$92I...` ✓ |

## 🔍 Verificação no Banco

```sql
SELECT 
    email, 
    perfil,
    LEFT(senha, 30) || '...' as senha_hash,
    LENGTH(senha) as hash_length
FROM usuarios 
WHERE email = 'usuario@teste.net';
```

**Resultado Esperado:**
- `hash_length` = 60 caracteres
- `senha_hash` começa com `$2a$10$92IXUNpkjO0rOQ5byMi.Ye...`

## ⚙️ Próximos Passos

1. ✅ Hashes corrigidos no [`init-database.sql`](init-database.sql)
2. ⏭️ Execute [`CORRIGIR-SENHAS.ps1`](CORRIGIR-SENHAS.ps1) ou [`fix-senha-usuarios.sql`](fix-senha-usuarios.sql)
3. ⏭️ Reinicie o backend se necessário
4. ⏭️ Execute os testes E2E

## 🐛 Troubleshooting

### Se o login ainda falhar:

1. **Verificar hash no banco:**
   ```sql
   SELECT email, senha FROM usuarios WHERE email = 'usuario@teste.net';
   ```

2. **Testar hash com endpoint de debug:**
   ```
   GET /api/test/bcrypt?email=usuario@teste.net&senha=123456
   ```

3. **Resetar senha via backend:**
   ```
   POST /api/test/reset-all-passwords
   ```

4. **Reinicializar banco de dados:**
   ```powershell
   # No pgAdmin, execute init-database.sql completo
   ```

## 📚 Arquivos Relacionados

- [`init-database.sql`](init-database.sql) - Dados iniciais (CORRIGIDO ✓)
- [`CORRIGIR-SENHAS.ps1`](CORRIGIR-SENHAS.ps1) - Script de correção automática
- [`fix-senha-usuarios.sql`](fix-senha-usuarios.sql) - Correção SQL direta
- [`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml) - Workflow CI/CD
- [`help-desk-frontend/e2e/fixtures/auth.ts`](help-desk-frontend/e2e/fixtures/auth.ts) - Credenciais de teste
