package com.rebuild.backend.service;

import com.rebuild.backend.config.properties.MailAppCredentials;
import com.rebuild.backend.exceptions.not_found_exceptions.UserNotFoundException;
import com.rebuild.backend.exceptions.otp_exceptions.InvalidOtpException;
import com.rebuild.backend.exceptions.otp_exceptions.OTPAlreadyGeneratedException;
import com.rebuild.backend.exceptions.otp_exceptions.OTPExpiredException;
import com.rebuild.backend.model.entities.enums.OTPGenerationPurpose;
import com.rebuild.backend.model.entities.User;
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

    private final UserService userService;

    private final Random random = new Random();

    @Autowired
    public OTPService(@Qualifier("otpCacheManager")
                          RedisCacheManager otpCacheManager,
                      @Qualifier("connectionsCacheManager") RedisCacheManager blockedCacheManager,
                      JavaMailSender mailSender, MailAppCredentials credentials,
                      UserService userService) {
        this.otpCacheManager = otpCacheManager;
        this.blockedCacheManager = blockedCacheManager;
        this.mailSender = mailSender;
        this.credentials = credentials;
        this.userService = userService;
    }


    private int generateRandomOtp(){
        return random.nextInt(100000, 1000000);
    }


    public int generateOtpFor(String email, OTPGenerationPurpose purpose){
        int generatedOtp = generateRandomOtp();
        Cache emailsCache;
        if (purpose.equals(OTPGenerationPurpose.ACCOUNT_UNLOCK)){
            emailsCache = otpCacheManager.getCache("email_otp");
        }
        else{
            emailsCache = otpCacheManager.getCache("reactivation_otp");
        }
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

    public int generateOtpFor(String phoneNumber){
        int generatedOtp = generateRandomOtp();
        Cache emailsCache = otpCacheManager.getCache("phone_otp");
        assert emailsCache != null;
        Cache.ValueWrapper wrapper = emailsCache.putIfAbsent(phoneNumber, generatedOtp);
        if(wrapper == null){
            return generatedOtp;
        }
        else{
            throw new OTPAlreadyGeneratedException("A one time passcode has already been sent to your email");
        }
    }


    public void validateOtpFor(String email, int enteredOtp, OTPGenerationPurpose purpose){
        Cache emailsCache;
        if(purpose.equals(OTPGenerationPurpose.ACCOUNT_UNLOCK)){
            emailsCache = otpCacheManager.getCache("email_otp");
        }
        else{
            emailsCache = otpCacheManager.getCache("reactivation_otp");
        }
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
                if(purpose.equals(OTPGenerationPurpose.ACCOUNT_UNLOCK)) {
                    Cache connectionsCache = blockedCacheManager.getCache("email_connections");
                    assert connectionsCache != null;
                    connectionsCache.evict(email);
                }
                else{
                    User actualUser = userService.findByEmail(email).orElseThrow(() ->
                        new UserNotFoundException("A user with email" + email + "has not been found")
                    );
                    userService.reactivateUserCredentials(actualUser);


                }
            }
        }
    }

    public void validateOtpFor(String phoneNumber, int enteredOtp){
        Cache emailsCache = otpCacheManager.getCache("phone_otp");
        assert emailsCache != null;
        Cache.ValueWrapper wrapper = emailsCache.get(phoneNumber);
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
