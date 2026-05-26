package com.smartwallet.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI smartWalletOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("SmartWallet API")
                                .version("1.0.0")
                                .description(
                                        "REST API SmartWallet — карты пользователя, транзакции, кэшбэк, подсказки ассистента."
                                )
                );
    }
}
