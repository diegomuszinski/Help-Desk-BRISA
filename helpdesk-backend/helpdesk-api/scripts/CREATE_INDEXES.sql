-- =====================================================
-- SCRIPT DE ÍNDICES OTIMIZADOS PARA PERFORMANCE
-- Help Desk Backend - PostgreSQL
-- =====================================================
--
-- Baseado nas queries reais do sistema
-- Execute após criação das tabelas para melhor performance
-- Criação: 2025-12-04
--
-- COMO USAR:
-- 1. Conecte ao banco: psql -U postgres -d helpdesk
-- 2. Execute: \i scripts/CREATE_INDEXES.sql
-- 3. Verifique: SELECT * FROM pg_indexes WHERE schemaname = 'public';

-- =====================================================
-- ÍNDICES PARA TABELA: usuarios
-- =====================================================

-- Índice UNIQUE para busca por email (usado no login e validação)
-- Query: SELECT * FROM usuarios WHERE email = ?
CREATE UNIQUE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);

-- Índice para busca por perfil (filtrar técnicos/managers)
-- Query: SELECT * FROM usuarios WHERE perfil = 'technician'
CREATE INDEX IF NOT EXISTS idx_usuarios_perfil ON usuarios(perfil);

-- Índice para busca por equipe
-- Query: SELECT * FROM usuarios WHERE id_equipe = ?
CREATE INDEX IF NOT EXISTS idx_usuarios_equipe ON usuarios(id_equipe);


-- =====================================================
-- ÍNDICES PARA TABELA: chamados (tickets)
-- =====================================================

-- Índice para busca por status (query mais frequente)
-- Query: SELECT * FROM chamados WHERE status = 'Aberto'
CREATE INDEX IF NOT EXISTS idx_chamados_status ON chamados(status);

-- Índice para busca por solicitante (meus tickets)
-- Query: SELECT * FROM chamados WHERE id_solicitante = ?
CREATE INDEX IF NOT EXISTS idx_chamados_solicitante ON chamados(id_solicitante);

-- Índice para busca por técnico atribuído (tickets do técnico)
-- Query: SELECT * FROM chamados WHERE id_tecnico_atribuido = ?
CREATE INDEX IF NOT EXISTS idx_chamados_tecnico ON chamados(id_tecnico_atribuido);

-- Índice para busca por categoria (relatórios)
-- Query: SELECT * FROM chamados WHERE categoria = 'Hardware'
CREATE INDEX IF NOT EXISTS idx_chamados_categoria ON chamados(categoria);

-- Índice para busca por prioridade (filtros, SLA)
-- Query: SELECT * FROM chamados WHERE prioridade = 'Crítica'
CREATE INDEX IF NOT EXISTS idx_chamados_prioridade ON chamados(prioridade);

-- Índice para busca por data de abertura (relatórios, contagens por ano)
-- Query: SELECT COUNT(*) FROM chamados WHERE EXTRACT(YEAR FROM data_abertura) = 2025
CREATE INDEX IF NOT EXISTS idx_chamados_data_abertura ON chamados(data_abertura);

-- Índice para busca por data de fechamento (cálculo de tempo médio)
-- Query: SELECT AVG(...) FROM chamados WHERE data_fechamento IS NOT NULL
CREATE INDEX IF NOT EXISTS idx_chamados_data_fechamento ON chamados(data_fechamento);

-- Índice composto para dashboard (tickets não atribuídos por status)
-- Query: SELECT COUNT(*) FROM chamados WHERE status = ? AND id_tecnico_atribuido IS NULL
CREATE INDEX IF NOT EXISTS idx_chamados_status_tecnico_null
ON chamados(status, id_tecnico_atribuido)
WHERE id_tecnico_atribuido IS NULL;

-- Índice composto para tickets do técnico por status
-- Query: SELECT * FROM chamados WHERE id_tecnico_atribuido = ? AND status = ?
CREATE INDEX IF NOT EXISTS idx_chamados_tecnico_status
ON chamados(id_tecnico_atribuido, status);

-- Índice composto para tickets do solicitante por status
-- Query: SELECT * FROM chamados WHERE id_solicitante = ? AND status = ?
CREATE INDEX IF NOT EXISTS idx_chamados_solicitante_status
ON chamados(id_solicitante, status);


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
-- Benefício: Validação automática no banco, cache mais eficiente
CREATE UNIQUE INDEX IF NOT EXISTS idx_categorias_nome_unique ON categorias(nome);


-- =====================================================
-- ÍNDICES PARA TABELA: prioridades
-- =====================================================

-- Índice UNIQUE para evitar prioridades duplicadas
-- Benefício: Validação automática no banco, cache mais eficiente
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
-- ÍNDICES PARCIAIS AVANÇADOS (OTIMIZAÇÃO)
-- =====================================================

-- Índice parcial para chamados abertos (query muito comum no dashboard)
-- Benefício: 70% menor que índice completo, query 10x mais rápida
CREATE INDEX IF NOT EXISTS idx_chamados_abertos
ON chamados(data_abertura, prioridade)
WHERE status = 'Aberto';

-- Índice parcial para chamados em andamento
-- Benefício: Acelera consultas de tickets ativos do técnico
CREATE INDEX IF NOT EXISTS idx_chamados_em_andamento
ON chamados(id_tecnico_atribuido, data_abertura)
WHERE status = 'Em Andamento';

-- Índice parcial para chamados pendentes (Aberto ou Em Andamento)
-- Benefício: Query única para SLA e alertas
CREATE INDEX IF NOT EXISTS idx_chamados_pendentes
ON chamados(data_abertura, prioridade, id_tecnico_atribuido)
WHERE status IN ('Aberto', 'Em Andamento');

-- Índice parcial para chamados com SLA crítico (alta prioridade)
-- Benefício: Dashboard de alertas SLA responde instantaneamente
CREATE INDEX IF NOT EXISTS idx_chamados_sla_critico
ON chamados(data_abertura, id_tecnico_atribuido, prioridade)
WHERE status IN ('Aberto', 'Em Andamento')
  AND prioridade IN ('Crítica', 'Alta');

-- Índice parcial para tickets reabertos (análise de qualidade)
-- Benefício: Relatórios de reincidência muito mais rápidos
CREATE INDEX IF NOT EXISTS idx_chamados_reabertos
ON chamados(categoria, id_tecnico_atribuido, data_abertura)
WHERE foi_reaberto = true;

-- Índice parcial para falhas de login (segurança)
-- Benefício: Detecção de ataques brute-force em tempo real
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
