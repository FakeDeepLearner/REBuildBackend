package com.rebuild.backend.config.redis.redis_generators;

import com.rebuild.backend.model.entities.user_entities.User;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

@Component(value = "resumeCacheKeyGenerator")
public class ResumeCacheKeyGenerator implements KeyGenerator {
    @Override
    public @NonNull Object generate(@NonNull Object target, @NonNull Method method,
                                    @Nullable Object @NonNull ... params) {

        User foundUser = new User();

        UUID resumeID =  UUID.randomUUID();

        for (Object param : params) {
            if (param instanceof UUID id) {
                resumeID = id;
            }

            if (param instanceof User u) {
                foundUser = u;
            }
        }

        //They are guaranteed to be initialized by the time the for loop is done
        return foundUser.getId().toString() + ":" + resumeID;
    }
}
