# AI Document Converter

Upload a PDF / DOCX / CSV / TXT, extract its text with Spring Batch, chunk it,
then ask questions about it with an LLM.

- `AiDocumentConverterApp/` — Spring Boot 3.5 + Spring Batch backend (Java 17, PostgreSQL)
- `ai-document-converter-frontend/` — React + TypeScript + Vite UI

---

## Quick start (3 terminals)

### 1. Start PostgreSQL

```bash
docker compose up -d
```

This starts Postgres 16 on `localhost:5432` with database `aidocconverter`,
user/password `postgres`/`postgres` — matching the committed defaults.

*(No Docker? Install Postgres yourself, run `CREATE DATABASE aidocconverter;`,
then set `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`.)*

### 2. Start the backend

```bash
cd AiDocumentConverterApp
mvn spring-boot:run
```

This boots on <http://localhost:8080> with **no configuration required** —
`src/main/resources/application.yml` ships with defaults and reads every
setting from an environment variable if you want to override it.

For Q&A you need an LLM API key. Either export it before starting:

```bash
export OPENAI_API_KEY=gsk_...          # Groq key (default base URL)
export OPENAI_MODEL=llama-3.3-70b-versatile
mvn spring-boot:run
```

or use OpenAI instead:

```bash
export OPENAI_BASE_URL=https://api.openai.com/v1/chat/completions
export OPENAI_MODEL=gpt-4o-mini
export OPENAI_API_KEY=sk-...
```

Without a key the app still starts and converts/chunks documents; only the
Q&A endpoint returns `502 OPENAI_API_KEY is not configured on the server`.

### 3. Start the frontend

```bash
cd ai-document-converter-frontend
npm install
npm run dev
```

Open <http://localhost:5173>, drop in a file, watch the status tracker, then
ask questions once the job is `COMPLETED`.

---

## Configuration

Everything in `application.yml` can be overridden with env vars:

| Env var | Default | Meaning |
|---|---|---|
| `PORT` | `8080` | HTTP port |
| `DB_URL` | `jdbc:postgresql://localhost:5432/aidocconverter` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `postgres` | DB credentials |
| `UPLOAD_DIR` | `./uploads` | Where uploaded files are stored |
| `CHUNK_SIZE` | `1000` | Characters per chunk |
| `MAX_CONTEXT_CHUNKS` | `8` | Chunks stuffed into the LLM prompt |
| `OPENAI_API_KEY` | *(empty)* | LLM API key |
| `OPENAI_MODEL` | `llama-3.3-70b-versatile` | LLM model name |
| `OPENAI_BASE_URL` | Groq chat-completions URL | Any OpenAI-compatible endpoint |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Browser origins allowed to call `/api/**` |

Frontend: create `ai-document-converter-frontend/.env` with
`VITE_API_BASE_URL=http://localhost:8080` only if the API is not on the default port.

---

## API

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/convert` | multipart `file` upload → `202` + job |
| `GET` | `/api/convert/{jobId}/status` | job status |
| `POST` | `/api/documents/{jobId}/query` | `{"question": "..."}` → grounded answer |

```bash
curl -F "file=@resume.pdf" http://localhost:8080/api/convert
curl http://localhost:8080/api/convert/<jobId>/status
curl -X POST http://localhost:8080/api/documents/<jobId>/query \
     -H "Content-Type: application/json" \
     -d '{"question":"What are the key skills mentioned?"}'
```

---

## Tests

```bash
cd AiDocumentConverterApp && mvn test
```

Covers chunking logic, TXT/CSV extraction, and the web layer (MockMvc with a
mocked service). The Batch job wiring and the LLM client are not covered yet.

---

## Honest limitations

- Q&A is **not RAG**: it stuffs the first N chunks in document order into the
  prompt, not the most *relevant* ones. Real retrieval would need embeddings +
  a vector store.
- No auth, no rate limiting, no virus scanning, no load testing.
- Chunking splits on character count, so it can cut mid-word.
- Only one file per job; PDFs are text-extracted (no OCR for scans).
