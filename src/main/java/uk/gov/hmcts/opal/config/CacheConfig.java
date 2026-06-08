package uk.gov.hmcts.opal.config;

import tools.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.data.redis.health.DataRedisHealthIndicator;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
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
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;

@Slf4j(topic = "opal.CacheConfig")
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${opal.redis.enabled}")
    private boolean redisEnabled;

    @Value("${spring.data.redis.url}")
    private String redisUrl;

    @Value("${opal.redis.ttl-duration}")
    private Duration redisTtlDuration;

    private CacheManager cacheManager;

    @Bean
    @ConditionalOnProperty(name = "opal.redis.enabled", havingValue = "true")
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(LettuceConnectionFactory.createRedisConfiguration(redisUrl));
    }

    @Bean
    @ConditionalOnProperty(name = "opal.redis.enabled", havingValue = "true")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        RedisSerializer<String> keySerializer = redisKeySerializer();
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        RedisSerializer<Object> valueSerializer = redisValueSerializer();
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "opal.redis.enabled", havingValue = "true")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(redisTtlDuration)
            .serializeKeysWith(SerializationPair.fromSerializer(redisKeySerializer()))
            .serializeValuesWith(SerializationPair.fromSerializer(redisValueSerializer()));

        this.cacheManager = RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(cacheConfig)
            .build();
        logCacheDetails(cacheManager);
        return cacheManager;
    }

    private RedisSerializer<Object> redisValueSerializer() {
        return new GenericJacksonJsonRedisSerializer(new ObjectMapper());
    }
    private RedisSerializer<String> redisKeySerializer() {
        return RedisSerializer.string();
    }

    @Bean
    @ConditionalOnProperty(name = "opal.redis.enabled", havingValue = "true")
    @ConditionalOnEnabledHealthIndicator("redis")
    public DataRedisHealthIndicator redisHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
        return new DataRedisHealthIndicator(redisConnectionFactory);
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
        log.info("Redis Url: {}", redisUrl);
        log.info("Redis TTL (duration): {}", redisTtlDuration);
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
            return Arrays.stream(params)
                .flatMap(param -> {
                    if (param instanceof Optional<?> optional) {
                        return optional.map(this::generateKeyParts)
                            .orElseGet(() -> Stream.of("noFilter"));
                    }
                    return generateKeyParts(param);
                })
                .collect(Collectors.joining("_"));
        };
    }

    private Stream<String> generateKeyParts(Object filter) {
        return switch (filter) {
            case String s -> Stream.of(s);
            case List<?> l -> l.stream().map(Object::toString).sorted();
            default -> Stream.empty();
        };
    }
}
