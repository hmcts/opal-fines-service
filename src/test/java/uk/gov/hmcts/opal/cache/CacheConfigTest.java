package uk.gov.hmcts.opal.cache;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@SpringBootTest(classes = {CacheConfig.class, CacheConfigTest.TestConfig.class})
class CacheConfigTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
        registry.add("opal.redis.ttl.hours", () -> "8");
        registry.add("opal.redis.enabled", () -> "false");
    }

    @Test
    void whenRedisEnabled_thenReturnRedisCacheManager() {
        assertTrue(cacheManager instanceof RedisCacheManager);
    }

    @Test
    void whenRedisDisabled_thenReturnSimpleCacheManager() {
        CacheConfig config = new CacheConfig();
        CacheManager simpleCacheManager = config.simpleCacheManager();
        assertTrue(simpleCacheManager instanceof ConcurrentMapCacheManager);
    }

    @Test
    void testRedisConnectionFactory() {
        assertNotNull(redisConnectionFactory);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public RedisConnectionFactory redisConnectionFactory() {
            return mock(RedisConnectionFactory.class);
        }

        @Bean
        @Primary
        public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
            return RedisCacheManager.builder(redisConnectionFactory).build();
        }
    }
}
