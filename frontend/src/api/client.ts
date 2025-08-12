import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const api = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL || '/api' })
const auth = axios.create({ baseURL: import.meta.env.VITE_AUTH_BASE_URL || '/auth' })

api.interceptors.request.use((config) => {
    const store = useAuthStore()
    if(store.token) {
        config.headers.Authorization = `Bearer ${store.token}`
        return config
    }
})

api.interceptors.response.use(
    (r) => r,
    async (error) => {
        const store = useAuthStore()
        if(error.response?.status === 401 && store.refreshToken) { // 응답코드가 401이면 토큰 갱신 시도
            try {
                const { data } = await auth.post('/refresh', { refreshToken: store.refreshToken })
                store.token = data.accessToken
                error.config.headers.Authorization = `Bearer ${data.accessToken}`
                return api.request(error.config)
            } catch(_) {
                store.logout()
            }
        }
        return Promise.reject(error)
    }
)

export { api, auth }