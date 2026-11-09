import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { Outlet } from 'react-router-dom'
import { propertiesApi } from '../api'
import { ApiClientError, type PropertyResponse } from '../api'
import { PropertyContext, type PropertyContextValue } from './property-context'

export function PropertyProvider({ children }: { children?: ReactNode }) {
  const [properties, setProperties] = useState<PropertyResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<ApiClientError | null>(null)

  const reload = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      setProperties(await propertiesApi.listMine())
    } catch (requestError) {
      setProperties([])
      setError(requestError instanceof ApiClientError
        ? requestError
        : new ApiClientError('Không thể tải danh sách nhà trọ.', 'unknown'))
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    void reload()
  }, [reload])

  const value = useMemo<PropertyContextValue>(() => ({
    properties,
    isLoading,
    error,
    reload,
    findById: (propertyId) => properties.find((property) => property.id === propertyId),
  }), [error, isLoading, properties, reload])

  return <PropertyContext.Provider value={value}>{children ?? <Outlet />}</PropertyContext.Provider>
}
