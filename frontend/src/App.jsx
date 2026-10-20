import { useState } from 'react'
import { fetchRiskStatus } from './api'
import './App.css'

export default function App() {
  const [result, setResult] = useState('')
  const [loading, setLoading] = useState(false)

  async function callApi() {
    setLoading(true)
    setResult('')
    try {
      const data = await fetchRiskStatus()
      setResult(JSON.stringify(data, null, 2))
    } catch (error) {
      setResult(`La solicitud falló: ${error.message}`)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="app">
      <header>
        <p className="eyebrow">Enterprise Risk Lab</p>
        <h1>Sistema Web Seguro</h1>
        <p className="subtitle">
          Frontend en React que consume la API de riesgo de Spring Boot.
        </p>
      </header>

      <section className="panel">
        <button type="button" onClick={callApi} disabled={loading}>
          {loading ? 'Llamando a la API…' : 'Llamar a la API protegida'}
        </button>
        <p className="hint">
          Fase 2: llamada sin token. La autenticación con Auth0 llega en la Fase 3.
        </p>
        <pre className="result">{result || 'Aún no se ha realizado ninguna solicitud.'}</pre>
      </section>
    </main>
  )
}
