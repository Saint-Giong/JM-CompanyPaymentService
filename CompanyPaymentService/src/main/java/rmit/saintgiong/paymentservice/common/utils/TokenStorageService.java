package rmit.saintgiong.paymentservice.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

// Service for managing token storage in Redis.
// Uses blocklist approach for access tokens (store revoked tokens).
// Uses whitelist approach for refresh tokens (store valid tokens).
// Stores activation tokens for account activation.
@Service
@Slf4j
@RequiredArgsConstructor
public class TokenStorageService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLOCKLIST_ACCESS_TOKEN_PREFIX = "blocklist:access_token:";

    public boolean isAccessTokenBlocked(String tokenId) {
        String key = BLOCKLIST_ACCESS_TOKEN_PREFIX + tokenId;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}
