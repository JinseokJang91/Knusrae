/**
 * JSON 요청을 위한 헬퍼 (Content-Type: application/json)
 * HttpOnly 쿠키를 통해 인증이 처리되므로 Authorization 헤더는 사용하지 않음
 */
export async function httpJson<T = unknown>(baseUrl: string, url: string, options: RequestInit = {}): Promise<T> {
    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        ...(options.headers as Record<string, string> | undefined)
    };

    const response = await fetch(`${baseUrl}${url}`, {
        ...options,
        headers,
        credentials: 'include' // HttpOnly 쿠키 자동 전송으로 인증 처리
    });

    if (!response.ok) {
        const text = await response.text().catch(() => '');
        throw new Error(`HTTP ${response.status}: ${text || response.statusText}`);
    }

    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
        return response.json() as Promise<T>;
    }
    return response.text() as Promise<T>;
}

/**
 * 멀티파트/파일 업로드용 헬퍼 (FormData 사용 시 Content-Type은 브라우저가 설정)
 * HttpOnly 쿠키를 통해 인증이 처리되므로 Authorization 헤더는 사용하지 않음
 */
export async function httpForm<T = unknown>(baseUrl: string, url: string, formData: FormData, options: RequestInit = {}): Promise<T> {
    // 1) body 제외한 options (FormData로 body 대체)
    const opts = { ...options };
    delete (opts as Record<string, unknown>).body;

    // 2) headers 구성 (Content-Type 삭제 - multipart는 브라우저가 자동 설정)
    const headers: Record<string, string> = {
        ...(opts.headers as Record<string, string> | undefined)
    };
    if ('Content-Type' in headers) delete headers['Content-Type'];

    // 3) 최종 Request 생성 - 메서드, 바디, headers 세팅
    const reqInit: RequestInit = {
        ...opts,
        method: opts.method || 'POST',
        body: formData,
        headers,
        credentials: 'include' // 쿠키 자동 전송 (HttpOnly 쿠키 포함)
    };

    const response = await fetch(`${baseUrl}${url}`, reqInit);

    if (!response.ok) {
        const text = await response.text().catch(() => '');

        throw new Error(`HTTP ${response.status}: ${text || response.statusText}`);
    }

    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
        return response.json() as Promise<T>;
    }
    return response.text() as Promise<T>;
}

/** httpMultipart 반환: 레시피 생성/수정 등 multipart 응답 시 recipe + 이미지 파일 */
export interface HttpMultipartResult<T = unknown> {
    recipe: T | null;
    images: File[];
    mainImageIndex?: number;
}

/**
 * 멀티파트 응답을 처리하는 헬퍼 (이미지와 JSON 데이터를 함께 받을 때 사용)
 * HttpOnly 쿠키를 통해 인증이 처리되므로 Authorization 헤더는 사용하지 않음
 */
export async function httpMultipart<T = unknown>(baseUrl: string, url: string, options: RequestInit = {}): Promise<HttpMultipartResult<T>> {
    const headers: Record<string, string> = {
        ...(options.headers as Record<string, string> | undefined)
    };

    const response = await fetch(`${baseUrl}${url}`, {
        ...options,
        headers,
        credentials: 'include' // HttpOnly 쿠키 자동 전송으로 인증 처리
    });

    if (!response.ok) {
        const text = await response.text().catch(() => '');
        throw new Error(`HTTP ${response.status}: ${text || response.statusText}`);
    }

    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('multipart/form-data')) {
        // FormData로 파싱
        const formData = await response.formData();

        const recipeJson = formData.get('recipe') as string | null;
        const recipe: T | null = recipeJson ? (JSON.parse(recipeJson) as T) : null;

        const images: File[] = [];
        const imageEntries = formData.getAll('images');
        for (const entry of imageEntries) {
            if (entry instanceof File) {
                images.push(entry);
            }
        }

        const mainImageIndexStr = formData.get('mainImageIndex') as string;
        const mainImageIndex = mainImageIndexStr ? parseInt(mainImageIndexStr) : undefined;

        return { recipe, images, mainImageIndex };
    }

    // fallback: JSON 응답인 경우
    if (contentType.includes('application/json')) {
        const recipe = (await response.json()) as T | null;
        return { recipe, images: [], mainImageIndex: undefined };
    }

    throw new Error('Unsupported content type for multipart response');
}
