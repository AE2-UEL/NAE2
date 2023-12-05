package co.neeve.nae2.common.blocks;

import appeng.api.definitions.ITileDefinition;
import appeng.block.crafting.BlockCraftingUnit;
import co.neeve.nae2.NAE2;

public class BlockDenseCraftingUnit extends BlockCraftingUnit {
	public final DenseCraftingUnitType type;

	public BlockDenseCraftingUnit(DenseCraftingUnitType type) {
		super(null);
		this.type = type;
	}

	public DenseCraftingUnitType getType() {
		return this.type;
	}

	public enum DenseCraftingUnitType {
		STORAGE_256K(256 * 1024, 0) {
			@Override
			public ITileDefinition getBlock() {
				return NAE2.definitions().blocks().storageCrafting256K();
			}
		},
		STORAGE_1024K(1024 * 1024, 0) {
			@Override
			public ITileDefinition getBlock() {
				return NAE2.definitions().blocks().storageCrafting1024K();
			}
		},
		STORAGE_4096K(4096 * 1024, 0) {
			@Override
			public ITileDefinition getBlock() {
				return NAE2.definitions().blocks().storageCrafting4096K();
			}
		},
		STORAGE_16384K(16384 * 1024, 0) {
			@Override
			public ITileDefinition getBlock() {
				return NAE2.definitions().blocks().storageCrafting16384K();
			}
		},
		COPROCESSOR_4X(0, 4) {
			@Override
			public ITileDefinition getBlock() {
				return NAE2.definitions().blocks().coprocessor4x();
			}
		},
		COPROCESSOR_16X(0, 16) {
			@Override
			public ITileDefinition getBlock() {
				return NAE2.definitions().blocks().coprocessor16x();
			}
		},
		COPROCESSOR_64X(0, 64) {
			@Override
			public ITileDefinition getBlock() {
				return NAE2.definitions().blocks().coprocessor64x();
			}
		};


		private final int bytes;
		private final int accelFactor;

		DenseCraftingUnitType(int bytes, int accelFactor) {
			this.bytes = bytes;
			this.accelFactor = accelFactor;
		}

		public int getBytes() {
			return this.bytes;
		}

		public abstract ITileDefinition getBlock();

		public int getAccelerationFactor() {
			return this.accelFactor;
		}
	}
}
