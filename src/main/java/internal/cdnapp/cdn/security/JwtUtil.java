package internal.cdnapp.cdn.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    // Clave secreta (en un sistema real deberías ponerla en application.properties y que sea larga y segura)
    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 1 hora de validez
    private final long expirationMs = 3600000;

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email) // identificador principal del usuario
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
