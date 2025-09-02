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
        if (request.getRequestURI().startsWith("/public/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. Request Header에서 JWT 토큰 추출
            String token = jwtTokenProvider.resolveToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 블랙리스트 검사
                if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))) {
                    throw new CustomException(ErrorCode.UNAUTHORIZED_TOKEN);
                }

                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            // 유효하지 않거나 없는 토큰은 그냥 통과 (permitAll 등에선 인증 객체 없이도 가능)
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

}
