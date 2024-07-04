package com.rebuild.backend.service.token_services;

import com.rebuild.backend.model.entities.TokenBlacklistPurpose;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final RedisCacheManager cacheManager;


    @Autowired
    public TokenBlacklistService(RedisCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void blacklistTokenFor(String token, TokenBlacklistPurpose blacklistPurpose){
        Cache blacklistCache = cacheManager.getCache("jwt_tokens");
        if (blacklistCache != null){
            String computedKey = generateProperKey(token, blacklistPurpose);
            blacklistCache.putIfAbsent(computedKey, true);
        }

    }

    public boolean isTokenBlacklisted(String token, TokenBlacklistPurpose purpose){
        Cache blacklistCache = cacheManager.getCache("jwt_tokens");
        assert blacklistCache != null;
        String computedKey = generateProperKey(token, purpose);
        return Boolean.TRUE.equals(blacklistCache.get(computedKey, Boolean.class));
    }

    private String generateProperKey(String token, TokenBlacklistPurpose purpose){
        return purpose.purposeName + ":" + token;
    }
}
