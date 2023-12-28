package co.neeve.nae2.common.interfaces;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IExposerHandler {
	boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing);
}
