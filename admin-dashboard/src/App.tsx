import { Navigate, Outlet, Route, Routes, Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { ApiClientError, contractsApi, roomsApi, servicePricesApi, tenantsApi, propertiesApi, type ContractCreationRequest, type ContractResponse, type ContractStatus, type MemberRequest, type MemberResponse, type PropertyResponse, type RoomRequest, type RoomResponse, type RoomStatus, type ServicePriceRequest, type ServicePriceResponse, type ServiceType, type TenantRequest, type TenantResponse } from './api'
import { useSession } from './auth/useSession'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { PropertyProvider } from './property/PropertyContext'
import { useProperties } from './property/useProperties'

type IconName =
  | 'building'
  | 'chevronDown'
  | 'chevronLeft'
  | 'clipboard'
  | 'file'
  | 'grid'
  | 'menu'
  | 'people'
  | 'settings'
  | 'users'
  | 'x'

const navigation = [
  { label: 'Tổng quan', path: 'overview', icon: 'grid' as IconName },
  { label: 'Phòng', path: 'rooms', icon: 'building' as IconName },
  { label: 'Khách thuê', path: 'tenants', icon: 'people' as IconName },
  { label: 'Hợp đồng', path: 'contracts', icon: 'file' as IconName },
  { label: 'Giá dịch vụ', path: 'services', icon: 'clipboard' as IconName },
  { label: 'Thành viên', path: 'members', icon: 'users' as IconName },
]

function Icon({ name, size = 20 }: { name: IconName; size?: number }) {
  const paths: Record<IconName, string> = {
    building: 'M4 20V4.8L12 2l8 2.8V20M8 20v-4h8v4M8 8h.01M12 8h.01M16 8h.01M8 11h.01M12 11h.01M16 11h.01',
    chevronDown: 'm6 9 6 6 6-6',
    chevronLeft: 'm15 18-6-6 6-6',
    clipboard: 'M8 4h8M9 3h6v3H9zM6 5H4v15h16V5h-2M8 11h8M8 15h5',
    file: 'M6 3h8l4 4v14H6zM14 3v5h5M9 13h6M9 17h6',
    grid: 'M4 4h6v6H4zM14 4h6v6h-6zM4 14h6v6H4zM14 14h6v6h-6z',
    menu: 'M4 6h16M4 12h16M4 18h16',
    people: 'M16 20v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2M9.5 10a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM22 20v-2a4 4 0 0 0-3-3.87M16.5 2.13a4 4 0 0 1 0 7.75',
    settings: 'M12 15.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7ZM19.4 15a1.7 1.7 0 0 0 .34 1.88l.06.06-1.42 1.42-.06-.06a1.7 1.7 0 0 0-1.88-.34 1.7 1.7 0 0 0-1.03 1.56V20h-2v-.48a1.7 1.7 0 0 0-1.03-1.56 1.7 1.7 0 0 0-1.88.34l-.06.06-1.42-1.42.06-.06A1.7 1.7 0 0 0 9.4 15a1.7 1.7 0 0 0-1.56-1.03H7v-2h.84A1.7 1.7 0 0 0 9.4 11a1.7 1.7 0 0 0-.34-1.88L9 9.06l1.42-1.42.06.06A1.7 1.7 0 0 0 12.36 8a1.7 1.7 0 0 0 1.03-1.56V6h2v.44A1.7 1.7 0 0 0 16.42 8a1.7 1.7 0 0 0 1.88-.34l.06-.06 1.42 1.42-.06.06A1.7 1.7 0 0 0 19.4 11a1.7 1.7 0 0 0 1.56 1.03H22v2h-1.04A1.7 1.7 0 0 0 19.4 15Z',
    users: 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75',
    x: 'M6 6l12 12M18 6 6 18',
  }

  return (
    <svg aria-hidden="true" className="icon" fill="none" height={size} viewBox="0 0 24 24" width={size}>
      <path d={paths[name]} stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" />
    </svg>
  )
}

function AppLayout() {
  const location = useLocation()
  const navigate = useNavigate()
  const { user, logout, error, clearError } = useSession()
  const { findById, isLoading: propertiesLoading } = useProperties()
  const [propertyId] = location.pathname.match(/\/app\/([^/]+)/)?.slice(1) ?? []
  const isPropertyRoute = Boolean(propertyId && propertyId !== 'settings')
  const selectedProperty = propertyId ? findById(propertyId) : undefined
  const [menuOpen, setMenuOpen] = useState(false)

  return (
    <div className="dashboard-shell">
      <button className="mobile-menu-button" onClick={() => setMenuOpen(true)} type="button">
        <Icon name="menu" />
        <span className="sr-only">Mở menu</span>
      </button>
      {menuOpen && <button aria-label="Đóng menu" className="sidebar-backdrop" onClick={() => setMenuOpen(false)} type="button" />}
      <aside className={`sidebar ${menuOpen ? 'sidebar--open' : ''}`}>
        <div className="brand">
          <div className="brand-mark">T</div>
          <div>
            <strong>TroHub</strong>
            <span>Quản lý nhà trọ</span>
          </div>
          <button aria-label="Đóng menu" className="sidebar-close" onClick={() => setMenuOpen(false)} type="button">
            <Icon name="x" />
          </button>
        </div>

        <div className="property-switcher">
          <span className="eyebrow">Nhà trọ đang chọn</span>
          <button className="property-switcher__button" onClick={() => navigate('/app')} type="button">
            <span className="property-avatar"><Icon name="building" size={17} /></span>
            <span className="property-switcher__name">{selectedProperty?.name ?? 'Chưa chọn nhà trọ'}</span>
            <Icon name="chevronDown" size={16} />
          </button>
        </div>

        <nav className="sidebar-nav" aria-label="Điều hướng chính">
          <span className="nav-section-title">Quản lý</span>
          {isPropertyRoute && selectedProperty ? navigation.map((item) => (
            <NavItem key={item.path} {...item} propertyId={propertyId} onNavigate={() => setMenuOpen(false)} />
          )) : isPropertyRoute && !propertiesLoading ? (
            <div className="permission-alert permission-alert--sidebar" role="alert">Nhà trọ không thuộc phạm vi tài khoản.</div>
          ) : (
            <div className="nav-disabled-note">Chọn một nhà trọ để mở các chức năng quản lý.</div>
          )}
          <span className="nav-section-title nav-section-title--spaced">Hệ thống</span>
          <NavItem icon="settings" label="Tài khoản & cài đặt" path="/app/settings" onNavigate={() => setMenuOpen(false)} />
        </nav>

        <div className="sidebar-footer">
          <div className="user-avatar">{user?.fullName.charAt(0).toUpperCase() ?? 'U'}</div>
          <div className="user-summary">
            <strong>{user?.fullName ?? user?.username}</strong>
            <span>{user?.role === 'ADMIN' ? 'System ADMIN' : 'Đang quản lý nhà trọ'}</span>
          </div>
          <button aria-label="Đăng xuất" className="logout-button" onClick={() => void logout()} type="button">Thoát</button>
        </div>
      </aside>

      <div className="main-area">
        <header className="topbar">
          <div className="breadcrumb">
            <span>TroHub</span>
            <span className="breadcrumb-separator">/</span>
            <strong>{getPageTitle(location.pathname)}</strong>
          </div>
          <div className="topbar-actions">
            <span className="role-badge">Owner / Manager</span>
            <button aria-label="Mở cài đặt tài khoản" className="icon-button" onClick={() => navigate('/app/settings')} type="button">
              <Icon name="settings" size={19} />
            </button>
          </div>
        </header>
        <main className="content">
          {error?.kind === 'forbidden' && (
            <div className="permission-alert" role="alert">
              <span>Bạn không có quyền thực hiện thao tác này.</span>
              <button onClick={clearError} type="button">Đóng</button>
            </div>
          )}
          {isPropertyRoute && !propertiesLoading && !selectedProperty ? (
            <PropertyAccessDenied />
          ) : <Outlet />}
        </main>
      </div>
    </div>
  )
}

function NavItem({ icon, label, path, propertyId, onNavigate }: { icon: IconName; label: string; path: string; propertyId?: string; onNavigate: () => void }) {
  const location = useLocation()
  const target = propertyId && !path.startsWith('/') ? `/app/${propertyId}/${path}` : path
  const active = location.pathname === target

  return (
    <Link className={`nav-item ${active ? 'nav-item--active' : ''}`} to={target} onClick={onNavigate}>
      <Icon name={icon} />
      <span>{label}</span>
    </Link>
  )
}

function PageIntro({ eyebrow, title, description }: { eyebrow: string; title: string; description: string }) {
  return (
    <div className="page-intro">
      <div>
        <span className="eyebrow">{eyebrow}</span>
        <h1>{title}</h1>
        <p>{description}</p>
      </div>
    </div>
  )
}

function EmptyState({ title, description }: { title: string; description: string }) {
  return (
    <section className="state-card">
      <div className="state-icon"><Icon name="clipboard" size={24} /></div>
      <h2>{title}</h2>
      <p>{description}</p>
      <span className="state-hint">Dữ liệu sẽ hiển thị sau khi kết nối API.</span>
    </section>
  )
}

function PropertyAccessDenied() {
  const navigate = useNavigate()
  return (
    <section className="state-card">
      <div className="state-icon"><Icon name="building" size={24} /></div>
      <h2>Không thể truy cập nhà trọ</h2>
      <p>Nhà trọ này không nằm trong danh sách membership mà backend trả về cho tài khoản hiện tại.</p>
      <button className="button button--primary" onClick={() => navigate('/app')} type="button">Chọn nhà trọ khác</button>
    </section>
  )
}

function OverviewPage() {
  const { propertyId = '' } = useParams()
  const { findById } = useProperties()
  const [data, setData] = useState<OverviewData | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [loadError, setLoadError] = useState<ApiClientError | null>(null)

  const loadOverview = useCallback(async () => {
    setIsLoading(true)
    setLoadError(null)
    const results = await Promise.allSettled([
      propertiesApi.get(propertyId),
      roomsApi.list(propertyId),
      tenantsApi.list(propertyId),
      contractsApi.list(propertyId, 'ACTIVE'),
      servicePricesApi.list(propertyId),
    ])
    const [property, rooms, tenants, contracts, services] = results
    const failures = results.filter((result): result is PromiseRejectedResult => result.status === 'rejected')
    setData({
      property: property.status === 'fulfilled' ? property.value : findById(propertyId),
      rooms: rooms.status === 'fulfilled' ? rooms.value : null,
      tenants: tenants.status === 'fulfilled' ? tenants.value : null,
      contracts: contracts.status === 'fulfilled' ? contracts.value : null,
      services: services.status === 'fulfilled' ? services.value : null,
    })
    setLoadError(failures[0] ? normalizeOverviewError(failures[0].reason) : null)
    setIsLoading(false)
  }, [findById, propertyId])

  useEffect(() => {
    void loadOverview()
  }, [loadOverview])

  if (isLoading) return <PageLoading label="Đang tải tổng quan nhà trọ..." />

  const property = data?.property
  return (
    <>
      <PageIntro eyebrow="Tổng quan" title={property?.name ?? 'Nhà trọ'} description={property?.address ?? 'Thông tin tổng hợp từ dữ liệu backend của nhà trọ đã chọn.'} />
      {loadError && <OverviewError error={loadError} onRetry={() => void loadOverview()} />}
      <div className="overview-grid">
        <StateMetric label="Tổng số phòng" value={data?.rooms?.length} source="GET /rooms" />
        <StateMetric label="Khách thuê" value={data?.tenants?.length} source="GET /tenants" />
        <StateMetric label="Hợp đồng ACTIVE" value={data?.contracts?.length} source="GET /contracts?status=ACTIVE" />
        <StateMetric label="Dịch vụ đang hoạt động" value={data?.services?.filter((service) => service.active).length} source="GET /service-prices" />
      </div>
      <section className="overview-section">
        <div className="section-heading"><div><span className="eyebrow">Phân bổ phòng</span><h2>Trạng thái phòng</h2></div><button className="text-button" onClick={() => void loadOverview()} type="button">Tải lại</button></div>
        <div className="status-grid">
          <StatusMetric label="Đang trống" value={data?.rooms?.filter((room) => room.status === 'AVAILABLE').length} />
          <StatusMetric label="Đang thuê" value={data?.rooms?.filter((room) => room.status === 'OCCUPIED').length} />
          <StatusMetric label="Bảo trì" value={data?.rooms?.filter((room) => room.status === 'MAINTENANCE').length} />
        </div>
      </section>
    </>
  )
}

type OverviewData = {
  property?: PropertyResponse
  rooms: RoomResponse[] | null
  tenants: TenantResponse[] | null
  contracts: ContractResponse[] | null
  services: ServicePriceResponse[] | null
}

function normalizeOverviewError(reason: unknown) {
  return reason instanceof ApiClientError ? reason : new ApiClientError('Không thể tải một phần dữ liệu tổng quan.', 'unknown')
}

function StateMetric({ label, value, source }: { label: string; value: number | undefined; source: string }) {
  return <div className="metric-card"><span>{label}</span><strong>{value === undefined ? '—' : value}</strong><small>{value === undefined ? 'Chưa tải được' : source}</small></div>
}

function StatusMetric({ label, value }: { label: string; value: number | undefined }) {
  return <div className="status-metric"><span>{label}</span><strong>{value === undefined ? '—' : value}</strong></div>
}

function PageLoading({ label }: { label: string }) {
  return <div className="page-loading" role="status">{label}</div>
}

function OverviewError({ error, onRetry }: { error: ApiClientError; onRetry: () => void }) {
  const message = error.kind === 'forbidden'
    ? 'Bạn không có quyền xem một hoặc nhiều dữ liệu của nhà trọ này.'
    : error.kind === 'network'
      ? 'Không thể kết nối đến máy chủ API.'
      : 'Một hoặc nhiều nguồn dữ liệu chưa tải được.'
  return <div className="overview-error" role="alert"><span>{message}</span><button onClick={onRetry} type="button">Thử lại</button></div>
}

const roomStatusLabels: Record<RoomStatus, string> = {
  AVAILABLE: 'Đang trống',
  OCCUPIED: 'Đang thuê',
  MAINTENANCE: 'Bảo trì',
}

function RoomPage() {
  const { propertyId = '' } = useParams()
  const [rooms, setRooms] = useState<RoomResponse[]>([])
  const [statusFilter, setStatusFilter] = useState<RoomStatus | ''>('')
  const [search, setSearch] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<ApiClientError | null>(null)
  const [formRoom, setFormRoom] = useState<RoomResponse | null | undefined>(undefined)
  const [detailRoom, setDetailRoom] = useState<RoomResponse | null>(null)
  const [actionMessage, setActionMessage] = useState<string | null>(null)

  const loadRooms = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      setRooms(await roomsApi.list(propertyId, statusFilter || undefined))
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason : new ApiClientError('Không thể tải danh sách phòng.', 'unknown'))
    } finally {
      setIsLoading(false)
    }
  }, [propertyId, statusFilter])

  useEffect(() => {
    void loadRooms()
  }, [loadRooms])

  const filteredRooms = rooms.filter((room) => {
    const query = search.trim().toLowerCase()
    return !query || [room.code, room.block, room.description].some((value) => value?.toLowerCase().includes(query))
  })

  const handleDelete = async (room: RoomResponse) => {
    if (room.status === 'OCCUPIED') {
      setActionMessage('Không thể xóa phòng đang có người thuê. Hãy kết thúc hợp đồng qua nghiệp vụ hợp đồng trước.')
      return
    }
    if (!window.confirm(`Bạn có chắc muốn xóa phòng ${room.code}?`)) return
    try {
      await roomsApi.remove(propertyId, room.id)
      setActionMessage(`Đã xóa phòng ${room.code}.`)
      await loadRooms()
    } catch (reason) {
      setActionMessage(getRoomActionError(reason, 'Không thể xóa phòng.'))
    }
  }

  const handleStatusChange = async (room: RoomResponse, status: RoomStatus) => {
    if (room.status === 'OCCUPIED' || status === 'OCCUPIED') {
      setActionMessage('Trạng thái Đang thuê được quản lý bởi hợp đồng và không thể đổi trực tiếp.')
      return
    }
    try {
      await roomsApi.changeStatus(propertyId, room.id, { status })
      setActionMessage(`Đã cập nhật trạng thái phòng ${room.code}.`)
      await loadRooms()
    } catch (reason) {
      setActionMessage(getRoomActionError(reason, 'Không thể cập nhật trạng thái phòng.'))
    }
  }

  return (
    <>
      <PageIntro eyebrow="Quản lý" title="Phòng" description="Theo dõi và cập nhật thông tin phòng trong nhà trọ đang chọn." />
      {actionMessage && <div className="form-message form-message--info" role="status"><span>{actionMessage}</span><button onClick={() => setActionMessage(null)} type="button">Đóng</button></div>}
      <div className="resource-toolbar">
        <input aria-label="Tìm phòng" className="toolbar-input" onChange={(event) => setSearch(event.target.value)} placeholder="Tìm theo mã, block hoặc mô tả..." value={search} />
        <select aria-label="Lọc trạng thái phòng" className="toolbar-select" onChange={(event) => setStatusFilter(event.target.value as RoomStatus | '')} value={statusFilter}>
          <option value="">Tất cả trạng thái</option>
          <option value="AVAILABLE">Đang trống</option>
          <option value="OCCUPIED">Đang thuê</option>
          <option value="MAINTENANCE">Bảo trì</option>
        </select>
        <button className="button button--primary" onClick={() => setFormRoom(null)} type="button">Thêm phòng</button>
      </div>
      {isLoading ? <PageLoading label="Đang tải danh sách phòng..." /> : error ? (
        <section className="state-card"><div className="state-icon"><Icon name="building" size={24} /></div><h2>Không thể tải danh sách phòng</h2><p>{error.kind === 'network' ? 'Kiểm tra kết nối đến máy chủ API.' : error.message}</p><button className="button button--primary" onClick={() => void loadRooms()} type="button">Thử lại</button></section>
      ) : filteredRooms.length === 0 ? (
        <EmptyState title={search || statusFilter ? 'Không tìm thấy phòng phù hợp' : 'Chưa có phòng'} description={search || statusFilter ? 'Thử thay đổi từ khóa hoặc bộ lọc.' : 'Tạo phòng đầu tiên cho nhà trọ này để bắt đầu quản lý.'} />
      ) : (
        <section className="room-table-card">
          <div className="room-table-wrap">
            <table className="room-table">
              <thead><tr><th>Mã phòng</th><th>Block / tầng</th><th>Diện tích</th><th>Giá</th><th>Trạng thái</th><th><span className="sr-only">Thao tác</span></th></tr></thead>
              <tbody>{filteredRooms.map((room) => (
                <tr key={room.id}>
                  <td><button className="link-button" onClick={() => setDetailRoom(room)} type="button">{room.code}</button></td>
                  <td>{room.block || '—'} / {room.floor === undefined ? '—' : room.floor}</td>
                  <td>{room.areaM2 === undefined ? '—' : `${room.areaM2} m²`}</td>
                  <td>{formatCurrency(room.price)}</td>
                  <td><span className={`status-pill status-pill--${room.status.toLowerCase()}`}>{roomStatusLabels[room.status]}</span></td>
                  <td><div className="table-actions"><button className="text-button" onClick={() => setFormRoom(room)} type="button">Sửa</button><select aria-label={`Cập nhật trạng thái ${room.code}`} disabled={room.status === 'OCCUPIED'} onChange={(event) => void handleStatusChange(room, event.target.value as RoomStatus)} value={room.status}><option value="AVAILABLE">Trống</option><option value="MAINTENANCE">Bảo trì</option></select><button className="danger-button" onClick={() => void handleDelete(room)} type="button">Xóa</button></div></td>
                </tr>
              ))}</tbody>
            </table>
          </div>
        </section>
      )}
      {formRoom !== undefined && <RoomForm propertyId={propertyId} room={formRoom} onClose={() => setFormRoom(undefined)} onSaved={async (message) => { setFormRoom(undefined); setActionMessage(message); await loadRooms() }} />}
      {detailRoom && <RoomDetail propertyId={propertyId} room={detailRoom} onClose={() => setDetailRoom(null)} />}
    </>
  )
}

