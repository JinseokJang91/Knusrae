/**
 * 공용 HTTP 클라이언트: accessToken을 자동으로 Authorization 헤더에 포함
 */

export interface HttpRequestOptions extends RequestInit {
    attachAuth?: boolean;
}

const getAccessToken = (): string | null => {
    try {
        return localStorage.getItem('accessToken');
    } catch {
        return null;
    }
};

/**
 * JSON 요청을 위한 헬퍼 (Content-Type: application/json)
 */
export async function httpJson(baseUrl: string, url: string, options: HttpRequestOptions = {}): Promise<any> {
    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        ...(options.headers as Record<string, string> | undefined)
    };

    if (options.attachAuth !== false) {
        const token = getAccessToken();
        if (token) headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${baseUrl}${url}`, {
        ...options,
        headers
    });

    if (!response.ok) {
        const text = await response.text().catch(() => '');
        throw new Error(`HTTP ${response.status}: ${text || response.statusText}`);
    }

    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
        return response.json();
    }
    return response.text();
}

/**
 * 멀티파트/파일 업로드용 헬퍼 (FormData 사용 시 Content-Type은 브라우저가 설정)
 */
export async function httpForm(baseUrl: string, url: string, formData: FormData, options: HttpRequestOptions = {}): Promise<any> {
    const headers: Record<string, string> = {
        ...(options.headers as Record<string, string> | undefined)
    };

    if (options.attachAuth !== false) {
        const token = getAccessToken();
        if (token) headers['Authorization'] = `Bearer ${token}`;
    }

    console.log('baseUrl', baseUrl);
    console.log('url', url);
    console.log('formData title', formData.get('title'));
    console.log('formData description', formData.get('description'));
    console.log('formData status', formData.get('status'));
    console.log('formData visibility', formData.get('visibility'));
    console.log('formData steps', formData.get('steps'));
    console.log('formData images', formData.get('images'));
    console.log('formData mainImageIndex', formData.get('mainImageIndex'));
    console.log('options', options);

    const response = await fetch(`${baseUrl}${url}`, {
        method: options.method || 'POST',
        body: formData,
        headers,
        ...options
    });

    if (!response.ok) {
        const text = await response.text().catch(() => '');
        throw new Error(`HTTP ${response.status}: ${text || response.statusText}`);
    }

    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
        return response.json();
    }
    return response.text();
}
