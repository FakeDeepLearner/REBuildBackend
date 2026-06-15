package com.rebuild.backend.config.other;

import com.cloudinary.Cloudinary;
import com.sendgrid.SendGrid;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.filter.UrlHandlerFilter;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import javax.sql.DataSource;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class SingularBeansConfig {
    @Bean
    public SendGrid sendGrid()
    {
        return new SendGrid(System.getenv("TWILIO_SENDGRID_KEY"));
    }

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(System.getenv("CLOUDINARY_URL"));
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

    @Bean(name = "executor")
    public Executor virtualThreadExecutor()
    {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean(name = "encoder")
    public PasswordEncoder encoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DataSource dataSource()
    {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/resumes");
        dataSource.setUsername("postgres");
        dataSource.setPassword("12Eren89");
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public S3Client s3Client()
    {
        return S3Client.builder().credentialsProvider(() ->
                AwsBasicCredentials.create(System.getenv("AWS_ACCESS_KEY_ID"),
                System.getenv("AWS_SECRET_ACCESS_KEY"))).
                region(Region.CA_CENTRAL_1).build();
    }

}
