package com.smartwallet.gateway;

import com.smartwallet.config.GigachatProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GigachatClient {

    private final RestTemplate gigachatRestTemplate;
    private final GigachatProperties gigachatProperties;

    public ResponseEntity<String> postMessage(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return gigachatRestTemplate.postForEntity(gigachatProperties.url(), entity, String.class);
    }
}
