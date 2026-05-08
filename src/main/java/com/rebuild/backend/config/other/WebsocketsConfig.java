package com.rebuild.backend.config.other;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
public class WebsocketsConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // This configures the endpoint that
        // the client will connect to initially once the login is complete
        registry.addEndpoint("chat-system").setAllowedOrigins("rerebuild.ca", "localhost").
                withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        //This is used to capture messages sent from the client to the server. It is unnecessary for now,
        //but it is here in case we need it later
        //registry.setApplicationDestinationPrefixes("/app");

        /*
        These are the topics that the client will subscribe to.
        The new_chats endpoint will be used to notify users of new chats that they have been added to.
        The new_messages endpoint will be used to
        notify users of a new message to a chat that they are already a part of.
         */
        registry.enableSimpleBroker("/new_chats", "/new_messages");

        //This makes it possible to send a message to a specific user.
        registry.setUserDestinationPrefix("/user");
    }
}