function RoomForm({ propertyId, room, onClose, onSaved }: { propertyId: string; room: RoomResponse | null; onClose: () => void; onSaved: (message: string) => Promise<void> }) {
  const [form, setForm] = useState<RoomRequest>(() => room ? { code: room.code, block: room.block, floor: room.floor, areaM2: room.areaM2, price: room.price, description: room.description } : { code: '', block: '', floor: undefined, areaM2: undefined, price: 0, description: '' })
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const update = (field: keyof RoomRequest, value: string | number | undefined) => setForm((current) => ({ ...current, [field]: value }))
  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!form.code.trim() || form.price < 0) { setError('Vui lòng nhập mã phòng và giá hợp lệ.'); return }
    setIsSubmitting(true)
    setError('')
    try {
      const body = { ...form, code: form.code.trim(), block: form.block?.trim() || undefined, description: form.description?.trim() || undefined }
      if (room) await roomsApi.update(propertyId, room.id, body)
      else await roomsApi.create(propertyId, body)
      await onSaved(room ? 'Đã cập nhật phòng.' : 'Đã tạo phòng.')
    } catch (reason) {
      setError(getRoomActionError(reason, room ? 'Không thể cập nhật phòng.' : 'Không thể tạo phòng.'))
    } finally {
      setIsSubmitting(false)
    }
  }
  return <div className="modal-backdrop"><section aria-labelledby="room-form-title" className="modal-card"><div className="modal-heading"><div><span className="eyebrow">Phòng</span><h2 id="room-form-title">{room ? 'Cập nhật phòng' : 'Thêm phòng'}</h2></div><button className="icon-button" onClick={onClose} type="button"><Icon name="x" /></button></div>{error && <div className="form-message form-message--error" role="alert">{error}</div>}<form className="room-form" onSubmit={submit}><label>Mã phòng<input required maxLength={20} onChange={(event) => update('code', event.target.value)} value={form.code} /></label><div className="form-grid"><label>Block<input onChange={(event) => update('block', event.target.value)} value={form.block ?? ''} /></label><label>Tầng<input min="0" onChange={(event) => update('floor', event.target.value ? Number(event.target.value) : undefined)} type="number" value={form.floor ?? ''} /></label><label>Diện tích (m²)<input min="0" step="0.01" onChange={(event) => update('areaM2', event.target.value ? Number(event.target.value) : undefined)} type="number" value={form.areaM2 ?? ''} /></label><label>Giá thuê<input min="0" required step="0.01" onChange={(event) => update('price', Number(event.target.value))} type="number" value={form.price} /></label></div><label>Mô tả<textarea onChange={(event) => update('description', event.target.value)} value={form.description ?? ''} /></label><div className="modal-actions"><button className="button" onClick={onClose} type="button">Hủy</button><button className="button button--primary" disabled={isSubmitting} type="submit">{isSubmitting ? 'Đang lưu...' : 'Lưu phòng'}</button></div></form></section></div>
}

