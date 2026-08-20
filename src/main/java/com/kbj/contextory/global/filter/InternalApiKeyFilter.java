package com.kbj.contextory.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

// /internal/** 경로로 들어오는 요청(FastAPI 콜백 등)의 X-Internal-Api-Key 헤더를 검증
@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";
    private static final String INTERNAL_PATH_PREFIX = "/internal/";

    private final String internalApiKey;
    // 스프링 빈 대신 직접 인스턴스화 (이 프로젝트는 ObjectMapper 자동 구성 빈이 등록되지 않음 - AiInternalService 참고)
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InternalApiKeyFilter(@Value("${ai.fastapi.internal-api-key}") String internalApiKey) {
        this.internalApiKey = internalApiKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(INTERNAL_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String providedKey = request.getHeader(INTERNAL_API_KEY_HEADER);

        if (providedKey == null || !isValidKey(providedKey)) {
            writeUnauthorized(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidKey(String providedKey) {
        byte[] provided = providedKey.getBytes(StandardCharsets.UTF_8);
        byte[] expected = internalApiKey.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(provided, expected);
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        ApiResponse<Void> body = ApiResponse.onFailure(ErrorCode.INTERNAL_API_KEY_INVALID);

        response.setStatus(ErrorCode.INTERNAL_API_KEY_INVALID.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
