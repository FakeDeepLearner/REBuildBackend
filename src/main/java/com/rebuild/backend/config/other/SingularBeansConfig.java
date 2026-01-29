package com.rebuild.backend.config.other;

import com.cloudinary.Cloudinary;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.sendgrid.SendGrid;
import com.twilio.Twilio;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.filter.UrlHandlerFilter;

import java.nio.charset.Charset;

@Configuration
public class SingularBeansConfig {

    @Bean
    public Dotenv dotenv()
    {
        return Dotenv.load();
    }

    @Bean
    public DbxRequestConfig requestConfig()
    {
        return DbxRequestConfig.newBuilder("rerebuild").build();
    }

    @Bean
    public Cloudinary cloudinary(Dotenv dotenv) {
        return new Cloudinary(dotenv.get("CLOUDINARY_URL"));
    }

    @Bean
    public BloomFilter<String> bloomFilter()
    {
        return BloomFilter.
                create(Funnels.stringFunnel(Charset.defaultCharset()),
                        1_000_000, 0.01);
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor hibernateTranslation(){
        return new PersistenceExceptionTranslationPostProcessor();
    }

    //This will redirect to the original endpoint if there is a trailing slash at the end of the request's URL.
    @Bean
    public UrlHandlerFilter handlerFilter()
    {

        return UrlHandlerFilter
                .trailingSlashHandler("/api/**", "/home/**").redirect(HttpStatus.PERMANENT_REDIRECT)
                .build();
    }


    @Bean(name = "processor")
    public MethodValidationPostProcessor processor(){
        return new MethodValidationPostProcessor();
    }

}
