import { useEffect, useRef, useState } from 'react'
import FileUpload from './components/FileUpload'
import StatusTracker from './components/StatusTracker'
import QaPanel from './components/QaPanel'
import { uploadFile, getJobStatus } from './api'
import type { ConversionJobResponse } from './types'

const POLL_INTERVAL_MS = 2000

export default function App() {
  const [job, setJob] = useState<ConversionJobResponse | null>(null)
  const [uploadError, setUploadError] = useState<string | null>(null)
  const [isUploading, setIsUploading] = useState(false)
  const pollTimerRef = useRef<ReturnType<typeof setInterval> | null>(null)

  useEffect(() => {
    return () => {
      if (pollTimerRef.current) clearInterval(pollTimerRef.current)
    }
  }, [])

  function stopPolling() {
    if (pollTimerRef.current) {
      clearInterval(pollTimerRef.current)
      pollTimerRef.current = null
    }
  }

  function startPolling(jobId: string) {
    stopPolling()
    pollTimerRef.current = setInterval(async () => {
      try {
        const updated = await getJobStatus(jobId)
        setJob(updated)
        if (updated.status === 'COMPLETED' || updated.status === 'FAILED') {
          stopPolling()
        }
      } catch (err) {
        setUploadError(err instanceof Error ? err.message : 'Failed to fetch job status')
        stopPolling()
      }
    }, POLL_INTERVAL_MS)
  }

  async function handleFileSelected(file: File) {
    setUploadError(null)
    setIsUploading(true)
    setJob(null)
    try {
      const created = await uploadFile(file)
      setJob(created)
      startPolling(created.jobId)
    } catch (err) {
      setUploadError(err instanceof Error ? err.message : 'Upload failed')
    } finally {
      setIsUploading(false)
    }
  }

  function handleReset() {
    stopPolling()
    setJob(null)
    setUploadError(null)
  }

  return (
    <div className="app">
      <header className="app__header">
        <h1>AI Document Converter</h1>
        <p>Upload a PDF, DOCX, CSV, or TXT file and ask questions about its content.</p>
      </header>

      <main className="app__main">
        {!job && (
          <FileUpload onFileSelected={handleFileSelected} disabled={isUploading} />
        )}

        {uploadError && <p className="error-text">{uploadError}</p>}

        {job && (
          <div className="job-panel">
            <div className="job-panel__meta">
              <span className="job-panel__filename">{job.originalFilename}</span>
              <button className="link-button" onClick={handleReset}>
                Upload a different file
              </button>
            </div>

            <StatusTracker status={job.status} errorMessage={job.errorMessage} />

            {job.status === 'COMPLETED' && <QaPanel jobId={job.jobId} />}
          </div>
        )}
      </main>
    </div>
  )
}
