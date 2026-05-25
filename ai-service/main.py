from datetime import datetime
from typing import Optional
from pydantic import BaseModel
from fastapi import FastAPI
import math

app = FastAPI(title="EnergiaClara AI Service", version="1.0.0")


class EnergyPredictionRequest(BaseModel):
    tenantId: str
    medidorId: str
    facilityId: Optional[str] = None
    meterId: Optional[str] = None
    measuredAt: datetime
    kwh: float
    voltage: Optional[float] = None
    powerFactor: Optional[float] = None
    baselineKwh: float
    tolerancePercent: float


class EnergyPredictionResponse(BaseModel):
    modelVersion: str
    predictedKwh: float
    confidence: float
    anomalyDetected: bool
    anomalyScore: float
    explanation: str
    recommendation: str


@app.get("/health")
def health():
    return {"status": "UP", "service": "energiaclara-ai-service"}


@app.post("/predict-energy-anomaly", response_model=EnergyPredictionResponse)
def predict_energy_anomaly(request: EnergyPredictionRequest):
    predicted_kwh = estimate_expected_consumption(request)
    deviation_percent = calculate_deviation_percent(request.kwh, predicted_kwh)
    anomaly_score = calculate_anomaly_score(deviation_percent, request.tolerancePercent)

    anomaly_detected = deviation_percent > request.tolerancePercent
    confidence = min(0.99, max(0.50, 0.55 + anomaly_score * 0.40))

    if anomaly_detected:
        explanation = (
            f"Consumo anómalo detectado. Lectura={request.kwh:.2f} kWh, "
            f"predicción={predicted_kwh:.2f} kWh, desviación={deviation_percent:.2f}%."
        )
        recommendation = "Revisar equipos activos fuera de horario, cargas conectadas y estado del medidor."
    else:
        explanation = (
            f"Consumo dentro del patrón esperado. Lectura={request.kwh:.2f} kWh, "
            f"predicción={predicted_kwh:.2f} kWh, desviación={deviation_percent:.2f}%."
        )
        recommendation = "Mantener monitoreo normal del consumo energético."

    return EnergyPredictionResponse(
        modelVersion="energy-anomaly-v1",
        predictedKwh=round(predicted_kwh, 4),
        confidence=round(confidence, 4),
        anomalyDetected=anomaly_detected,
        anomalyScore=round(anomaly_score, 4),
        explanation=explanation,
        recommendation=recommendation,
    )


def estimate_expected_consumption(request: EnergyPredictionRequest) -> float:
    hour = request.measuredAt.hour
    day_of_week = request.measuredAt.weekday()

    hour_factor = 1.0

    if 0 <= hour <= 5:
        hour_factor = 0.65
    elif 6 <= hour <= 8:
        hour_factor = 0.85
    elif 9 <= hour <= 17:
        hour_factor = 1.05
    elif 18 <= hour <= 22:
        hour_factor = 0.90
    else:
        hour_factor = 0.75

    weekend_factor = 0.80 if day_of_week >= 5 else 1.0

    power_factor_adjustment = 1.0
    if request.powerFactor is not None and request.powerFactor > 0:
        if request.powerFactor < 0.80:
            power_factor_adjustment = 1.08
        elif request.powerFactor > 0.95:
            power_factor_adjustment = 0.98

    voltage_adjustment = 1.0
    if request.voltage is not None and request.voltage > 0:
        if request.voltage < 200 or request.voltage > 240:
            voltage_adjustment = 1.05

    predicted = request.baselineKwh * hour_factor * weekend_factor * power_factor_adjustment * voltage_adjustment
    return max(predicted, 1.0)


def calculate_deviation_percent(kwh: float, predicted_kwh: float) -> float:
    if predicted_kwh <= 0:
        return 0.0

    return ((kwh - predicted_kwh) / predicted_kwh) * 100.0


def calculate_anomaly_score(deviation_percent: float, tolerance_percent: float) -> float:
    if deviation_percent <= tolerance_percent:
        return 0.0

    excess = deviation_percent - tolerance_percent
    score = 1.0 - math.exp(-excess / 75.0)
    return min(1.0, max(0.0, score))
