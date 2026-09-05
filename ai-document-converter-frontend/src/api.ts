import type { ConversionJobResponse, QaResponse } from './types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

async function parseErrorMessage(response: Response): Promise<string> {
  try {
    const body = await response.json()
    return body.message ?? `Request failed with status ${response.status}`
  } catch {
    return `Request failed with status ${response.status}`
  }
}

export async function uploadFile(file: File): Promise<ConversionJobResponse> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch(`${API_BASE_URL}/api/convert`, {
    method: 'POST',
    body: formData
  })

  if (!response.ok) {
    throw new Error(await parseErrorMessage(response))
  }
  return response.json()
}

export async function getJobStatus(jobId: string): Promise<ConversionJobResponse> {
  const response = await fetch(`${API_BASE_URL}/api/convert/${jobId}/status`)
  if (!response.ok) {
    throw new Error(await parseErrorMessage(response))
  }
  return response.json()
}

export async function askQuestion(jobId: string, question: string): Promise<QaResponse> {
  const response = await fetch(`${API_BASE_URL}/api/documents/${jobId}/query`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question })
  })
  if (!response.ok) {
    throw new Error(await parseErrorMessage(response))
  }
  return response.json()
}
