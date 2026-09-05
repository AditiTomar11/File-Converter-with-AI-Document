export type JobStatus =
  | 'PENDING'
  | 'EXTRACTING'
  | 'EXTRACTED'
  | 'CHUNKING'
  | 'COMPLETED'
  | 'FAILED'

export interface ConversionJobResponse {
  jobId: string
  originalFilename: string
  status: JobStatus
  errorMessage: string | null
}

export interface QaResponse {
  jobId: string
  question: string
  answer: string
}

export interface ChatMessage {
  role: 'user' | 'assistant'
  text: string
}
