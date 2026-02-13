package uk.gov.hmcts.opal.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.data.redis.RedisHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;

@Slf4j(topic = "opal.CacheConfig")
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${opal.redis.enabled}")
    private boolean redisEnabled;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${opal.redis.ttl-hours}")
    private long redisTtlHours;

    private CacheManager cacheManager;

    @Bean
    @ConditionalOnProperty(name = "opal.redis.enabled", havingValue = "true")
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    @ConditionalOnProperty(name = "opal.redis.enabled", havingValue = "true")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "opal.redis.enabled", havingValue = "true")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(redisTtlHours))
            .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        this.cacheManager = RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(cacheConfig)
            .build();
        logCacheDetails(cacheManager);
        return cacheManager;
    }

    @Bean
    @ConditionalOnProperty(name = "opal.redis.enabled", havingValue = "true")
    @ConditionalOnEnabledHealthIndicator("redis")
    public RedisHealthIndicator redisHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
        return new RedisHealthIndicator(redisConnectionFactory);
    }

    @Bean
    @ConditionalOnProperty(name = "opal.redis.enabled", havingValue = "false", matchIfMissing = true)
    public CacheManager simpleCacheManager() {
        this.cacheManager = new ConcurrentMapCacheManager();
        logCacheDetails(cacheManager);
        return cacheManager;
    }

    public void logCacheDetails(CacheManager cacheManager) {
        log.info("------------------------------");
        log.info("Cache Configuration Details:");
        log.info("Redis Enabled: {}", redisEnabled);
        log.info("Redis Host: {}", redisHost);
        log.info("Redis Port: {}", redisPort);
        log.info("Redis TTL (hours): {}", redisTtlHours);
        if (cacheManager != null) {
            log.info("Cache Manager: {}", cacheManager.getClass().getName());
            if (cacheManager instanceof RedisCacheManager) {
                log.info("Using Redis Cache Manager");
            } else if (cacheManager instanceof ConcurrentMapCacheManager) {
                log.info("Using Concurrent Map Cache Manager (local cache)");
            }
        } else {
            log.warn("Cache Manager is null. This might indicate a configuration issue.");
        }

        log.info("Available Caches:");
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> log.debug("- {}", cacheName));
        }
        log.info("------------------------------");
    }

    @Bean("KeyGeneratorForOptionalList")
    public KeyGenerator generateKeyFromList() {
        return (target, method, params) -> {
            List<String> key = new ArrayList<>();
            for (Object param : params) {
                if (param instanceof Optional) {
                    ((Optional<?>) param).ifPresentOrElse(
                        filter -> generateKey(filter, key), () -> key.add("noFilter"));
                } else {
                    generateKey(param, key);
                }
            }
            return String.join("_", key);
        };
    }

    private void generateKey(Object filter, List<String> sb) {
        if (filter instanceof String) {
            sb.add((String) filter);
        }
        if (filter instanceof List) {
            List<String> stringStream = ((List<?>) filter)
                .stream()
                .map((obj) -> Objects.toString(obj, null))
                .sorted()
                .toList();
            sb.addAll(stringStream);
        }
    }
}
