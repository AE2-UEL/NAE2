package co.neeve.nae2.server;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldListener implements IWorldEventListener {
	public static WorldListener instance;
	private final Long2ObjectOpenHashMap<IBlockStateListener> blockStateListeners = new Long2ObjectOpenHashMap<>();

	public WorldListener() {
		instance = this;
	}

	public void registerBlockStateListener(IBlockStateListener listener, Iterable<BlockPos> blocks) {
		for (var bp : blocks) {
			this.blockStateListeners.put(bp.toLong(), listener);
		}
	}

	public void unregisterBlockStateListener(IBlockStateListener listener) {
		this.blockStateListeners.entrySet().removeIf(entry -> entry.getValue() == listener);
	}

	@Override
	public void notifyBlockUpdate(@NotNull World worldIn, BlockPos pos, @NotNull IBlockState oldState,
	                              @NotNull IBlockState newState, int flags) {
		var listener = this.blockStateListeners.get(pos.toLong());
		if (listener != null) {
			listener.notifyBlockUpdate(worldIn, pos, oldState, newState, flags);
		}
	}

	@Override
	public void notifyLightSet(@NotNull BlockPos pos) {

	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {

	}

	@Override
	public void playSoundToAllNearExcept(@Nullable EntityPlayer player, @NotNull SoundEvent soundIn,
	                                     @NotNull SoundCategory category,
	                                     double x, double y, double z, float volume, float pitch) {

	}

	@Override
	public void playRecord(@NotNull SoundEvent soundIn, @NotNull BlockPos pos) {

	}

	@Override
	public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord,
	                          double xSpeed, double ySpeed, double zSpeed, int @NotNull ... parameters) {

	}

	@Override
	public void spawnParticle(int id, boolean ignoreRange, boolean minimiseParticleLevel, double x, double y, double z
		, double xSpeed, double ySpeed, double zSpeed, int @NotNull ... parameters) {

	}

	@Override
	public void onEntityAdded(@NotNull Entity entityIn) {

	}

	@Override
	public void onEntityRemoved(@NotNull Entity entityIn) {

	}

	@Override
	public void broadcastSound(int soundID, @NotNull BlockPos pos, int data) {

	}

	@Override
	public void playEvent(@NotNull EntityPlayer player, int type, @NotNull BlockPos blockPosIn, int data) {

	}

	@Override
	public void sendBlockBreakProgress(int breakerId, @NotNull BlockPos pos, int progress) {

	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		event.getWorld().addEventListener(this);
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		event.getWorld().removeEventListener(this);
	}
}