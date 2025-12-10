-- Script para verificar e corrigir o formato dos status dos chamados
-- Execute este script no banco de dados helpdesk

-- 1. Verificar os status atuais
SELECT DISTINCT status, COUNT(*) as quantidade
FROM chamados
GROUP BY status
ORDER BY quantidade DESC;

-- 2. Atualizar status que estiverem em formato incorreto
-- Converter de MAIÚSCULO para formato correto (Primeira letra maiúscula)

UPDATE chamados SET status = 'Aberto' WHERE UPPER(status) = 'ABERTO' AND status != 'Aberto';
UPDATE chamados SET status = 'Em Andamento' WHERE UPPER(status) = 'EM ANDAMENTO' OR UPPER(status) = 'EM_ANDAMENTO' AND status != 'Em Andamento';
UPDATE chamados SET status = 'Resolvido' WHERE UPPER(status) = 'RESOLVIDO' AND status != 'Resolvido';
UPDATE chamados SET status = 'Fechado' WHERE UPPER(status) = 'FECHADO' AND status != 'Fechado';
UPDATE chamados SET status = 'Encerrado' WHERE UPPER(status) = 'ENCERRADO' AND status != 'Encerrado';
UPDATE chamados SET status = 'Cancelado' WHERE UPPER(status) = 'CANCELADO' AND status != 'Cancelado';

-- 3. Verificar resultado após correção
SELECT DISTINCT status, COUNT(*) as quantidade
FROM chamados
GROUP BY status
ORDER BY quantidade DESC;

-- 4. Listar chamados abertos
SELECT id, numero_chamado, status, categoria, prioridade, data_abertura, 
       (SELECT nome FROM usuarios WHERE id = chamados.id_solicitante) as solicitante
FROM chamados
WHERE status = 'Aberto'
ORDER BY data_abertura DESC
LIMIT 20;
