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
        if (request.getRequestURI().equals("/member/reissue")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // 1. Request Header에서 JWT 토큰 추출
            String token = jwtTokenProvider.resolveToken(request);

            // 2. 유효성 검증
            if (token != null) {
                if (!jwtTokenProvider.validateToken(token)) {
                    // 토큰이 왔는데 유효하지 않으면 401 JSON으로 바로 반환하고 종료하고 싶다면:
                    throw new CustomException(ErrorCode.UNAUTHORIZED_TOKEN);
                }
                // 3. 블랙리스트 검사
                if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))) {
                    throw new CustomException(ErrorCode.UNAUTHORIZED_TOKEN);
                }

                // 4. 인증 처리
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

}
