/**
 * 공용 HTTP 클라이언트: accessToken을 자동으로 Authorization 헤더에 포함
 */

export interface HttpRequestOptions extends RequestInit {
    attachAuth?: boolean;
}

const getAccessToken = (): string | null => {
    try {
        // localStorage에서 토큰 읽기
        // HttpOnly 쿠키는 JavaScript로 읽을 수 없으므로, 
        // API 요청 시 쿠키가 자동으로 전송되도록 credentials: 'include' 설정 필요
        // 또는 Authorization 헤더에 토큰을 넣어야 하는 경우 localStorage 사용
        return localStorage.getItem('accessToken');
    } catch {
        return null;
    }
};

// /**
//  * JWT 토큰을 디코딩하여 페이로드를 반환
//  */
// const decodeJWT = (token: string): any => {
//     try {
//         const base64Url = token.split('.')[1];
//         const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
//         const jsonPayload = decodeURIComponent(
//             atob(base64)
//                 .split('')
//                 .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
//                 .join('')
//         );
//         return JSON.parse(jsonPayload);
//     } catch (error) {
//         console.error('JWT 디코딩 오류:', error);
//         return null;
//     }
// };

// /**
//  * 토큰이 유효한지 확인 (만료 시간 체크)
//  */
// const isTokenValid = (token: string): boolean => {
//     try {
//         const decoded = decodeJWT(token);
//         if (!decoded || !decoded.exp) {
//             return false;
//         }
//         const currentTime = Math.floor(Date.now() / 1000);
//         const isValid = decoded.exp > currentTime;
//         console.log('🔐 토큰 유효성 검사:', {
//             currentTime,
//             expTime: decoded.exp,
//             isValid,
//             timeLeft: decoded.exp - currentTime
//         });
//         return isValid;
//     } catch {
//         return false;
//     }
// };

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
        headers,
        credentials: 'include' // 쿠키 자동 전송 (HttpOnly 쿠키 포함)
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
    // 1) options 세팅
    const reqInit: RequestInit = { ...options };

    // 2) 외부에서 들어온 body는 사용 금지 (FormData 사용)
    if ('body' in reqInit) delete (reqInit as any).body;

    // 3) headers 구성 (Content-Type 삭제)
    const headers: Record<string, string> = {
        ...(reqInit.headers as Record<string, string> | undefined)
    };
    if ('Content-Type' in headers) delete headers['Content-Type'];

    // 4) 인증 토큰 추가 및 디버깅
    if (options.attachAuth !== false) {
        const token = getAccessToken();
        if (token) headers['Authorization'] = `Bearer ${token}`;
    }
    //     console.log('🔐 httpForm - Access Token:', token ? '토큰 존재' : '토큰 없음');
    //     if (token) {
    //         // 토큰 유효성 검사
    //         if (!isTokenValid(token)) {
    //             console.error('🚨 httpForm - 토큰이 만료되었습니다.');
    //             localStorage.removeItem('accessToken');
    //             localStorage.removeItem('refreshToken');
    //             throw new Error('토큰이 만료되었습니다. 다시 로그인해주세요.');
    //         }
    //         headers['Authorization'] = `Bearer ${token}`;
    //         console.log('🔐 httpForm - Authorization 헤더 추가됨');
    //     } else {
    //         console.warn('⚠️ httpForm - 토큰이 없어 인증 헤더를 추가할 수 없습니다.');
    //     }
    // } else {
    //     console.log('🔐 httpForm - 인증 비활성화됨');
    // }

    // console.log('baseUrl', baseUrl);
    // console.log('url', url);
    // console.log('formData title', formData.get('title'));
    // console.log('formData description', formData.get('description'));
    // console.log('formData status', formData.get('status'));
    // console.log('formData visibility', formData.get('visibility'));
    // console.log('formData steps', formData.get('steps'));
    // console.log('formData images', formData.get('images'));
    // console.log('formData mainImageIndex', formData.get('mainImageIndex'));
    // console.log('options', options);

    // 5) 최종 Request 생성 - 메서드, 바디, headers 세팅
    reqInit.method = reqInit.method || 'POST';
    reqInit.body = formData;
    reqInit.headers = headers;
    reqInit.credentials = 'include'; // 쿠키 자동 전송 (HttpOnly 쿠키 포함)

    const response = await fetch(`${baseUrl}${url}`, reqInit);

    // const response = await fetch(`${baseUrl}${url}`, {
    //     method: options.method || 'POST',
    //     body: formData,
    //     headers,
    //     ...options
    // });

    if (!response.ok) {
        const text = await response.text().catch(() => '');
        // console.error('❌ httpForm - API 에러:', {
        //     status: response.status,
        //     statusText: response.statusText,
        //     url: `${baseUrl}${url}`,
        //     responseText: text
        // });

        // // 401 에러인 경우 특별 처리
        // if (response.status === 401) {
        //     console.error('🚨 401 Unauthorized - 토큰이 유효하지 않거나 만료되었습니다.');
        //     // 토큰 삭제 및 로그인 페이지로 리다이렉트
        //     localStorage.removeItem('accessToken');
        //     localStorage.removeItem('refreshToken');
        //     if (window.location.pathname !== '/login') {
        //         window.location.href = '/login';
        //     }
        // }

        throw new Error(`HTTP ${response.status}: ${text || response.statusText}`);
    }

    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
        return response.json();
    }
    return response.text();
}

/**
 * 멀티파트 응답을 처리하는 헬퍼 (이미지와 JSON 데이터를 함께 받을 때 사용)
 */
export async function httpMultipart(baseUrl: string, url: string, options: HttpRequestOptions = {}): Promise<{
    recipe: any;
    images: File[];
    mainImageIndex?: number
}> {
    const headers: Record<string, string> = {
        ...(options.headers as Record<string, string> | undefined)
    };

    if (options.attachAuth !== false) {
        const token = getAccessToken();
        if (token) headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${baseUrl}${url}`, {
        ...options,
        headers,
        credentials: 'include' // 쿠키 자동 전송 (HttpOnly 쿠키 포함)
    });

    if (!response.ok) {
        const text = await response.text().catch(() => '');
        throw new Error(`HTTP ${response.status}: ${text || response.statusText}`);
    }

    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('multipart/form-data')) {
        // FormData로 파싱
        const formData = await response.formData();

        const recipeJson = formData.get('recipe') as string;
        const recipe = recipeJson ? JSON.parse(recipeJson) : null;

        const images: File[] = [];
        const imageEntries = formData.getAll('images');
        for (const entry of imageEntries) {
            if (entry instanceof File) {
                images.push(entry);
            }
        }

        const mainImageIndexStr = formData.get('mainImageIndex') as string;
        const mainImageIndex = mainImageIndexStr ? parseInt(mainImageIndexStr) : undefined;

        return {recipe, images, mainImageIndex};
    }

    // fallback: JSON 응답인 경우
    if (contentType.includes('application/json')) {
        const recipe = await response.json();
        return {recipe, images: [], mainImageIndex: undefined};
    }

    throw new Error('Unsupported content type for multipart response');
}
