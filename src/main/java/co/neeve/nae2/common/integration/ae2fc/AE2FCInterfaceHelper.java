package co.neeve.nae2.common.integration.ae2fc;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class AE2FCInterfaceHelper {
	private static TileEntity interfaceOverride = null;
	private static EnumFacing enumFacingOverride = null;

	public static TileEntity getInterfaceOverride() {
		return interfaceOverride;
	}

	public static void setInterfaceOverride(TileEntity interfaceOverride) {
		AE2FCInterfaceHelper.interfaceOverride = interfaceOverride;
	}

	public static EnumFacing getEnumFacingOverride() {
		return enumFacingOverride;
	}

	public static void setEnumFacingOverride(EnumFacing enumFacingOverride) {
		AE2FCInterfaceHelper.enumFacingOverride = enumFacingOverride;
	}
}
