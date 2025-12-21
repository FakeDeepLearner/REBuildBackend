package com.rebuild.backend.config.redis;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.github.cdimascio.dotenv.Dotenv;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;

@Configuration
public class Bucket4JConfig {


    @Bean
    public ProxyManager<String> bucketProxyManager(Dotenv dotenv){

        RedisURI uri = RedisURI.Builder.redis(dotenv.get("REDIS_DATABASE_URL"),
                Integer.parseInt(dotenv.get("REDIS_DATABASE_PORT"))).
                withAuthentication("default", dotenv.get("REDIS_DATABASE_PASSWORD")).
                withSsl(true).
                build();

        RedisClient redisClient = RedisClient.create(uri);

        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        RedisAsyncCommands<String, byte[]> asyncCommands = connection.async();

        return LettuceBasedProxyManager.builderFor(asyncCommands).build();

    }

    @Bean
    public BucketConfiguration bucketConfiguration(){
        Bandwidth bandwidth = Bandwidth.builder().
                capacity(10).refillIntervally(5, Duration.ofMinutes(2)).build();

        return BucketConfiguration.builder().addLimit(bandwidth).build();
    }
}
