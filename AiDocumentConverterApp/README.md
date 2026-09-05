# AiDocumentConverterApp

Spring Boot backend for multi-format file conversion + AI-powered document Q&A.

## What it actually does

1. **Upload** (`POST /api/convert`): accepts PDF, DOCX, CSV, or TXT. Saves the
   file, creates a `ConversionJob` row, and launches an async Spring Batch job.
2. **Step 1 — extraction** (Tasklet): extracts plain text from the file
   (PDFBox for PDF, Apache POI for DOCX, plain read for CSV/TXT).
3. **Step 2 — chunking** (chunk-oriented step, commit-interval 20): splits the
   extracted text into fixed-size chunks and bulk-persists them as
   `DocumentChunk` rows. **This is the part that's genuinely chunk-oriented** —
   format conversion itself is a single-tasklet step, not chunked, because
   there's only one file per job.
4. **Status** (`GET /api/convert/{jobId}/status`): poll job progress
   (`PENDING → EXTRACTING → EXTRACTED → CHUNKING → COMPLETED/FAILED`).
5. **Q&A** (`POST /api/documents/{jobId}/query`): once `COMPLETED`, takes a
   natural-language question, stuffs the first N stored chunks into a prompt,
   and calls the OpenAI Chat Completions API for an answer grounded in the
   document.

## What it does NOT do (be honest about this in interviews)

- No embedding-based vector search / real RAG pipeline — Q&A context is the
  first `app.max-context-chunks` chunks in document order, not the most
  relevant ones. A natural v2 improvement is embeddings + a vector store
  (pgvector, for example) for actual similarity search.
- No auth/rate-limiting on the API yet.
- No virus/malware scanning on uploaded files.
- Not load-tested — don't call it "production-grade" without backing that up.

## Running locally

1. Create a Postgres database:
   ```sql
   CREATE DATABASE aidocconverter;
   ```
2. Set environment variables (or edit `application.yml` directly):
   ```
   DB_USERNAME=postgres
   DB_PASSWORD=yourpassword
   OPENAI_API_KEY=sk-...
   UPLOAD_DIR=./uploads
   ```
3. Build and run:
   ```
   mvn spring-boot:run
   ```
4. Try it:
   ```
   curl -F "file=@resume.pdf" http://localhost:8080/api/convert
   curl http://localhost:8080/api/convert/{jobId}/status
   curl -X POST http://localhost:8080/api/documents/{jobId}/query \
        -H "Content-Type: application/json" \
        -d '{"question":"What are the key skills mentioned?"}'
   ```

## Running tests

```
mvn test
```

Tests included: `ChunkUtilsTest` (pure chunking logic), `FileConversionServiceTest`
(TXT/CSV extraction), `ConversionControllerTest` (MockMvc web-layer test with
`JobService` mocked). Batch job wiring (`BatchConfig`) and the OpenAI client
are not covered by automated tests yet — that's the honest next step if you
want to claim full test coverage on a resume.
