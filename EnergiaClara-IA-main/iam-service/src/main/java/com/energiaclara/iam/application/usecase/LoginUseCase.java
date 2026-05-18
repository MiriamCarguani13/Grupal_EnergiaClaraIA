package com.energiaclara.iam.application.usecase;

import com.energiaclara.iam.application.dto.LoginCommand;
import com.energiaclara.iam.application.dto.LoginResult;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}
