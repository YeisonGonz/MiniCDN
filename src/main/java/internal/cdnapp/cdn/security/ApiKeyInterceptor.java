package internal.cdnapp.cdn.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {
    @Value("${security.api-key}")
    private String apiKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Si es GET a /photo/**, dejar pasar
        if (path.startsWith("/photo") && method.equalsIgnoreCase("GET")) {
            return true;
        }

        // Para /photo (POST) y todo /prune, requerimos API key
        String headerKey = request.getHeader("X-API-Key");
        if (headerKey == null || !headerKey.equals(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                    "status": 401,
                    "error": "Unauthorized",
                    "message": "Missing or invalid API key",
                    "timestamp": "%s"
                }
            """.formatted(java.time.LocalDateTime.now()));
            return false;
        }

        return true;
    }
}
