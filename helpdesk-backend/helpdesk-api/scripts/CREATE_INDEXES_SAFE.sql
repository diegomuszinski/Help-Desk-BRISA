-- =====================================================
-- SCRIPT DE ÍNDICES PARA PERFORMANCE (VERSÃO SEGURA)
-- Help Desk Backend - PostgreSQL
-- =====================================================
-- Este script verifica se colunas existem antes de criar índices

-- =====================================================
-- ÍNDICES BÁSICOS (SEMPRE FUNCIONAM)
-- =====================================================

-- USUARIOS
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'usuarios' AND column_name = 'email') THEN
        CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'usuarios' AND column_name = 'perfil') THEN
        CREATE INDEX IF NOT EXISTS idx_usuarios_perfil ON usuarios(perfil);
    END IF;
END $$;

-- CATEGORIAS
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'categorias' AND column_name = 'nome') THEN
        CREATE UNIQUE INDEX IF NOT EXISTS idx_categorias_nome_unique ON categorias(nome);
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'categorias' AND column_name = 'name') THEN
        CREATE UNIQUE INDEX IF NOT EXISTS idx_categorias_name_unique ON categorias(name);
    END IF;
END $$;

-- PRIORIDADES
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'prioridades' AND column_name = 'nome') THEN
        CREATE UNIQUE INDEX IF NOT EXISTS idx_prioridades_nome_unique ON prioridades(nome);
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'prioridades' AND column_name = 'name') THEN
        CREATE UNIQUE INDEX IF NOT EXISTS idx_prioridades_name_unique ON prioridades(name);
    END IF;
END $$;

-- EQUIPES
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'equipes' AND column_name = 'nome') THEN
        CREATE UNIQUE INDEX IF NOT EXISTS idx_equipes_nome_unique ON equipes(nome);
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'equipes' AND column_name = 'name') THEN
        CREATE UNIQUE INDEX IF NOT EXISTS idx_equipes_name_unique ON equipes(name);
    END IF;
END $$;

-- REFRESH_TOKENS
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'refresh_tokens' AND column_name = 'token') THEN
        CREATE UNIQUE INDEX IF NOT EXISTS idx_refresh_token_unique ON refresh_tokens(token);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'refresh_tokens' AND column_name = 'user_id') THEN
        CREATE INDEX IF NOT EXISTS idx_refresh_user ON refresh_tokens(user_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'refresh_tokens' AND column_name = 'expiry_date') THEN
        CREATE INDEX IF NOT EXISTS idx_refresh_expiry ON refresh_tokens(expiry_date);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'refresh_tokens' AND column_name = 'revoked') THEN
        EXECUTE 'CREATE INDEX IF NOT EXISTS idx_refresh_revoked ON refresh_tokens(revoked) WHERE revoked = false';
    END IF;
END $$;

-- AUDIT_LOGS
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_logs' AND column_name = 'user_id') THEN
        CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(user_id);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_logs' AND column_name = 'action') THEN
        CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs(action);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_logs' AND column_name = 'timestamp') THEN
        CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs(timestamp);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_logs' AND column_name = 'ip_address') THEN
        CREATE INDEX IF NOT EXISTS idx_audit_ip ON audit_logs(ip_address);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_logs' AND column_name = 'status') THEN
        CREATE INDEX IF NOT EXISTS idx_audit_status ON audit_logs(status);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_logs' AND column_name = 'user_id') 
       AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_logs' AND column_name = 'timestamp') THEN
        CREATE INDEX IF NOT EXISTS idx_audit_user_timestamp ON audit_logs(user_id, timestamp);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_logs' AND column_name = 'action')
       AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_logs' AND column_name = 'status')
       AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_logs' AND column_name = 'ip_address') THEN
        CREATE INDEX IF NOT EXISTS idx_audit_action_status_ip ON audit_logs(action, status, ip_address);
    END IF;
END $$;


-- =====================================================
-- ÍNDICES CONDICIONAIS PARA CHAMADOS
-- =====================================================

