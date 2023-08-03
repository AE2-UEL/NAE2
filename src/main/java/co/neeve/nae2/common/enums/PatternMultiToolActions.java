package co.neeve.nae2.common.enums;

import appeng.core.localization.ButtonToolTips;

import javax.annotation.Nullable;

public enum PatternMultiToolActions {
	CLEAR("nae2.pattern_multiplier.unencode"),
	REPLACE("nae2.pattern_multiplier.replace"),
	INV_SWITCH(),
	SUB("-1", ButtonToolTips.DecreaseByOne),
	DIV2("/2", ButtonToolTips.DivideByTwo),
	DIV3("/3", ButtonToolTips.DivideByThree),
	ADD("+1", ButtonToolTips.IncreaseByOne, SUB),
	MUL2("*2", ButtonToolTips.MultiplyByTwo, DIV2),
	MUL3("*3", ButtonToolTips.MultiplyByThree, DIV3);

	private String desc = "?";
	private String toolTip = "?";
	private String name = "?";
	private PatternMultiToolActions shiftAction = null;

	PatternMultiToolActions(String name, ButtonToolTips toolTip) {
		this.name = name;
		this.toolTip = toolTip.getUnlocalized();
		this.desc = getDescFromAEToolTip(toolTip);
	}

	PatternMultiToolActions(String name, ButtonToolTips toolTip, PatternMultiToolActions shiftAction) {
		this.name = name;
		this.toolTip = toolTip.getUnlocalized();
		this.desc = getDescFromAEToolTip(toolTip);
		this.shiftAction = shiftAction;
	}

	PatternMultiToolActions(String name) {
		this.name = name;
		this.toolTip = name;
		this.desc = name + ".desc";
	}

	PatternMultiToolActions() {}

	private static String getDescFromAEToolTip(ButtonToolTips toolTip) {
		return toolTip.getUnlocalized() + "Desc";
	}

	public String getName() {
		return this.name;
	}

	public String getTitle() {
		return this.toolTip;
	}

	public String getDesc() {
		return this.desc;
	}

	public @Nullable PatternMultiToolActions getShiftAction() {
		return this.shiftAction;
	}
}

