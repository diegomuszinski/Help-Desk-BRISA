-- Script para corrigir os hashes de senha dos usuários de teste
-- Execute este script no pgAdmin conectado ao banco 'helpdesk'
-- Hash BCrypt para senha "123456": $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi

-- Atualizar senha do admin
UPDATE usuarios 
SET senha = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email = 'admin@admin.net';

-- Atualizar senha do técnico
UPDATE usuarios 
SET senha = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email = 'mariana@tecnico.net';

-- Atualizar senha do usuário comum
UPDATE usuarios 
SET senha = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email = 'usuario@teste.net';

-- Verificar se as senhas foram atualizadas
SELECT 
    email, 
    perfil,
    LEFT(senha, 30) || '...' as senha_hash,
    LENGTH(senha) as hash_length
FROM usuarios 
WHERE email IN ('admin@admin.net', 'mariana@tecnico.net', 'usuario@teste.net')
ORDER BY id;
