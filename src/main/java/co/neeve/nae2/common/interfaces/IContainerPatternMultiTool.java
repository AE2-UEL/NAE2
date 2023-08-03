package co.neeve.nae2.common.interfaces;

public interface IContainerPatternMultiTool extends IPatternMultiToolHost {
	boolean canTakeStack();

	boolean isViewingInterface();

	void toggleInventory();
}
