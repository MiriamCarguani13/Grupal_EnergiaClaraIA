package com.energiaclara.core.domain.challenge;

import com.energiaclara.core.domain.energy.KwhValue;

public class ChallengePolicy {
    public static boolean isAchieved(KwhValue baselineConsumption, KwhValue actualConsumption, KwhTarget target) {
        KwhValue saved = baselineConsumption.subtract(actualConsumption);
        return saved.isGreaterThan(target.getValue()) || saved.getValue() == target.getValue().getValue();
    }
}
