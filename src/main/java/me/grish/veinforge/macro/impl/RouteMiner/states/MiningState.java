package me.grish.veinforge.macro.impl.RouteMiner.states;

import me.grish.veinforge.feature.impl.BlockMiner.BlockMiner;
import me.grish.veinforge.macro.impl.RouteMiner.RouteMinerMacro;
import me.grish.veinforge.util.InventoryUtil;
import me.grish.veinforge.util.ToolSelector;
import me.grish.veinforge.util.helper.MineableBlock;

/**
 * This state is responsible for starting BlockMiner and detecting when to move to next waypoint
 * before proceeding to the moving state in the Route Miner Macro.
 */
public class MiningState implements RouteMinerMacroState {

    @Override
    public void onStart(RouteMinerMacro macro) {
        log("Entering Mining State");
        InventoryUtil.holdItem(ToolSelector.getMiningTool());
        startMining(macro);
    }

    @Override
    public RouteMinerMacroState onTick(RouteMinerMacro macro) {
        if (BlockMiner.getInstance().getError() == BlockMiner.BlockMinerError.NOT_ENOUGH_BLOCKS) {
            BlockMiner.getInstance().stop();
            macro.setRouteIndex(macro.getRouteIndex() + 1);
            return new MovingState();
        }

        return this;
    }

    private void startMining(RouteMinerMacro macro) {
        MineableBlock[] blocksToMine = macro.getBlocksToMine();

        if (blocksToMine.length == 0) {
            macro.disable("No targets provided in configuration.");
            return;
        }

        BlockMiner.getInstance().start(
                blocksToMine,
                macro.getMiningSpeed(),
                macro.getPickaxeAbility(),
                macro.getBlockPriority(),
                ToolSelector.getMiningTool()
        );
    }

    @Override
    public void onEnd(RouteMinerMacro macro) {
        log("Exiting Mining State");
    }

}