function RoomDetail({ propertyId, room, onClose }: { propertyId: string; room: RoomResponse; onClose: () => void }) {
  const [detail, setDetail] = useState<RoomResponse>(room)
  const [error, setError] = useState('')
  useEffect(() => { void roomsApi.get(propertyId, room.id).then(setDetail).catch((reason) => setError(getRoomActionError(reason, 'Không thể tải chi tiết phòng.'))) }, [propertyId, room.id])
  return <div className="modal-backdrop"><section aria-labelledby="room-detail-title" className="modal-card"><div className="modal-heading"><div><span className="eyebrow">Chi tiết phòng</span><h2 id="room-detail-title">{detail.code}</h2></div><button className="icon-button" onClick={onClose} type="button"><Icon name="x" /></button></div>{error && <div className="form-message form-message--error" role="alert">{error}</div>}<dl className="detail-list"><div><dt>Trạng thái</dt><dd>{roomStatusLabels[detail.status]}</dd></div><div><dt>Block / tầng</dt><dd>{detail.block || '—'} / {detail.floor === undefined ? '—' : detail.floor}</dd></div><div><dt>Diện tích</dt><dd>{detail.areaM2 === undefined ? '—' : `${detail.areaM2} m²`}</dd></div><div><dt>Giá</dt><dd>{formatCurrency(detail.price)}</dd></div><div><dt>Mô tả</dt><dd>{detail.description || '—'}</dd></div></dl></section></div>
}

