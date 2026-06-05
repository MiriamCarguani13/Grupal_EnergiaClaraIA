package com.energiaclara.iam.application.port.in;

import com.energiaclara.iam.application.dto.RegisterUserCommand;

public interface RegisterUserUseCase {
    String register(RegisterUserCommand command);
}
