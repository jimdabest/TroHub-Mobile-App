import { httpClient, request, requestVoid } from './http'
import type {
  AuthenticationRequest,
  AuthenticationResponse,
  ContractCreationRequest,
  ContractResponse,
  ContractStatus,
  IntrospectRequest,
  IntrospectResponse,
  LogoutRequest,
  MemberRequest,
  MemberResponse,
  PropertyRequest,
  PropertyResponse,
  RefreshRequest,
  RoomRequest,
  RoomResponse,
  RoomStatus,
  RoomStatusRequest,
  ServicePriceRequest,
  ServicePriceResponse,
  TenantRequest,
  TenantResponse,
  UserCreationRequest,
  UserResponse,
  UserUpdateRequest,
} from './types'

export const authApi = {
  authenticate: (body: AuthenticationRequest) =>
    request<AuthenticationResponse>(httpClient.post('/auth/token', body)),
  introspect: (body: IntrospectRequest) =>
    request<IntrospectResponse>(httpClient.post('/auth/introspect', body)),
  refresh: (body: RefreshRequest) =>
    request<AuthenticationResponse>(httpClient.post('/auth/refresh', body)),
  logout: (body: LogoutRequest) =>
    requestVoid(httpClient.post('/auth/logout', body)),
}

export const usersApi = {
  register: (body: UserCreationRequest) =>
    request<UserResponse>(httpClient.post('/users', body)),
  getMyInfo: () => request<UserResponse>(httpClient.get('/users/myInfo')),
  updateMyInfo: (body: UserUpdateRequest) =>
    request<UserResponse>(httpClient.put('/users/myInfo', body)),
  list: () => request<UserResponse[]>(httpClient.get('/users')),
}

export const propertiesApi = {
  create: (body: PropertyRequest) =>
    request<PropertyResponse>(httpClient.post('/properties', body)),
  listMine: () => request<PropertyResponse[]>(httpClient.get('/properties')),
  get: (propertyId: string) =>
    request<PropertyResponse>(httpClient.get(`/properties/${propertyId}`)),
  update: (propertyId: string, body: PropertyRequest) =>
    request<PropertyResponse>(httpClient.put(`/properties/${propertyId}`, body)),
  remove: (propertyId: string) =>
    requestVoid(httpClient.delete(`/properties/${propertyId}`)),
  listMembers: (propertyId: string) =>
    request<MemberResponse[]>(httpClient.get(`/properties/${propertyId}/members`)),
  addMember: (propertyId: string, body: MemberRequest) =>
    request<MemberResponse>(httpClient.post(`/properties/${propertyId}/members`, body)),
  removeMember: (propertyId: string, userId: string) =>
    requestVoid(httpClient.delete(`/properties/${propertyId}/members/${userId}`)),
}

export const roomsApi = {
  list: (propertyId: string, status?: RoomStatus) =>
    request<RoomResponse[]>(httpClient.get(`/properties/${propertyId}/rooms`, { params: { status } })),
  get: (propertyId: string, roomId: string) =>
    request<RoomResponse>(httpClient.get(`/properties/${propertyId}/rooms/${roomId}`)),
  create: (propertyId: string, body: RoomRequest) =>
    request<RoomResponse>(httpClient.post(`/properties/${propertyId}/rooms`, body)),
  update: (propertyId: string, roomId: string, body: RoomRequest) =>
    request<RoomResponse>(httpClient.put(`/properties/${propertyId}/rooms/${roomId}`, body)),
  changeStatus: (propertyId: string, roomId: string, body: RoomStatusRequest) =>
    request<RoomResponse>(httpClient.patch(`/properties/${propertyId}/rooms/${roomId}/status`, body)),
  remove: (propertyId: string, roomId: string) =>
    requestVoid(httpClient.delete(`/properties/${propertyId}/rooms/${roomId}`)),
}

export const tenantsApi = {
  list: (propertyId: string) =>
    request<TenantResponse[]>(httpClient.get(`/properties/${propertyId}/tenants`)),
  get: (propertyId: string, tenantId: string) =>
    request<TenantResponse>(httpClient.get(`/properties/${propertyId}/tenants/${tenantId}`)),
  create: (propertyId: string, body: TenantRequest) =>
    request<TenantResponse>(httpClient.post(`/properties/${propertyId}/tenants`, body)),
  update: (propertyId: string, tenantId: string, body: TenantRequest) =>
    request<TenantResponse>(httpClient.put(`/properties/${propertyId}/tenants/${tenantId}`, body)),
  remove: (propertyId: string, tenantId: string) =>
    requestVoid(httpClient.delete(`/properties/${propertyId}/tenants/${tenantId}`)),
}

export const contractsApi = {
  list: (propertyId: string, status?: ContractStatus) =>
    request<ContractResponse[]>(httpClient.get(`/properties/${propertyId}/contracts`, { params: { status } })),
  get: (propertyId: string, contractId: string) =>
    request<ContractResponse>(httpClient.get(`/properties/${propertyId}/contracts/${contractId}`)),
  create: (propertyId: string, body: ContractCreationRequest) =>
    request<ContractResponse>(httpClient.post(`/properties/${propertyId}/contracts`, body)),
  terminate: (propertyId: string, contractId: string) =>
    request<ContractResponse>(httpClient.post(`/properties/${propertyId}/contracts/${contractId}/terminate`)),
}

export const servicePricesApi = {
  list: (propertyId: string) =>
    request<ServicePriceResponse[]>(httpClient.get(`/properties/${propertyId}/service-prices`)),
  create: (propertyId: string, body: ServicePriceRequest) =>
    request<ServicePriceResponse>(httpClient.post(`/properties/${propertyId}/service-prices`, body)),
  update: (propertyId: string, serviceId: string, body: ServicePriceRequest) =>
    request<ServicePriceResponse>(httpClient.put(`/properties/${propertyId}/service-prices/${serviceId}`, body)),
  remove: (propertyId: string, serviceId: string) =>
    requestVoid(httpClient.delete(`/properties/${propertyId}/service-prices/${serviceId}`)),
}
