import { useState } from 'react'
import { askQuestion } from '../api'
import type { ChatMessage } from '../types'

interface Props {
  jobId: string
}

export default function QaPanel({ jobId }: Props) {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [question, setQuestion] = useState('')
  const [isAsking, setIsAsking] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    const trimmed = question.trim()
    if (!trimmed || isAsking) return

    setMessages((prev) => [...prev, { role: 'user', text: trimmed }])
    setQuestion('')
    setIsAsking(true)
    setError(null)

    try {
      const response = await askQuestion(jobId, trimmed)
      setMessages((prev) => [...prev, { role: 'assistant', text: response.answer }])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get an answer')
    } finally {
      setIsAsking(false)
    }
  }

  return (
    <div className="qa-panel">
      <div className="qa-panel__messages">
        {messages.length === 0 && (
          <p className="qa-panel__hint">Ask anything about the uploaded document.</p>
        )}
        {messages.map((msg, i) => (
          <div key={i} className={`chat-bubble chat-bubble--${msg.role}`}>
            {msg.text}
          </div>
        ))}
        {isAsking && <div className="chat-bubble chat-bubble--assistant">Thinking…</div>}
      </div>

      {error && <p className="error-text">{error}</p>}

      <form className="qa-panel__form" onSubmit={handleSubmit}>
        <input
          type="text"
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          placeholder="e.g. What are the key skills mentioned?"
          disabled={isAsking}
        />
        <button type="submit" disabled={isAsking || !question.trim()}>
          Ask
        </button>
      </form>
    </div>
  )
}
