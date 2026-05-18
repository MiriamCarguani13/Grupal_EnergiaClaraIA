package com.energiaclara.iam.application.usecase;

import com.energiaclara.core.domain.shared.UserId;
import com.energiaclara.iam.application.dto.RegisterUserCommand;

public interface RegisterUserUseCase {
    UserId register(RegisterUserCommand command);
}
