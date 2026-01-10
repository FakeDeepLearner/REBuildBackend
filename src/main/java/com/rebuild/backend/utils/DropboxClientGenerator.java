package com.rebuild.backend.utils;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DropboxClientGenerator {

    private final DbxRequestConfig requestConfig;

    @Autowired
    public DropboxClientGenerator(DbxRequestConfig requestConfig) {
        this.requestConfig = requestConfig;
    }

    public DbxClientV2 generateClientWith(String accessToken)
    {
        return new DbxClientV2(requestConfig, accessToken);
    }
}
