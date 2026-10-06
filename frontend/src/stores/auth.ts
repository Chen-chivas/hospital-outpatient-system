import { defineStore } from 'pinia'
import { apiLogin, apiMe } from '../api/auth'
import { httpSetToken } from '../api/http'

export type Me = {
  userId: number
  username: string
  roles: string[]
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: (localStorage.getItem('token') as string | null) ?? '',
    me: null as Me | null,
    meLoaded: false,
  }),
  actions: {
    async login(username: string, password: string) {
      const resp = await apiLogin({ username, password })
      this.token = resp.token
      localStorage.setItem('token', resp.token)
      httpSetToken(resp.token)
      this.me = { userId: resp.userId, username: resp.username, roles: Array.from(resp.roles) }
      this.meLoaded = true
    },
    logout() {
      this.token = ''
      this.me = null
      this.meLoaded = false
      localStorage.removeItem('token')
      httpSetToken('')
    },
    async fetchMe() {
      if (!this.token) {
        this.me = null
        this.meLoaded = true
        return
      }
      httpSetToken(this.token)
      try {
        const me = await apiMe()
        this.me = { userId: me.userId, username: me.username, roles: Array.from(me.roles) }
        this.meLoaded = true
      } catch {
        this.logout()
      }
    },
    hasAnyRole(roles: string[]) {
      if (!this.me) return false
      const set = new Set(this.me.roles)
      return roles.some((r) => set.has(r))
    },
  },
})
