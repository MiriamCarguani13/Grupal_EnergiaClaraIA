package com.energiaclara.application.port.in;

import com.energiaclara.application.usecase.EvaluateChallengeCommand;
import com.energiaclara.core.domain.challenge.ChallengeStatus;

public interface EvaluateChallengeUseCase {
    ChallengeStatus evaluate(EvaluateChallengeCommand command);
}
