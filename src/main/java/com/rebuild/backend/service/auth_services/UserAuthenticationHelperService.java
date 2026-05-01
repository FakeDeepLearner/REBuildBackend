package com.rebuild.backend.service.auth_services;

import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceClient;
import com.google.recaptchaenterprise.v1.Assessment;
import com.google.recaptchaenterprise.v1.CreateAssessmentRequest;
import com.google.recaptchaenterprise.v1.Event;
import com.google.recaptchaenterprise.v1.ProjectName;
import com.rebuild.backend.model.entities.user_entities.*;
import com.rebuild.backend.model.exceptions.UserAuthException;
import com.rebuild.backend.model.forms.auth_forms.LoginInitializationForm;
import com.rebuild.backend.model.forms.auth_forms.SignupInitializationForm;
import com.rebuild.backend.model.dtos.CredentialValidationDTO;
import com.rebuild.backend.model.dtos.PasswordFeedbackDTO;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.CustomPasswordService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class UserAuthenticationHelperService {

    private final UserRepository userRepository;

    private final Dotenv dotenv;

    private final PasswordEncoder encoder;

    private final CustomPasswordService passwordService;

    private final ProxyManager<String> proxyManager;

    private final BucketConfiguration bucketConfiguration;


    public UserAuthenticationHelperService(UserRepository userRepository,
                                           Dotenv dotenv,
                                           PasswordEncoder encoder,
                                           CustomPasswordService passwordService,
                                           ProxyManager<String> proxyManager,
                                           BucketConfiguration bucketConfiguration) {
        this.userRepository = userRepository;
        this.dotenv = dotenv;
        this.encoder = encoder;
        this.passwordService = passwordService;
        this.proxyManager = proxyManager;
        this.bucketConfiguration = bucketConfiguration;
    }


    private Assessment createAssessment(
            String projectID, String recaptchaSiteKey, String token,
             String userIpAddress, String userAgent)
            throws IOException {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the `client.close()` method on the client to safely
        // clean up any remaining background resources.
        try (RecaptchaEnterpriseServiceClient client = RecaptchaEnterpriseServiceClient.create()) {

            // Set the properties of the event to be tracked.
            Event event = Event.newBuilder()
                    .setSiteKey(recaptchaSiteKey)
                    .setToken(token)
                    .setUserIpAddress(userIpAddress)
                    .setUserAgent(userAgent)
                    .build();

            // Build the assessment request.
            CreateAssessmentRequest createAssessmentRequest =
                    CreateAssessmentRequest.newBuilder()
                            .setParent(ProjectName.of(projectID).toString())
                            .setAssessment(Assessment.newBuilder().setEvent(event).build())
                            .build();

            return client.createAssessment(createAssessmentRequest);
        }
    }


    public boolean captchaFailed(String userResponse, String userIp) {

        try {
            Assessment assessment = createAssessment(dotenv.get("GOOGLE_CAPTCHA_PROJECT_ID"), "6Lel3s0sAAAAAFBcui1DEbyHP99ydRlS6XHnaqlz",
                    userResponse, userIp, "");

            float assessmentScore = assessment.getRiskAnalysis().getScore();

            return assessmentScore < 0.7;
        }
        catch (IOException e){
            throw new UserAuthException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to verify captcha");
        }
    }




    public CredentialValidationDTO validateLoginCredentials(LoginInitializationForm form) {
        String formField = form.emailOrPhone();

        User foundUser = userRepository.findByEmailOrPhoneNumber(formField).orElse(null);

        if (foundUser == null) {
            return null;
        }


        String userSalt = foundUser.getSaltValue();
        String pepper = dotenv.get("PEPPER_VALUE");

        return new CredentialValidationDTO(encoder.matches(form.password() + userSalt + pepper,
                foundUser.getPassword()), foundUser);

    }


    public boolean signupCredentialsAreFree(SignupInitializationForm form) {
        if (form.forumUsername().startsWith("Anonymous"))
        {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "Username cannot start with \"Anonymous\"");
        }
        Optional<User> foundUser = userRepository.findByEmailOrForumUsernameOrPhoneNumber(form.email(),
                form.forumUsername(), form.phoneNumber());

        if (foundUser.isPresent()) {
            throw new UserAuthException(HttpStatus.CONFLICT, "A user already exists with the same email, forum username, or phone number");
        }
        return doPreliminaryPasswordChecks(form);

    }
    public boolean doPreliminaryPasswordChecks(SignupInitializationForm signupInitializationForm) {
        //Do preliminary checks. If any of them fail, abort the signup immediately
        if (!signupInitializationForm.password().equals(signupInitializationForm.repeatedPassword())){
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        if (signupInitializationForm.password().length() < 8)
        {
            throw new UserAuthException(HttpStatus.BAD_REQUEST,
                    "Password is too short, please ensure it has a length of at least 8 characters");
        }

        if (passwordService.passwordFoundInDataBreach(signupInitializationForm.password()))
        {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "The password you entered was found in a data breach." +
                    "We strongly recommend that you choose a different one.");
        }

        PasswordFeedbackDTO feedbackResponse = passwordService.evaluateUserPassword(signupInitializationForm);
        int score = feedbackResponse.score();

        if (score <= 2)
        {

            StringBuilder builder = new StringBuilder();
            for (String suggestion : feedbackResponse.suggestions())
            {
                builder.append(suggestion);
                builder.append("\n");
            }
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "This password is not recommended due to " +
                    "the following reason:\n " + feedbackResponse.warning() + "\n" +
                    "We recommend the following:\n" + builder);
        }

        return true;
    }

    public Bucket returnUserBucket(User user){
        //The lambda is to get around the fact that building
        // with supplying a bucket configuration directly is deprecated, thank god for lambdas
        return proxyManager.builder().build(user.getEmail(), () -> bucketConfiguration);
    }

}
