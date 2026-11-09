import { useContext } from 'react'
import { SessionContext } from './session-context'

export function useSession() {
  const context = useContext(SessionContext)
  if (!context) throw new Error('useSession phải được sử dụng bên trong SessionProvider.')
  return context
}
