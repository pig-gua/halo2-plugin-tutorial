package cloud.abaaba.tutorial.service;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
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
    private final ReactiveExtensionClient client;
    private final ReactiveSettingFetcher settingFetcher;

    public BarkService(ReactiveExtensionClient client, ReactiveSettingFetcher settingFetcher) {
        this.client = client;
        this.settingFetcher = settingFetcher;
    }

    /**
     * 发送新评论通知
     *
     * @param comment 新评论
     */
    public void sendNewCommentNotification(Comment comment) {
        settingFetcher.fetch("bark", BarkSetting.class)
            .filter(setting -> setting.getNotifyEvents().contains("newComment"))
            .subscribe(setting -> {
                BarkOptions options = new BarkOptions();
                options.setIcon("https://www.halo.run/upload/2021/03/Adaptive256-463ca9b92e2d40268431018c07735842.png");
                options.setTitle("新评论");
                options.setMessage(stripHtmlTags(comment.getSpec().getContent()));
                if (StringUtils.isBlank(setting.getBaseUrl())) {
                    sendBarkNotification(setting, options);
                } else {
                    getSubjectPermalink(comment, setting.getBaseUrl())
                        .subscribe(permalink -> {
                            options.setUrl(permalink);
                            sendBarkNotification(setting, options);
                        });
                }
            });
    }

    /**
     * 获取评论对象的地址
     *
     * @param comment 评论
     * @return 地址
     */
    private Mono<String> getSubjectPermalink(Comment comment, String baseUrl) {
        Ref subjectRef = comment.getSpec().getSubjectRef();
        String kind = subjectRef.getKind();
        String name = subjectRef.getName();
        if ("Post".equals(kind)) {
            return client.fetch(Post.class, name).map(p -> baseUrl + p.getStatus().getPermalink());
        } else if ("SinglePage".equals(kind)) {
            return client.fetch(SinglePage.class, name)
                .map(p -> baseUrl + p.getStatus().getPermalink());
        } else if ("Moment".equals(kind)) {
            return Mono.just(baseUrl + "/moments");
        }
        return Mono.empty();
    }

    /**
     * 去除HTML标签，替换为空格
     *
     * @param html 包含HTML标签的文本
     * @return 纯文本
     */
    private String stripHtmlTags(String html) {
        if (StringUtils.isBlank(html)) {
            return html;
        }

        // 去除HTML标签
        String text = html.replaceAll("<[^>]+>", " ");

        // 将多个连续空格合并为一个
        text = text.replaceAll("\\s+", " ");

        // 去除首尾空格
        return text.trim();
    }

    /**
     * 推送新用户注册通知
     *
     * @param user 新用户
     */
    public void sendNewRegisterNotification(User user) {
        // 获取设置项
        settingFetcher.fetch("bark", BarkSetting.class)
            .filter(setting -> setting.getNotifyEvents().contains("newRegister"))
            .subscribe(setting -> {
                BarkOptions options = new BarkOptions();
                options.setIcon("https://www.halo.run/upload/2021/03/Adaptive256-463ca9b92e2d40268431018c07735842.png");
                options.setTitle("新用户注册");
                options.setMessage(user.getSpec().getDisplayName());
                sendBarkNotification(setting, options);
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
        WebClient.create().get()
            .uri(setting.getServerUrl() + "/" + setting.getDeviceKey() + "/" + options.build())
            .retrieve().bodyToMono(Void.class)
            .subscribe(result -> log.info("Bark notification sent successfully."),
                error -> log.error("Failed to send Bark notification: " + error.getMessage(),
                    error));
    }

    @Data
    public static class BarkSetting {
        private String serverUrl;
        private String deviceKey;
        private String baseUrl;
        private List<String> notifyEvents;

        public String getBaseUrl() {
            // 移除末尾的斜杠
            if (baseUrl != null && baseUrl.endsWith("/")) {
                return baseUrl.substring(0, baseUrl.length() - 1);
            } else {
                return baseUrl;
            }
        }
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
