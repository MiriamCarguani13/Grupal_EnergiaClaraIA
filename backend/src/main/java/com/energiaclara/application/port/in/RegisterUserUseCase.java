package com.energiaclara.application.port.in;

import com.energiaclara.application.dto.RegisterUserCommand;

public interface RegisterUserUseCase {
    String register(RegisterUserCommand command);
}
