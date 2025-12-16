# Guia de Uso do Git - Branch em-desenvolvimento

## Branch Configurado
Você está configurado para trabalhar no branch **em-desenvolvimento**.

## Como Fazer Commits

### Opção 1: Usar o Script Automatizado (Recomendado)
```powershell
.\commit-dev.ps1 "Sua mensagem de commit aqui"
```

O script automaticamente:
- ✓ Verifica se você está no branch correto
- ✓ Muda para `em-desenvolvimento` se necessário
- ✓ Salva mudanças temporariamente (stash) se precisar trocar de branch
- ✓ Adiciona todos os arquivos modificados
- ✓ Faz o commit com sua mensagem

### Opção 2: Comandos Manuais
```powershell
# 1. Verificar em qual branch você está
git branch

# 2. Se não estiver em em-desenvolvimento, mudar para ele
git checkout em-desenvolvimento

# 3. Adicionar arquivos
git add .

# 4. Fazer commit
git commit -m "Sua mensagem aqui"

# 5. Enviar para o repositório remoto (quando quiser)
git push origin em-desenvolvimento
```

## Comandos Úteis

### Ver status atual
```powershell
git status
```

### Ver histórico de commits
```powershell
git log --oneline
```

### Criar novo branch a partir de em-desenvolvimento
```powershell
git checkout -b nova-feature
```

### Voltar para em-desenvolvimento
```powershell
git checkout em-desenvolvimento
```

### Atualizar branch com mudanças remotas
```powershell
git pull origin em-desenvolvimento
```

### Enviar commits para o repositório remoto
```powershell
git push origin em-desenvolvimento
```

## Fluxo de Trabalho Recomendado

1. **Sempre trabalhe no branch em-desenvolvimento**
2. **Faça commits frequentes** com mensagens descritivas
3. **Use o script commit-dev.ps1** para facilitar o processo
4. **Envie para o remoto** regularmente: `git push origin em-desenvolvimento`

## Exemplo de Uso Completo

```powershell
# Fazer modificações nos arquivos...

# Commit usando o script
.\commit-dev.ps1 "Adicionado nova funcionalidade de relatórios"

# Enviar para o repositório remoto
git push origin em-desenvolvimento
```

## Dicas

- ✓ Use mensagens de commit claras e descritivas
- ✓ Faça commits pequenos e frequentes
- ✓ Sempre verifique o status antes de commitar: `git status`
- ✓ Use `git diff` para ver as mudanças antes de commitar
