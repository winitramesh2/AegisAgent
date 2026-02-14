from pathlib import Path

import joblib
from flask import Flask, jsonify, request


ROOT = Path(__file__).resolve().parent
MODEL_PATH = ROOT / "models" / "intent_model.joblib"

app = Flask(__name__)

_model = None
if MODEL_PATH.exists():
    _model = joblib.load(MODEL_PATH)


@app.get("/health")
def health():
    return jsonify({"status": "ok", "model_loaded": _model is not None})


@app.post("/infer")
def infer():
    global _model

    payload = request.get_json(silent=True) or {}
    query = payload.get("query")
    texts = payload.get("texts")
    text = query or (texts[0] if isinstance(texts, list) and texts else None)

    if not text:
        return jsonify({"intent": "Unknown", "confidence": 0.0}), 400

    if _model is None and MODEL_PATH.exists():
        _model = joblib.load(MODEL_PATH)

    if _model is None:
        return jsonify({"intent": "Unknown", "confidence": 0.0, "message": "Model not trained yet"}), 503

    probabilities = _model.predict_proba([text])[0]
    labels = _model.classes_
    best_index = int(probabilities.argmax())

    return jsonify({
        "intent": str(labels[best_index]),
        "confidence": float(probabilities[best_index]),
    })


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)
