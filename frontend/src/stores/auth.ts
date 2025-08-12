import { defineStore } from "pinia";
import { loginApi, refreshApi } from '../api/auth'

interface User {
    id: string
    email: string
    name?: string
}

interface AuthState {
    token: string | null
    refreshToken: string | null
    user: User | null
}

export const useAuthStore = defineStore('auth', {
    state: (): AuthState => ({token: null, refreshToken: null, user: null}),
    getters: {
        isAuthenticated: (s) => !s.token
    },
    actions: {
        async login(email: string, password: string) {
            const {accessToken, refreshToken, user} = await loginApi(email, password)
            this.token = accessToken
            this.refreshToken = refreshToken
            this.user = user
        },
        async refresh() {
            if(!this.refreshToken) return

            const {accessToken} = await refreshApi(this.refreshToken)
            this.token = accessToken
        },
        logout() {
            this.token = null;
            this.refreshToken = null;
            this.user = null;
        }
    }
})