package br.com.brisabr.helpdesk_api.user;

/**
 * Enum que representa os perfis/roles de usuários no sistema.
 * 
 * Hierarquia de permissões (do maior para o menor):
 * ADMIN > MANAGER > TECHNICIAN > USER
 */
public enum UserRole {
    /**
     * Administrador do sistema - acesso total.
     */
    ADMIN("admin", "Administrador"),
    
    /**
     * Gestor - pode atribuir tickets e visualizar relatórios gerenciais.
     */
    MANAGER("manager", "Gestor"),
    
    /**
     * Técnico - pode capturar e resolver tickets.
     */
    TECHNICIAN("technician", "Técnico"),
    
    /**
     * Usuário comum - pode criar e visualizar seus próprios tickets.
     */
    USER("user", "Usuário");
    
    private final String value;
    private final String displayName;
    
    UserRole(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Verifica se este role tem permissão de admin.
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
    
    /**
     * Verifica se este role tem permissão de gerenciamento (admin ou manager).
     */
    public boolean isManagerOrAbove() {
        return this == ADMIN || this == MANAGER;
    }
    
    /**
     * Verifica se este role pode trabalhar em tickets (técnico ou superior).
     */
    public boolean isTechnicianOrAbove() {
        return this == ADMIN || this == MANAGER || this == TECHNICIAN;
    }
    
    /**
     * Converte string para enum, aceitando tanto o value quanto o displayName.
     */
    public static UserRole fromString(String role) {
        if (role == null) {
            return null;
        }
        
        String normalized = role.toLowerCase().trim();
        for (UserRole ur : UserRole.values()) {
            if (ur.value.equals(normalized) || ur.displayName.equalsIgnoreCase(role)) {
                return ur;
            }
        }
        throw new IllegalArgumentException("Role inválido: " + role);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
