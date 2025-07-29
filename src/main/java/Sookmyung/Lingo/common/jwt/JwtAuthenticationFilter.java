package Sookmyung.Lingo.jwt;

import Sookmyung.Lingo.common.CustomException;
import Sookmyung.Lingo.common.ErrorCode;
import Sookmyung.Lingo.common.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        jakarta.servlet.http.HttpServletResponse httpResponse = (jakarta.servlet.http.HttpServletResponse) response;

        try {
            // 1. Request Header에서 JWT 토큰 추출
            String token = jwtTokenProvider.resolveToken(httpRequest);

            // 2. 유효성 검증
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 3. 블랙리스트 검사
                if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))) {
                    throw new CustomException(ErrorCode.UNAUTHORIZED_TOKEN);
                }

                // 4. 인증 처리
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            chain.doFilter(request, response);

        } catch (CustomException ex) {
            // JSON 형태로 응답 내려주기
            httpResponse.setStatus(ex.getErrorCode().getHttpStatus().value());
            httpResponse.setContentType("application/json;charset=UTF-8");

            ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode());
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(errorResponse);

            httpResponse.getWriter().write(json);
        }
    }

}
