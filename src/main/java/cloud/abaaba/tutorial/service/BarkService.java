package cloud.abaaba.tutorial.service;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import run.halo.app.plugin.ReactiveSettingFetcher;

/**
 * BarkService
 *
 * @author Pig-Gua
 * @date 2026-05-13
 */
@Component
public class BarkService {

    private static final Logger log = LoggerFactory.getLogger(BarkService.class);
    private final ReactiveSettingFetcher settingFetcher;

    public BarkService(ReactiveSettingFetcher settingFetcher) {
        this.settingFetcher = settingFetcher;
    }

    public void sendBarkNotification(String message) {
        // 获取设置项
        settingFetcher.fetch("bark", BarkSetting.class)
            .subscribe(setting -> sendBarkNotification(message, setting));
    }

    private void sendBarkNotification(String message, BarkSetting setting) {
        WebClient.create()
            .get()
            .uri(setting.getServerUrl() + "/" + setting.getDeviceKey() + "/" + message)
            .retrieve()
            .bodyToMono(Void.class)
            .subscribe(
                result -> {},
                error -> log.error("Failed to send Bark notification: " + error.getMessage(), error)
            );
    }

    @Data
    public static class BarkSetting {
        private String serverUrl;
        private String deviceKey;
    }

}
