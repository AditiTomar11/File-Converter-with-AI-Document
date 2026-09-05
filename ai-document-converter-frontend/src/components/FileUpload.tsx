import { useRef, useState } from 'react'

interface Props {
  onFileSelected: (file: File) => void
  disabled: boolean
}

const ACCEPTED_EXTENSIONS = ['.pdf', '.docx', '.csv', '.txt']

export default function FileUpload({ onFileSelected, disabled }: Props) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [isDragging, setIsDragging] = useState(false)
  const [validationError, setValidationError] = useState<string | null>(null)

  function validateAndEmit(file: File) {
    const hasValidExtension = ACCEPTED_EXTENSIONS.some((ext) =>
      file.name.toLowerCase().endsWith(ext)
    )
    if (!hasValidExtension) {
      setValidationError('Only PDF, DOCX, CSV, or TXT files are supported.')
      return
    }
    setValidationError(null)
    onFileSelected(file)
  }

  function handleDrop(e: React.DragEvent<HTMLDivElement>) {
    e.preventDefault()
    setIsDragging(false)
    if (disabled) return
    const file = e.dataTransfer.files?.[0]
    if (file) validateAndEmit(file)
  }

  function handleFileInputChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (file) validateAndEmit(file)
    e.target.value = '' // allow re-selecting the same file later
  }

  return (
    <div>
      <div
        className={`dropzone ${isDragging ? 'dropzone--active' : ''} ${disabled ? 'dropzone--disabled' : ''}`}
        onDragOver={(e) => {
          e.preventDefault()
          if (!disabled) setIsDragging(true)
        }}
        onDragLeave={() => setIsDragging(false)}
        onDrop={handleDrop}
        onClick={() => !disabled && inputRef.current?.click()}
      >
        <p className="dropzone__title">
          {disabled ? 'Processing…' : 'Drag & drop a file here, or click to browse'}
        </p>
        <p className="dropzone__subtitle">PDF, DOCX, CSV, or TXT</p>
        <input
          ref={inputRef}
          type="file"
          accept={ACCEPTED_EXTENSIONS.join(',')}
          onChange={handleFileInputChange}
          disabled={disabled}
          hidden
        />
      </div>
      {validationError && <p className="error-text">{validationError}</p>}
    </div>
  )
}
