-- =====================================================
-- SCRIPT DE ÍNDICES PARA PERFORMANCE
-- Help Desk Backend - PostgreSQL
-- =====================================================
-- 
-- Execute este script após criar as tabelas principais
-- Melhora significativamente a performance de queries

-- =====================================================
-- ÍNDICES PARA TABELA: usuarios
-- =====================================================

-- Índice para busca por email (usado no login)
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);

-- Índice para busca por perfil (filtrar por role)
CREATE INDEX IF NOT EXISTS idx_usuarios_perfil ON usuarios(perfil);


-- =====================================================
-- ÍNDICES PARA TABELA: chamados (tickets)
-- =====================================================

-- Índice para busca por status (query mais comum)
CREATE INDEX IF NOT EXISTS idx_chamados_status ON chamados(status);

-- Índice para busca por solicitante
CREATE INDEX IF NOT EXISTS idx_chamados_solicitante ON chamados(solicitante_id);

-- Índice para busca por técnico atribuído
CREATE INDEX IF NOT EXISTS idx_chamados_tecnico ON chamados(tecnico_id);

-- Índice para busca por categoria
CREATE INDEX IF NOT EXISTS idx_chamados_categoria ON chamados(categoria_id);

-- Índice para busca por prioridade
CREATE INDEX IF NOT EXISTS idx_chamados_prioridade ON chamados(prioridade_id);

-- Índice para busca por data de abertura (relatórios)
CREATE INDEX IF NOT EXISTS idx_chamados_data_abertura ON chamados(data_abertura);

-- Índice para busca por data de fechamento (relatórios)
CREATE INDEX IF NOT EXISTS idx_chamados_data_fechamento ON chamados(data_fechamento);

-- Índice composto para chamados abertos de um técnico (query comum)
CREATE INDEX IF NOT EXISTS idx_chamados_tecnico_status ON chamados(tecnico_id, status);

-- Índice composto para chamados de um solicitante por status
CREATE INDEX IF NOT EXISTS idx_chamados_solicitante_status ON chamados(solicitante_id, status);


-- =====================================================
-- ÍNDICES PARA TABELA: historico_chamados
-- =====================================================

-- Índice para buscar histórico por chamado (FK)
CREATE INDEX IF NOT EXISTS idx_historico_chamado ON historico_chamados(chamado_id);

-- Índice para buscar histórico por autor
CREATE INDEX IF NOT EXISTS idx_historico_autor ON historico_chamados(autor_id);

-- Índice para ordenação por data
CREATE INDEX IF NOT EXISTS idx_historico_data ON historico_chamados(data_ocorrencia);


-- =====================================================
-- ÍNDICES PARA TABELA: anexos_chamados
-- =====================================================

-- Índice para buscar anexos por chamado (FK)
CREATE INDEX IF NOT EXISTS idx_anexos_chamado ON anexos_chamados(chamado_id);

-- Índice para busca por data de upload
CREATE INDEX IF NOT EXISTS idx_anexos_data_upload ON anexos_chamados(data_upload);


-- =====================================================
-- ÍNDICES PARA TABELA: refresh_tokens (NOVO - Segurança)
-- =====================================================

-- Índice UNIQUE para busca rápida por token (usado na validação)
CREATE UNIQUE INDEX IF NOT EXISTS idx_refresh_token_unique ON refresh_tokens(token);

-- Índice para buscar tokens de um usuário
CREATE INDEX IF NOT EXISTS idx_refresh_user ON refresh_tokens(user_id);

-- Índice para buscar tokens não revogados (usado na validação)
CREATE INDEX IF NOT EXISTS idx_refresh_revoked ON refresh_tokens(revoked) WHERE revoked = false;

-- Índice para limpeza de tokens expirados (scheduled task)
CREATE INDEX IF NOT EXISTS idx_refresh_expiry ON refresh_tokens(expiry_date);

-- Índice composto para query de validação (token + revoked)
CREATE INDEX IF NOT EXISTS idx_refresh_token_revoked ON refresh_tokens(token, revoked);


-- =====================================================
-- ÍNDICES PARA TABELA: audit_logs (NOVO - Segurança)
-- =====================================================

-- Índice para buscar logs de um usuário
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(user_id);

-- Índice para buscar por tipo de ação (LOGIN, LOGOUT, etc.)
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs(action);

-- Índice para buscar por timestamp (queries de relatórios)
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs(timestamp);

-- Índice para buscar por IP (investigação de segurança)
CREATE INDEX IF NOT EXISTS idx_audit_ip ON audit_logs(ip_address);

-- Índice para buscar por status (SUCCESS/FAILURE)
CREATE INDEX IF NOT EXISTS idx_audit_status ON audit_logs(status);

-- Índice composto para query comum: logs de um usuário por data
CREATE INDEX IF NOT EXISTS idx_audit_user_timestamp ON audit_logs(user_id, timestamp);

