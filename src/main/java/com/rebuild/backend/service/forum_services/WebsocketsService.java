package com.rebuild.backend.service.forum_services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Service
public class WebsocketsService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public WebsocketsService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }
}
