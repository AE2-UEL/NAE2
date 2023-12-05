package co.neeve.nae2.common.registration.registry.components;

import appeng.bootstrap.components.IPreInitComponent;
import appeng.bootstrap.definitions.TileEntityDefinition;
import co.neeve.nae2.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

public class NAETileEntityComponent implements IPreInitComponent {
	private final List<TileEntityDefinition> tileEntityDefinitions = new ArrayList<>();

	public NAETileEntityComponent() {
	}

	public void addTileEntity(TileEntityDefinition tileEntityDefinition) {
		if (!this.tileEntityDefinitions.contains(tileEntityDefinition)) {
			this.tileEntityDefinitions.add(tileEntityDefinition);
		}

	}

	public void preInitialize(Side side) {

		for (var tileEntityDefinition : this.tileEntityDefinitions) {
			if (!tileEntityDefinition.isRegistered()) {
				GameRegistry.registerTileEntity(tileEntityDefinition.getTileEntityClass(),
					new ResourceLocation(Tags.MODID, tileEntityDefinition.getName()));
				tileEntityDefinition.setRegistered(true);
			}
		}

	}
}
