package com.rebuild.backend.config.redis.bucket4j;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.resource.ClientResources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
public class Bucket4JConfig {

    @Bean
    public ProxyManager<String> bucketProxyManager(LettuceConnectionFactory connectionFactory){
        RedisClient redisClient = RedisClient.create("redis://localhost:6379");

        StatefulRedisConnection<byte[], byte[]> connection = redisClient.connect(new ByteArrayCodec());

        RedisAsyncCommands<byte[], byte[]> asyncCommands = connection.async();

        return LettuceBasedProxyManager.builderFor(asyncCommands).build().
                withMapper(input -> input.getBytes(StandardCharsets.UTF_8));

    }

    @Bean
    public BucketConfiguration bucketConfiguration(){
        Bandwidth bandwidth = Bandwidth.builder().
                capacity(5).refillIntervally(5, Duration.ofMinutes(5)).build();

        return BucketConfiguration.builder().addLimit(bandwidth).build();
    }
}
