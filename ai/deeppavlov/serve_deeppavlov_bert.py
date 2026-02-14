import os

from flask import Flask, jsonify, request


app = Flask(__name__)

MODEL = None
MODEL_ERROR = None


def load_model():
    global MODEL
    global MODEL_ERROR
    try:
        from deeppavlov import build_model

        config = os.getenv("DEEPPAVLOV_CONFIG", "configs/classifiers/intents_dstc2_bert.json")
        MODEL = build_model(config, download=True)
    except Exception as ex:  # noqa: BLE001
        MODEL_ERROR = str(ex)
        MODEL = None


load_model()


@app.get("/health")
def health():
    return jsonify({"status": "ok", "model_loaded": MODEL is not None, "error": MODEL_ERROR})


@app.post("/infer")
def infer():
    payload = request.get_json(silent=True) or {}
    query = payload.get("query")
    texts = payload.get("texts")
    text = query or (texts[0] if isinstance(texts, list) and texts else None)

    if not text:
        return jsonify({"intent": "Unknown", "confidence": 0.0}), 400

    if MODEL is None:
        return jsonify({"intent": "Unknown", "confidence": 0.0, "error": MODEL_ERROR or "Model not available"}), 503

    try:
        output = MODEL([text])
        intent = "Unknown"
        confidence = 0.0

        if isinstance(output, list) and output:
            first = output[0]
            if isinstance(first, list) and first:
                intent = str(first[0])
            elif isinstance(first, str):
                intent = first

            if len(output) > 1 and isinstance(output[1], list) and output[1]:
                confidence = float(output[1][0]) if output[1][0] is not None else 0.0

        return jsonify({"intent": intent, "confidence": confidence})
    except Exception as ex:  # noqa: BLE001
        return jsonify({"intent": "Unknown", "confidence": 0.0, "error": str(ex)}), 500


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)
