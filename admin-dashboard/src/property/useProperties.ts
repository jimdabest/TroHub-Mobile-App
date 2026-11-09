import { useContext } from 'react'
import { PropertyContext } from './property-context'

export function useProperties() {
  const context = useContext(PropertyContext)
  if (!context) throw new Error('useProperties phải được sử dụng bên trong PropertyProvider.')
  return context
}
