package prosistemas.support.chat.security;

import java.security.Principal;

import prosistemas.support.chat.model.Sender;

public record User (Sender type, Long id) implements Principal {

    @Override
    public String getName() {
        return type + "-" + id;
    }
}