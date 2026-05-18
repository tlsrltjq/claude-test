package com.eactive.resourcehub.certificate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CertificateService {

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl;

    public CertificateService(
            @Value("${certificate.service.url:http://certificate:5000}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /** 템플릿이 등록된 직원 이름 목록 */
    public List<String> getTemplates() {
        try {
            JsonNode node = get("/templates");
            return toList(node.path("templates"));
        } catch (Exception e) {
            log.warn("재직증명서 서비스 연결 실패 (templates): {}", e.getMessage());
            return List.of();
        }
    }

    /** 발급된 파일 목록 */
    public List<String> getFiles() {
        try {
            JsonNode node = get("/files");
            return toList(node.path("files"));
        } catch (Exception e) {
            log.warn("재직증명서 서비스 연결 실패 (files): {}", e.getMessage());
            return List.of();
        }
    }

    /** 단건 또는 다건 발급 — 템플릿 없으면 자동 생성 후 발급 */
    public CertificateResult generate(List<String> names) {
        List<String> existing = getTemplates();
        for (String name : names) {
            if (!existing.contains(name)) {
                try {
                    createTemplate(name);
                    log.info("재직증명서 기본 템플릿 자동 생성: {}", name);
                } catch (Exception e) {
                    log.warn("템플릿 자동 생성 실패 — name={}: {}", name, e.getMessage());
                }
            }
        }
        try {
            String body = mapper.writeValueAsString(
                    names.size() == 1
                            ? java.util.Map.of("name", names.get(0))
                            : java.util.Map.of("names", names)
            );
            JsonNode node = post("/generate", body);
            return new CertificateResult(toList(node.path("success")), toList(node.path("failed")));
        } catch (Exception e) {
            log.error("재직증명서 발급 실패: {}", e.getMessage());
            return new CertificateResult(List.of(), names);
        }
    }

    /** 전체 발급 — DB에서 받은 전체 이름 목록 기준 */
    public CertificateResult generateAll(List<String> allNames) {
        return generate(allNames);
    }

    /** 기본 템플릿 생성 */
    public void createTemplate(String name) throws IOException {
        try {
            post("/create", mapper.writeValueAsString(java.util.Map.of("name", name)));
        } catch (Exception e) {
            throw new IOException("템플릿 생성 실패: " + e.getMessage(), e);
        }
    }

    /** 파일 다운로드 (byte 배열 반환) */
    public byte[] download(String filename) throws IOException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/download/" + filename))
                .GET()
                .timeout(Duration.ofSeconds(30))
                .build();
        try {
            HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() != 200) {
                throw new IOException("파일 없음: " + filename);
            }
            return resp.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("다운로드 중단", e);
        }
    }

    public boolean isAvailable() {
        try {
            JsonNode node = get("/health");
            return "ok".equals(node.path("status").asText());
        } catch (Exception e) {
            return false;
        }
    }

    // ── 내부 헬퍼 ──────────────────────────────────────────────

    private JsonNode get(String path) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(resp.body());
    }

    private JsonNode post(String path, String jsonBody) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(60))
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(resp.body());
    }

    private List<String> toList(JsonNode node) {
        List<String> result = new ArrayList<>();
        if (node.isArray()) node.forEach(n -> result.add(n.asText()));
        return result;
    }

    public record CertificateResult(List<String> success, List<String> failed) {}
}
