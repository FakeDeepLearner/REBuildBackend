package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

@Component(value = "resumeCacheKeyGenerator")
public class ResumeCacheKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, @Nullable Object... params) {

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
