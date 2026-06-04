package com.smartwallet.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwallet.config.OllamaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaClient {

    private final RestTemplate ollamaRestTemplate;
    private final OllamaProperties ollamaProperties;
    private final ObjectMapper objectMapper;

    public String chat(String systemMessage, String userMessage) {
        if (!ollamaProperties.enabled()) {
            return null;
        }

        String url = normalizeBaseUrl(ollamaProperties.baseUrl()) + "/api/chat";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", ollamaProperties.model());
        body.put("stream", false);
        body.put(
                "messages",
                List.of(
                        Map.of("role", "system", "content", systemMessage),
                        Map.of("role", "user", "content", userMessage)
                )
        );
        body.put("options", Map.of("num_predict", 256, "temperature", 0.4));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = ollamaRestTemplate.postForEntity(url, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.warn("Ollama HTTP {}", response.getStatusCode().value());
            return null;
        }
        return parseAssistantContent(response.getBody());
    }

    static String normalizeBaseUrl(String baseUrl) {
        String u = baseUrl.strip();
        if (u.endsWith("/")) {
            return u.substring(0, u.length() - 1);
        }
        return u;
    }

    String parseAssistantContent(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode message = root.path("message");
            if (message.hasNonNull("content")) {
                String content = message.get("content").asText().strip();
                if (!content.isEmpty()) {
                    return content;
                }
            }
            if (root.hasNonNull("response")) {
                return root.get("response").asText().strip();
            }
        } catch (Exception e) {
            log.warn("Failed to parse Ollama response: {}", e.getMessage());
        }
        return null;
    }
}
