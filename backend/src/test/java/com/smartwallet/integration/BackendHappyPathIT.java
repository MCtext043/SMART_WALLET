package com.smartwallet.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwallet.gateway.GigachatClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BackendHappyPathIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("smartwallet")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    GigachatClient gigachatClient;

    @Test
    void fullHappyPathThroughApi() throws Exception {
        when(gigachatClient.postMessage(any()))
                .thenAnswer(inv -> ResponseEntity.ok("{\"content\":\"Ответ ассистента: тест\\nГотово\"}"));

        StringBuilder digits = new StringBuilder("+7");
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < 10; i++) {
            digits.append(r.nextInt(10));
        }
        String phone = digits.toString();
        String email = UUID.randomUUID().toString().substring(0, 10) + "@example.com";
        String password = "123456";
        String name = "HappyPath User";

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "phone": "%s",
                                          "email": "%s",
                                          "name": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(phone, email, name, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        MvcResult loginResult =
                mockMvc.perform(
                                post("/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"phone\": \"%s\", \"password\": \"%s\"}".formatted(
                                                        phone, password)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.access_token").exists())
                        .andExpect(jsonPath("$.token_type").value("bearer"))
                        .andReturn();

        JsonNode loginJson =
                objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.get("access_token").asText();

        mockMvc.perform(get("/auth/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value(phone));

        mockMvc.perform(
                        post("/cards/")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "bank_name": "Test Bank",
                                          "card_name": "Test Card",
                                          "last4": "1234",
                                          "cashback_rules": { "еда": 5, "транспорт": 3, "прочее": 1 },
                                          "limit_monthly": 10000.0
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        MvcResult cardList =
                mockMvc.perform(get("/cards/").header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(1)))
                        .andReturn();

        int cardId =
                objectMapper
                        .readTree(cardList.getResponse().getContentAsString())
                        .get(0)
                        .get("id")
                        .asInt();

        mockMvc.perform(get("/cards/" + cardId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(
                        post("/transactions/")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"card_id\": %d, \"amount\": 2000.0, \"category\": \"еда\"}"
                                                .formatted(cardId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cashback_earned").exists())
                .andExpect(jsonPath("$.cashback_earned").value(closeTo(100.0, 0.001)));

        mockMvc.perform(get("/transactions/").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/assistant/recommendations").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(
                        get("/cashback/best-card")
                                .header("Authorization", "Bearer " + token)
                                .queryParam("category", "еда"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card_id").value(cardId))
                .andExpect(jsonPath("$.category").value("еда"));

        mockMvc.perform(
                        post("/assistant/chat")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"message\": \"Привет, тест!\"}"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.reply",
                                allOf(containsString("Ответ ассистента"), containsString("\n"))));
    }
}
