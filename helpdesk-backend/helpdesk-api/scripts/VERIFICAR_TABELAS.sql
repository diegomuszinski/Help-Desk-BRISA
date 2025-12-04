-- =====================================================
-- SCRIPT PARA VERIFICAR TABELAS CRIADAS PELO HIBERNATE
-- Execute no pgAdmin ou qualquer ferramenta SQL
-- =====================================================

-- 1. Listar todas as tabelas do schema public
SELECT 
    table_name,
    table_type
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

-- 2. Ver estrutura detalhada de cada tabela (colunas)
SELECT 
    table_name,
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public'
ORDER BY table_name, ordinal_position;

-- 3. Verificar se tabelas principais existem
SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'usuarios') 
        THEN '✅ usuarios existe' 
        ELSE '❌ usuarios NÃO existe' 
    END as status_usuarios,
    
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tickets') 
        THEN '✅ tickets existe' 
        ELSE '❌ tickets NÃO existe' 
    END as status_tickets,
    
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'categorias') 
        THEN '✅ categorias existe' 
        ELSE '❌ categorias NÃO existe' 
    END as status_categorias,
    
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'prioridades') 
        THEN '✅ prioridades existe' 
        ELSE '❌ prioridades NÃO existe' 
    END as status_prioridades,
    
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'refresh_tokens') 
        THEN '✅ refresh_tokens existe' 
        ELSE '❌ refresh_tokens NÃO existe' 
    END as status_refresh_tokens,
    
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'audit_logs') 
        THEN '✅ audit_logs existe' 
        ELSE '❌ audit_logs NÃO existe' 
    END as status_audit_logs;

-- 4. Contar registros em cada tabela (só se existirem)
DO $$
DECLARE
    sql_query TEXT;
BEGIN
    -- Criar query dinâmica apenas para tabelas que existem
    sql_query := '';
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'usuarios') THEN
        sql_query := sql_query || 'SELECT ''usuarios'' as tabela, COUNT(*)::text as registros FROM usuarios UNION ALL ';
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tickets') THEN
        sql_query := sql_query || 'SELECT ''tickets'', COUNT(*)::text FROM tickets UNION ALL ';
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'categorias') THEN
        sql_query := sql_query || 'SELECT ''categorias'', COUNT(*)::text FROM categorias UNION ALL ';
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'prioridades') THEN
        sql_query := sql_query || 'SELECT ''prioridades'', COUNT(*)::text FROM prioridades UNION ALL ';
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'historico_tickets') THEN
        sql_query := sql_query || 'SELECT ''historico_tickets'', COUNT(*)::text FROM historico_tickets UNION ALL ';
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'anexos') THEN
        sql_query := sql_query || 'SELECT ''anexos'', COUNT(*)::text FROM anexos UNION ALL ';
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'refresh_tokens') THEN
        sql_query := sql_query || 'SELECT ''refresh_tokens'', COUNT(*)::text FROM refresh_tokens UNION ALL ';
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'audit_logs') THEN
        sql_query := sql_query || 'SELECT ''audit_logs'', COUNT(*)::text FROM audit_logs UNION ALL ';
    END IF;
    
    -- Remover último UNION ALL
    IF sql_query != '' THEN
        sql_query := LEFT(sql_query, LENGTH(sql_query) - 11);
        sql_query := sql_query || ' ORDER BY 1';
        
        RAISE NOTICE 'Contando registros...';
        EXECUTE sql_query;
    ELSE
        RAISE NOTICE 'Nenhuma tabela encontrada!';
    END IF;
END $$;
