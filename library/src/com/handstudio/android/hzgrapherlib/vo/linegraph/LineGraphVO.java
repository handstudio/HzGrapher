package com.handstudio.android.hzgrapherlib.vo.linegraph;

import java.util.List;

public class LineGraphVO {
	
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

	//max value
	private int maxValue 		= DEFAULT_MAX_VALUE;

	//increment
	private int increment 		= DEFAULT_INCREMENT;
	
	private String[] legendArr 			= null;
	private List<Graph> arrGraph 		= null;
	
	private int graphBG = -1;
	
	public LineGraphVO(String[] legendArr, List<Graph> arrGraph) {
		super();
		this.setLegendArr(legendArr);
		this.arrGraph = arrGraph;
	}
	
	public LineGraphVO(String[] legendArr, List<Graph> arrGraph, int graphBG) {
		super();
		this.setLegendArr(legendArr);
		this.arrGraph = arrGraph;
		this.setGraphBG(graphBG);
	}
	
	public LineGraphVO(int paddingBottom, int paddingTop, int paddingLeft,
			int paddingRight, int marginTop, int marginRight, int maxValue,
			int increment, String[] legendArr, List<Graph> arrGraph) {
		super();
		this.paddingBottom = paddingBottom;
		this.paddingTop = paddingTop;
		this.paddingLeft = paddingLeft;
		this.paddingRight = paddingRight;
		this.marginTop = marginTop;
		this.marginRight = marginRight;
		this.maxValue = maxValue;
		this.increment = increment;
		this.setLegendArr(legendArr);
		this.arrGraph = arrGraph;
	}
	
	public LineGraphVO(int paddingBottom, int paddingTop, int paddingLeft,
			int paddingRight, int marginTop, int marginRight, int maxValue,
			int increment, String[] legendArr, List<Graph> arrGraph, int graphBG) {
		super();
		this.paddingBottom = paddingBottom;
		this.paddingTop = paddingTop;
		this.paddingLeft = paddingLeft;
		this.paddingRight = paddingRight;
		this.marginTop = marginTop;
		this.marginRight = marginRight;
		this.maxValue = maxValue;
		this.increment = increment;
		this.setLegendArr(legendArr);
		this.arrGraph = arrGraph;
		this.setGraphBG(graphBG);
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

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public int getIncrement() {
		return increment;
	}

	public void setIncrement(int increment) {
		this.increment = increment;
	}
	
	public String[] getLegendArr() {
		return legendArr;
	}

	public void setLegendArr(String[] legendArr) {
		this.legendArr = legendArr;
	}
	
	public List<Graph> getArrGraph() {
		return arrGraph;
	}

	public void setArrGraph(List<Graph> arrGraph) {
		this.arrGraph = arrGraph;
	}

	public int getGraphBG() {
		return graphBG;
	}

	public void setGraphBG(int graphBG) {
		this.graphBG = graphBG;
	}
}
