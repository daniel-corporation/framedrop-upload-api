package com.framedrop.upload_api.adapters.config;

import com.framedrop.upload_api.core.application.usecases.TokenUseCase;
import com.framedrop.upload_api.core.domain.ports.in.TokenInputPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenConfig {

    @Bean
    public TokenInputPort createTokenInputPort(){
        return new TokenUseCase();
    }

}
