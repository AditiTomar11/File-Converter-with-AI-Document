import type { JobStatus } from '../types'

const STAGES: { key: JobStatus; label: string }[] = [
  { key: 'PENDING', label: 'Queued' },
  { key: 'EXTRACTING', label: 'Extracting text' },
  { key: 'EXTRACTED', label: 'Extracted' },
  { key: 'CHUNKING', label: 'Chunking & indexing' },
  { key: 'COMPLETED', label: 'Ready' }
]

interface Props {
  status: JobStatus
  errorMessage: string | null
}

export default function StatusTracker({ status, errorMessage }: Props) {
  if (status === 'FAILED') {
    return (
      <div className="status-tracker status-tracker--failed">
        <p className="status-tracker__title">Job failed</p>
        <p className="error-text">{errorMessage ?? 'Unknown error'}</p>
      </div>
    )
  }

  const currentIndex = STAGES.findIndex((s) => s.key === status)

  return (
    <div className="status-tracker">
      {STAGES.map((stage, index) => {
        const isComplete = index < currentIndex || status === 'COMPLETED'
        const isCurrent = index === currentIndex && status !== 'COMPLETED'
        return (
          <div key={stage.key} className="status-tracker__step">
            <span
              className={`status-tracker__dot ${
                isComplete ? 'status-tracker__dot--done' : ''
              } ${isCurrent ? 'status-tracker__dot--active' : ''}`}
            />
            <span className="status-tracker__label">{stage.label}</span>
          </div>
        )
      })}
    </div>
  )
}
