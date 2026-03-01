package com.framedrop.upload_api.core.application.usecases;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.framedrop.upload_api.adapters.in.controller.dto.UserDTO;
import com.framedrop.upload_api.core.domain.ports.in.TokenInputPort;
import org.springframework.util.StringUtils;

import java.util.Map;

public class TokenUseCase implements TokenInputPort {
    @Override
    public UserDTO getUserFromToken(String token) {
        if(!StringUtils.hasText(token)){
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        if(token.contains("Bearer ")){
            token = token.substring(7);
        }
        Map<String, Claim> claims = JWT.decode(token).getClaims();
        return new UserDTO(
                claims.get("client_id").asString(),
                claims.get("sub").asString(),
                null);
    }
}
