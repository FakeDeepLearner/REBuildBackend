package com.rebuild.backend.config.redis;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Component
public class RedisProfileSerializer implements RedisSerializer<UserProfile> {

    @SneakyThrows
    @Override
    public byte @NonNull [] serialize(UserProfile value) throws SerializationException {
        if(value == null) {
            return new byte[0];
        }

        //The try-with-resources statement automatically closes the streams when it is done.
        try(ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream)) {
            objectOutputStream.writeObject(value);

            objectOutputStream.flush();

            return byteStream.toByteArray();
        }
    }

    @SneakyThrows
    @Override
    public UserProfile deserialize(byte @NonNull [] bytes) throws SerializationException {
        if(bytes.length == 0) {
            return null;
        }

        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            Object result =  objectInputStream.readObject();
            if (result instanceof UserProfile u) {
                return u;
            }
            throw new SerializationException("The result is type " +  result.getClass() + " instead of UserProfile");

        }

    }
}
