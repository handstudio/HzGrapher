package com.handstudio.android.hzgrapherlib.vo.bargraph;

import java.util.List;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.vo.Graph;

public class BarGraphVO extends Graph {

	private float minValueX;
	private float maxValueX;

	private float minValueY;
	private float maxValueY;

	private float incrementX;
	private float incrementY;

	private float barWidth;

	private int graphBG;
	private long animationDuration;

	private boolean isAnimationShow;
	private boolean isDrawRegion = false;

	private GraphAnimation animation = null;
	private List<BarGraph> arrGraph = null;
	private String[] legendArr = null;

	public BarGraphVO(String[] legendArr, List<BarGraph> arrGraph,
			int paddingTop, int paddingBottom, int paddingLeft,int paddingRight, 
			int marginTop, int marginRight, 
			int minValueX, int minValueY, int maxValueX, int maxValueY, 
			int incrementX, int incrementY,
			int barWidth, 
			int graphBG) {
		super(paddingBottom, paddingTop, paddingLeft, paddingRight, marginTop, marginRight);
		this.minValueX = minValueX;
		this.maxValueX = maxValueX;
		this.minValueY = minValueY;
		this.maxValueY = maxValueY;
		this.incrementX = incrementX;
		this.incrementY = incrementY;
		this.barWidth = barWidth;
		this.legendArr = legendArr;
		this.arrGraph = arrGraph;
		this.graphBG = graphBG;
	}

	public float getMinValueX() {
		return minValueX;
	}

	public void setMinValueX(float minValueX) {
		this.minValueX = minValueX;
	}

	public float getMaxValueX() {
		return maxValueX;
	}

	public void setMaxValueX(float maxValueX) {
		this.maxValueX = maxValueX;
	}

	public float getMinValueY() {
		return minValueY;
	}

	public void setMinValueY(float minValueY) {
		this.minValueY = minValueY;
	}

	public float getMaxValueY() {
		return maxValueY;
	}

	public void setMaxValueY(float maxValueY) {
		this.maxValueY = maxValueY;
	}

	public float getIncrementX() {
		return incrementX;
	}

	public void setIncrementX(float incrementX) {
		this.incrementX = incrementX;
	}

	public float getIncrementY() {
		return incrementY;
	}

	public void setIncrementY(float incrementY) {
		this.incrementY = incrementY;
	}

	public float getBarWidth() {
		return barWidth;
	}

	public void setBarWidth(float barWidth) {
		this.barWidth = barWidth;
	}

	public int getGraphBG() {
		return graphBG;
	}

	public void setGraphBG(int graphBG) {
		this.graphBG = graphBG;
	}

	public long getAnimationDuration() {
		return animationDuration;
	}

	public void setAnimationDuration(long animationDuration) {
		this.animationDuration = animationDuration;
	}

	public boolean isAnimationShow() {
		return isAnimationShow;
	}

	public void setAnimationShow(boolean isAnimationShow) {
		this.isAnimationShow = isAnimationShow;
	}

	public boolean isDrawRegion() {
		return isDrawRegion;
	}

	public void setDrawRegion(boolean isDrawRegion) {
		this.isDrawRegion = isDrawRegion;
	}

	public List<BarGraph> getArrGraph() {
		return arrGraph;
	}

	public void setArrGraph(List<BarGraph> arrGraph) {
		this.arrGraph = arrGraph;
	}

	public String[] getLegendArr() {
		return legendArr;
	}

	public void setLegendArr(String[] legendArr) {
		this.legendArr = legendArr;
	}

	public GraphAnimation getAnimation() {
		return animation;
	}

	public void setAnimation(GraphAnimation animation) {
		this.animation = animation;
	}
	
}
