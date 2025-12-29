package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.users.User;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component(value = "profileCacheKeyGenerator")
public class ProfileCacheKeyGenerator implements KeyGenerator {
    @Override
    public @NonNull Object generate(@NonNull Object target, @NonNull Method method,
                                    @Nullable Object @NonNull ... params) {

        User foundUser = new User();


        for (Object param : params) {

            if (param instanceof User u) {
                foundUser = u;
                break;
            }
        }

        //They are guaranteed to be initialized by the time the for loop is done
        return foundUser.getId().toString();
    }
}
