package org.admany.taczt.platform.fabric.client;

import cn.sh1rocu.simplebedrockmodel.api.event.RenderTickEvent;
import com.mojang.brigadier.CommandDispatcher;
import java.util.Locale;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.admany.taczt.platform.fabric.TacztFabric;

public final class TacztClientBenchmarkEvents {
    private static final boolean AUTO_BENCHMARK = Boolean.getBoolean("taczt.benchmark.auto");
    private static final boolean BENCHMARK_SCENARIO = Boolean.getBoolean("taczt.benchmark.scenario");
    private static final int AUTO_WARM_FRAMES =
            Math.max(0, Integer.getInteger("taczt.benchmark.warmFrames", 120));
    private static final ResourceLocation BENCHMARK_GUN =
            ResourceLocation.fromNamespaceAndPath("tacz", "modern_kinetic_gun");

    private static int autoWarmFrames;
    private static int scenarioSettledFrames;
    private static boolean autoStarted;
    private static boolean scenarioLoadoutIssued;

    private TacztClientBenchmarkEvents() {
    }

    public static void onRenderTick(RenderTickEvent event) {
        if (event.phase == RenderTickEvent.Phase.START && AUTO_BENCHMARK && !autoStarted) {
            Minecraft minecraft = Minecraft.getInstance();
            if (BENCHMARK_SCENARIO) {
                prepareScenario(minecraft);
            } else if (minecraft.player != null && ++autoWarmFrames >= AUTO_WARM_FRAMES) {
                autoStarted = true;
                startBenchmark();
            }
        }

        if (event.phase == RenderTickEvent.Phase.START) {
            TacztClientBenchmark.beginFrame();
        } else {
            TacztClientBenchmark.endFrame();
            TacztClientBenchmark.Summary completed = TacztClientBenchmark.consumeCompletedSummary();
            if (completed != null) {
                announceCompleted(completed);
            }
        }
    }

    private static void prepareScenario(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.player.connection == null) {
            return;
        }

        if (!scenarioLoadoutIssued) {
            if (++autoWarmFrames >= AUTO_WARM_FRAMES) {
                minecraft.player.connection.sendCommand("give @s tacz:modern_kinetic_gun{GunId:\"tacz:ak47\"}");
                scenarioLoadoutIssued = true;
            }
            return;
        }

