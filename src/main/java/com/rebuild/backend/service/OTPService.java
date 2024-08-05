package com.rebuild.backend.service;

import com.rebuild.backend.exceptions.otp_exceptions.InvalidOtpException;
import com.rebuild.backend.exceptions.otp_exceptions.OTPAlreadyGeneratedException;
import com.rebuild.backend.exceptions.otp_exceptions.OTPExpiredException;
import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@Service
public class OTPService {
    private final RedisCacheManager cacheManager;

    private final JavaMailSender mailSender;
    private final Random random = new Random();

    @Autowired
    public OTPService(@Qualifier("otpCacheManager")
                          RedisCacheManager cacheManager, JavaMailSender mailSender) {
        this.cacheManager = cacheManager;
        this.mailSender = mailSender;
    }


    private int generateRandomOtp(){
        return random.nextInt(100000, 1000000);
    }


    public int generateOtpFor(String email){
        int generatedOtp = generateRandomOtp();
        Cache emailsCache = cacheManager.getCache("email_otp");
        assert emailsCache != null;
        Cache.ValueWrapper wrapper = emailsCache.putIfAbsent(email, generatedOtp);
        if(wrapper == null){
            return generatedOtp;
        }
        else{
            Instant curr = Instant.now();

            throw new OTPAlreadyGeneratedException("A one time passcode has already been sent to your email");
        }
    }

    public int generateOtpFor(PhoneNumber phoneNumber){
        int generatedOtp = generateRandomOtp();
        Cache emailsCache = cacheManager.getCache("phone_otp");
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
        Cache emailsCache = cacheManager.getCache("email_otp");
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
        }
    }

    public void validateOtpFor(PhoneNumber phoneNumber, int enteredOtp){
        Cache emailsCache = cacheManager.getCache("phone_otp");
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

}
