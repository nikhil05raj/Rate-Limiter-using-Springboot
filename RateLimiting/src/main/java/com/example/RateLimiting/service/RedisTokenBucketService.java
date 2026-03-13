package com.example.RateLimiting.service;

import com.example.RateLimiting.config.RateLimiterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

// to store token bucket state in the redis
// manage token per client
// handle token refill based on time
// provide rate limiting logic.
@Service
@RequiredArgsConstructor
public class RedisTokenBucketService {

    private final JedisPool jedisPool;  //Connection pool which is ready to use

    private final RateLimiterProperties properties;  // Redis connection configuration

    private final String TOKENS_KEY_PREFIX = "rate_limiter:tokens:";

    private final String LAST_REFILL_KEY_PREFIX = "rate_limiter:last_refill:";

    public boolean isAllowed(String clientId)
    {
        String tokenKey = TOKENS_KEY_PREFIX + clientId;

        try(Jedis jedis = jedisPool.getResource())
        {
            refillTokens(clientId, jedis);

            String tokenStr = jedis.get(tokenKey);

            long currentTokens = tokenStr != null ? Long.parseLong(tokenStr) : properties.getCapacity();

            if (currentTokens <= 0){

                return false;
            }

            long decremented = jedis.decr(tokenKey);
            return decremented >= 0;
        }
    }

    public long getCapacity(String clientId)
    {
        return properties.getCapacity();
    }

    public long getAvailableTokens(String clientId)
    {
        String tokenKey = TOKENS_KEY_PREFIX + clientId;

        try(Jedis jedis = jedisPool.getResource()) {

            refillTokens(clientId, jedis);
            String tokenStr = jedis.get(tokenKey);
            return tokenStr != null ? Long.parseLong(tokenStr) : properties.getCapacity();
        }
    }

    // purpose is to add tokens based on the elapsed time/interval
    public void refillTokens(String clientId, Jedis jedis)
    {
        String tokensKey = TOKENS_KEY_PREFIX + clientId;
        String lastRefillKey = LAST_REFILL_KEY_PREFIX + clientId;

        // getting the last refill time
        String lastRefillStr = jedis.get(lastRefillKey);

        if (lastRefillStr == null)
        {
            jedis.set(tokensKey, String.valueOf(properties.getCapacity()));
            jedis.set(lastRefillKey, String.valueOf(now));
        }

        long now = System.currentTimeMillis();
        long lastRefillTime = Long.parseLong(lastRefillStr);
        // calc. the time passed /elapsed
        long elapsedTime = now - lastRefillTime;

        if (elapsedTime <=0){
            return;
        }

        //if elapsedTime>0 , then calc. how much tokens to add
        long tokensToAdd = (elapsedTime * properties.getCapacity())/1000;

        if (tokensToAdd <= 0) {
            return;
        }



    }




}