function getRoomActionError(reason: unknown, fallback: string) {
  if (!(reason instanceof ApiClientError)) return fallback
  if (reason.kind === 'validation') return 'Dữ liệu phòng không hợp lệ. Kiểm tra mã phòng, giá và diện tích.'
  if (reason.kind === 'forbidden') return 'Bạn không có quyền thực hiện thao tác này trên nhà trọ.'
  if (reason.kind === 'not-found') return 'Không tìm thấy phòng hoặc phòng đã được thay đổi.'
  if (reason.message.includes('ROOM_CODE_EXISTED')) return 'Mã phòng đã tồn tại trong nhà trọ.'
  if (reason.message.includes('ROOM_HAS_ACTIVE_CONTRACT')) return 'Không thể xóa phòng vì phòng còn hợp đồng ACTIVE.'
  if (reason.message.includes('ROOM_STATUS_INVALID')) return 'Không thể đổi trạng thái OCCUPIED trực tiếp; trạng thái này do hợp đồng quản lý.'
  return reason.message || fallback
}

function formatCurrency(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value)
}

function TenantPage() {
  const { propertyId = '' } = useParams()
  const [tenants, setTenants] = useState<TenantResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<ApiClientError | null>(null)
  const [search, setSearch] = useState('')
  const [formTenant, setFormTenant] = useState<TenantResponse | null | undefined>(undefined)
  const [detailTenant, setDetailTenant] = useState<TenantResponse | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const loadTenants = useCallback(async () => {
    setIsLoading(true); setError(null)
    try { setTenants(await tenantsApi.list(propertyId)) } catch (reason) { setError(reason instanceof ApiClientError ? reason : new ApiClientError('Không thể tải danh sách khách thuê.', 'unknown')) } finally { setIsLoading(false) }
  }, [propertyId])
  useEffect(() => { void loadTenants() }, [loadTenants])
  const filtered = tenants.filter((tenant) => {
    const query = search.trim().toLowerCase()
    return !query || [tenant.fullName, tenant.phone, tenant.note].some((value) => value?.toLowerCase().includes(query))
  })
  const removeTenant = async (tenant: TenantResponse) => {
    if (!window.confirm(`Bạn có chắc muốn xóa khách thuê ${tenant.fullName}?`)) return
    try { await tenantsApi.remove(propertyId, tenant.id); setMessage(`Đã xóa khách thuê ${tenant.fullName}.`); await loadTenants() } catch (reason) { setMessage(getTenantActionError(reason)) }
  }
  return (
    <>
      <PageIntro eyebrow="Quản lý" title="Khách thuê" description="Quản lý thông tin khách thuê trong nhà trọ đang chọn." />
      {message && <div className="form-message form-message--info" role="status"><span>{message}</span><button onClick={() => setMessage(null)} type="button">Đóng</button></div>}
      <div className="resource-toolbar"><input aria-label="Tìm khách thuê" className="toolbar-input" onChange={(event) => setSearch(event.target.value)} placeholder="Tìm theo họ tên, số điện thoại..." value={search} /><button className="button button--primary" onClick={() => setFormTenant(null)} type="button">Thêm khách thuê</button></div>
      {isLoading ? <PageLoading label="Đang tải danh sách khách thuê..." /> : error ? (
        <section className="state-card"><div className="state-icon"><Icon name="people" size={24} /></div><h2>Không thể tải danh sách khách thuê</h2><p>{error.kind === 'network' ? 'Kiểm tra kết nối đến máy chủ API.' : error.message}</p><button className="button button--primary" onClick={() => void loadTenants()} type="button">Thử lại</button></section>
      ) : filtered.length === 0 ? <EmptyState title={search ? 'Không tìm thấy khách thuê phù hợp' : 'Chưa có khách thuê'} description={search ? 'Thử thay đổi từ khóa tìm kiếm.' : 'Tạo khách thuê đầu tiên cho nhà trọ này.'} /> : (
        <section className="room-table-card"><div className="room-table-wrap"><table className="room-table"><thead><tr><th>Họ tên</th><th>Số điện thoại</th><th>Số định danh</th><th>Ghi chú</th><th><span className="sr-only">Thao tác</span></th></tr></thead><tbody>{filtered.map((tenant) => <tr key={tenant.id}><td><button className="link-button" onClick={() => setDetailTenant(tenant)} type="button">{tenant.fullName}</button></td><td>{tenant.phone || '—'}</td><td>{maskIdNumber(tenant.idNumber)}</td><td>{tenant.note || '—'}</td><td><div className="table-actions"><button className="text-button" onClick={() => setFormTenant(tenant)} type="button">Sửa</button><button className="danger-button" onClick={() => void removeTenant(tenant)} type="button">Xóa</button></div></td></tr>)}</tbody></table></div></section>
      )}
      {formTenant !== undefined && <TenantForm propertyId={propertyId} tenant={formTenant} onClose={() => setFormTenant(undefined)} onSaved={async (saved) => { setFormTenant(undefined); setMessage(saved); await loadTenants() }} />}
      {detailTenant && <TenantDetail propertyId={propertyId} tenant={detailTenant} onClose={() => setDetailTenant(null)} />}
    </>
  )
}

function TenantForm({ propertyId, tenant, onClose, onSaved }: { propertyId: string; tenant: TenantResponse | null; onClose: () => void; onSaved: (message: string) => Promise<void> }) {
  const [form, setForm] = useState<TenantRequest>(() => tenant ? { fullName: tenant.fullName, phone: tenant.phone, idNumber: tenant.idNumber, note: tenant.note } : { fullName: '', phone: '', idNumber: '', note: '' })
  const [error, setError] = useState(''); const [isSubmitting, setIsSubmitting] = useState(false)
  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!form.fullName.trim()) { setError('Họ tên khách thuê là bắt buộc.'); return }
    setError(''); setIsSubmitting(true)
    try { const body = { ...form, fullName: form.fullName.trim(), phone: form.phone?.trim() || undefined, idNumber: form.idNumber?.trim() || undefined, note: form.note?.trim() || undefined }; if (tenant) await tenantsApi.update(propertyId, tenant.id, body); else await tenantsApi.create(propertyId, body); await onSaved(tenant ? 'Đã cập nhật khách thuê.' : 'Đã tạo khách thuê.') } catch (reason) { setError(getTenantActionError(reason)) } finally { setIsSubmitting(false) }
  }
  return <div className="modal-backdrop"><section aria-labelledby="tenant-form-title" className="modal-card"><div className="modal-heading"><div><span className="eyebrow">Khách thuê</span><h2 id="tenant-form-title">{tenant ? 'Cập nhật khách thuê' : 'Thêm khách thuê'}</h2></div><button className="icon-button" onClick={onClose} type="button"><Icon name="x" /></button></div>{error && <div className="form-message form-message--error" role="alert">{error}</div>}<form className="room-form" onSubmit={submit}><label>Họ tên<input required onChange={(event) => setForm({ ...form, fullName: event.target.value })} value={form.fullName} /></label><label>Số điện thoại<input onChange={(event) => setForm({ ...form, phone: event.target.value })} value={form.phone ?? ''} /></label><label>Số định danh<input autoComplete="off" onChange={(event) => setForm({ ...form, idNumber: event.target.value })} value={form.idNumber ?? ''} /></label><label>Ghi chú<textarea onChange={(event) => setForm({ ...form, note: event.target.value })} value={form.note ?? ''} /></label><div className="modal-actions"><button className="button" onClick={onClose} type="button">Hủy</button><button className="button button--primary" disabled={isSubmitting} type="submit">{isSubmitting ? 'Đang lưu...' : 'Lưu khách thuê'}</button></div></form></section></div>
}

