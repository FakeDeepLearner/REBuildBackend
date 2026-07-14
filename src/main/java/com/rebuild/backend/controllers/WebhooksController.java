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
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.ContentCachingRequestWrapper;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpHeaders;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


@RestController("/webhooks")
@Transactional
public class WebhooksController {

    private final UserRepository userRepository;

    @Autowired
    public WebhooksController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private HttpHeaders createHeaders(HttpServletRequestWrapper request) {
        HashMap<String, List<String>> headerMap = new HashMap<>();
        headerMap.put("svix-id", Collections.singletonList(request.getHeader("svix-id")));
        headerMap.put("svix-timestamp", Collections.singletonList(request.getHeader("svix-timestamp")));
        headerMap.put("svix-signature", Collections.singletonList(request.getHeader("svix-signature")));

        return HttpHeaders.of(headerMap, (_, _) -> true);
    }

    private String verifyWebhook(String secret,
                               HttpServletRequest request)
            throws EmptyWebhookSecretException {
        Webhook webhook = new Webhook(secret);
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 0);

        HttpHeaders headers = createHeaders(requestWrapper);

        String payload = requestWrapper.getContentAsString();

        try {
            webhook.verify(payload, headers.map());
            return payload;
        }
        catch (WebhookVerificationException verificationException)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Webhook content has been tampered with");
        }
    }

    @PostMapping("/update")
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
