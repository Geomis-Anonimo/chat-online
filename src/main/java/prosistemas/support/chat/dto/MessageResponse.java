package prosistemas.support.chat.dto;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import prosistemas.support.chat.model.Message;
import prosistemas.support.chat.model.Sender;


public record MessageResponse(Long id, Sender sender, String content, OffsetDateTime sentDate) {

    public static MessageResponse from(Message message) {
        OffsetDateTime sentDate = message.getSentDate().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        return new MessageResponse(message.getId(), message.getSender(), message.getContent(), sentDate);
    }
}