function TenantDetail({ propertyId, tenant, onClose }: { propertyId: string; tenant: TenantResponse; onClose: () => void }) {
  const [detail, setDetail] = useState(tenant); const [error, setError] = useState('')
  useEffect(() => { void tenantsApi.get(propertyId, tenant.id).then(setDetail).catch((reason) => setError(getTenantActionError(reason))) }, [propertyId, tenant.id])
  return <div className="modal-backdrop"><section aria-labelledby="tenant-detail-title" className="modal-card"><div className="modal-heading"><div><span className="eyebrow">Chi tiết khách thuê</span><h2 id="tenant-detail-title">{detail.fullName}</h2></div><button className="icon-button" onClick={onClose} type="button"><Icon name="x" /></button></div>{error && <div className="form-message form-message--error" role="alert">{error}</div>}<dl className="detail-list"><div><dt>Số điện thoại</dt><dd>{detail.phone || '—'}</dd></div><div><dt>Số định danh</dt><dd>{maskIdNumber(detail.idNumber)}</dd></div><div><dt>Ghi chú</dt><dd>{detail.note || '—'}</dd></div></dl></section></div>
}

function getTenantActionError(reason: unknown) {
  if (!(reason instanceof ApiClientError)) return 'Không thể thực hiện thao tác với khách thuê.'
  if (reason.kind === 'validation') return 'Họ tên khách thuê không được để trống.'
  if (reason.kind === 'forbidden') return 'Bạn không có quyền thực hiện thao tác này.'
  if (reason.kind === 'not-found') return 'Không tìm thấy khách thuê.'
  if (reason.message.includes('TENANT_HAS_ACTIVE_CONTRACT')) return 'Không thể xóa khách thuê vì đang có hợp đồng ACTIVE.'
  return reason.message || 'Không thể thực hiện thao tác với khách thuê.'
}

function maskIdNumber(value?: string) {
  if (!value) return '—'
  return value.length <= 4 ? '****' : `${'*'.repeat(Math.max(4, value.length - 4))}${value.slice(-4)}`
}

function ContractsPage() {
  const { propertyId = '' } = useParams()
  const [contracts, setContracts] = useState<ContractResponse[]>([]); const [status, setStatus] = useState<ContractStatus | ''>('')
  const [rooms, setRooms] = useState<RoomResponse[]>([]); const [tenants, setTenants] = useState<TenantResponse[]>([])
  const [isLoading, setIsLoading] = useState(true); const [error, setError] = useState<ApiClientError | null>(null); const [message, setMessage] = useState<string | null>(null); const [formOpen, setFormOpen] = useState(false)
  const loadContracts = useCallback(async () => { setIsLoading(true); setError(null); try { setContracts(await contractsApi.list(propertyId, status || undefined)) } catch (reason) { setError(reason instanceof ApiClientError ? reason : new ApiClientError('Không thể tải danh sách hợp đồng.', 'unknown')) } finally { setIsLoading(false) } }, [propertyId, status])
  useEffect(() => { void loadContracts() }, [loadContracts])
  const openForm = async () => { try { const [availableRooms, propertyTenants] = await Promise.all([roomsApi.list(propertyId, 'AVAILABLE'), tenantsApi.list(propertyId)]); setRooms(availableRooms); setTenants(propertyTenants); setFormOpen(true) } catch (reason) { setMessage(getContractActionError(reason, 'Không thể tải dữ liệu để tạo hợp đồng.')) } }
  const terminate = async (contract: ContractResponse) => { if (!window.confirm(`Bạn có chắc muốn kết thúc hợp đồng của ${contract.tenantName}?`)) return; try { await contractsApi.terminate(propertyId, contract.id); setMessage('Đã kết thúc hợp đồng và cập nhật trạng thái phòng.'); await loadContracts() } catch (reason) { setMessage(getContractActionError(reason, 'Không thể kết thúc hợp đồng.')) } }
  return <>
    <PageIntro eyebrow="Quản lý" title="Hợp đồng" description="Theo dõi hợp đồng thuê và trạng thái hiệu lực của nhà trọ." />
    {message && <div className="form-message form-message--info" role="status"><span>{message}</span><button onClick={() => setMessage(null)} type="button">Đóng</button></div>}
    <div className="resource-toolbar"><select aria-label="Lọc trạng thái hợp đồng" className="toolbar-select" onChange={(event) => setStatus(event.target.value as ContractStatus | '')} value={status}><option value="">Tất cả trạng thái</option><option value="ACTIVE">Đang hiệu lực</option><option value="ENDED">Đã kết thúc</option></select><button className="button button--primary" onClick={() => void openForm()} type="button">Tạo hợp đồng</button></div>
    {isLoading ? <PageLoading label="Đang tải danh sách hợp đồng..." /> : error ? <section className="state-card"><div className="state-icon"><Icon name="file" size={24} /></div><h2>Không thể tải danh sách hợp đồng</h2><p>{error.kind === 'network' ? 'Kiểm tra kết nối đến máy chủ API.' : error.message}</p><button className="button button--primary" onClick={() => void loadContracts()} type="button">Thử lại</button></section> : contracts.length === 0 ? <EmptyState title="Chưa có hợp đồng" description="Không có hợp đồng phù hợp với bộ lọc hiện tại." /> : <section className="room-table-card"><div className="room-table-wrap"><table className="room-table"><thead><tr><th>Phòng</th><th>Khách thuê</th><th>Thời hạn</th><th>Tiền thuê</th><th>Tiền cọc</th><th>Trạng thái</th><th><span className="sr-only">Thao tác</span></th></tr></thead><tbody>{contracts.map((contract) => <tr key={contract.id}><td>{contract.roomCode}</td><td>{contract.tenantName}</td><td>{contract.startDate} – {contract.endDate || 'Không thời hạn'}</td><td>{formatCurrency(contract.monthlyRent)}</td><td>{formatCurrency(contract.deposit)}</td><td><span className={`status-pill status-pill--${contract.status.toLowerCase()}`}>{contract.status === 'ACTIVE' ? 'Đang hiệu lực' : 'Đã kết thúc'}</span></td><td>{contract.status === 'ACTIVE' && <button className="danger-button" onClick={() => void terminate(contract)} type="button">Kết thúc</button>}</td></tr>)}</tbody></table></div></section>}
    {formOpen && <ContractForm propertyId={propertyId} rooms={rooms} tenants={tenants} onClose={() => setFormOpen(false)} onSaved={async () => { setFormOpen(false); setMessage('Đã tạo hợp đồng và cập nhật trạng thái phòng.'); await loadContracts() }} />}
  </>
}

