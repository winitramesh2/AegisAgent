# Ops Stack

This folder contains local runtime assets for OpenSearch, Dashboards, and Data Prepper.

## Start Stack

```bash
docker compose up -d
```

## Seed Dashboard Data

1. Apply index template from `ops/opensearch/index-template.json`
2. Run sample inserts and searches from `ops/opensearch/seed-queries.http`

You can execute the HTTP file from tools like VS Code REST Client, Insomnia, or Postman equivalents.