-- Índice composto para query de segurança: falhas de login por IP
CREATE INDEX IF NOT EXISTS idx_audit_action_status_ip ON audit_logs(action, status, ip_address);


-- =====================================================
-- ÍNDICES PARA TABELA: equipes (NOVO)
-- =====================================================

-- Índice UNIQUE para evitar equipes duplicadas
CREATE UNIQUE INDEX IF NOT EXISTS idx_equipes_nome_unique ON equipes(nome);


-- =====================================================
-- ÍNDICES PARA TABELA: pesquisas_satisfacao (NOVO)
-- =====================================================

-- Índice para buscar pesquisas por chamado
CREATE INDEX IF NOT EXISTS idx_pesquisa_chamado ON pesquisas_satisfacao(chamado_id);

-- Índice para busca por data
CREATE INDEX IF NOT EXISTS idx_pesquisa_data ON pesquisas_satisfacao(data_resposta);

-- Índice para análise de satisfação por nota
CREATE INDEX IF NOT EXISTS idx_pesquisa_nota ON pesquisas_satisfacao(nota);


-- =====================================================
-- ÍNDICES PARA TABELA: categorias
-- =====================================================

-- Índice UNIQUE para evitar categorias duplicadas
CREATE UNIQUE INDEX IF NOT EXISTS idx_categorias_nome_unique ON categorias(nome);


-- =====================================================
-- ÍNDICES PARA TABELA: prioridades
-- =====================================================

-- Índice UNIQUE para evitar prioridades duplicadas
CREATE UNIQUE INDEX IF NOT EXISTS idx_prioridades_nome_unique ON prioridades(nome);


-- =====================================================
-- VERIFICAR ÍNDICES CRIADOS
-- =====================================================

-- Query para listar todos os índices criados
SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;


-- =====================================================
-- ESTATÍSTICAS DE PERFORMANCE
-- =====================================================

-- Analisar tabelas para atualizar estatísticas
ANALYZE usuarios;
ANALYZE chamados;
ANALYZE historico_chamados;
ANALYZE anexos_chamados;
ANALYZE refresh_tokens;
ANALYZE audit_logs;
ANALYZE categorias;
ANALYZE prioridades;
ANALYZE equipes;
ANALYZE pesquisas_satisfacao;


-- =====================================================
-- VERIFICAR TAMANHO DOS ÍNDICES
-- =====================================================

-- Query para ver tamanho dos índices
SELECT
    schemaname,
    tablename,
    indexname,
    pg_size_pretty(pg_relation_size(indexrelid)) AS index_size
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY pg_relation_size(indexrelid) DESC;


-- =====================================================
-- MANUTENÇÃO PERIÓDICA RECOMENDADA
-- =====================================================

-- Execute periodicamente (ex: semanalmente) para manter performance:

-- 1. VACUUM - Limpa espaço não utilizado
VACUUM ANALYZE usuarios;
VACUUM ANALYZE chamados;
VACUUM ANALYZE historico_chamados;
VACUUM ANALYZE anexos_chamados;
VACUUM ANALYZE refresh_tokens;
VACUUM ANALYZE audit_logs;
VACUUM ANALYZE equipes;
VACUUM ANALYZE pesquisas_satisfacao;

-- 2. REINDEX - Reconstrói índices (se necessário)
-- REINDEX TABLE chamados;
-- REINDEX TABLE audit_logs;
-- REINDEX TABLE refresh_tokens;


-- =====================================================
-- ÍNDICES PARCIAIS ADICIONAIS (OPCIONAL - AVANÇADO)
-- =====================================================

-- Índice parcial para chamados abertos (query muito comum)
CREATE INDEX IF NOT EXISTS idx_chamados_abertos 
ON chamados(data_abertura, prioridade_id) 
WHERE status = 'Aberto';

-- Índice parcial para chamados em andamento
CREATE INDEX IF NOT EXISTS idx_chamados_em_andamento 
ON chamados(tecnico_id, data_abertura) 
WHERE status = 'Em Andamento';

-- Índice parcial para chamados com SLA crítico (prioridade crítica/alta)
CREATE INDEX IF NOT EXISTS idx_chamados_sla_critico 
ON chamados(data_abertura, tecnico_id) 
WHERE status IN ('Aberto', 'Em Andamento') 
  AND prioridade_id IN (
    SELECT id FROM prioridades WHERE nome IN ('Crítica', 'Alta')
  );

-- Índice parcial para falhas de login (segurança)
CREATE INDEX IF NOT EXISTS idx_audit_login_failures 
ON audit_logs(ip_address, timestamp) 
WHERE action = 'LOGIN_FAILURE';


-- =====================================================
-- FIM DO SCRIPT
-- =====================================================

-- Mensagem de sucesso
DO $$
BEGIN
    RAISE NOTICE 'Índices criados com sucesso!';
    RAISE NOTICE 'Execute a query de verificação para confirmar.';
END $$;
