-- =====================================================
-- SCRIPT PARA VERIFICAR CHAMADOS CRIADOS (VERSÃO FINAL)
-- Help Desk Backend - PostgreSQL
-- =====================================================

-- 1. Ver últimos chamados criados
SELECT 
    id,
    numero_chamado,
    descricao,
    status,
    categoria,
    prioridade,
    data_abertura,
    id_solicitante
FROM chamados
ORDER BY data_abertura DESC
LIMIT 10;

-- 2. Ver histórico dos chamados
SELECT 
    id,
    id_chamado,
    id_autor,
    comentario,
    data_ocorrencia
FROM historico_chamados
ORDER BY data_ocorrencia DESC
LIMIT 10;

-- 3. Contar total de chamados por status
SELECT 
    status,
    COUNT(*) as total
FROM chamados
GROUP BY status
ORDER BY total DESC;

-- 4. Ver chamado mais recente com histórico
SELECT 
    c.numero_chamado,
    c.status,
    c.categoria,
    c.prioridade,
    c.data_abertura,
    COUNT(h.id) as total_historico
FROM chamados c
LEFT JOIN historico_chamados h ON c.id = h.id_chamado
GROUP BY c.id, c.numero_chamado, c.status, c.categoria, c.prioridade, c.data_abertura
ORDER BY c.data_abertura DESC
LIMIT 5;
