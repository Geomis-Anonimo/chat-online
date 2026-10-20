package prosistemas.support.chat.dto;

import prosistemas.support.chat.model.StatusSupportConversation;
import prosistemas.support.chat.model.SupportConversation;

public record SupportConversationResponse(
        Long id,
        String title,
        StatusSupportConversation status,
        String clientName,
        String clientEmail,
        String clientPhone,
        String adminName) {

    public static SupportConversationResponse from(SupportConversation supportConversation) {
        var cliente = supportConversation.getClient();
        var atendente = supportConversation.getAdmin();
        return new SupportConversationResponse(
                supportConversation.getId(),
                supportConversation.getTitle(),
                supportConversation.getStatus(),
                cliente.getName(),
                cliente.getEmail(),
                cliente.getPhone(),
                atendente != null ? atendente.getName() : null);
    }
}
