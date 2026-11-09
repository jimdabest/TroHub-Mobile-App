import { useEffect } from 'react'
import { Outlet, useNavigate } from 'react-router-dom'
import { useSession } from './useSession'

export function ProtectedRoute() {
  const { isLoading, user } = useSession()
  if (isLoading) return <div className="route-loading" role="status">Đang kiểm tra phiên đăng nhập...</div>
  if (user) return <Outlet />
  return <NavigateToLogin />
}

function NavigateToLogin() {
  const navigate = useNavigate()
  useEffect(() => {
    navigate('/login', { replace: true })
  }, [navigate])
  return <div className="route-loading" role="status">Đang chuyển đến trang đăng nhập...</div>
}