        for (int slot = 0; slot < 9; ++slot) {
            ItemStack stack = minecraft.player.getInventory().getItem(slot);
            if (BENCHMARK_GUN.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
                minecraft.player.getInventory().selected = slot;
                if (++scenarioSettledFrames >= AUTO_WARM_FRAMES) {
                    autoStarted = true;
                    startBenchmark();
                }
                return;
            }
        }
    }

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("tacztbench")
                .then(ClientCommandManager.literal("start").executes(context -> start()))
                .then(ClientCommandManager.literal("stop").executes(context -> stop()))
                .then(ClientCommandManager.literal("report").executes(context -> report()))
                .then(ClientCommandManager.literal("hotspots").executes(context -> hotspots()))
                .then(ClientCommandManager.literal("cube")
                        .then(ClientCommandManager.literal("vanilla")
                                .executes(context -> setCubeMode(BedrockCubeCompiler.Mode.VANILLA)))
                        .then(ClientCommandManager.literal("scalar")
                                .executes(context -> setCubeMode(BedrockCubeCompiler.Mode.SCALAR)))));
    }

    private static int start() {
        if (TacztClientBenchmark.isRunning()) {
            sendMessage("[TaCZ:Accel] A 30 second benchmark is already running.");
            return 0;
        }

        startBenchmark();
        sendMessage("[TaCZ:Accel] The 30 second benchmark has started with the "
                + BedrockCubeCompiler.mode().name().toLowerCase(Locale.ROOT)
                + " cube compiler. It will finish automatically.");
        return 1;
    }

    private static void startBenchmark() {
        TacztClientBenchmark.start();
        BulletRenderProfiler.snapshotAndReset();
        GunRenderProfiler.snapshotFirstPersonAndReset();
        GunRenderProfiler.snapshotItemAndReset();
        GunRenderProfiler.snapshotAnimationAndReset();
        GunRenderProfiler.snapshotTransformAndReset();
        GunRenderProfiler.snapshotModelAndReset();
        GunRenderProfiler.snapshotModelGeometryAndReset();
        GunRenderProfiler.snapshotModelFlushAndReset();
        GunRenderProfiler.snapshotModelDelegatesAndReset();
        GunRenderProfiler.snapshotCubeVanillaAndReset();
        GunRenderProfiler.snapshotCubeScalarAndReset();
        GunRenderProfiler.snapshotAttachmentModelAndReset();
        GunRenderProfiler.snapshotAttachmentFallbackAndReset();
        GunRenderProfiler.snapshotAttachmentDeferredAndReset();
        GunRenderProfiler.snapshotFunctionalAttachmentAndReset();
        GunRenderProfiler.snapshotFunctionalHandAndReset();
        GunRenderProfiler.snapshotFunctionalTextAndReset();
        GunRenderProfiler.snapshotFunctionalEffectAndReset();
        GunRenderProfiler.snapshotFunctionalOtherAndReset();
        GunRenderProfiler.snapshotOtherClassesAndReset();
    }

    private static int setCubeMode(BedrockCubeCompiler.Mode mode) {
        if (TacztClientBenchmark.isRunning()) {
            sendMessage("[TaCZ:Accel] Wait for the current benchmark to finish before changing the cube compiler.");
            return 0;
        }

        BedrockCubeCompiler.use(mode);
        sendMessage("[TaCZ:Accel] Cube compiler set to "
                + mode.name().toLowerCase(Locale.ROOT)
                + ". Run the benchmark from the same position.");
        return 1;
    }

    private static int stop() {
        sendMessage("[TaCZ:Accel] This 30 second benchmark cannot be stopped early.");
        return 0;
    }

    private static int report() {
        String state = TacztClientBenchmark.isRunning() ? "running: " : "idle: ";
        sendMessage("[TaCZ:Accel] benchmark " + state + TacztClientBenchmark.summary().format());
        return 1;
    }

    private static int hotspots() {
        BulletRenderProfiler.Snapshot bullets = BulletRenderProfiler.snapshotAndReset();
        GunRenderProfiler.Snapshot firstPerson = GunRenderProfiler.snapshotFirstPersonAndReset();
        GunRenderProfiler.Snapshot items = GunRenderProfiler.snapshotItemAndReset();
        sendMessage(formatLane("bullets", bullets));
        sendMessage(formatLane("gun-first-person", firstPerson));
        sendMessage(formatLane("gun-item", items));
        return 1;
    }

    private static void announceCompleted(TacztClientBenchmark.Summary summary) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        sendMessage("[TaCZ:Accel] The 30 second benchmark is complete. " + summary.format());
        TacztFabric.logBenchmark(summary.format());
        sendMessage(formatLane("Bullet rendering", BulletRenderProfiler.snapshotAndReset()));
        sendMessage(formatLane("First person gun rendering", GunRenderProfiler.snapshotFirstPersonAndReset()));
        sendMessage(formatLane("Item gun rendering", GunRenderProfiler.snapshotItemAndReset()));
        sendMessage(formatLane("Gun animation update", GunRenderProfiler.snapshotAnimationAndReset()));
        sendMessage(formatLane("Gun transform", GunRenderProfiler.snapshotTransformAndReset()));
        sendMessage(formatLane("Gun model draw", GunRenderProfiler.snapshotModelAndReset()));
        sendMessage(formatLane("Gun model geometry", GunRenderProfiler.snapshotModelGeometryAndReset()));
        sendMessage(formatLane("Gun model buffer flush", GunRenderProfiler.snapshotModelFlushAndReset()));
        sendMessage(formatLane("Gun attachment and text draw", GunRenderProfiler.snapshotModelDelegatesAndReset()));
        sendMessage(formatLane("Vanilla cube compiler", GunRenderProfiler.snapshotCubeVanillaAndReset()));
        sendMessage(formatLane("Scalar cube compiler", GunRenderProfiler.snapshotCubeScalarAndReset()));
        sendMessage(formatLane("Attachment model draw", GunRenderProfiler.snapshotAttachmentModelAndReset()));
        sendMessage(formatLane("Attachment fallback draw", GunRenderProfiler.snapshotAttachmentFallbackAndReset()));
        sendMessage(formatLane("Attachment deferred draw", GunRenderProfiler.snapshotAttachmentDeferredAndReset()));
        sendMessage(formatLane("Attachment functional draw", GunRenderProfiler.snapshotFunctionalAttachmentAndReset()));
        sendMessage(formatLane("Hand functional draw", GunRenderProfiler.snapshotFunctionalHandAndReset()));
        sendMessage(formatLane("Text functional draw", GunRenderProfiler.snapshotFunctionalTextAndReset()));
        sendMessage(formatLane("Effect functional draw", GunRenderProfiler.snapshotFunctionalEffectAndReset()));
        sendMessage(formatLane("Other functional draw", GunRenderProfiler.snapshotFunctionalOtherAndReset()));

        for (GunRenderProfiler.ClassSnapshot snapshot : GunRenderProfiler.snapshotOtherClassesAndReset()) {
            sendMessage(formatLane("Delegate " + snapshot.className(), snapshot.timing()));
        }
    }

    private static void sendMessage(String message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.literal(message), false);
        }
    }

    private static String formatLane(String name, BulletRenderProfiler.Snapshot snapshot) {
        return String.format(
                Locale.ROOT,
                "[TaCZ:Accel] %s. %d samples. Total %.3fms. Average %.2fus. Median %.2fus. P95 %.2fus. P99 %.2fus. Maximum %.2fus.",
                name, snapshot.renders(), snapshot.totalMillis(), snapshot.averageMicros(),
                snapshot.p50Micros(), snapshot.p95Micros(), snapshot.p99Micros(), snapshot.maximumMicros());
    }

    private static String formatLane(String name, GunRenderProfiler.Snapshot snapshot) {
        return String.format(
                Locale.ROOT,
                "[TaCZ:Accel] %s. %d samples. Total %.3fms. Average %.2fus. Median %.2fus. P95 %.2fus. P99 %.2fus. Maximum %.2fus.",
                name, snapshot.renders(), snapshot.totalMillis(), snapshot.averageMicros(),
                snapshot.p50Micros(), snapshot.p95Micros(), snapshot.p99Micros(), snapshot.maximumMicros());
    }
}
