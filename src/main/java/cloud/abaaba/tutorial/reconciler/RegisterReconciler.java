package cloud.abaaba.tutorial.reconciler;

import cloud.abaaba.tutorial.service.BarkService;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import java.util.Map;

/**
 * RegisterReconciler
 *
 * @author Pig-Gua
 * @date 2026-05-13
 */
@Component
public class RegisterReconciler implements Reconciler<Reconciler.Request> {
    private final ExtensionClient client;
    private final BarkService barkService;

    public RegisterReconciler(ExtensionClient client, BarkService barkService) {
        this.client = client;
        this.barkService = barkService;
    }

    @Override
    public Result reconcile(Request request) {
        var userOpt = client.fetch(User.class, request.name());
        if (userOpt.isEmpty()) return Result.doNotRetry();
        User user = userOpt.get();
        // 1. 跳过已删除
        if (ExtensionUtil.isDeleted(user)) return Result.doNotRetry();
        // 2. 跳过已通知
        Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(user);
        if (annotations.containsKey("tutorial.halo.run/register-notified"))
            return Result.doNotRetry();
        // 3. 标记已处理
        annotations.put("tutorial.halo.run/register-notified", "true");
        client.update(user);
        // 4. 异步推送
        barkService.sendNewRegisterNotification(user);
        return Result.doNotRetry();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new User())
            .syncAllOnStart(false)
            .build();
    }

}
