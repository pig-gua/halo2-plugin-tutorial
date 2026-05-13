package cloud.abaaba.tutorial.endpoint;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import java.util.Map;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

@Component
public class BarkEndpoint implements CustomEndpoint {

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return SpringdocRouteBuilder.route()
            .POST("/bark/test",this::testBark,builder -> builder.operationId("test")
                .description("bark test")
                .tag("Bark"))
            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        // 自动拼接为 /apis/console.api.tutorial.halo.run/v1alpha1/bark/test
        return new GroupVersion("console.api.tutorial.halo.run", "v1alpha1");
    }

    private Mono<ServerResponse> testBark(ServerRequest request) {
        System.out.println("testBark");

        return request.bodyToMono(BarkTestRequest.class)
            .flatMap(req -> {
                String url = req.getServerUrl();
                String key = req.getDeviceKey();
                // 使用 WebClient 调用 Bark API
                return WebClient.create()
                    .get()
                    .uri(url + "/" + key + "/hello from halo")
                    .retrieve()
                    .bodyToMono(Object.class);
            })
            .flatMap(obj -> {
                System.out.println("obj: " + obj);
                return ServerResponse.ok().bodyValue(obj);
            })
            .onErrorResume(e -> {
                return ServerResponse
                    .status(500)
                    .bodyValue(Map.of(
                        "title", "推送失败",
                        "status", 500,
                        "detail", "推送失败，请检查服务端Url或设备Key"
                    ));
            });
    }

    @Data
    static class BarkTestRequest {
        private String serverUrl;
        private String deviceKey;
    }

}