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

## IMPORTANT: backend CORS

By default, Spring Boot blocks cross-origin requests, so the browser will
reject calls from `localhost:5173` to `localhost:8080` unless you add a CORS
config to the backend. See the `CorsConfig.java` snippet provided alongside
this project — add it to
`AiDocumentConverterApp/src/main/java/com/aiconverter/config/` and restart
the backend before testing the frontend.
