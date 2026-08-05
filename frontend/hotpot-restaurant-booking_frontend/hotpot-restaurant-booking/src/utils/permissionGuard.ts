import { computed } from 'vue'
import { useAuthStore } from '@/stores/AuthStore'
import { normalizePermissionValue } from '@/utils/rolePermissions'

export type PermissionState = 'none' | 'view' | 'full'

const normalizePermission = (permission: string | null | undefined) => normalizePermissionValue(permission)

const normalizeModule = (moduleKey: string | null | undefined) => normalizePermissionValue(moduleKey)

export const getModulePermissionState = (
  permissions: string[] | undefined,
  moduleKey: string,
): PermissionState => {
  const normalizedModule = normalizeModule(moduleKey)
  const normalizedPermissions = (Array.isArray(permissions) ? permissions : []).map(normalizePermission)

  if (!normalizedModule || !normalizedPermissions.length) return 'none'

  const hasBaseModule = normalizedPermissions.includes(normalizedModule)
  const hasActionPermission = normalizedPermissions.some(
    (permission) => permission.startsWith(`${normalizedModule}.`) && permission !== `${normalizedModule}.view`,
  )

  if (hasBaseModule || hasActionPermission) return 'full'
  if (normalizedPermissions.includes(`${normalizedModule}.view`)) return 'view'
  return 'none'
}

export const canAccessModule = (permissions: string[] | undefined, moduleKey: string) =>
  getModulePermissionState(permissions, moduleKey) !== 'none'

export const canUseModule = (permissions: string[] | undefined, moduleKey: string) =>
  getModulePermissionState(permissions, moduleKey) === 'full'

const DEFAULT_DENIED_MESSAGE = 'Tài khoản của bạn chỉ có quyền xem, không được phép thực hiện chức năng này'

const emitToast = (message: string) => {
  if (typeof window === 'undefined') return
  window.dispatchEvent(new CustomEvent('app:toast', { detail: message }))
}

export const showPermissionDeniedToast = (message = DEFAULT_DENIED_MESSAGE) => {
  emitToast(message)
}

export const useModulePermission = (moduleKey: string) => {
  const authStore = useAuthStore()

  const permissionState = computed(() => getModulePermissionState(authStore.permissions, moduleKey))
  const canAccess = computed(() => permissionState.value !== 'none')
  const canView = computed(() => permissionState.value === 'view' || permissionState.value === 'full')
  const canUse = computed(() => permissionState.value === 'full')

  const ensureUse = (message = DEFAULT_DENIED_MESSAGE) => {
    if (!canUse.value) {
      showPermissionDeniedToast(message)
      return false
    }
    return true
  }

  return {
    permissionState,
    canAccess,
    canView,
    canUse,
    ensureUse,
  }
}
