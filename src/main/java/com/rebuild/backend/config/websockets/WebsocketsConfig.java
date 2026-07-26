package com.rebuild.backend.config.websockets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
public class WebsocketsConfig implements WebSocketMessageBrokerConfigurer {

    private final WebsocketHandshakeHandler handshakeHandler;

    private final WebsocketHandshakeInterceptor handshakeInterceptor;

    @Autowired
    public WebsocketsConfig(WebsocketHandshakeHandler handshakeHandler,
                            WebsocketHandshakeInterceptor handshakeInterceptor) {
        this.handshakeHandler = handshakeHandler;
        this.handshakeInterceptor = handshakeInterceptor;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        WebSocketMessageBrokerConfigurer.super.configureClientInboundChannel(registration);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.setPreserveReceiveOrder(true);
        // This configures the endpoint that
        // the client will connect to initially once the login is complete
        registry.addEndpoint("/rerebuild").setAllowedOrigins("https://rerebuild.ca", "localhost").
                setHandshakeHandler(handshakeHandler).
                addInterceptors(handshakeInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        //This is used to capture messages sent from the client to the server. It is unnecessary for now,
        //but it is here in case we need it later
        registry.setApplicationDestinationPrefixes("/app");

        /*
        These are the topics that the client will subscribe to.
        The new_chats endpoint will be used to notify users of new chats that they have been added to.
        The new_messages endpoint will be used to
        notify users of a new message to a chat that they are already a part of.
         */

        //The server will send a heartbeat every 5 seconds, and the client every 10 seconds
        registry.enableSimpleBroker("/new_messages", "/new_chat_invitations",
                        "/new_friend_invitations", "/new_chat_notifications", "/typing",
                        "/kicked_notifications", "/friendship_notifications").
                setHeartbeatValue(new long[] {5000, 10000});

        //This makes it possible to send a message to a specific user.
        registry.setUserDestinationPrefix("/user");
    }
}
