-- ============================================================================
-- Script de Inicialização - VERSÃO PGADMIN
-- Help Desk System - Compatível com pgAdmin Query Tool
-- ============================================================================
-- IMPORTANTE: 
-- 1. Crie o banco 'helpdesk' primeiro (se ainda não existir)
-- 2. Conecte-se ao banco 'helpdesk' no pgAdmin
-- 3. Abra este script no Query Tool do banco 'helpdesk'
-- 4. Execute o script completo (F5)
-- ============================================================================
-- CRIAÇÃO DAS TABELAS
-- ============================================================================

-- Tabela para Categorias 
CREATE TABLE public.categorias (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) UNIQUE NOT NULL
);

-- Tabela para Prioridades
CREATE TABLE public.prioridades (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) UNIQUE NOT NULL
);

-- Tabela para organizar os técnicos em equipes
CREATE TABLE public.equipes (
    id SERIAL PRIMARY KEY,
    nome_equipe VARCHAR(255) UNIQUE NOT NULL,
    id_gestor INTEGER
);

-- Tabela para armazenar todos os usuários
CREATE TABLE public.usuarios (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(50) NOT NULL, -- 'ADMIN', 'MANAGER', 'TECHNICIAN', 'USER'
    id_equipe INTEGER,
    data_criacao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_equipe) REFERENCES public.equipes(id) ON DELETE SET NULL
);

-- Adiciona a restrição de chave estrangeira na tabela de equipes
ALTER TABLE public.equipes
ADD CONSTRAINT fk_gestor
FOREIGN KEY (id_gestor) REFERENCES public.usuarios(id) ON DELETE SET NULL;

-- Tabela principal para armazenar os chamados
CREATE TABLE public.chamados (
    id SERIAL PRIMARY KEY,
    numero_chamado VARCHAR(50) UNIQUE NOT NULL,
    descricao TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    data_abertura TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    data_fechamento TIMESTAMP WITH TIME ZONE,
    solucao TEXT,
    id_solicitante INTEGER NOT NULL,
    id_tecnico_atribuido INTEGER,
    foi_reaberto BOOLEAN DEFAULT FALSE,
    id_categoria INTEGER,
    id_prioridade INTEGER,
    FOREIGN KEY (id_solicitante) REFERENCES public.usuarios(id) ON DELETE RESTRICT,
    FOREIGN KEY (id_tecnico_atribuido) REFERENCES public.usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (id_categoria) REFERENCES public.categorias(id),
    FOREIGN KEY (id_prioridade) REFERENCES public.prioridades(id)
);

-- Tabela para Anexos
CREATE TABLE public.anexos_chamados (
    id SERIAL PRIMARY KEY,
    nome_arquivo VARCHAR(255) NOT NULL,
    tipo_arquivo VARCHAR(100) NOT NULL,
    dados TEXT NOT NULL,
    id_chamado INTEGER NOT NULL,
    data_upload TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_chamado) REFERENCES public.chamados(id) ON DELETE CASCADE
);

-- Tabela para o Histórico de interações
CREATE TABLE public.historico_chamados (
    id SERIAL PRIMARY KEY,
    id_chamado INTEGER NOT NULL,
    id_autor INTEGER,
    comentario TEXT NOT NULL,
    data_ocorrencia TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_chamado) REFERENCES public.chamados(id) ON DELETE CASCADE,
    FOREIGN KEY (id_autor) REFERENCES public.usuarios(id) ON DELETE SET NULL
);

-- Tabela para as Pesquisas de Satisfação
CREATE TABLE public.pesquisas_satisfacao (
    id SERIAL PRIMARY KEY,
    id_chamado INTEGER UNIQUE NOT NULL,
    nota INTEGER NOT NULL CHECK (nota >= 1 AND nota <= 5),
    comentario TEXT,
    data_resposta TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_chamado) REFERENCES public.chamados(id) ON DELETE CASCADE
);

-- ============================================================================
-- INSERÇÃO DE DADOS PADRÃO
-- ============================================================================

-- Insere as categorias padrão
INSERT INTO public.categorias (nome) VALUES
('Toner/Cartucho'),
('Software (Office, Adobe, etc.)'),
('Software (Sistema Operacional)'),
('Hardware (HD, memória, etc.)'),
('Permissão de acesso (pasta, arquivo)'),
('Dúvidas/Outros')
ON CONFLICT (nome) DO NOTHING;

