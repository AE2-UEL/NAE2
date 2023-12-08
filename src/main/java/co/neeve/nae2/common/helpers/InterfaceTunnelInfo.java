package co.neeve.nae2.common.helpers;

import co.neeve.nae2.common.parts.p2p.PartP2PInterface;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class InterfaceTunnelInfo {
	public PartP2PInterface inputTunnel;
	public TileEntity targetEntity;
	public EnumFacing facing;

	public InterfaceTunnelInfo(PartP2PInterface inputTunnel, TileEntity targetEntity, EnumFacing originalFacing) {
		this.inputTunnel = inputTunnel;
		this.targetEntity = targetEntity;
		this.facing = originalFacing;
	}
}
