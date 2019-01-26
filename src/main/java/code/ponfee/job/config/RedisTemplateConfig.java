//package code.ponfee.job.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.RedisSentinelConfiguration;
//import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//import code.ponfee.commons.jedis.spring.BytesRedisSerializer;
//import code.ponfee.commons.jedis.spring.KryoRedisSerializer;
//import code.ponfee.commons.jedis.spring.SentinelRedisNodes;
//import redis.clients.jedis.JedisPoolConfig;
//
///**
// * RedisTemplate初始化
// *
// * @author Ponfee
// */
//@Configuration
//public class RedisTemplateConfig {
//
//    private @Value("${redis.sentinel.master}") String   sentinelMaster;
//    private @Value("${redis.sentinel.nodes}" ) String[] sentinelNodes;
//    private @Value("${redis.password}"       ) String   password;
//    private @Value("${redis.timeout}"        ) int      timeout;
//    private @Value("${redis.pool.max-active}") int      poolMaxActive;
//    private @Value("${redis.pool.max-wait}"  ) int      poolMaxWait;
//    private @Value("${redis.pool.max-idle}"  ) int      poolMaxIdle;
//    private @Value("${redis.pool.min-idle}"  ) int      poolMinIdle;
//
//    /**
//     * 连接redis的工厂类
//     *
//     * @return
//     */
//    @Bean
//    public JedisConnectionFactory jedisConnectionFactory() {
//        JedisPoolConfig poolConfig = new JedisPoolConfig();
//        poolConfig.setMaxTotal(poolMaxActive);
//        poolConfig.setMaxIdle(poolMaxIdle);
//        poolConfig.setMinIdle(poolMinIdle);
//        poolConfig.setMaxWaitMillis(poolMaxWait);
//
//        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
//        sentinelConfig.setMaster(sentinelMaster);
//        sentinelConfig.setSentinels(new SentinelRedisNodes(sentinelNodes));
//
//        JedisConnectionFactory connFactory = new JedisConnectionFactory(sentinelConfig);
//        connFactory.setTimeout(timeout);
//        connFactory.setUsePool(true);
//        connFactory.setPassword(password);
//        connFactory.setPoolConfig(poolConfig);
//
//        return connFactory;
//    }
//
//    @Bean
//    public RedisTemplate<String, String> strRedisTemplate(
//            RedisConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String, String> template = new StringRedisTemplate(redisConnectionFactory);
//        template.setEnableTransactionSupport(false);
//        template.setExposeConnection(false);
//        return template;
//    }
//
//    @Bean("redisTemplate")
//    public RedisTemplate<String, Object> redisTemplate(
//            RedisConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisConnectionFactory);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new KryoRedisSerializer<>());
//        template.setEnableTransactionSupport(false);
//        template.setExposeConnection(false);
//        template.afterPropertiesSet();
//        return template;
//    }
//
//    @Bean
//    public RedisTemplate<String, byte[]> bytRedisTemplate(
//            RedisConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisConnectionFactory);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new BytesRedisSerializer());
//        template.setEnableTransactionSupport(false);
//        template.setExposeConnection(false);
//        template.afterPropertiesSet();
//        return template;
//    }
//
//}
