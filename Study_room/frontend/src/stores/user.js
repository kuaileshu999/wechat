import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

function loadStoredUserInfo() {
  try {
    const raw = localStorage.getItem('userInfo')
    if (!raw || raw === 'null' || raw === 'undefined') {
      return null
    }
    return JSON.parse(raw)
  } catch {
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(loadStoredUserInfo())

  const isLoggedIn = computed(() => !!token.value)
  const permissions = computed(() => userInfo.value?.permissions || [])
  const campusIds = computed(() => userInfo.value?.campusIds || [])
  const menus = computed(() => userInfo.value?.menus || [])

  function setLogin(data) {
    token.value = data.token
    userInfo.value = data
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  function hasPermission(code) {
    return permissions.value.includes(code)
  }

  return { token, userInfo, isLoggedIn, permissions, campusIds, menus, setLogin, logout, hasPermission }
})
