/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.core.debug.benchmark;

import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.terasology.engine.context.Context;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Region;
import org.terasology.math.geom.Vector3i;

import java.util.HashMap;
import java.util.Map;

/**
 * Can benchmark either {@link WorldProvider#setBlock(Vector3i, Block)} or {@link WorldProvider#setBlocks(Map)}
 * depending on a constructor argument.
 */
class BlockPlacementBenchmark extends AbstractBenchmarkInstance {
    private final WorldProvider worldProvider;
    private final BlockRegion region3i;
    private final Block air;
    private final Block stone;
    private final boolean useSetBlocksInsteadOfSetBlock;
    private Block blockToPlace;

    BlockPlacementBenchmark(Context context, boolean useSetBlocksInsteadOfSetBlock) {
        this.worldProvider = context.get(WorldProvider.class);
        LocalPlayer localPlayer = context.get(LocalPlayer.class);
        this.region3i = BenchmarkScreen.getChunkRegionAbove(localPlayer.getPosition(new Vector3f()));
        BlockManager blockManager = context.get(BlockManager.class);
        this.stone = blockManager.getBlock("CoreBlocks:Stone");
        this.useSetBlocksInsteadOfSetBlock = useSetBlocksInsteadOfSetBlock;
        this.air = blockManager.getBlock("engine:air");
        blockToPlace = stone;
    }

    @Override
    public void runStep() {
        if (useSetBlocksInsteadOfSetBlock) {
            Map<Vector3ic, Block> blocksToPlace = new HashMap<>();
            for (Vector3ic v : region3i) {
                blocksToPlace.put(v, blockToPlace);
            }
            worldProvider.setBlocks(blocksToPlace);
        } else {
            for (Vector3ic v : region3i) {
                worldProvider.setBlock(v, blockToPlace);
            }
        }
        if (blockToPlace == stone) {
            blockToPlace = air;
        } else {
            blockToPlace = stone;
        }
    }
}
