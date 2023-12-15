package co.neeve.nae2.common.interfaces;

import appeng.api.util.AEColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBeamFormer {
	AEColor getColor();

	int getBeamLength();

	EnumFacing getDirection();

	World getWorld();

	boolean isValid();

	boolean shouldRenderBeam();

	BlockPos getPos();
}
