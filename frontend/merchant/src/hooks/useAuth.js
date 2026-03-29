import { useState, useEffect, useCallback } from 'react'
import { logout as authLogout } from '../services/authService'

/**
 * useAuth Hook
 * Đọc thông tin user từ localStorage.
 * Returns: { user, isAuthenticated, logout }
 */
export function useAuth() {
  const [user, setUser] = useState(() => {
    try {
      const raw = localStorage.getItem('user')
      return raw ? JSON.parse(raw) : null
    } catch {
      return null
    }
  })

  const isAuthenticated = Boolean(user && localStorage.getItem('access_token'))

  const logout = useCallback(() => {
    authLogout()
    setUser(null)
  }, [])

  // Sync across tabs
  useEffect(() => {
    const handler = (e) => {
      if (e.key === 'user') {
        try {
          setUser(e.newValue ? JSON.parse(e.newValue) : null)
        } catch {
          setUser(null)
        }
      }
    }
    window.addEventListener('storage', handler)
    return () => window.removeEventListener('storage', handler)
  }, [])

  return { user, isAuthenticated, logout }
}
