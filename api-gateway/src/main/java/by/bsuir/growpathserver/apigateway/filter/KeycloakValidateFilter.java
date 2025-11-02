package by.bsuir.growpathserver.apigateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import by.bsuir.growpathserver.common.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class KeycloakValidateFilter extends AbstractGatewayFilterFactory<KeycloakValidateFilter.Config> {

    private final ObjectMapper objectMapper;

    public KeycloakValidateFilter(ObjectMapper objectMapper) {
        super(Config.class);
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "KeycloakValidate";
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return exchange.getPrincipal()
                    .cast(Authentication.class)
                    .filter(auth -> auth instanceof JwtAuthenticationToken)
                    .cast(JwtAuthenticationToken.class)
                    .map(JwtAuthenticationToken::getToken)
                    .flatMap(jwt -> {
                        ServerHttpResponse response = exchange.getResponse();
                        response.setStatusCode(HttpStatus.OK);
                        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

                        Map<String, Object> validationResponse = Map.of(
                                "valid", true,
                                "username", JwtUtils.getUsername(jwt),
                                "expiresAt", jwt.getExpiresAt() != null ? jwt.getExpiresAt().toString() : "N/A"
                        );

                        try {
                            String json = objectMapper.writeValueAsString(validationResponse);
                            DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
                            return response.writeWith(Mono.just(buffer));
                        }
                        catch (JsonProcessingException e) {
                            log.error("Error serializing validation response", e);
                            return handleError(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
                                               "Error serializing response");
                        }
                    })
                    .switchIfEmpty(handleError(exchange, HttpStatus.UNAUTHORIZED, "Not authenticated"));
        };
    }

    private Mono<Void> handleError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            Map<String, String> errorResponse = Map.of(
                    "error", status.getReasonPhrase(),
                    "message", message
            );
            String json = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
        catch (JsonProcessingException e) {
            DataBuffer buffer = response.bufferFactory().wrap(message.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }

    public static class Config {
        // Configuration properties if needed
    }
}
