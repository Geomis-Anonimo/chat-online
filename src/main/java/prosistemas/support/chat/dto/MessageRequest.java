package prosistemas.support.chat.dto;

import org.hibernate.validator.constraints.Length;

public record MessageRequest(
    @Length(max = 2000, min = 1, message = "O conteúdo deve ter no mínimo 1 caratere e no máximo 2000 caracteres")
    String content
) {
}
