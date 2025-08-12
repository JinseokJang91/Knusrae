import { auth } from './client'

export async function loginApi(email: string, password: string) {
    const { data } = await auth.post('/login', { email, password })

    return data as { accessToken: string; refreshToken: string; user: { id: string; email: string; name?: string }}
}

export async function refreshApi(refreshToken: string) {
    const { data } = await auth.post('/refresh', { refreshToken })
    return data as { accessToken: string }
}