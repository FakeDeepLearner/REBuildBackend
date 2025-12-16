package com.rebuild.backend.config.redis;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class RedisResumeSerializer implements RedisSerializer<@NonNull Resume> {

    @SneakyThrows
    @Override
    public byte @NonNull [] serialize(Resume value) throws SerializationException {
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
    public Resume deserialize(byte @NonNull [] bytes) throws SerializationException {
        if(bytes.length == 0) {
            return null;
        }

        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return (Resume) objectInputStream.readObject();

        }

    }
}
