package cn.sh1rocu.tacz.mixin.common;

import com.tacz.guns.GunMod;
import net.fabricmc.fabric.impl.itemgroup.FabricItemGroupImpl;
import net.fabricmc.fabric.mixin.itemgroup.ItemGroupAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Fabric API's paginateGroups lays out modded item groups sorted by registry id (path first),
 * which scatters the tacz tabs among other mods' tabs. Runs after it (higher priority) and
 * regroups all tacz tabs into one contiguous block, keeping the mod's registration order
 * and the relative order of every other mod's tabs.
 */
@Mixin(value = CreativeModeTabs.class, priority = 1100)
public class CreativeModeTabsMixin {
    private static final int TABS_PER_PAGE = FabricItemGroupImpl.TABS_PER_PAGE;
    private static final Set<ResourceKey<CreativeModeTab>> VANILLA_TABS = Set.of(
            CreativeModeTabs.BUILDING_BLOCKS,
            CreativeModeTabs.COLORED_BLOCKS,
            CreativeModeTabs.NATURAL_BLOCKS,
            CreativeModeTabs.FUNCTIONAL_BLOCKS,
            CreativeModeTabs.REDSTONE_BLOCKS,
            CreativeModeTabs.HOTBAR,
            CreativeModeTabs.SEARCH,
            CreativeModeTabs.TOOLS_AND_UTILITIES,
            CreativeModeTabs.COMBAT,
            CreativeModeTabs.FOOD_AND_DRINKS,
            CreativeModeTabs.INGREDIENTS,
            CreativeModeTabs.SPAWN_EGGS,
            CreativeModeTabs.OP_BLOCKS,
            CreativeModeTabs.INVENTORY);

    @Inject(method = "tryRebuildTabContents", at = @At("TAIL"))
    private static void tacz$groupModTabsTogether(FeatureFlagSet featureFlagSet, boolean hasPermissions,
                                                HolderLookup.Provider provider, CallbackInfoReturnable<Boolean> cir) {
        List<Holder.Reference<CreativeModeTab>> moddedTabs = BuiltInRegistries.CREATIVE_MODE_TAB.holders()
                .sorted(Comparator.comparing((Holder.Reference<CreativeModeTab> ref) -> !ref.value().shouldDisplay())
                        .thenComparing(ref -> ref.key().location()))
                .filter(ref -> !VANILLA_TABS.contains(ref.key()))
                .toList();

        List<Holder.Reference<CreativeModeTab>> taczTabs = moddedTabs.stream()
                .filter(ref -> GunMod.MOD_ID.equals(ref.key().location().getNamespace()))
                .sorted(Comparator.comparingInt(ref -> BuiltInRegistries.CREATIVE_MODE_TAB.getId(ref.value())))
                .toList();
        if (taczTabs.isEmpty()) {
            return;
        }

        int insertAt = 0;
        while (insertAt < moddedTabs.size()
                && !GunMod.MOD_ID.equals(moddedTabs.get(insertAt).key().location().getNamespace())) {
            insertAt++;
        }
        List<Holder.Reference<CreativeModeTab>> merged = new ArrayList<>(moddedTabs);
        merged.removeAll(taczTabs);
        merged.addAll(insertAt, taczTabs);

        for (int i = 0; i < merged.size(); i++) {
            CreativeModeTab tab = merged.get(i).value();
            int pageIndex = i % TABS_PER_PAGE;
            CreativeModeTab.Row row = pageIndex < TABS_PER_PAGE / 2 ? CreativeModeTab.Row.TOP : CreativeModeTab.Row.BOTTOM;
            ((FabricItemGroupImpl) tab).fabric_setPage(i / TABS_PER_PAGE + 1);
            ((ItemGroupAccessor) tab).setRow(row);
            ((ItemGroupAccessor) tab).setColumn(row == CreativeModeTab.Row.TOP ? pageIndex : pageIndex - TABS_PER_PAGE / 2);
        }
    }
}
