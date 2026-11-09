export type ApiResponse<T> = {
  code: number
  message?: string
  result?: T
}

export type Role = 'ADMIN' | 'USER'
export type PropertyRole = 'OWNER' | 'MANAGER'
export type RoomStatus = 'AVAILABLE' | 'OCCUPIED' | 'MAINTENANCE'
export type ContractStatus = 'ACTIVE' | 'ENDED'
export type ServiceType = 'ELECTRIC' | 'WATER' | 'FIXED' | 'PER_PERSON'

export type AuthenticationRequest = {
  username: string
  password: string
}

export type AuthenticationResponse = {
  token: string
  authenticated: boolean
}

export type IntrospectRequest = {
  token: string
}

export type IntrospectResponse = {
  valid: boolean
}

export type RefreshRequest = {
  token: string
}

export type LogoutRequest = {
  token: string
}

export type UserCreationRequest = {
  username: string
  password: string
  fullName: string
  phone?: string
  email?: string
}

export type UserUpdateRequest = {
  fullName: string
  phone?: string
  email?: string
}

export type UserResponse = {
  id: string
  username: string
  fullName: string
  phone?: string
  email?: string
  role: Role
}

export type PropertyRequest = {
  name: string
  address?: string
  description?: string
}

export type PropertyResponse = {
  id: string
  name: string
  address?: string
  description?: string
  myRole: PropertyRole
  createdAt: string
  updatedAt: string
}

export type MemberRequest = {
  username: string
  role: PropertyRole
}

export type MemberResponse = {
  userId: string
  username: string
  fullName: string
  role: PropertyRole
}

export type RoomRequest = {
  code: string
  block?: string
  floor?: number
  areaM2?: number
  price: number
  description?: string
}

export type RoomStatusRequest = {
  status: RoomStatus
}

export type RoomResponse = {
  id: string
  propertyId: string
  code: string
  block?: string
  floor?: number
  areaM2?: number
  price: number
  status: RoomStatus
  description?: string
  createdAt: string
  updatedAt: string
}

export type TenantRequest = {
  fullName: string
  phone?: string
  idNumber?: string
  note?: string
}

export type TenantResponse = {
  id: string
  propertyId: string
  fullName: string
  phone?: string
  idNumber?: string
  note?: string
  createdAt: string
  updatedAt: string
}

export type ContractCreationRequest = {
  roomId: string
  tenantId: string
  startDate: string
  endDate?: string
  monthlyRent?: number
  deposit?: number
  note?: string
}

export type ContractResponse = {
  id: string
  propertyId: string
  roomId: string
  roomCode: string
  tenantId: string
  tenantName: string
  startDate: string
  endDate?: string
  monthlyRent: number
  deposit: number
  status: ContractStatus
  note?: string
  createdAt: string
  updatedAt: string
}

export type ServicePriceRequest = {
  name: string
  type: ServiceType
  unit: string
  unitPrice: number
  active?: boolean
}

export type ServicePriceResponse = {
  id: string
  propertyId: string
  name: string
  type: ServiceType
  unit: string
  unitPrice: number
  active: boolean
  createdAt: string
  updatedAt: string
}

export type HttpErrorKind =
  | 'validation'
  | 'unauthenticated'
  | 'forbidden'
  | 'not-found'
  | 'server'
  | 'network'
  | 'unknown'

export class ApiClientError extends Error {
  readonly kind: HttpErrorKind
  readonly status?: number
  readonly code?: number

  constructor(message: string, kind: HttpErrorKind, status?: number, code?: number) {
    super(message)
    this.name = 'ApiClientError'
    this.kind = kind
    this.status = status
    this.code = code
  }
}
