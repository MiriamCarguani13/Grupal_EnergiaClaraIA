package com.energiaclara.core.domain.energy;

public final class KwhValue {
    private final double value;

    public KwhValue(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new InvalidKwhValueException("El consumo debe ser un número válido: " + value);
        }
        if (value < 0) {
            throw new InvalidKwhValueException("El consumo no puede ser negativo: " + value);
        }
        if (value > 999_999) {
            throw new InvalidKwhValueException("Valor fuera de rango físico: " + value);
        }
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public boolean isGreaterThan(KwhValue other) {
        return this.value > other.value;
    }

    public KwhValue subtract(KwhValue other) {
        return new KwhValue(Math.max(0, this.value - other.value));
    }

    public KwhValue add(KwhValue other) {
        return new KwhValue(this.value + other.value);
    }
}
