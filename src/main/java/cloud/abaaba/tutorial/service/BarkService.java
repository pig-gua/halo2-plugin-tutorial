package cloud.abaaba.tutorial.service;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.plugin.ReactiveSettingFetcher;
import java.util.List;

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

    /**
     * 发送新评论通知
     *
     * @param comment 新评论
     */
    public void sendNewCommentNotification(Comment comment) {
        // 获取设置项
        settingFetcher.fetch("bark", BarkSetting.class)
            .subscribe(setting -> {
                if (setting.getNotifyEvents().contains("newComment")) {
                    BarkOptions options = new BarkOptions();
                    options.setTitle("新评论");
                    options.setMessage(comment.getSpec().getContent());
                    sendBarkNotification(setting, options);
                }
            });
    }

    /**
     * 推送新用户注册通知
     *
     * @param user 新用户
     */
    public void sendNewRegisterNotification(User user) {
        // 获取设置项
        settingFetcher.fetch("bark", BarkSetting.class)
            .subscribe(setting -> {
                if (setting.getNotifyEvents().contains("newRegister")) {
                    BarkOptions options = new BarkOptions();
                    options.setTitle("新用户注册");
                    options.setMessage(user.getSpec().getDisplayName());
                    sendBarkNotification(setting, options);
                }
            });
    }

    /**
     * 发送Bark通知
     *
     * @param setting Bark设置项
     * @param options Bark配置项
     */
    private void sendBarkNotification(BarkSetting setting, BarkOptions options) {
        log.info("Sending Bark notification: " + options.build());
        WebClient.create()
            .get()
            .uri(setting.getServerUrl() + "/" + setting.getDeviceKey() + "/" + options.build())
            .retrieve()
            .bodyToMono(Void.class)
            .subscribe(
                result -> log.info("Bark notification sent successfully."),
                error -> log.error("Failed to send Bark notification: " + error.getMessage(), error)
            );
    }

    @Data
    public static class BarkSetting {
        private String serverUrl;
        private String deviceKey;
        private List<String> notifyEvents;
    }

    @Data
    public static class BarkOptions {
        private String title;   // 标题
        private String message; // 消息
        private String icon;    // 图标
        private String group;   // 分组
        private String url;     // 跳转链接
        private String image;   // 图片
        private String badge;   // 角标: 任意数字
        private String copy;    // 复制内容
        private String autoCopy; // 长按自动复制

        private String buildPath() {
            if (title == null) {
                return message;
            } else {
                return title + "/" + message;
            }
        }

        public String build() {
            if (StringUtils.isBlank(buildQuery())) {
                return buildPath();
            } else {
                return buildPath() + "?" + buildQuery();
            }
        }

        private String buildQuery() {
            StringBuilder query = new StringBuilder();
            if (icon != null) {
                if (!query.isEmpty()) {
                    query.append("&");
                }
                query.append("icon=").append(icon);
            }
            if (group != null) {
                if (!query.isEmpty()) {
                    query.append("&");
                }
                query.append("group=").append(group);
            }
            if (url != null) {
                if (!query.isEmpty()) {
                    query.append("&");
                }
                query.append("url=").append(url);
            }
            if (image != null) {
                if (!query.isEmpty()) {
                    query.append("&");
                }
                query.append("image=").append(image);
            }
            if (badge != null) {
                if (!query.isEmpty()) {
                    query.append("&");
                }
                query.append("badge=").append(badge);
            }
            if (copy != null) {
                if (!query.isEmpty()) {
                    query.append("&");
                }
                query.append("copy=").append(copy);
            }
            if (autoCopy != null) {
                if (!query.isEmpty()) {
                    query.append("&");
                }
                query.append("autoCopy=").append(autoCopy);
            }
            return query.toString();
        }
    }

}
