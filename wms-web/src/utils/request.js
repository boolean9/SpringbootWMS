import axios from 'axios'
import { getCurrentUser } from './session'

export const apiBaseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8090'

const request = axios.create({
  baseURL: apiBaseURL,
  timeout: 15000,
})

function toHeaderValue(value) {
  if (value === null || value === undefined) {
    return ''
  }
  return String(value)
}

function toEncodedHeaderValue(value) {
  const text = toHeaderValue(value)
  return text ? encodeURIComponent(text) : ''
}

request.interceptors.request.use((config) => {
  const currentUser = getCurrentUser()

  if (currentUser) {
    config.headers['X-User-Id'] = toHeaderValue(currentUser.id)
    config.headers['X-User-Name'] = toEncodedHeaderValue(currentUser.name)
    config.headers['X-User-No'] = toHeaderValue(currentUser.no)
    config.headers['X-User-Role'] = toHeaderValue(currentUser.roleId)
  }

  return config
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => Promise.reject(error),
)

export default request
