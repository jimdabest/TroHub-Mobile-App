import { createContext } from 'react'
import type { ApiClientError, AuthenticationRequest, UserResponse } from '../api'

export type SessionContextValue = {
  user: UserResponse | null
  isLoading: boolean
  isSubmitting: boolean
  error: ApiClientError | null
  login: (credentials: AuthenticationRequest) => Promise<void>
  logout: () => Promise<void>
  clearError: () => void
}

export const SessionContext = createContext<SessionContextValue | null>(null)
