package com.rebuild.backend.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Component
public class RedisResumeSerializer implements RedisSerializer<Resume> {

    private final ObjectMapper mapper;

    public RedisResumeSerializer(@Qualifier("mapper") ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @SneakyThrows
    @Override
    public byte[] serialize(Resume value) throws SerializationException {
        if(value == null) {
            return new byte[0];
        }

        //The try-with-resources statement automatically closes the streams when it is done.
        try(ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteStream)) {
                mapper.writeValue(gzipOutputStream, value);

                //We flush here in order to ensure that the data will be in the stream when we need it.
                gzipOutputStream.flush();

                return byteStream.toByteArray();
        }
    }

    @SneakyThrows
    @Override
    public Resume deserialize(byte[] bytes) throws SerializationException {
        if(bytes == null || bytes.length == 0) {
            return null;
        }

        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);) {
            return mapper.readValue(gzipInputStream, Resume.class);

        }

    }
}
