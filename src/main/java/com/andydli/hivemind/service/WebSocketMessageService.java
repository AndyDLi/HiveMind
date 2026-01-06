package com.andydli.hivemind.service;

import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.andydli.hivemind.dto.PortalEventDTO;

@Service
public class WebSocketMessageService {
    private final SimpMessagingTemplate messagingTemplate; // broadcast messages to connected clients

    public WebSocketMessageService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastMessage(PortalEventDTO portalEventDTO) {
        messagingTemplate.convertAndSend("/topic/portals", portalEventDTO); // broadcast portal event to subscribers
    }
}