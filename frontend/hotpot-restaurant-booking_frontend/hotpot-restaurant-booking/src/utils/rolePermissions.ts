export interface PermissionModule {
  key: string
  label: string
}

export const PERMISSION_MODULES: PermissionModule[] = [
  { key: 'menu', label: 'Quản lý thực đơn' },
  { key: 'invoice', label: 'Hóa đơn' },
  { key: 'pos', label: 'Bán hàng' },
  { key: 'discount', label: 'Quản lý giảm giá' },
  { key: 'table', label: 'Quản lý bàn' },
  { key: 'reservation', label: 'Quản lý đặt bàn' },
  { key: 'shift', label: 'Quản lý ca' },
  { key: 'account', label: 'Quản lý tài khoản' },
  { key: 'statistics', label: 'Thống kê' },
  { key: 'area', label: 'Quản lý khu vực' },
  { key: 'deposit', label: 'Quản lý tiền cọc' },
]

const STORAGE_KEY = 'role_permissions'

const loadStorage = (): Record<string, string[]> => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return {}
    return JSON.parse(raw) as Record<string, string[]>
  } catch {
    return {}
  }
}

const saveStorage = (data: Record<string, string[]>) => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
  } catch {
    // ignore storage failure
  }
}

export const getPermissionsForRole = (roleKey: string): string[] => {
  const storage = loadStorage()
  return Array.isArray(storage[roleKey]) ? storage[roleKey] : []
}

export const savePermissionsForRole = (roleKey: string, permissions: string[]) => {
  const storage = loadStorage()
  storage[roleKey] = Array.isArray(permissions) ? permissions : []
  saveStorage(storage)
}

export const deletePermissionsForRole = (roleKey: string) => {
  const storage = loadStorage()
  if (storage[roleKey]) {
    delete storage[roleKey]
    saveStorage(storage)
  }
}

export const generateRoleCode = (): string => {
  const stamp = Date.now().toString().slice(-6)
  const random = Math.floor(Math.random() * 900 + 100).toString()
  return `CV${stamp}${random}`
}
