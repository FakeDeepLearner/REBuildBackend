package com.rebuild.backend.controllers;

import com.rebuild.backend.model.dtos.ClerkInformation;
import com.rebuild.backend.model.dtos.ClerkUserId;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.utils.exceptions.ApiException;
import com.svix.Webhook;
import com.svix.exceptions.EmptyWebhookSecretException;
import com.svix.exceptions.WebhookVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.ContentCachingRequestWrapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiPredicate;


@RestController("/webhooks")
public class WebhooksController {

    private final UserRepository userRepository;

    @Autowired
    public WebhooksController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private HttpHeaders createHeaders(HttpServletRequest request) {
        HashMap<String, List<String>> headerMap = new HashMap<>();
        headerMap.put("svix-id", Collections.singletonList(request.getHeader("svix-id")));
        headerMap.put("svix-timestamp", Collections.singletonList(request.getHeader("svix-timestamp")));
        headerMap.put("svix-signature", Collections.singletonList(request.getHeader("svix-signature")));

        return HttpHeaders.of(headerMap, (_, _) -> true);
    }

    private void verifyWebhook(String secret, HttpHeaders headers, String payload)
            throws EmptyWebhookSecretException {
        Webhook webhook = new Webhook(secret);

        try {
            webhook.verify(payload, headers.map());
        }
        catch (WebhookVerificationException verificationException)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Webhook content has been tampered with");
        }
    }

    @PostMapping("/update")
    public void createUser(HttpServletRequest request) throws EmptyWebhookSecretException, IOException {

        String secret = System.getenv("SIGNUP_WEBHOOK_SECRET");
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 0);

        HttpHeaders headers = createHeaders(request);

        String payload = requestWrapper.getContentAsString();

        verifyWebhook(secret, headers, payload);

        ObjectMapper mapper = new ObjectMapper();

        ClerkInformation clerkInformation = mapper.readValue(payload, ClerkInformation.class);

        User newUser = new User(clerkInformation);

        userRepository.save(newUser);

    }


    @PostMapping("/delete")
    public void deleteUser(HttpServletRequest request) throws EmptyWebhookSecretException {
        String secret = System.getenv("DELETE_WEBHOOK_SECRET");
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 0);

        HttpHeaders headers = createHeaders(request);

        String payload = requestWrapper.getContentAsString();

        verifyWebhook(secret, headers, payload);

        ClerkUserId clerkUserId = new ObjectMapper().readValue(payload, ClerkUserId.class);

        userRepository.deleteByClerkId(clerkUserId.id());

    }

    @PostMapping("/update")
    public void updateUser(HttpServletRequest request) throws EmptyWebhookSecretException {
        String secret = System.getenv("UPDATE_WEBHOOK_SECRET");
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 0);

        HttpHeaders headers = createHeaders(request);

        String payload = requestWrapper.getContentAsString();

        verifyWebhook(secret, headers, payload);

        ObjectMapper mapper = new ObjectMapper();

        ClerkInformation clerkInformation = mapper.readValue(payload, ClerkInformation.class);

        User user = userRepository.findByClerkId(clerkInformation.id()).orElseThrow();

        user.update(clerkInformation);

        userRepository.save(user);
    }
}
