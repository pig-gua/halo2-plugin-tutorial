package cloud.abaaba.bark;

import cloud.abaaba.bark.BarkPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.plugin.PluginContext;

@ExtendWith(MockitoExtension.class)
class BarkPluginTest {

    @Mock
    PluginContext context;

    @InjectMocks
    BarkPlugin plugin;

    @Test
    void contextLoads() {
        // plugin.start();
        // plugin.stop();
    }
}
