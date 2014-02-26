package com.handstudio.android.hzgrapherlib.vo;

import android.graphics.Color;


public class GraphNameBox {

	public static final int DEFAULT_NAMEBOX_COLOR				= Color.BLUE;
	public static final int DEFAULT_NAMEBOX_MARGINTOP 			= 100;
	public static final int DEFAULT_NAMEBOX_MARGINRIGHT 		= 100;
	public static final int DEFAULT_NAMEBOX_PADDING 			= 10;
	public static final int DEFAULT_NAMEBOX_TEXT_SIZE			= 20;
	public static final int DEFAULT_NAMEBOX_TEXT_COLOR			= Color.BLACK;
	public static final int DEFAULT_NAMEBOX_ICON_WIDTH 			= 30;
	public static final int DEFAULT_NAMEBOX_ICON_HEIGHT			= 10;
	public static final int DEFAULT_NAMEBOX_TEXT_ICON_MARGIN 	= 10;
	public static final int DEFAULT_NAMEBOX_ICON_MARGIN 		= 10;

	private int nameboxColor	 			= DEFAULT_NAMEBOX_COLOR;
	private int nameboxMarginTop 			= DEFAULT_NAMEBOX_MARGINTOP;
	private int nameboxMarginRight 			= DEFAULT_NAMEBOX_MARGINRIGHT;
	private int nameboxPadding 				= DEFAULT_NAMEBOX_PADDING;
	private int nameboxTextSize				= DEFAULT_NAMEBOX_TEXT_SIZE;
	private int nameboxTextColor			= DEFAULT_NAMEBOX_TEXT_COLOR;
	private int nameboxIconWidth 			= DEFAULT_NAMEBOX_ICON_WIDTH;
	private int nameboxIconHeight 			= DEFAULT_NAMEBOX_ICON_HEIGHT;
	private int nameboxTextIconMargin 		= DEFAULT_NAMEBOX_TEXT_ICON_MARGIN;
	private int nameboxIconMargin 			= DEFAULT_NAMEBOX_ICON_MARGIN;
	
	public GraphNameBox() {
		
	}
	
	public GraphNameBox(int nameboxColor, int nameboxMarginTop,
			int nameboxMarginRight, int nameboxPadding, int nameboxTextSize,
			int nameboxTextColor, int nameboxIconWidth, int nameboxIconHeight,
			int nameboxTextIconMargin, int nameboxIconMargin) {
		super();
		this.nameboxColor = nameboxColor;
		this.nameboxMarginTop = nameboxMarginTop;
		this.nameboxMarginRight = nameboxMarginRight;
		this.nameboxPadding = nameboxPadding;
		this.nameboxTextSize = nameboxTextSize;
		this.nameboxTextColor = nameboxTextColor;
		this.nameboxIconWidth = nameboxIconWidth;
		this.nameboxIconHeight = nameboxIconHeight;
		this.nameboxTextIconMargin = nameboxTextIconMargin;
		this.nameboxIconMargin = nameboxIconMargin;
	}

	public int getNameboxColor() {
		return nameboxColor;
	}

	public void setNameboxColor(int nameboxColor) {
		this.nameboxColor = nameboxColor;
	}

	public int getNameboxIconWidth() {
		return nameboxIconWidth;
	}

	public void setNameboxIconWidth(int nameboxIconWidth) {
		this.nameboxIconWidth = nameboxIconWidth;
	}

	public int getNameboxIconHeight() {
		return nameboxIconHeight;
	}

	public void setNameboxIconHeight(int nameboxIconHeight) {
		this.nameboxIconHeight = nameboxIconHeight;
	}

	public int getNameboxTextSize() {
		return nameboxTextSize;
	}

	public void setNameboxTextSize(int nameboxTextSize) {
		this.nameboxTextSize = nameboxTextSize;
	}
	
	public int getNameboxTextColor() {
		return nameboxTextColor;
	}

	public void setNameboxTextColor(int nameboxTextColor) {
		this.nameboxTextColor = nameboxTextColor;
	}

	public int getNameboxMarginTop() {
		return nameboxMarginTop;
	}

	public void setNameboxMarginTop(int nameboxMarginTop) {
		this.nameboxMarginTop = nameboxMarginTop;
	}

	public int getNameboxMarginRight() {
		return nameboxMarginRight;
	}

	public void setNameboxMarginRight(int nameboxMarginRight) {
		this.nameboxMarginRight = nameboxMarginRight;
	}

	public int getNameboxPadding() {
		return nameboxPadding;
	}

	public void setNameboxPadding(int nameboxPadding) {
		this.nameboxPadding = nameboxPadding;
	}

	public int getNameboxTextIconMargin() {
		return nameboxTextIconMargin;
	}

	public void setNameboxTextIconMargin(int nameboxTextIconMargin) {
		this.nameboxTextIconMargin = nameboxTextIconMargin;
	}

	public int getNameboxIconMargin() {
		return nameboxIconMargin;
	}

	public void setNameboxIconMargin(int nameboxIconMargin) {
		this.nameboxIconMargin = nameboxIconMargin;
	}
}
