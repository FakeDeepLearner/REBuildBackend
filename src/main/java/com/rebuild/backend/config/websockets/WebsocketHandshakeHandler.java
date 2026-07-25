package com.rebuild.backend.config.websockets;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@Component
public class WebsocketHandshakeHandler extends DefaultHandshakeHandler {

    private final UserRepository userRepository;

    @Autowired
    public WebsocketHandshakeHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected @Nullable Principal determineUser(ServerHttpRequest request,
                                                WebSocketHandler wsHandler,
                                                Map<String, Object> attributes) {
        String userClerkId = (String) attributes.get("clerkId");

        Optional<User> foundUser = userRepository.findByClerkId(userClerkId);

        if (foundUser.isEmpty()) {
            return null;
        }

        User user = foundUser.get();

        //What matters is the name of this principal
        return new PreAuthenticatedAuthenticationToken(
                user.getForumUsername(),
                null,
                null
        );
    }
}
