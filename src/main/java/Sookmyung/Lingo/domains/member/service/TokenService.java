package Sookmyung.Lingo.domains.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RedisTemplate<String, String> redisTemplate;

    // 토큰을 Redis에 저장
    public void saveRefreshToken(String userId, String refreshToken, long expirationMillis) {
        redisTemplate.opsForValue().set("refresh:" + userId, refreshToken, expirationMillis, TimeUnit.MILLISECONDS);
    }

    // 토큰 삭제
    public void deleteRefreshToken(String userId) {
        redisTemplate.delete("refresh:" + userId);
    }

    // 토큰 유효성 검사
    public boolean isRefreshTokenValid(String userId, String token) {
        String stored = redisTemplate.opsForValue().get("refresh:" + userId);
        return token.equals(stored);
    }
}
