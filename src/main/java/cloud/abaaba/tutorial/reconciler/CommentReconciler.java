package cloud.abaaba.tutorial.reconciler;

import cloud.abaaba.tutorial.service.BarkService;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import java.util.Map;

/**
 * CommentReconciler 新评论通知
 *
 * @author Pig-Gua
 * @date 2026-05-13
 */
@Component
public class CommentReconciler implements Reconciler<Reconciler.Request> {

    private final ExtensionClient client;
    private final BarkService barkService;

    public CommentReconciler(ExtensionClient client, BarkService barkService) {
        this.client = client;
        this.barkService = barkService;
    }

    @Override
    public Result reconcile(Request request) {
        var commentOpt = client.fetch(Comment.class, request.name());
        if (commentOpt.isEmpty()) {
            return Result.doNotRetry();
        }
        Comment comment = commentOpt.get();

        // 1、检查是否已删除
        if (ExtensionUtil.isDeleted(comment)) {
            return Result.doNotRetry();
        }

        // 2、检查是否已通知
        Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(comment);
        if (annotations.containsKey("tutorial.halo.run/notified")) {
            return Result.doNotRetry();
        }

        // 3、标记为已处理
        MetadataUtil.nullSafeAnnotations(comment).put("tutorial.halo.run/notified", "true");
        client.update(comment);

        // 4、Bark 异步推送
        barkService.sendNewCommentNotification(comment);
        return Result.doNotRetry();
    }


    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Comment())
            .syncAllOnStart(false)  // 启动时不同步历史数据，仅监听新事件
            .build();
    }
}
