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

        String token = jwtTokenProvider.resolveToken(request);

        // 공개 API는 무조건 통과
        if (isPublicApi(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (token != null) {
                // 만료된 토큰
                if (!jwtTokenProvider.validateToken(token)) {
                    throw new CustomException(ErrorCode.UNAUTHORIZED_TOKEN);
                }

                // 로그아웃된 토큰
                if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))) {
                    throw new CustomException(ErrorCode.UNAUTHORIZED_TOKEN);
                }

                // 유효한 토큰 → 인증 객체 등록
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
