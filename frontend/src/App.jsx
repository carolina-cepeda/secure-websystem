import { useState } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { fetchRiskStatus } from './api'
import './App.css'

export default function App() {
  const {
    loginWithRedirect,
    logout,
    isAuthenticated,
    isLoading,
    user,
    getAccessTokenSilently,
    error: authError,
  } = useAuth0()

  const [result, setResult] = useState('')
  const [loading, setLoading] = useState(false)
  const [apiError, setApiError] = useState('')

  async function callApi() {
    setLoading(true)
    setResult('')
    setApiError('')
    try {
      const token = await getAccessTokenSilently()
      const data = await fetchRiskStatus(token)
      setResult(JSON.stringify(data, null, 2))
    } catch (error) {
      setApiError(error.message)
      setResult('')
    } finally {
      setLoading(false)
    }
  }

  if (isLoading) {
    return (
      <main className="app">
        <header>
          <p className="eyebrow">Enterprise Risk Lab</p>
          <h1>Sistema Web Seguro</h1>
          <p className="subtitle">Cargando autenticación…</p>
        </header>
      </main>
    )
  }

  if (!isAuthenticated) {
    return (
      <main className="app">
        <header>
          <p className="eyebrow">Enterprise Risk Lab</p>
          <h1>Sistema Web Seguro</h1>
          <p className="subtitle">
            Debes iniciar sesión para llamar a la API protegida con un token JWT.
          </p>
        </header>
        <section className="panel">
          <button type="button" onClick={() => loginWithRedirect()}>
            Iniciar sesión
          </button>
          {authError ? (
            <p className="hint">
              Error de autenticación: {authError.message}
            </p>
          ) : null}
          <p className="hint">
            Fase 3: autenticación con Auth0 y envío de Authorization: Bearer &lt;token&gt;.
          </p>
        </section>
      </main>
    )
  }

  return (
    <main className="app">
      <header>
        <p className="eyebrow">Enterprise Risk Lab</p>
        <h1>Sistema Web Seguro</h1>
        <p className="subtitle">
          Sesión iniciada como{' '}
          <strong>{user?.name || user?.email || user?.sub}</strong>
        </p>
      </header>

      <section className="panel">
        <div className="actions">
          <button type="button" onClick={callApi} disabled={loading}>
            {loading ? 'Llamando a la API…' : 'Llamar a la API protegida'}
          </button>
          <button
            type="button"
            className="secondary"
            onClick={() =>
              logout({ logoutParams: { returnTo: window.location.origin } })
            }
          >
            Cerrar sesión
          </button>
        </div>
        {apiError ? (
          <p className="hint error">La solicitud falló: {apiError}</p>
        ) : null}
        <pre className="result">{result || 'Aún no se ha realizado ninguna solicitud.'}</pre>
      </section>
    </main>
  )
}
