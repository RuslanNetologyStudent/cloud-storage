package security;

import java.util.Date;
import java.util.List;
import lombok.NonNull;
import java.time.ZoneId;
import java.security.Key;
import entity.UserEntity;
import io.jsonwebtoken.*;
import java.util.ArrayList;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.stereotype.Component;
import configuration.AuthenticationConfigurationConstants;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Component
public class JWTToken {

    private final SecretKey secret;
    private final int tokenLifetime;
    private final List<String> listTokens = new ArrayList<>();
    private UserEntity userEntity;

    public JWTToken(@Value("${jwt.secret}") String secret, @Value("${TOKEN_LIFETIME}") int tokenLifetime) {
        this.secret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.tokenLifetime = tokenLifetime;
    }

    public UserEntity getAuthenticatedUser() {
        return userEntity;
    }

    public String generateToken(@NonNull UserEntity userEntity) throws IllegalArgumentException {
        this.userEntity = userEntity;
        Date now = new Date();
        Date exp = Date.from(LocalDateTime.now().plusMinutes(tokenLifetime)
                .atZone(ZoneId.systemDefault()).toInstant());

        String token = Jwts.builder()
                .setId(String.valueOf(userEntity.getId()))
                .setSubject(userEntity.getLogin())
                .setIssuedAt(now)
                .setNotBefore(now)
                .setExpiration(exp)
                .signWith(secret)
                .claim("roles", userEntity.getRoles())
                .compact();
        log.info("Auth-token {} добавлен в список активных токеннов", token);
        listTokens.add(token);
        return token;
    }

    public boolean validateAccessToken(@NonNull String token) {
        for (String t : listTokens) {
            if (!t.equals(token)) {
                return false;
            }
        }
        return validateToken(token, secret);
    }

    private boolean validateToken(@NonNull String token, @NonNull Key secret) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.error("Срок действия токена истек ", expEx);
        } catch (UnsupportedJwtException unsEx) {
            log.error("Форма токена неподдерживается jwt ", unsEx);
        } catch (MalformedJwtException mjEx) {
            log.error("Форма токена некорректна для jwt ", mjEx);
        } catch (SignatureException sEx) {
            log.error("Недействительная подпись ", sEx);
        } catch (Exception e) {
            log.error("Недопустимый токен ", e);
        }
        return false;
    }

    public Claims getAccessClaims(@NonNull String token) {
        return getClaims(token, secret);
    }

    private Claims getClaims(@NonNull String token, @NonNull Key secret) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void removeToken(String token) {
        listTokens.remove(token.substring(AuthenticationConfigurationConstants.TOKEN_PREFIX.length()));
    }
}