package com.handstudio.android.hzgrapherlib.vo;


public class Graph {
	public static final int DEFAULT_PADDING 		= 100;
	public static final int DEFAULT_MARGIN_TOP		= 10;
	public static final int DEFAULT_MARGIN_RIGHT 	= 100;
	public static final int DEFAULT_MAX_VALUE 		= 500;
	public static final int DEFAULT_INCREMENT 		= 100;

	//padding
	private int paddingBottom 	= DEFAULT_PADDING;
	private int paddingTop 		= DEFAULT_PADDING;
	private int paddingLeft 	= DEFAULT_PADDING;
	private int paddingRight 	= DEFAULT_PADDING;

	//graph margin
	private int marginTop 		= DEFAULT_MARGIN_TOP;
	private int marginRight 	= DEFAULT_MARGIN_RIGHT;
	
	private GraphNameBox graphNameBox = null;	
	
	public Graph() {
		
	}
	public Graph(int paddingBottom, int paddingTop, int paddingLeft,
			int paddingRight, int marginTop, int marginRight) {
		super();
		this.paddingBottom = paddingBottom;
		this.paddingTop = paddingTop;
		this.paddingLeft = paddingLeft;
		this.paddingRight = paddingRight;
		this.marginTop = marginTop;
		this.marginRight = marginRight;
	}
	public int getPaddingBottom() {
		return paddingBottom;
	}
	public void setPaddingBottom(int paddingBottom) {
		this.paddingBottom = paddingBottom;
	}
	public int getPaddingTop() {
		return paddingTop;
	}
	public void setPaddingTop(int paddingTop) {
		this.paddingTop = paddingTop;
	}
	public int getPaddingLeft() {
		return paddingLeft;
	}
	public void setPaddingLeft(int paddingLeft) {
		this.paddingLeft = paddingLeft;
	}
	public int getPaddingRight() {
		return paddingRight;
	}
	public void setPaddingRight(int paddingRight) {
		this.paddingRight = paddingRight;
	}
	public int getMarginTop() {
		return marginTop;
	}
	public void setMarginTop(int marginTop) {
		this.marginTop = marginTop;
	}
	public int getMarginRight() {
		return marginRight;
	}
	public void setMarginRight(int marginRight) {
		this.marginRight = marginRight;
	}
	public GraphNameBox getGraphNameBox() {
		return graphNameBox;
	}
	public void setGraphNameBox(GraphNameBox graphNameBox) {
		this.graphNameBox = graphNameBox;
	}
}
