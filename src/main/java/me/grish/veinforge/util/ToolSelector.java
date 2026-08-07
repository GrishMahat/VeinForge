package me.grish.veinforge.util;

import me.grish.veinforge.VeinForge;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Automatically detects the best mining tool and weapon from the player's hotbar,
 * mirroring the tool selection logic used by V5's ToolFinder.
 *
 * When {@code general.autoSelectTools} is enabled each accessor first looks for a
 * suitable item in the hotbar and falls back to the manually configured name when
 * nothing (or nothing better) is found.
 */
public class ToolSelector {

    private static final Minecraft mc = Minecraft.getInstance();

    private record ToolMatch(String match, int priority, boolean fuelTool) {}

    private static final List<ToolMatch> TOOL_PRIORITY_LIST = List.of(
            new ToolMatch("Gauntlet", 5, true),
            new ToolMatch("Drill", 5, true),
            new ToolMatch("Pickonimbus", 3, false),
            new ToolMatch("Eon Pickaxe", 2, false),
            new ToolMatch("Chrono Pickaxe", 2, false),
            new ToolMatch("Jungle Pickaxe", 2, false),
            new ToolMatch("Titanium Pickaxe", 1, false),
            new ToolMatch("Mithril Pickaxe", 1, false)
    );

    private static final String[] WEAPON_KEYWORDS = {
            "Sword", "Blade", "Dagger", "Rapier", "Stilletto", "Maul", "Hammer",
            "Axe", "Cudgel", "Matter", "Halberd", "Glaive", "Daedalus", "Flaming"
    };

    private ToolSelector() {
    }

    public static boolean isAutoSelectEnabled() {
        return VeinForge.config() != null && VeinForge.config().general.autoSelectTools;
    }

    public static String getMiningTool() {
        if (isAutoSelectEnabled()) {
            String auto = getBestMiningToolName(-1);
            if (auto != null && !auto.isEmpty()) {
                return auto;
            }
        }
        return VeinForge.config().general.miningTool;
    }

    public static String getSlayerWeapon() {
        if (isAutoSelectEnabled()) {
            String auto = getBestWeaponName();
            if (auto != null && !auto.isEmpty()) {
                return auto;
            }
        }
        return VeinForge.config().commission.dwarvenCommission.slayerWeapon;
    }

    public static String getAltMiningTool() {
        if (isAutoSelectEnabled()) {
            String primary = getBestMiningToolName(-1);
            String auto = getBestMiningToolNameFromHotbarExcluding(primary);
            if (auto != null && !auto.isEmpty()) {
                return auto;
            }
        }
        return VeinForge.config().commission.dwarvenCommission.altMiningTool;
    }

    private static String getBestMiningToolName(int excludeSlot) {
        if (mc.player == null) return null;
        int bestSlot = -1;
        int bestPriority = -1;
        for (int i = 0; i < 9; i++) {
            if (i == excludeSlot) continue;
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString());
            if (name == null) continue;

            int priority = getToolPriority(name);
            if (priority <= 0) continue;

            if (hasBlueCheese(stack)) {
                priority += 10;
            }

            if (priority > bestPriority) {
                bestPriority = priority;
                bestSlot = i;
            }
        }
        return getHotbarName(bestSlot);
    }

    private static String getBestMiningToolNameFromHotbarExcluding(String primary) {
        if (mc.player == null) return null;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString());
            if (name == null || name.equals(primary)) continue;

            if (getToolPriority(name) > 0) {
                return name;
            }
        }
        return null;
    }

    private static String getBestWeaponName() {
        if (mc.player == null) return null;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString());
            if (name == null) continue;

            if (isWeapon(stack, name)) {
                return name;
            }
        }
        return null;
    }

    private static String getHotbarName(int slot) {
        if (slot == -1 || mc.player == null) return null;
        ItemStack stack = mc.player.getInventory().getItem(slot);
        if (stack.isEmpty()) return null;
        return ChatFormatting.stripFormatting(stack.getHoverName().getString());
    }

    private static int getToolPriority(String name) {
        for (ToolMatch tool : TOOL_PRIORITY_LIST) {
            if (name.contains(tool.match())) {
                return tool.priority();
            }
        }
        return -1;
    }

    private static boolean hasBlueCheese(ItemStack stack) {
        for (String line : InventoryUtil.getItemLore(stack)) {
            if (line.contains("Blue Cheese")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWeapon(ItemStack stack, String name) {
        if (getToolPriority(name) > 0) {
            return false;
        }

        String lower = name.toLowerCase();
        for (String keyword : WEAPON_KEYWORDS) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        String registryPath = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return registryPath.endsWith("_sword")
                || registryPath.endsWith("_axe")
                || registryPath.endsWith("_hammer")
                || registryPath.endsWith("_mace");
    }
}