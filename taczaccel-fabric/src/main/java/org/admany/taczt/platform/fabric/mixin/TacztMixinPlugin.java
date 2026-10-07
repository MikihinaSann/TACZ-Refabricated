package org.admany.taczt.platform.fabric.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class TacztMixinPlugin implements IMixinConfigPlugin {
    private static final boolean OPTIMIZATIONS_ENABLED = Boolean.parseBoolean(System.getProperty("taczt.optimizations.enabled", "true"));
    private static final boolean FRAME_ONLY_BENCHMARK = Boolean.getBoolean("taczt.benchmark.frameOnly");
    private static final boolean DETAILED_PROFILING = Boolean.getBoolean("taczt.profile.render") || Boolean.getBoolean("taczt.profile.bullets");
    private static final Set<String> PROFILING_MIXINS = Set.of("BedrockModelMixin", "BedrockGunModelProfileMixin", "EntityBulletRenderProfileMixin", "GunItemRenderProfileMixin");
    private static final Set<String> ARCANA_RENDER_MIXINS = Set.of("AttachmentRenderMixin", "BedrockModelMixin", "BedrockGunModelMixin", "EntityBulletRenderProfileMixin");
    private static final Set<String> ACCELERATED_RENDERING_MIXINS = Set.of("AcceleratedBedrockPartCacheMixin", "AcceleratedRenderStateMixin");
    private static final Set<String> SODIUM_CONFLICT_MIXINS = Set.of("PoseStackMixin", "PoseStackPoseMixin");

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!OPTIMIZATIONS_ENABLED) {
            return false;
        }

        String simpleName = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);
        if (ACCELERATED_RENDERING_MIXINS.contains(simpleName) && !acceleratedRenderingLoaded()) {
            return false;
        }
        if (SODIUM_CONFLICT_MIXINS.contains(simpleName) && sodiumLoaded()) {
            return false;
        }
        if (PROFILING_MIXINS.contains(simpleName) && !DETAILED_PROFILING) {
            return false;
        }
        if (arcanaLoaded() && ARCANA_RENDER_MIXINS.contains(simpleName)) {
            return false;
        }
        return !FRAME_ONLY_BENCHMARK || !PROFILING_MIXINS.contains(simpleName);
    }

    private static boolean acceleratedRenderingLoaded() {
        return FabricLoader.getInstance().isModLoaded("acceleratedrendering");
    }

    private static boolean sodiumLoaded() {
        return FabricLoader.getInstance().isModLoaded("sodium");
    }

    private static boolean arcanaLoaded() {
        if (FabricLoader.getInstance().isModLoaded("taczexpands")) {
            return true;
        }

        String[] markers = {
                "group/taczexpands/client/mixin/MixinBedrockGunModel.class",
                "group/taczexpands/coremod/TACZExpandsPlugin.class"
        };

        try {
            ClassLoader loader = TacztMixinPlugin.class.getClassLoader();
            if (loader != null) {
                for (String marker : markers) {
                    if (loader.getResource(marker) != null) return true;
                }
            }

            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            if (contextLoader != null) {
                for (String marker : markers) {
                    if (contextLoader.getResource(marker) != null) return true;
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