function ContractForm({ propertyId, rooms, tenants, onClose, onSaved }: { propertyId: string; rooms: RoomResponse[]; tenants: TenantResponse[]; onClose: () => void; onSaved: () => Promise<void> }) {
  const [form, setForm] = useState<ContractCreationRequest>({ roomId: '', tenantId: '', startDate: '', endDate: '', monthlyRent: undefined, deposit: undefined, note: '' })
  const [error, setError] = useState(''); const [isSubmitting, setIsSubmitting] = useState(false)
  const selectedRoom = rooms.find((room) => room.id === form.roomId)
  const submit = async (event: FormEvent<HTMLFormElement>) => { event.preventDefault(); if (!form.roomId || !form.tenantId || !form.startDate) { setError('Vui lòng chọn phòng, khách thuê và ngày bắt đầu.'); return } if (form.endDate && form.endDate <= form.startDate) { setError('Ngày kết thúc phải sau ngày bắt đầu.'); return } setError(''); setIsSubmitting(true); try { await contractsApi.create(propertyId, { ...form, endDate: form.endDate || undefined, monthlyRent: form.monthlyRent === undefined ? undefined : Number(form.monthlyRent), deposit: form.deposit === undefined ? undefined : Number(form.deposit), note: form.note?.trim() || undefined }); await onSaved() } catch (reason) { setError(getContractActionError(reason, 'Không thể tạo hợp đồng.')) } finally { setIsSubmitting(false) } }
  return <div className="modal-backdrop"><section aria-labelledby="contract-form-title" className="modal-card"><div className="modal-heading"><div><span className="eyebrow">Hợp đồng</span><h2 id="contract-form-title">Tạo hợp đồng</h2></div><button className="icon-button" onClick={onClose} type="button"><Icon name="x" /></button></div>{error && <div className="form-message form-message--error" role="alert">{error}</div>}{rooms.length === 0 || tenants.length === 0 ? <div className="state-card"><h2>Chưa đủ dữ liệu để tạo hợp đồng</h2><p>{rooms.length === 0 ? 'Không có phòng AVAILABLE.' : 'Chưa có khách thuê.'}</p></div> : <form className="room-form" onSubmit={submit}><label>Phòng AVAILABLE<select required onChange={(event) => setForm({ ...form, roomId: event.target.value, monthlyRent: rooms.find((room) => room.id === event.target.value)?.price })} value={form.roomId}><option value="">Chọn phòng</option>{rooms.map((room) => <option key={room.id} value={room.id}>{room.code} — {formatCurrency(room.price)}</option>)}</select></label><label>Khách thuê<select required onChange={(event) => setForm({ ...form, tenantId: event.target.value })} value={form.tenantId}><option value="">Chọn khách thuê</option>{tenants.map((tenant) => <option key={tenant.id} value={tenant.id}>{tenant.fullName}</option>)}</select></label><div className="form-grid"><label>Ngày bắt đầu<input required onChange={(event) => setForm({ ...form, startDate: event.target.value })} type="date" value={form.startDate} /></label><label>Ngày kết thúc<input onChange={(event) => setForm({ ...form, endDate: event.target.value })} type="date" value={form.endDate ?? ''} /></label><label>Tiền thuê/tháng<input min="0" onChange={(event) => setForm({ ...form, monthlyRent: event.target.value ? Number(event.target.value) : undefined })} type="number" value={form.monthlyRent ?? selectedRoom?.price ?? ''} /></label><label>Tiền cọc<input min="0" onChange={(event) => setForm({ ...form, deposit: event.target.value ? Number(event.target.value) : undefined })} type="number" value={form.deposit ?? ''} /></label></div><label>Ghi chú<textarea onChange={(event) => setForm({ ...form, note: event.target.value })} value={form.note ?? ''} /></label><div className="modal-actions"><button className="button" onClick={onClose} type="button">Hủy</button><button className="button button--primary" disabled={isSubmitting} type="submit">{isSubmitting ? 'Đang tạo...' : 'Tạo hợp đồng'}</button></div></form>}</section></div>
}

function getContractActionError(reason: unknown, fallback: string) {
  if (!(reason instanceof ApiClientError)) return fallback
  if (reason.kind === 'validation') return 'Dữ liệu hợp đồng không hợp lệ. Kiểm tra phòng, khách thuê và số tiền.'
  if (reason.kind === 'forbidden') return 'Bạn không có quyền thực hiện thao tác này.'
  if (reason.kind === 'not-found') return 'Không tìm thấy phòng, khách thuê hoặc hợp đồng.'
  if (reason.message.includes('ROOM_NOT_AVAILABLE')) return 'Phòng không còn AVAILABLE. Hãy tải lại và chọn phòng khác.'
  if (reason.message.includes('CONTRACT_DATE_INVALID')) return 'Ngày kết thúc phải sau ngày bắt đầu.'
  if (reason.message.includes('CONTRACT_NOT_ACTIVE')) return 'Hợp đồng này không còn ACTIVE.'
  return reason.message || fallback
}

const serviceTypeLabels: Record<ServiceType, string> = { ELECTRIC: 'Điện', WATER: 'Nước', FIXED: 'Cố định', PER_PERSON: 'Theo người' }

function ServicePage() {
  const { propertyId = '' } = useParams()
  const { findById } = useProperties()
  const property = findById(propertyId)
  const canMutate = property?.myRole === 'OWNER'
  const [services, setServices] = useState<ServicePriceResponse[]>([]); const [isLoading, setIsLoading] = useState(true); const [error, setError] = useState<ApiClientError | null>(null); const [message, setMessage] = useState<string | null>(null); const [formService, setFormService] = useState<ServicePriceResponse | null | undefined>(undefined)
  const loadServices = useCallback(async () => { setIsLoading(true); setError(null); try { setServices(await servicePricesApi.list(propertyId)) } catch (reason) { setError(reason instanceof ApiClientError ? reason : new ApiClientError('Không thể tải danh sách giá dịch vụ.', 'unknown')) } finally { setIsLoading(false) } }, [propertyId])
  useEffect(() => { void loadServices() }, [loadServices])
  const remove = async (service: ServicePriceResponse) => { if (!window.confirm(`Bạn có chắc muốn xóa dịch vụ ${service.name}?`)) return; try { await servicePricesApi.remove(propertyId, service.id); setMessage(`Đã xóa dịch vụ ${service.name}.`); await loadServices() } catch (reason) { setMessage(getServiceActionError(reason)) } }
  return <>
    <PageIntro eyebrow="Quản lý" title="Giá dịch vụ" description="Thiết lập các loại dịch vụ và đơn giá áp dụng cho nhà trọ." />
    {message && <div className="form-message form-message--info" role="status"><span>{message}</span><button onClick={() => setMessage(null)} type="button">Đóng</button></div>}
    {!canMutate && <div className="permission-alert" role="status">Bạn đang xem với quyền MANAGER. Chỉ OWNER được tạo, sửa hoặc xóa giá dịch vụ.</div>}
    <div className="resource-toolbar">{canMutate && <button className="button button--primary" onClick={() => setFormService(null)} type="button">Thêm giá dịch vụ</button>}</div>
    {isLoading ? <PageLoading label="Đang tải giá dịch vụ..." /> : error ? <section className="state-card"><div className="state-icon"><Icon name="clipboard" size={24} /></div><h2>Không thể tải giá dịch vụ</h2><p>{error.kind === 'network' ? 'Kiểm tra kết nối đến máy chủ API.' : error.message}</p><button className="button button--primary" onClick={() => void loadServices()} type="button">Thử lại</button></section> : services.length === 0 ? <EmptyState title="Chưa có giá dịch vụ" description="Backend chưa trả về dịch vụ nào cho nhà trọ này." /> : <section className="room-table-card"><div className="room-table-wrap"><table className="room-table"><thead><tr><th>Tên dịch vụ</th><th>Loại</th><th>Đơn vị</th><th>Đơn giá</th><th>Trạng thái</th>{canMutate && <th><span className="sr-only">Thao tác</span></th>}</tr></thead><tbody>{services.map((service) => <tr key={service.id}><td>{service.name}</td><td>{serviceTypeLabels[service.type]}</td><td>{service.unit}</td><td>{formatCurrency(service.unitPrice)}</td><td><span className={`status-pill ${service.active ? 'status-pill--available' : 'status-pill--maintenance'}`}>{service.active ? 'Đang áp dụng' : 'Tạm ngưng'}</span></td>{canMutate && <td><div className="table-actions"><button className="text-button" onClick={() => setFormService(service)} type="button">Sửa</button><button className="danger-button" onClick={() => void remove(service)} type="button">Xóa</button></div></td>}</tr>)}</tbody></table></div></section>}
    {formService !== undefined && <ServiceForm propertyId={propertyId} service={formService} onClose={() => setFormService(undefined)} onSaved={async (saved) => { setFormService(undefined); setMessage(saved); await loadServices() }} />}
  </>
}

