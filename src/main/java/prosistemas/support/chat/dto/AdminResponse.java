package prosistemas.support.chat.dto;

import prosistemas.support.chat.model.Admin;

public record AdminResponse(Long id, String name, String token) {

    public static AdminResponse from(Admin admin, String token) {
        return new AdminResponse(admin.getId(), admin.getName(), token);
    }
}