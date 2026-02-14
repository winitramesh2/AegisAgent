# AI Training Assets (Phase 1)

This folder contains starter assets for intent training and model artifact generation.

## Files

- `train_data.json` - IAM-focused intent examples
- `train_model.py` - baseline classifier training pipeline
- `docker-compose.yml` - containerized local training run

## Run Training

```bash
docker compose run --rm deeppavlov-trainer
```

Model artifacts are saved under `models/`.

## Run Inference API

```bash
docker compose up deeppavlov-api
```

Inference endpoint:

- `POST /infer` with body `{ "query": "otp not generating" }`
- Returns `{ "intent": "GenerateOTP", "confidence": 0.91 }`

## Run DeepPavlov BERT API (Containerized)

```bash
docker compose up deeppavlov-bert-api
```

- Service runs on `http://localhost:8001/infer`
- Configure backend with:
  - `DEEPPAVLOV_ENABLED=true`
  - `DEEPPAVLOV_URL=http://localhost:8001/infer`
- Health endpoint: `http://localhost:8001/health`

## Next Iteration

Keep IAM intent taxonomy aligned with `train_data.json` when migrating BERT config to production datasets.
