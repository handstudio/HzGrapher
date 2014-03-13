package com.handstudio.android.hzgrapherlib.vo.curvegraph;

import java.util.List;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.vo.Graph;

public class CurveGraphVO extends Graph {

	// max value
	private int 						maxValue = DEFAULT_MAX_VALUE;

	// increment
	private int 						increment = DEFAULT_INCREMENT;

	// animation
	private GraphAnimation 				animation = null;

	private String[] 					legendArr = null;
	private List<CurveGraph> 			arrGraph = null;

	private int 						graphBG = -1;

	private boolean 					isDrawRegion = false;

	public CurveGraphVO(String[] legendArr, List<CurveGraph> arrGraph) {
		super();
		this.setLegendArr(legendArr);
		this.arrGraph = arrGraph;
	}

	public CurveGraphVO(String[] legendArr, List<CurveGraph> arrGraph,
			int graphBG) {
		super();
		this.setLegendArr(legendArr);
		this.arrGraph = arrGraph;
		this.setGraphBG(graphBG);
	}

	public CurveGraphVO(int paddingBottom, int paddingTop, int paddingLeft,
			int paddingRight, int marginTop, int marginRight, int maxValue,
			int increment, String[] legendArr, List<CurveGraph> arrGraph) {
		super(paddingBottom, paddingTop, paddingLeft, paddingRight, marginTop,
				marginRight);
		this.maxValue = maxValue;
		this.increment = increment;
		this.setLegendArr(legendArr);
		this.arrGraph = arrGraph;
	}

	public CurveGraphVO(int paddingBottom, int paddingTop, int paddingLeft,
			int paddingRight, int marginTop, int marginRight, int maxValue,
			int increment, String[] legendArr, List<CurveGraph> arrGraph,
			int graphBG) {
		super(paddingBottom, paddingTop, paddingLeft, paddingRight, marginTop,
				marginRight);
		this.maxValue = maxValue;
		this.increment = increment;
		this.setLegendArr(legendArr);
		this.arrGraph = arrGraph;
		this.setGraphBG(graphBG);
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

	public List<CurveGraph> getArrGraph() {
		return arrGraph;
	}

	public void setArrGraph(List<CurveGraph> arrGraph) {
		this.arrGraph = arrGraph;
	}

	public int getGraphBG() {
		return graphBG;
	}

	public void setGraphBG(int graphBG) {
		this.graphBG = graphBG;
	}

	public GraphAnimation getAnimation() {
		return animation;
	}

	public void setAnimation(GraphAnimation animation) {
		this.animation = animation;
	}

	public boolean isDrawRegion() {
		return isDrawRegion;
	}

	public void setDrawRegion(boolean isDrawRegion) {
		this.isDrawRegion = isDrawRegion;
	}
}
