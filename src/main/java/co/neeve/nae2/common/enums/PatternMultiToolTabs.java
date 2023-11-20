package co.neeve.nae2.common.enums;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum PatternMultiToolTabs {
	MULTIPLIER("multiply"), SEARCH_REPLACE("replace");

	private static PatternMultiToolTabs[] cachedValues;
	private final String message;

	PatternMultiToolTabs(String message) {this.message = message;}

	public static PatternMultiToolTabs fromInt(int i) {
		if (cachedValues == null) cachedValues = values();
		if (i < 0 || i > cachedValues.length) return cachedValues[0];
		return cachedValues[i];
	}

	@SideOnly(Side.CLIENT)
	public String getLocal() {
		return I18n.format("nae2.pattern_multiplier.tab." + this.message);
	}
}
