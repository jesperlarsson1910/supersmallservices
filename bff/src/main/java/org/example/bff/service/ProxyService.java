package org.example.bff.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Service
public class ProxyService {

    private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);

    // Strips /api prefix and forwards to upstream with user headers injected
    public ResponseEntity<String> forward(RestClient client,
                                          HttpMethod method,
                                          String upstreamPath,
                                          String queryString,
                                          String userId,
                                          String username,
                                          String role,
                                          Object body) {
        String url = upstreamPath + (queryString != null ? "?" + queryString : "");
        logger.debug("Proxying {} {}", method, url);

        try {
            var spec = client.method(method)
                    .uri(url)
                    .header("X-User-Id",   userId   != null ? userId   : "")
                    .header("X-Username",  username != null ? username : "")
                    .header("X-User-Role", role     != null ? role     : "");

            if (body != null) {
                spec.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(body);
            }

            return spec.retrieve()
                    .toEntity(String.class);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.warn("Upstream returned {}: {}", e.getStatusCode(), e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Proxy error: {}", e.getMessage());
            return ResponseEntity.status(502).body("{\"error\":\"Upstream service unavailable\"}");
        }
    }
}
