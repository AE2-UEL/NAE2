package co.neeve.nae2.common.integration.opencomputer;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.EnvironmentProvider;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;

public class OpenComputer {
    public static void postInit(Side side) {
        DriverMEUpgrade upgrade = new DriverMEUpgrade();
        Driver.add((DriverItem) upgrade);
        Driver.add((EnvironmentProvider) upgrade);
    }
}
