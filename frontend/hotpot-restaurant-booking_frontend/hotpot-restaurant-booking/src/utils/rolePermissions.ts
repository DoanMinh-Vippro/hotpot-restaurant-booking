export interface PermissionAction {
  key: string
  label: string
}

export interface PermissionModule {
  key: string
  label: string
  actions: PermissionAction[]
}

export const PERMISSION_MODULES: PermissionModule[] = [
  {
    key: 'menu',
    label: 'Quản lý thực đơn',
    actions: [
      { key: 'menu.view', label: 'Xem' },
      { key: 'menu.create', label: 'Thêm' },
      { key: 'menu.update', label: 'Sửa' },
      { key: 'menu.delete', label: 'Xóa' },
    ],
  },
  {
    key: 'invoice',
    label: 'Hóa đơn',
    actions: [
      { key: 'invoice.view', label: 'Xem' },
      { key: 'invoice.create', label: 'Thêm' },
      { key: 'invoice.update', label: 'Sửa' },
      { key: 'invoice.delete', label: 'Xóa' },
    ],
  },
  {
    key: 'discount',
    label: 'Quản lý giảm giá',
    actions: [
      { key: 'discount.view', label: 'Xem' },
      { key: 'discount.create', label: 'Thêm' },
      { key: 'discount.update', label: 'Sửa' },
      { key: 'discount.delete', label: 'Xóa' },
    ],
  },
  {
    key: 'shift',
    label: 'Quản lý ca',
    actions: [
      { key: 'shift.view', label: 'Xem' },
      { key: 'shift.create', label: 'Thêm' },
      { key: 'shift.update', label: 'Sửa' },
      { key: 'shift.delete', label: 'Xóa' },
    ],
  },
  {
    key: 'account',
    label: 'Quản lý tài khoản',
    actions: [
      { key: 'account.view', label: 'Xem' },
      { key: 'account.create', label: 'Thêm' },
      { key: 'account.update', label: 'Sửa' },
      { key: 'account.delete', label: 'Xóa' },
    ],
  },
  {
    key: 'message',
    label: 'Tin nhắn',
    actions: [
      { key: 'message.view', label: 'Xem' },
      { key: 'message.create', label: 'Thêm' },
      { key: 'message.update', label: 'Sửa' },
      { key: 'message.delete', label: 'Xóa' },
    ],
  },
  {
    key: 'statistics',
    label: 'Thống kê',
    actions: [
      { key: 'statistics.view', label: 'Xem' },
      { key: 'statistics.create', label: 'Thêm' },
      { key: 'statistics.update', label: 'Sửa' },
      { key: 'statistics.delete', label: 'Xóa' },
    ],
  },
  {
    key: 'area',
    label: 'Quản lý khu vực',
    actions: [
      { key: 'area.view', label: 'Xem' },
      { key: 'area.create', label: 'Thêm' },
      { key: 'area.update', label: 'Sửa' },
      { key: 'area.delete', label: 'Xóa' },
    ],
  },
  {
    key: 'deposit',
    label: 'Quản lý tiền cọc',
    actions: [
      { key: 'deposit.view', label: 'Xem' },
      { key: 'deposit.create', label: 'Thêm' },
      { key: 'deposit.update', label: 'Sửa' },
      { key: 'deposit.delete', label: 'Xóa' },
    ],
  },
  {
    key: 'order',
    label: 'Order',
    actions: [
      { key: 'order.view', label: 'Xem' },
      { key: 'order.create', label: 'Thêm' },
      { key: 'order.update', label: 'Sửa' },
      { key: 'order.delete', label: 'Xóa' },
    ],
  },
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

export const normalizePermissionValue = (permission: string | null | undefined): string =>
  (permission || '').trim().toLowerCase()

export const normalizePermissions = (permissions: string[] | undefined): string[] => {
  const safePermissions = (Array.isArray(permissions) ? permissions : [])
    .filter((value): value is string => typeof value === 'string' && value.trim() !== '')
    .map((permission) => normalizePermissionValue(permission))

  const detailPermissions = safePermissions.filter((permission) => permission.includes('.'))
  const baseModulePermissions = detailPermissions
    .map((permission) => permission.split('.')[0])
    .filter((permission): permission is string => Boolean(permission))

  return Array.from(new Set([...safePermissions, ...baseModulePermissions]))
}

export const getPermissionsForRole = (roleKey: string): string[] => {
  const storage = loadStorage()
  return normalizePermissions(Array.isArray(storage[roleKey]) ? storage[roleKey] : [])
}

export const savePermissionsForRole = (roleKey: string, permissions: string[]) => {
  const storage = loadStorage()
  storage[roleKey] = normalizePermissions(Array.isArray(permissions) ? permissions : [])
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
