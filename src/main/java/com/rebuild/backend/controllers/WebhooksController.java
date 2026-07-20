package com.rebuild.backend.controllers;

import com.rebuild.backend.model.dtos.auth_dtos.ClerkInformation;
import com.rebuild.backend.model.dtos.auth_dtos.ClerkUserId;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.utils.exceptions.ApiException;
import com.svix.Webhook;
import com.svix.exceptions.EmptyWebhookSecretException;
import com.svix.exceptions.WebhookVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@Transactional
@RequestMapping("/webhooks")
public class WebhooksController {

    private final UserRepository userRepository;


    @Autowired
    public WebhooksController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private Map<String, List<String>> createHeaders(HttpServletRequest request) {
        HashMap<String, List<String>> headerMap = new HashMap<>();
        headerMap.put("svix-id", Collections.singletonList(request.getHeader("svix-id")));
        headerMap.put("svix-timestamp", Collections.singletonList(request.getHeader("svix-timestamp")));
        headerMap.put("svix-signature", Collections.singletonList(request.getHeader("svix-signature")));

        return headerMap;
    }

    private String verifyWebhook(String secret,
                               HttpServletRequest request)
            throws EmptyWebhookSecretException {
        Webhook webhook = new Webhook(secret);

        Map<String, List<String>> headers = createHeaders(request);

        try {
            String payload = StreamUtils.copyToString(
                    request.getInputStream(),
                    StandardCharsets.UTF_8
            );
            webhook.verify(payload, headers);
            return payload;
        }
        catch (WebhookVerificationException verificationException)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Webhook content has been tampered with");
        }

        catch (IOException ioe)
        {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing the request");
        }
    }

    @PostMapping("/create")
    public void createUser(HttpServletRequest request) throws EmptyWebhookSecretException {

        String secret = System.getenv("SIGNUP_WEBHOOK_SECRET");

        String payload = verifyWebhook(secret, request);
        ObjectMapper mapper = new ObjectMapper();

        ClerkInformation clerkInformation = mapper.readValue(payload, ClerkInformation.class);

        User newUser = new User(clerkInformation);

        userRepository.save(newUser);

    }


    @PostMapping("/delete")
    public void deleteUser(HttpServletRequest request) throws EmptyWebhookSecretException {
        String secret = System.getenv("DELETE_WEBHOOK_SECRET");

        String payload = verifyWebhook(secret, request);

        ObjectMapper mapper = new ObjectMapper();

        ClerkUserId clerkUserId = mapper.readValue(payload, ClerkUserId.class);

        userRepository.deleteByClerkId(clerkUserId.id());

    }

    @PostMapping("/update")
    public void updateUser(HttpServletRequest request) throws EmptyWebhookSecretException {
        String secret = System.getenv("UPDATE_WEBHOOK_SECRET");

        String payload = verifyWebhook(secret, request);

        ObjectMapper mapper = new ObjectMapper();

        ClerkInformation clerkInformation = mapper.readValue(payload, ClerkInformation.class);

        User user = userRepository.findByClerkId(clerkInformation.id()).orElseThrow();

        user.update(clerkInformation);

        userRepository.save(user);
    }
}
