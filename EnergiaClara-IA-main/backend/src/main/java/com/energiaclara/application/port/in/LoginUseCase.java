package com.energiaclara.application.port.in;

import com.energiaclara.application.dto.LoginCommand;
import com.energiaclara.application.dto.LoginResult;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}
