package me.grish.veinforge.macro.impl.CommissionMacro.states;

import me.grish.veinforge.VeinForge;
import me.grish.veinforge.macro.impl.CommissionMacro.CommissionMacro;
import me.grish.veinforge.util.InventoryUtil;
import me.grish.veinforge.util.ToolSelector;

import java.util.Objects;

public class StartingState implements CommissionMacroState {

    @Override
    public void onStart(CommissionMacro macro) {
        log("Entering starting state");
    }

    @Override
    public CommissionMacroState onTick(CommissionMacro macro) {
        if (Objects.equals(VeinForge.config().general.miningTool, "") && !ToolSelector.isAutoSelectEnabled()) {
            macro.disable("Please set a Mining Tool in the config");
            return null;
        }
        if (Objects.equals(VeinForge.config().commission.dwarvenCommission.slayerWeapon, "") && !ToolSelector.isAutoSelectEnabled()) {
            macro.disable("Please set a Slayer Weapon in the config");
            return null;
        }
        if (!InventoryUtil.areItemsInHotbar(macro.getNecessaryItems())) {
            macro.disable("Please put the following items in hotbar: " + InventoryUtil.getMissingItemsInHotbar(macro.getNecessaryItems()));
            return null;
        }
        return macro.getMiningSpeed() == 0 ? new GettingStatsState() : new PathingState();
    }

    @Override
    public void onEnd(CommissionMacro macro) {
        log("Exiting starting state");
    }
}
