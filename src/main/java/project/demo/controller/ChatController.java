package project.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import project.demo.dto.ChatMessageDTO;
import project.demo.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessageDTO messageDTO, Authentication authentication) {
        // Set the session ID from Spring Security's session
        messageDTO.setSessionId(authentication.getName());
        messageDTO.setSender("customer");
        messageDTO.setTimestamp(System.currentTimeMillis());

        return ResponseEntity.ok(chatService.sendMessageToN8n(messageDTO));
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleN8nWebhook(@RequestBody ChatMessageDTO messageDTO) {
        // Simply return OK as we don't need to process the webhook response
        return ResponseEntity.ok().build();
    }
}