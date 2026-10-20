package prosistemas.support.chat.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import prosistemas.support.chat.model.Sender;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service 
public class TokenService {
    private static final Duration VALIDATION = Duration.ofHours(12);

    private final SecretKey key;

    public TokenService(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(Sender type, Long id) {
        Date now = new Date();

        return Jwts.builder()
            .subject(String.valueOf(id))
            .claim("type", type.name())
            .issuedAt(now)
            .expiration(new Date(now.getTime() + VALIDATION.toMillis()))
            .signWith(key)
            .compact();
    }

    public User validate(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token not setted");
        }

        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(authorization.substring(7))
                    .getPayload();
            return new User(Sender.valueOf(claims.get("type", String.class)), Long.valueOf(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }
    }

    public User validateAdmin(String authorization) {
        User user = validate(authorization);
        if (user.type() != Sender.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Restricted access to admin");
        }
        return user;
    }
}
