# AI Document Converter — Frontend

React + TypeScript + Vite frontend for the AiDocumentConverterApp backend.

## Flow
1. Upload a PDF/DOCX/CSV/TXT file (drag & drop or click to browse).
2. Polls `GET /api/convert/{jobId}/status` every 2s and shows progress
   (Queued → Extracting → Extracted → Chunking → Ready).
3. Once `COMPLETED`, a chat-style Q&A box appears, calling
   `POST /api/documents/{jobId}/query`.

## Setup

```
npm install
npm run dev
```

Opens on http://localhost:5173. Make sure the Spring Boot backend is running
on http://localhost:8080 (default assumed — override with a `.env` file:
`VITE_API_BASE_URL=http://localhost:8080`).

## CORS

The backend already ships `config/CorsConfig.java`, which allows
`http://localhost:5173` and `http://127.0.0.1:5173`. If you serve the
frontend from another origin, add it via the `CORS_ALLOWED_ORIGINS`
environment variable on the backend (comma-separated).
