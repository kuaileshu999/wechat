const STORAGE_KEY = 'wechat_hosting_user'

export function getUser() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function setUser(user) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(user))
}

export function clearUser() {
  localStorage.removeItem(STORAGE_KEY)
}

export function isLoggedIn() {
  return !!getUser()?.userId
}

export const ROLE = {
  ADMIN: 1,
  TUTOR: 2,
  TAKEOVER: 3,
}

export function roleLabel(role) {
  switch (role) {
    case ROLE.ADMIN:
      return '管理员'
    case ROLE.TUTOR:
      return '辅导老师'
    case ROLE.TAKEOVER:
      return '接管者'
    default:
      return '用户'
  }
}
