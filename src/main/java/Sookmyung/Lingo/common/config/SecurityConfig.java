package Sookmyung.Lingo.common.config;

import Sookmyung.Lingo.common.exception.CustomAccessDeniedHandler;
import Sookmyung.Lingo.common.exception.CustomAuthenticationEntryPoint;
import Sookmyung.Lingo.common.jwt.JwtAuthenticationFilter;
import Sookmyung.Lingo.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CustomAuthenticationEntryPoint entryPoint,
                                           CustomAccessDeniedHandler accessDeniedHandler) throws Exception {
        // CSRF 보호 비활성화
        http.csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        // CORS 설정
        // http.cors(cors -> cors.disable()); // CORS 설정은 필요에 따라 활성화 http
        http.httpBasic(httpBasic -> httpBasic.disable());  // HTTP Basic 인증을 비활성화합니다.
        http.cors(Customizer.withDefaults());

        // 세션 생성/사용 금지 = 완전 무상태
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.securityContext(sc -> sc.securityContextRepository(new NullSecurityContextRepository()));

        // H2 콘솔 접근 허용
        http.authorizeHttpRequests(authz -> authz
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 프리플라이트 요청 허용
                .requestMatchers("/h2-console/**", "/favicon.ico").permitAll()  // H2 콘솔 경로는 인증 없이 접근 가능
                .requestMatchers("/api/**", "/api-docs", "/swagger-ui.html", "/api-docs/**", "/swagger-ui/**", "/v3/api-docs/**", "/api/test/**").permitAll() // 스웨거 인증 없이 접근 허용
                // 인증 없이 가능한 회원 관련 엔드포인트만 명시적으로 공개
                .requestMatchers(HttpMethod.POST,
                        "/member/login",
                        "/member/signup",
                        "/member/check-email",
                        "/member/find-email",
                        "/member/reset-password/**"   // send / verify 포함
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/member/reissue").permitAll()
                .anyRequest().authenticated()  // 나머지 요청은 인증 필요
        );

        // 예외 처리
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(accessDeniedHandler)
        );

        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider, redisTemplate),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    // CORS 허용 도메인/헤더/메서드 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 네이티브 앱만 쓴다면 * 도 무방. 웹(크리덴셜)과 함께라면 명시 오리진 필요
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(false); // JWT 헤더 인증이면 보통 false

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}