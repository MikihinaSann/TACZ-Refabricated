package org.admany.taczt.platform.fabric;

import cn.sh1rocu.simplebedrockmodel.api.event.RenderTickEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.admany.taczt.platform.fabric.client.BulletRenderProfileReporter;
import org.admany.taczt.platform.fabric.client.TacztClientBenchmarkEvents;

public final class TacztFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> BulletRenderProfileReporter.onClientTick());
        RenderTickEvent.EVENT.register(TacztClientBenchmarkEvents::onRenderTick);
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> TacztClientBenchmarkEvents.registerCommands(dispatcher));
    }
}
