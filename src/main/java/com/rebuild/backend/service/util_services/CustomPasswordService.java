package com.rebuild.backend.service.util_services;

import com.nulabinc.zxcvbn.Feedback;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.UserAuthException;
import com.rebuild.backend.model.forms.auth_forms.SignupInitializationForm;
import com.rebuild.backend.model.dtos.PasswordFeedbackDTO;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service(value = "password_service")
@Transactional(readOnly = true)
public class CustomPasswordService implements UserDetailsPasswordService {

    private final UserRepository userRepository;

    @Autowired
    public CustomPasswordService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        User updatingUser = userRepository.findByEmailOrPhoneNumber(user.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        updatingUser.setPassword(newPassword);
        return userRepository.save(updatingUser);
    }


    private String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b)); // UPPERCASE hex
        }
        return sb.toString();
    }

    private String encryptWithSHA1(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return bytesToString(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException e) {
            return e.getMessage();
        }

    }

    private String determinePasswordToEvaluate(String password)
    {
        if (password.length() >= 100) {
            return password.substring(0, 100);
        }
        return password;
    }


    public boolean passwordFoundInDataBreach(String password) {
        String sha1Hash = encryptWithSHA1(password);

        String first5chars = sha1Hash.substring(0, 5);

        String otherChars = sha1Hash.substring(5);

        HttpRequest request = HttpRequest.newBuilder().
                uri(URI.create("https://api.pwnedpasswords.com/range/" + first5chars)).
                header("User-Agent", "rerebuild.ca").GET().build();

        HttpClient client = HttpClient.newHttpClient();
        Thread.currentThread().interrupt();

        try {
            HttpResponse<Stream<String>> response = client.send(request, HttpResponse.BodyHandlers.ofLines());

            return response.body().
                    anyMatch(line -> line.split(":")[0].equals(otherChars));
        }
        catch (IOException | InterruptedException e) {
            throw new UserAuthException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while checking the database for a password breach");
        }
    }


    public PasswordFeedbackDTO evaluateUserPassword(SignupInitializationForm signupInitializationForm)
    {
        List<String> penalizedWords = new ArrayList<>();
        penalizedWords.add(signupInitializationForm.email());
        penalizedWords.add(signupInitializationForm.forumUsername());
        if (signupInitializationForm.forumUsername() != null)
        {
            penalizedWords.add(signupInitializationForm.forumUsername());
        }
        if (signupInitializationForm.phoneNumber() != null)
        {
            penalizedWords.add(signupInitializationForm.phoneNumber());
        }

        Zxcvbn zxcvbn = new Zxcvbn();

        Strength strength = zxcvbn.measure(determinePasswordToEvaluate(signupInitializationForm.password()), penalizedWords);

        int score = strength.getScore();

        Feedback passwordFeedback = strength.getFeedback();

        return new PasswordFeedbackDTO(score,
                passwordFeedback.getSuggestions(), passwordFeedback.getWarning());


    }
}
