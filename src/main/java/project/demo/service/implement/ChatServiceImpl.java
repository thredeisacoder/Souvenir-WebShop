package project.demo.service.implement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.demo.dto.ChatMessageDTO;
import project.demo.service.ChatService;

import java.util.concurrent.CompletableFuture; // Import CompletableFuture for asynchronous processing

@Service
public class ChatServiceImpl implements ChatService {

    @Value("${n8n.webhook.url}")
    private String n8nWebhookUrl;

    private final RestTemplate restTemplate;

    public ChatServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public CompletableFuture<ChatMessageDTO> sendMessageToN8n(ChatMessageDTO messageDTO) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // Create request body according to chatbot API format
                String requestBody = String.format("""
                        {
                            "sessionId": "%s",
                            "message": "%s",
                            "timestamp": %d
                        }""",
                        messageDTO.getSessionId(),
                        messageDTO.getMessage(),
                        messageDTO.getTimestamp());

                HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

                // Send message to n8n and get response
                String response = restTemplate.postForObject(n8nWebhookUrl, request, String.class);

                // Create response DTO
                ChatMessageDTO responseDTO = new ChatMessageDTO();
                responseDTO.setSessionId(messageDTO.getSessionId());
                responseDTO.setMessage(response != null ? response : "No response from agent");
                responseDTO.setSender("agent");
                responseDTO.setTimestamp(System.currentTimeMillis());

                return responseDTO;
            } catch (Exception e) {
                // Log the error and create an error response
                ChatMessageDTO errorResponse = new ChatMessageDTO();
                errorResponse.setSessionId(messageDTO.getSessionId());
                errorResponse.setSender("system");
                errorResponse.setMessage("Sorry, there was an error processing your message. Please try again later.");
                errorResponse.setTimestamp(System.currentTimeMillis());
                return errorResponse;
            }
        });
    }
}