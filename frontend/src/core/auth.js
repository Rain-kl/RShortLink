import Cookies from 'js-cookie'

const TokenKey = 'token'
const RoleKey = 'role'

export function getToken() {
  const token = Cookies.get(TokenKey)
  // 处理各种假值情况
  if (!token || token === 'null' || token === 'undefined') {
    return null
  }
  return token
}

export function getUsername() {
  return Cookies.get('username')
}

export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

export function setUsername(username) {
  return Cookies.set('username', username)
}

export function getRole() {
  return Cookies.get(RoleKey)
}

export function setRole(role) {
  return Cookies.set(RoleKey, role)
}

export function removeKey() {
  return Cookies.remove(TokenKey)
}

export function removeUsername() {
  return Cookies.remove('username')
}

export function removeRole() {
  return Cookies.remove(RoleKey)
}


