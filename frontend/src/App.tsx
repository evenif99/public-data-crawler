import { useEffect, useState } from 'react'
import './App.css'

type Status = 'loading' | 'ok' | 'error'

function App() {
  const [status, setStatus] = useState<Status>('loading')

  useEffect(() => {
    fetch('/api/health')
      .then((res) => (res.ok ? res.text() : Promise.reject(res.status)))
      .then((body) => setStatus(body === 'ok' ? 'ok' : 'error'))
      .catch(() => setStatus('error'))
  }, [])

  const label = {
    loading: '확인 중...',
    ok: '연결됨',
    error: '연결 실패 (백엔드 실행 여부 확인)',
  }[status]

  return (
    <main className="app">
      <h1>공공데이터 수집 대시보드</h1>
      <p>
        백엔드 상태: <span className={`status status-${status}`}>{label}</span>
      </p>
    </main>
  )
}

export default App