function ServiceForm({ propertyId, service, onClose, onSaved }: { propertyId: string; service: ServicePriceResponse | null; onClose: () => void; onSaved: (message: string) => Promise<void> }) {
  const [form, setForm] = useState<ServicePriceRequest>(() => service ? { name: service.name, type: service.type, unit: service.unit, unitPrice: service.unitPrice, active: service.active } : { name: '', type: 'FIXED', unit: '', unitPrice: 0, active: true })
  const [error, setError] = useState(''); const [isSubmitting, setIsSubmitting] = useState(false)
  const submit = async (event: FormEvent<HTMLFormElement>) => { event.preventDefault(); if (!form.name.trim() || !form.unit.trim() || form.unitPrice < 0) { setError('Vui lòng nhập tên, đơn vị và đơn giá hợp lệ.'); return } setError(''); setIsSubmitting(true); try { const body = { ...form, name: form.name.trim(), unit: form.unit.trim() }; if (service) await servicePricesApi.update(propertyId, service.id, body); else await servicePricesApi.create(propertyId, body); await onSaved(service ? 'Đã cập nhật giá dịch vụ.' : 'Đã tạo giá dịch vụ.') } catch (reason) { setError(getServiceActionError(reason)) } finally { setIsSubmitting(false) } }
  return <div className="modal-backdrop"><section aria-labelledby="service-form-title" className="modal-card"><div className="modal-heading"><div><span className="eyebrow">Giá dịch vụ</span><h2 id="service-form-title">{service ? 'Cập nhật giá dịch vụ' : 'Thêm giá dịch vụ'}</h2></div><button className="icon-button" onClick={onClose} type="button"><Icon name="x" /></button></div>{error && <div className="form-message form-message--error" role="alert">{error}</div>}<form className="room-form" onSubmit={submit}><label>Tên dịch vụ<input required onChange={(event) => setForm({ ...form, name: event.target.value })} value={form.name} /></label><div className="form-grid"><label>Loại<select onChange={(event) => setForm({ ...form, type: event.target.value as ServiceType })} value={form.type}>{(Object.keys(serviceTypeLabels) as ServiceType[]).map((type) => <option key={type} value={type}>{serviceTypeLabels[type]}</option>)}</select></label><label>Đơn vị<input required onChange={(event) => setForm({ ...form, unit: event.target.value })} value={form.unit} /></label><label>Đơn giá<input min="0" required onChange={(event) => setForm({ ...form, unitPrice: Number(event.target.value) })} type="number" value={form.unitPrice} /></label><label className="checkbox-field"><span>Đang áp dụng</span><input checked={form.active ?? false} onChange={(event) => setForm({ ...form, active: event.target.checked })} type="checkbox" /></label></div><div className="modal-actions"><button className="button" onClick={onClose} type="button">Hủy</button><button className="button button--primary" disabled={isSubmitting} type="submit">{isSubmitting ? 'Đang lưu...' : 'Lưu dịch vụ'}</button></div></form></section></div>
}

function getServiceActionError(reason: unknown) {
  if (!(reason instanceof ApiClientError)) return 'Không thể thực hiện thao tác giá dịch vụ.'
  if (reason.kind === 'validation') return 'Tên, loại, đơn vị và đơn giá phải hợp lệ.'
  if (reason.kind === 'forbidden') return 'Chỉ OWNER mới có quyền thay đổi giá dịch vụ.'
  if (reason.kind === 'not-found') return 'Không tìm thấy dịch vụ.'
  return reason.message || 'Không thể thực hiện thao tác giá dịch vụ.'
}

function MembersPage() {
  const { propertyId = '' } = useParams()
  const { findById } = useProperties()
  const property = findById(propertyId); const canMutate = property?.myRole === 'OWNER'
  const [members, setMembers] = useState<MemberResponse[]>([]); const [isLoading, setIsLoading] = useState(true); const [error, setError] = useState<ApiClientError | null>(null); const [message, setMessage] = useState<string | null>(null); const [formOpen, setFormOpen] = useState(false)
  const loadMembers = useCallback(async () => { setIsLoading(true); setError(null); try { setMembers(await propertiesApi.listMembers(propertyId)) } catch (reason) { setError(reason instanceof ApiClientError ? reason : new ApiClientError('Không thể tải danh sách thành viên.', 'unknown')) } finally { setIsLoading(false) } }, [propertyId])
  useEffect(() => { void loadMembers() }, [loadMembers])
  const remove = async (member: MemberResponse) => { if (member.role === 'OWNER') { setMessage('Không thể xóa OWNER.'); return } if (!window.confirm(`Bạn có chắc muốn xóa ${member.fullName || member.username}?`)) return; try { await propertiesApi.removeMember(propertyId, member.userId); setMessage('Đã xóa thành viên.'); await loadMembers() } catch (reason) { setMessage(getMemberActionError(reason)) } }
  return <>
    <PageIntro eyebrow="Quản lý" title="Thành viên" description="Quản lý thành viên trong property đang chọn." />
    {message && <div className="form-message form-message--info" role="status"><span>{message}</span><button onClick={() => setMessage(null)} type="button">Đóng</button></div>}
    {!canMutate && <div className="permission-alert" role="status">Bạn đang xem với quyền MANAGER. Chỉ OWNER được thêm hoặc xóa Manager.</div>}
    <div className="resource-toolbar">{canMutate && <button className="button button--primary" onClick={() => setFormOpen(true)} type="button">Thêm Manager</button>}</div>
    {isLoading ? <PageLoading label="Đang tải danh sách thành viên..." /> : error ? <section className="state-card"><div className="state-icon"><Icon name="users" size={24} /></div><h2>Không thể tải thành viên</h2><p>{error.kind === 'network' ? 'Kiểm tra kết nối đến máy chủ API.' : error.message}</p><button className="button button--primary" onClick={() => void loadMembers()} type="button">Thử lại</button></section> : members.length === 0 ? <EmptyState title="Chưa có thành viên" description="Backend chưa trả về thành viên nào cho property này." /> : <section className="room-table-card"><div className="room-table-wrap"><table className="room-table"><thead><tr><th>Họ tên</th><th>Tên đăng nhập</th><th>Vai trò</th>{canMutate && <th><span className="sr-only">Thao tác</span></th>}</tr></thead><tbody>{members.map((member) => <tr key={member.userId}><td>{member.fullName}</td><td>{member.username}</td><td><span className="status-pill status-pill--available">{member.role}</span></td>{canMutate && <td>{member.role === 'MANAGER' && <button className="danger-button" onClick={() => void remove(member)} type="button">Xóa</button>}</td>}</tr>)}</tbody></table></div></section>}
    {formOpen && <MemberForm propertyId={propertyId} onClose={() => setFormOpen(false)} onSaved={async () => { setFormOpen(false); setMessage('Đã thêm Manager.'); await loadMembers() }} />}
  </>
}