-- Insere as prioridades padrão
INSERT INTO public.prioridades (nome) VALUES
('Baixa'),
('Média'),
('Alta'),
('Crítica')
ON CONFLICT (nome) DO NOTHING;

-- ============================================================================
-- INSERÇÃO DE USUÁRIOS DE TESTE
-- ============================================================================
-- Nota: As senhas estão hasheadas usando BCrypt (compatível com Spring Security)
-- Senhas originais:
--   admin@admin.net       -> senha: 123456
--   sonia.lima@gestor.net -> senha: 1234546  
--   mariana@tecnico.net   -> senha: 123456
--   usuario@teste.net     -> senha: 123456
-- ============================================================================

-- Criar equipe de exemplo
INSERT INTO public.equipes (id, nome_equipe, id_gestor) VALUES
(1, 'Equipe Suporte Técnico', NULL);

-- Inserir usuários de teste
-- ADMIN: admin@admin.net (senha: 123456)
INSERT INTO public.usuarios (id, nome, email, senha, perfil, id_equipe) VALUES
(1, 'Administrador do Sistema', 'admin@admin.net', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN', NULL);

-- MANAGER: sonia.lima@gestor.net (senha: 1234546)
INSERT INTO public.usuarios (id, nome, email, senha, perfil, id_equipe) VALUES
(2, 'Sônia Lima', 'sonia.lima@gestor.net', '$2a$10$3Lkj8vFqNHnLMnXbXKYB0eDVYBfQxX5z5qVzKQl5N/B3Q8XCxqh2O', 'MANAGER', 1);

-- TECHNICIAN: mariana@tecnico.net (senha: 123456)
INSERT INTO public.usuarios (id, nome, email, senha, perfil, id_equipe) VALUES
(3, 'Mariana Silva', 'mariana@tecnico.net', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'TECHNICIAN', 1);

-- USER: usuario@teste.net (senha: 123456)
INSERT INTO public.usuarios (id, nome, email, senha, perfil, id_equipe) VALUES
(4, 'Usuário de Teste', 'usuario@teste.net', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'USER', NULL);

-- Atualizar o gestor da equipe
UPDATE public.equipes SET id_gestor = 2 WHERE id = 1;

-- Resetar as sequences para os próximos IDs
SELECT setval('public.usuarios_id_seq', (SELECT MAX(id) FROM public.usuarios));
SELECT setval('public.equipes_id_seq', (SELECT MAX(id) FROM public.equipes));
SELECT setval('public.categorias_id_seq', (SELECT MAX(id) FROM public.categorias));
SELECT setval('public.prioridades_id_seq', (SELECT MAX(id) FROM public.prioridades));

-- ============================================================================
-- VERIFICAÇÃO DOS DADOS INSERIDOS
-- ============================================================================

SELECT 'Categorias cadastradas:' AS info;
SELECT * FROM public.categorias ORDER BY id;

SELECT 'Prioridades cadastradas:' AS info;
SELECT * FROM public.prioridades ORDER BY id;

SELECT 'Equipes cadastradas:' AS info;
SELECT e.id, e.nome_equipe, u.nome AS gestor 
FROM public.equipes e
LEFT JOIN public.usuarios u ON e.id_gestor = u.id
ORDER BY e.id;

SELECT 'Usuários cadastrados:' AS info;
SELECT id, nome, email, perfil, 
       CASE WHEN id_equipe IS NOT NULL THEN 'Sim' ELSE 'Não' END AS tem_equipe
FROM public.usuarios 
ORDER BY id;

-- ============================================================================
-- FIM DO SCRIPT - SUCESSO!
-- ============================================================================
-- Se você chegou até aqui sem erros, o banco está pronto! ✅
-- 
-- 📋 Credenciais de teste:
--    Admin:      admin@admin.net       / 123456
--    Gestor:     sonia.lima@gestor.net / 1234546
--    Técnico:    mariana@tecnico.net   / 123456
--    Usuário:    usuario@teste.net     / 123456
-- 
-- ⚠️ ATENÇÃO: Altere estas senhas antes de usar em produção!
-- ============================================================================
