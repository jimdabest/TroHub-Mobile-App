import { createContext } from 'react'
import type { ApiClientError, PropertyResponse } from '../api'

export type PropertyContextValue = {
  properties: PropertyResponse[]
  isLoading: boolean
  error: ApiClientError | null
  reload: () => Promise<void>
  findById: (propertyId: string) => PropertyResponse | undefined
}

export const PropertyContext = createContext<PropertyContextValue | null>(null)