function MemberForm({ propertyId, onClose, onSaved }: { propertyId: string; onClose: () => void; onSaved: () => Promise<void> }) {
  const [username, setUsername] = useState(''); const [error, setError] = useState(''); const [isSubmitting, setIsSubmitting] = useState(false)
  const submit = async (event: FormEvent<HTMLFormElement>) => { event.preventDefault(); if (!username.trim()) { setError('Tên đăng nhập là bắt buộc.'); return } setError(''); setIsSubmitting(true); try { const body: MemberRequest = { username: username.trim(), role: 'MANAGER' }; await propertiesApi.addMember(propertyId, body); await onSaved() } catch (reason) { setError(getMemberActionError(reason)) } finally { setIsSubmitting(false) } }
  return <div className="modal-backdrop"><section aria-labelledby="member-form-title" className="modal-card"><div className="modal-heading"><div><span className="eyebrow">Thành viên</span><h2 id="member-form-title">Thêm Manager</h2></div><button className="icon-button" onClick={onClose} type="button"><Icon name="x" /></button></div>{error && <div className="form-message form-message--error" role="alert">{error}</div>}<form className="room-form" onSubmit={submit}><label>Tên đăng nhập<input autoComplete="off" required onChange={(event) => setUsername(event.target.value)} value={username} /></label><div className="modal-actions"><button className="button" onClick={onClose} type="button">Hủy</button><button className="button button--primary" disabled={isSubmitting} type="submit">{isSubmitting ? 'Đang thêm...' : 'Thêm Manager'}</button></div></form></section></div>
}

function getMemberActionError(reason: unknown) {
  if (!(reason instanceof ApiClientError)) return 'Không thể thực hiện thao tác thành viên.'
  if (reason.kind === 'validation') return 'Tên đăng nhập không được để trống.'
  if (reason.kind === 'forbidden') return 'Chỉ OWNER mới có quyền quản lý thành viên.'
  if (reason.kind === 'not-found') return 'Không tìm thấy người dùng hoặc thành viên.'
  return reason.message || 'Không thể thực hiện thao tác thành viên.'
}

function SettingsPage() {
  return (
    <>
      <PageIntro eyebrow="Hệ thống" title="Tài khoản & cài đặt" description="Quản lý thông tin tài khoản và tùy chọn dashboard." />
      <EmptyState title="Cài đặt đang được chuẩn bị" description="Thông tin tài khoản và tùy chọn cá nhân sẽ được bổ sung sau khi hoàn thiện authentication." />
    </>
  )
}

function PropertySelectionPage() {
  const navigate = useNavigate()
  const { properties, isLoading, error, reload } = useProperties()
  return (
    <>
      <PageIntro eyebrow="Bắt đầu" title="Chọn nhà trọ" description="Chọn một nhà trọ để bắt đầu quản lý nghiệp vụ." />
      {isLoading ? <PageLoading label="Đang tải danh sách nhà trọ..." /> : error ? (
        <section className="state-card"><div className="state-icon"><Icon name="building" size={24} /></div><h2>{error.kind === 'forbidden' ? 'Không có quyền xem danh sách' : 'Không thể tải danh sách nhà trọ'}</h2><p>{error.kind === 'network' ? 'Kiểm tra kết nối đến máy chủ API và thử lại.' : error.message}</p><button className="button button--primary" onClick={() => void reload()} type="button">Thử lại</button></section>
      ) : properties.length === 0 ? (
        <EmptyState title="Chưa có nhà trọ được cấp quyền" description="Backend chưa trả về nhà trọ nào mà tài khoản hiện tại có membership." />
      ) : (
        <section className="property-list">{properties.map((property) => <button className="property-card" key={property.id} onClick={() => navigate(`/app/${property.id}/overview`)} type="button"><span className="property-avatar"><Icon name="building" size={20} /></span><span><strong>{property.name}</strong><small>{property.address ?? 'Chưa có địa chỉ'} · {property.myRole}</small></span><Icon name="chevronLeft" size={18} /></button>)}</section>
      )}
    </>
  )
}

function LoginPage() {
  const { user, isSubmitting, error, login, clearError } = useSession()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [validationError, setValidationError] = useState('')
  const location = useLocation()

  if (user) return <Navigate replace to="/app" />

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    clearError()
    const normalizedUsername = username.trim()
    if (!normalizedUsername || !password) {
      setValidationError('Vui lòng nhập tên đăng nhập và mật khẩu.')
      return
    }
    setValidationError('')
    try {
      await login({ username: normalizedUsername, password })
    } catch {
      // SessionContext exposes the normalized error for the form.
    }
  }

  const errorMessage = validationError
    || (error?.kind === 'unauthenticated' ? 'Tên đăng nhập hoặc mật khẩu không đúng.' : '')
    || (error?.kind === 'network' ? 'Không thể kết nối đến máy chủ. Vui lòng thử lại.' : '')
    || (error?.kind === 'server' ? 'Máy chủ đang gặp sự cố. Vui lòng thử lại sau.' : '')
    || (error ? 'Không thể đăng nhập. Vui lòng kiểm tra lại thông tin.' : '')
  const sessionMessage = (location.state as { reason?: string } | null)?.reason === 'session-expired'

  return (
    <main className="auth-page">
      <section className="auth-panel">
        <div className="brand brand--auth"><div className="brand-mark">T</div><div><strong>TroHub</strong><span>Quản lý nhà trọ</span></div></div>
        <span className="eyebrow">Khu vực quản lý</span>
        <h1>Đăng nhập vào dashboard</h1>
        <p className="auth-description">Đăng nhập để quản lý các nhà trọ mà bạn có quyền truy cập.</p>
        {sessionMessage && <div className="form-message form-message--info" role="status">Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.</div>}
        {errorMessage && <div className="form-message form-message--error" role="alert">{errorMessage}</div>}
        <form className="auth-form" onSubmit={submit} noValidate>
          <label htmlFor="username">Tên đăng nhập</label>
          <input autoComplete="username" disabled={isSubmitting} id="username" onChange={(event) => setUsername(event.target.value)} value={username} />
          <label htmlFor="password">Mật khẩu</label>
          <input autoComplete="current-password" disabled={isSubmitting} id="password" onChange={(event) => setPassword(event.target.value)} type="password" value={password} />
          <button className="button button--primary button--full" disabled={isSubmitting} type="submit">{isSubmitting ? 'Đang đăng nhập...' : 'Đăng nhập'}</button>
        </form>
      </section>
    </main>
  )
}

function NotFoundPage() {
  return <div className="not-found"><h1>Không tìm thấy trang</h1><p>Đường dẫn này chưa được cấu hình trong dashboard.</p></div>
}

function getPageTitle(pathname: string) {
  if (pathname === '/app') return 'Chọn nhà trọ'
  if (pathname.includes('/settings')) return 'Tài khoản & cài đặt'
  return navigation.find((item) => pathname.endsWith(`/${item.path}`))?.label ?? 'Dashboard'
}

function App() {
  return (
    <Routes>
      <Route element={<LoginPage />} path="/login" />
      <Route element={<ProtectedRoute />} path="/app">
        <Route element={<PropertyProvider />}>
          <Route element={<AppLayout />}>
        <Route element={<PropertySelectionPage />} index />
        <Route element={<SettingsPage />} path="settings" />
        <Route element={<OverviewPage />} path=":propertyId/overview" />
        <Route element={<RoomPage />} path=":propertyId/rooms" />
        <Route element={<TenantPage />} path=":propertyId/tenants" />
        <Route element={<ContractsPage />} path=":propertyId/contracts" />
        <Route element={<ServicePage />} path=":propertyId/services" />
        <Route element={<MembersPage />} path=":propertyId/members" />
          </Route>
        </Route>
      </Route>
      <Route element={<Navigate replace to="/login" />} path="/" />
      <Route element={<NotFoundPage />} path="*" />
    </Routes>
  )
}

export default App
