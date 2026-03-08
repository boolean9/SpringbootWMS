const USER_KEY = 'CurUser'
const MENU_KEY = 'CurMenu'

function safeParse(value, fallback) {
  if (!value) {
    return fallback
  }

  try {
    return JSON.parse(value)
  } catch (error) {
    return fallback
  }
}

function toHeaderValue(value) {
  if (value === null || value === undefined) {
    return ''
  }

  return String(value)
}

function toEncodedHeaderValue(value) {
  const text = toHeaderValue(value)
  return text ? encodeURIComponent(text) : ''
}

export function getCurrentUser() {
  return safeParse(sessionStorage.getItem(USER_KEY), null)
}

export function setCurrentUser(user) {
  sessionStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function getCurrentUserHeaders() {
  const currentUser = getCurrentUser()
  if (!currentUser) {
    return {}
  }

  return {
    'X-User-Id': toHeaderValue(currentUser.id),
    'X-User-Name': toEncodedHeaderValue(currentUser.name),
    'X-User-No': toHeaderValue(currentUser.no),
    'X-User-Role': toHeaderValue(currentUser.roleId),
  }
}

export function getMenuList() {
  return safeParse(sessionStorage.getItem(MENU_KEY), [])
}

export function setMenuList(menuList) {
  sessionStorage.setItem(MENU_KEY, JSON.stringify(menuList))
}

export function clearSessionState() {
  sessionStorage.removeItem(USER_KEY)
  sessionStorage.removeItem(MENU_KEY)
}
