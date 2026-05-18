package com.energiaclara.application.port.in;

import com.energiaclara.application.dto.RegisterUserCommand;
import com.energiaclara.domain.model.vo.UserId;

public interface RegisterUserUseCase {
    UserId register(RegisterUserCommand command);
}
