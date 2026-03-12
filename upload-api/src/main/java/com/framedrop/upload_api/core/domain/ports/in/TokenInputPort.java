package com.framedrop.upload_api.core.domain.ports.in;

import com.framedrop.upload_api.adapters.in.controller.dto.UserDTO;

public interface TokenInputPort {

    UserDTO getUserFromToken(String token);

}
