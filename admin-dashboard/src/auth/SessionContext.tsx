import { useEffect, useMemo, useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi, configureAccessTokenStore, configureUnauthorizedHandler, usersApi } from '../api'
import { ApiClientError, type UserResponse } from '../api'
import { SessionContext, type SessionContextValue } from './session-context'

const tokenStorageKey = 'trohub.dashboard.access-token'

const sessionTokenStore = {
  get: () => sessionStorage.getItem(tokenStorageKey),
}

export function SessionProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate()
  const [user, setUser] = useState<UserResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<ApiClientError | null>(null)

  const clearSession = () => {
    sessionStorage.removeItem(tokenStorageKey)
    setUser(null)
  }

  useEffect(() => {
    configureAccessTokenStore(sessionTokenStore)
    configureUnauthorizedHandler(() => {
      clearSession()
      navigate('/login', { replace: true, state: { reason: 'session-expired' } })
    })

    const token = sessionTokenStore.get()
    if (!token) {
      setIsLoading(false)
      return
    }

    usersApi.getMyInfo()
      .then(setUser)
      .catch((requestError: unknown) => {
        if (!(requestError instanceof ApiClientError) || requestError.kind !== 'unauthenticated') {
          setError(requestError instanceof ApiClientError ? requestError : new ApiClientError('Không thể tải thông tin tài khoản.', 'unknown'))
        }
        clearSession()
      })
      .finally(() => setIsLoading(false))
  }, [navigate])

  const value = useMemo<SessionContextValue>(() => ({
    user,
    isLoading,
    isSubmitting,
    error,
    clearError: () => setError(null),
    login: async (credentials) => {
      setError(null)
      setIsSubmitting(true)
      try {
        const authentication = await authApi.authenticate(credentials)
        if (!authentication.authenticated || !authentication.token) {
          throw new ApiClientError('Thông tin đăng nhập không hợp lệ.', 'unauthenticated')
        }
        sessionStorage.setItem(tokenStorageKey, authentication.token)
        const profile = await usersApi.getMyInfo()
        setUser(profile)
        navigate('/app', { replace: true })
      } catch (requestError) {
        const normalizedError = requestError instanceof ApiClientError
          ? requestError
          : new ApiClientError('Không thể hoàn tất đăng nhập.', 'unknown')
        setError(normalizedError)
        sessionStorage.removeItem(tokenStorageKey)
        setUser(null)
        throw normalizedError
      } finally {
        setIsSubmitting(false)
      }
    },
    logout: async () => {
      const token = sessionTokenStore.get()
      clearSession()
      if (token) {
        try {
          await authApi.logout({ token })
        } catch (requestError) {
          if (!(requestError instanceof ApiClientError) || requestError.kind !== 'unauthenticated') {
            setError(requestError instanceof ApiClientError ? requestError : new ApiClientError('Không thể hoàn tất đăng xuất.', 'unknown'))
          }
        }
      }
      navigate('/login', { replace: true })
    },
  }), [error, isLoading, isSubmitting, navigate, user])

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
}
