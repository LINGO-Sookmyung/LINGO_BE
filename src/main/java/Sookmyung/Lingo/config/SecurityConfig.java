package Sookmyung.Lingo.config;

import Sookmyung.Lingo.jwt.JwtAuthenticationFilter;
import Sookmyung.Lingo.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF 보호 비활성화
        http.csrf(csrf -> csrf.disable());
        // CORS 설정
        // http.cors(cors -> cors.disable()); // CORS 설정은 필요에 따라 활성화 http
        http.httpBasic(httpBasic -> httpBasic.disable());  // HTTP Basic 인증을 비활성화합니다.
        http.sessionManagement((auth) -> auth
                .maximumSessions(1)  // 한 사용자가 가질 수 있는 세션의 최대 수를 1로 설정합니다.
                .maxSessionsPreventsLogin(true));  // 최대 세션 수를 초과한 경우 새로운 로그인을 막습니다.
        http.sessionManagement((auth) -> auth
                .sessionFixation().changeSessionId());  // 세션 고정을 방지하기 위해 세션 ID를 변경합니다.

        // H2 콘솔 접근 허용
        http.authorizeHttpRequests(authz -> authz
                .requestMatchers("/h2-console/**").permitAll()  // H2 콘솔 경로는 인증 없이 접근 가능
                .requestMatchers("/api-docs", "/swagger-ui.html", "/api-docs/**", "/swagger-ui/**").permitAll() // 스웨거 인증 없이 접근 허용
                .requestMatchers("/member/**").permitAll() // 임시 접근 허용
                .anyRequest().authenticated()  // 나머지 요청은 인증 필요
        );

        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
