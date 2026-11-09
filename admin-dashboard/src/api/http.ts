import axios, { AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { ApiClientError, type ApiResponse, type HttpErrorKind } from './types'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL

if (!apiBaseUrl) {
  console.warn('VITE_API_BASE_URL chưa được cấu hình; API client chưa thể gửi request.')
}

export type AccessTokenStore = {
  get: () => string | null
}

export type UnauthorizedHandler = () => void

const emptyTokenStore: AccessTokenStore = {
  get: () => null,
}

let accessTokenStore: AccessTokenStore = emptyTokenStore
let unauthorizedHandler: UnauthorizedHandler | null = null

export function configureAccessTokenStore(store: AccessTokenStore) {
  accessTokenStore = store
}

export function configureUnauthorizedHandler(handler: UnauthorizedHandler | null) {
  unauthorizedHandler = handler
}

function addBearerToken(config: InternalAxiosRequestConfig) {
  const token = accessTokenStore.get()
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
}

function classifyStatus(status?: number): HttpErrorKind {
  if (status === 400) return 'validation'
  if (status === 401) return 'unauthenticated'
  if (status === 403) return 'forbidden'
  if (status === 404) return 'not-found'
  if (status !== undefined && status >= 500) return 'server'
  return 'unknown'
}

function messageFromResponse(response: ApiResponse<unknown> | undefined, fallback: string) {
  return response?.message?.trim() || fallback
}

function normalizeError(error: AxiosError<ApiResponse<unknown>>) {
  if (!error.response) {
    const message = error.request
      ? 'Không thể kết nối đến máy chủ API.'
      : 'Không thể tạo yêu cầu API.'
    return new ApiClientError(message, error.request ? 'network' : 'unknown')
  }

  const { status, data } = error.response
  if (status === 401) unauthorizedHandler?.()
  return new ApiClientError(
    messageFromResponse(data, 'Yêu cầu API không thành công.'),
    classifyStatus(status),
    status,
    data?.code,
  )
}

export const httpClient: AxiosInstance = axios.create({
  baseURL: apiBaseUrl,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
})

httpClient.interceptors.request.use(addBearerToken)
httpClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => Promise.reject(normalizeError(error)),
)

export async function request<T>(promise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const { data } = await promise
  if (data.result === undefined) {
    throw new ApiClientError(
      data.message || 'API không trả về dữ liệu như mong đợi.',
      'unknown',
      undefined,
      data.code,
    )
  }
  return data.result
}

export async function requestVoid(promise: Promise<{ data: ApiResponse<void> }>): Promise<void> {
  await promise
}