DO $$
BEGIN
    -- Índice para status
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'chamados' AND column_name = 'status') THEN
        CREATE INDEX IF NOT EXISTS idx_chamados_status ON chamados(status);
    END IF;

    -- Índice para solicitante_id
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'chamados' AND column_name = 'solicitante_id') THEN
        CREATE INDEX IF NOT EXISTS idx_chamados_solicitante ON chamados(solicitante_id);
    END IF;

    -- Índice para tecnico_id
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'chamados' AND column_name = 'tecnico_id') THEN
        CREATE INDEX IF NOT EXISTS idx_chamados_tecnico ON chamados(tecnico_id);
    END IF;

    -- Índice para categoria_id
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'chamados' AND column_name = 'categoria_id') THEN
        CREATE INDEX IF NOT EXISTS idx_chamados_categoria ON chamados(categoria_id);
    END IF;

    -- Índice para prioridade_id
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'chamados' AND column_name = 'prioridade_id') THEN
        CREATE INDEX IF NOT EXISTS idx_chamados_prioridade ON chamados(prioridade_id);
    END IF;

    -- Índice para data_abertura
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'chamados' AND column_name = 'data_abertura') THEN
        CREATE INDEX IF NOT EXISTS idx_chamados_data_abertura ON chamados(data_abertura);
    END IF;

    -- Índice para data_fechamento
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'chamados' AND column_name = 'data_fechamento') THEN
        CREATE INDEX IF NOT EXISTS idx_chamados_data_fechamento ON chamados(data_fechamento);
    END IF;

    RAISE NOTICE 'Índices de chamados criados conforme colunas disponíveis';
END $$;


-- =====================================================
-- ÍNDICES CONDICIONAIS PARA HISTORICO_CHAMADOS
-- =====================================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'historico_chamados' AND column_name = 'chamado_id') THEN
        CREATE INDEX IF NOT EXISTS idx_historico_chamado ON historico_chamados(chamado_id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'historico_chamados' AND column_name = 'autor_id') THEN
        CREATE INDEX IF NOT EXISTS idx_historico_autor ON historico_chamados(autor_id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'historico_chamados' AND column_name = 'data_ocorrencia') THEN
        CREATE INDEX IF NOT EXISTS idx_historico_data ON historico_chamados(data_ocorrencia);
    END IF;

    RAISE NOTICE 'Índices de historico_chamados criados';
END $$;


-- =====================================================
-- ÍNDICES CONDICIONAIS PARA ANEXOS_CHAMADOS
-- =====================================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'anexos_chamados' AND column_name = 'chamado_id') THEN
        CREATE INDEX IF NOT EXISTS idx_anexos_chamado ON anexos_chamados(chamado_id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'anexos_chamados' AND column_name = 'data_upload') THEN
        CREATE INDEX IF NOT EXISTS idx_anexos_data_upload ON anexos_chamados(data_upload);
    END IF;

    RAISE NOTICE 'Índices de anexos_chamados criados';
END $$;


-- =====================================================
-- ÍNDICES CONDICIONAIS PARA PESQUISAS_SATISFACAO
-- =====================================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pesquisas_satisfacao' AND column_name = 'chamado_id') THEN
        CREATE INDEX IF NOT EXISTS idx_pesquisa_chamado ON pesquisas_satisfacao(chamado_id);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pesquisas_satisfacao' AND column_name = 'data_resposta') THEN
        CREATE INDEX IF NOT EXISTS idx_pesquisa_data ON pesquisas_satisfacao(data_resposta);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pesquisas_satisfacao' AND column_name = 'nota') THEN
        CREATE INDEX IF NOT EXISTS idx_pesquisa_nota ON pesquisas_satisfacao(nota);
    END IF;

    RAISE NOTICE 'Índices de pesquisas_satisfacao criados';
END $$;


-- =====================================================
-- LISTAR ÍNDICES CRIADOS
-- =====================================================

SELECT 
    n.nspname AS schema,
    t.relname AS table,
    i.relname AS index,
    pg_size_pretty(pg_relation_size(i.oid)) AS index_size
FROM pg_class t
JOIN pg_index ix ON t.oid = ix.indrelid
JOIN pg_class i ON i.oid = ix.indexrelid
JOIN pg_namespace n ON n.oid = i.relnamespace
WHERE n.nspname = 'public'
  AND t.relkind = 'r'
ORDER BY t.relname, i.relname;


-- =====================================================
-- ANALISAR TABELAS
-- =====================================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'usuarios') THEN
        ANALYZE usuarios;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'chamados') THEN
        ANALYZE chamados;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'historico_chamados') THEN
        ANALYZE historico_chamados;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'anexos_chamados') THEN
        ANALYZE anexos_chamados;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'refresh_tokens') THEN
        ANALYZE refresh_tokens;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'audit_logs') THEN
        ANALYZE audit_logs;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'categorias') THEN
        ANALYZE categorias;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'prioridades') THEN
        ANALYZE prioridades;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'equipes') THEN
        ANALYZE equipes;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'pesquisas_satisfacao') THEN
        ANALYZE pesquisas_satisfacao;
    END IF;
    
    RAISE NOTICE 'Análise de tabelas concluída';
END $$;


-- =====================================================
-- MENSAGEM FINAL
-- =====================================================

DO $$
BEGIN
    RAISE NOTICE '✅ Script executado com sucesso!';
    RAISE NOTICE '📊 Índices criados para todas as colunas existentes';
    RAISE NOTICE '🔍 Confira a lista de índices acima';
END $$;
