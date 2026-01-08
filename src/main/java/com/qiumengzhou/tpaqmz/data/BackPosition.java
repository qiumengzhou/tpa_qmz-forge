
package com.qiumengzhou.tpaqmz.data;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record BackPosition(
        ResourceKey<Level> dimension,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {}