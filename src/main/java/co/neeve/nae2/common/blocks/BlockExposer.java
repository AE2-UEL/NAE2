package co.neeve.nae2.common.blocks;

import appeng.block.AEBaseTileBlock;
import co.neeve.nae2.NAE2;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

public class BlockExposer extends AEBaseTileBlock {
	public BlockExposer() {
		super(Material.IRON);
	}

	@Override
	public void addInformation(ItemStack is, World world, List<String> lines, ITooltipFlag advancedItemTooltips) {
		super.addInformation(is, world, lines, advancedItemTooltips);

		lines.add("Exposes the network contents as capabilities.");

		var registered = NAE2.api().exposer().getRegisteredHandlers();
		if (registered.isEmpty()) {
			lines.add("");
			lines.add("No handlers registered.");
		} else {
			lines.add("");
			lines.add("Registered handlers:");

			for (var handler : registered.object2ObjectEntrySet()) {
				var name = handler.getKey().getName();

				// If name is a class path, strip everything but the name.
				if (name.contains(".")) {
					name = name.substring(name.lastIndexOf('.') + 1);
				}

				lines.add(" - "
					+ "§6" + name + "§r"
					+ " (" + handler.getValue().getMod().getAnnotation(Mod.class).name() + ")");
			}
		}
	}
}
