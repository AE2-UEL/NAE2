package co.neeve.nae2.common.integration.opencomputer;

import li.cil.oc.api.Driver;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;

public class OpenComputer {
    public static void postInit(Side side) {
        Driver.add(new DriverMEUpgrade());
    }
}
