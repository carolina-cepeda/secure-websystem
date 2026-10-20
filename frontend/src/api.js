const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export async function fetchRiskStatus(token) {
  const headers = {}
  if (token) headers.Authorization = `Bearer ${token}`

  const response = await fetch(`${API_URL}/api/risk-status`, { headers })
  const body = await response.text()

  if (!response.ok) {
    throw new Error(
      `La API devolvió ${response.status}${body ? `: ${body}` : ''}`,
    )
  }

  return JSON.parse(body)
}
