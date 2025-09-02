package Sookmyung.Lingo.common.jwt;

import Sookmyung.Lingo.common.dto.ErrorResponse;
import Sookmyung.Lingo.common.exception.CustomException;
import Sookmyung.Lingo.common.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Public API인 경우 필터링하지 않고 다음 필터로 넘어감
        if (isPublicApi(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. Request Header에서 JWT 토큰 추출
            String token = jwtTokenProvider.resolveToken(request);

            if (token != null) {
                if (!jwtTokenProvider.validateToken(token)) {
                    // 유효하지 않은 토큰이 왔으면 → 그냥 인증 안 함 (예외 안 던지고)
                    filterChain.doFilter(request, response);
                    return;
                }

                if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))) {
                    filterChain.doFilter(request, response);
                    return;
                }

                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (CustomException ex) {
            // JSON 형태로 응답 내려주기
            response.setStatus(ex.getErrorCode().getHttpStatus().value());
            response.setContentType("application/json;charset=UTF-8");

            ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode());
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(errorResponse);

            response.getWriter().write(json);
            // 여기서 return 해서 체인 진행 중단
            return;
        }
    }

    private boolean isPublicApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.equals("/member/login") ||
                uri.equals("/member/signup") ||
                uri.equals("/member/check-email") ||
                uri.startsWith("/member/reset-password") ||
                uri.equals("/member/find-email") ||
                uri.equals("/member/reissue");
    }

}
