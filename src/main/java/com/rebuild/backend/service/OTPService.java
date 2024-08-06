package com.rebuild.backend.service;

import com.rebuild.backend.config.properties.MailAppCredentials;
import com.rebuild.backend.exceptions.otp_exceptions.InvalidOtpException;
import com.rebuild.backend.exceptions.otp_exceptions.OTPAlreadyGeneratedException;
import com.rebuild.backend.exceptions.otp_exceptions.OTPExpiredException;
import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class OTPService {
    private final RedisCacheManager otpCacheManager;

    private final RedisCacheManager blockedCacheManager;

    private final JavaMailSender mailSender;

    private final MailAppCredentials credentials;
    private final Random random = new Random();

    @Autowired
    public OTPService(@Qualifier("otpCacheManager")
                          RedisCacheManager otpCacheManager,
                      @Qualifier("connectionsCacheManager") RedisCacheManager blockedCacheManager,
                      JavaMailSender mailSender, MailAppCredentials credentials) {
        this.otpCacheManager = otpCacheManager;
        this.blockedCacheManager = blockedCacheManager;
        this.mailSender = mailSender;
        this.credentials = credentials;
    }


    private int generateRandomOtp(){
        return random.nextInt(100000, 1000000);
    }


    public int generateOtpFor(String email){
        int generatedOtp = generateRandomOtp();
        Cache emailsCache = otpCacheManager.getCache("email_otp");
        assert emailsCache != null;
        Cache.ValueWrapper wrapper = emailsCache.putIfAbsent(email, generatedOtp);
        if(wrapper == null){
            sendOtpEmail(email, generatedOtp);
            return generatedOtp;
        }
        else{

            throw new OTPAlreadyGeneratedException("A one time passcode has already been sent to your email");
        }
    }

    public int generateOtpFor(PhoneNumber phoneNumber){
        int generatedOtp = generateRandomOtp();
        Cache emailsCache = otpCacheManager.getCache("phone_otp");
        assert emailsCache != null;
        Cache.ValueWrapper wrapper = emailsCache.putIfAbsent(phoneNumber.fullNumber(), generatedOtp);
        if(wrapper == null){
            return generatedOtp;
        }
        else{
            throw new OTPAlreadyGeneratedException("A one time passcode has already been sent to your email");
        }
    }


    public void validateOtpFor(String email, int enteredOtp){
        Cache emailsCache = otpCacheManager.getCache("email_otp");
        assert emailsCache != null;
        Cache.ValueWrapper wrapper = emailsCache.get(email);
        if(wrapper == null){
            throw new OTPExpiredException("The requested passcode has expired, please request a new one");
        }
        else{
            boolean otpIsValid = (Integer) wrapper.get() == enteredOtp;
            if(!otpIsValid){
                throw new InvalidOtpException("Wrong passcode, please try again");
            }
            else{
                Cache connectionsCache = blockedCacheManager.getCache("email_connections");
                assert connectionsCache != null;
                connectionsCache.evict(email);
            }
        }
    }

    public void validateOtpFor(PhoneNumber phoneNumber, int enteredOtp){
        Cache emailsCache = otpCacheManager.getCache("phone_otp");
        assert emailsCache != null;
        Cache.ValueWrapper wrapper = emailsCache.get(phoneNumber.fullNumber());
        if(wrapper == null){
            throw new OTPExpiredException("The requested passcode has expired, please request a new one");
        }
        else{
            boolean otpIsValid = (Integer) wrapper.get() == enteredOtp;
            if(!otpIsValid){
                throw new InvalidOtpException("Wrong passcode, please try again");
            }
        }
    }

    public void sendOtpEmail(String email, int otpForEmail){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(credentials.address());
        mailMessage.setReplyTo(credentials.replyTo());
        mailMessage.setTo(email);
        mailMessage.setSubject("One time passcode to unlock your account");
        mailMessage.setText("""
                Your one time passcode to unlock your account is 
                """ + "\n" + otpForEmail);
        mailSender.send(mailMessage);
    }

}
