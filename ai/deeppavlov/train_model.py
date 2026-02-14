import json
from pathlib import Path

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report
import joblib


ROOT = Path(__file__).resolve().parent
DATA_PATH = ROOT / "train_data.json"
MODEL_DIR = ROOT / "models"
MODEL_PATH = MODEL_DIR / "intent_model.joblib"


def load_data(path: Path):
    payload = json.loads(path.read_text())
    x = []
    y = []
    for intent in payload["intents"]:
        label = intent["name"]
        for sample in intent["examples"]:
            x.append(sample)
            y.append(label)
    return x, y


def main():
    x, y = load_data(DATA_PATH)
    class_count = len(set(y))
    sample_count = len(y)

    test_fraction = max(class_count / sample_count, 0.34)
    if test_fraction >= 0.5:
        test_fraction = 0.4

    x_train, x_test, y_train, y_test = train_test_split(
        x, y, test_size=test_fraction, random_state=42, stratify=y
    )

    model = Pipeline(
        steps=[
            ("tfidf", TfidfVectorizer(ngram_range=(1, 2))),
            ("clf", LogisticRegression(max_iter=2000)),
        ]
    )

    model.fit(x_train, y_train)
    predictions = model.predict(x_test)

    print(classification_report(y_test, predictions))

    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    joblib.dump(model, MODEL_PATH)
    print(f"Saved model to {MODEL_PATH}")


if __name__ == "__main__":
    main()
