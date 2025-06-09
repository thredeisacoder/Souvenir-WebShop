package project.demo.service;

import project.demo.dto.ChatMessageDTO;
import java.util.concurrent.CompletableFuture;

public interface ChatService {
    /**
     * Send a message to n8n and get the response
     * 
     * @param messageDTO the message to send
     * @return CompletableFuture containing the response message
     */
    CompletableFuture<ChatMessageDTO> sendMessageToN8n(ChatMessageDTO messageDTO);
}