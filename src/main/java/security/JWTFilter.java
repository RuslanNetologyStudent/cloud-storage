package security;

import enums.Role;
import java.util.Set;
import java.util.List;
import java.io.IOException;
import io.jsonwebtoken.Claims;
import javax.servlet.FilterChain;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import configuration.AuthenticationConfigurationConstants;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTFilter extends GenericFilterBean {

    private final JWTToken jwtToken;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String token = getTokenFromRequest((HttpServletRequest) servletRequest);
        if (token != null && jwtToken.validateAccessToken(token)) {
            log.info("Токен действителен: {}", token);
            Claims claims = jwtToken.getAccessClaims(token);

            JWTAuthentication jwtInfoToken = new JWTAuthentication();
            jwtInfoToken.setRoles(getRoles(claims));
            jwtInfoToken.setUsername(claims.getSubject());
            jwtInfoToken.setAuthenticated(true);

            SecurityContextHolder.getContext().setAuthentication(jwtInfoToken);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader(AuthenticationConfigurationConstants.AUTH_TOKEN);
        if (StringUtils.hasText(bearer) && bearer.startsWith(AuthenticationConfigurationConstants.TOKEN_PREFIX)) {
            return bearer.substring(7);
        }
        return null;
    }

    private static Set<Role> getRoles(Claims claims) {
        List<String> roles = claims.get("roles", List.class);
        return roles.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }
}