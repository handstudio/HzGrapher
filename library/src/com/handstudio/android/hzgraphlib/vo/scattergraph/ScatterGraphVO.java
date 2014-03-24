package com.handstudio.android.hzgraphlib.vo.scattergraph;

import java.util.List;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.vo.Graph;

public class ScatterGraphVO extends Graph
{
	public static final String TAG = ScatterGraphVO.class.getSimpleName();
	
	
	private int maxValueX = 100;
	private int maxValueY = 100;
	private int incrementX = 20;
	private int incrementY = 20;
	
	private GraphAnimation animation = null;
	private String[] legendArr = null;
	private List<ScatterGraph> arrGraph = null;
	
	private int graphBG = -1;
	private boolean isDrawRegion = false;
	
	
	public ScatterGraphVO(String[] legendArr, List<ScatterGraph> arrGraph)
	{
		super();
		this.legendArr = legendArr;
		this.arrGraph = arrGraph;
	}
	
	public ScatterGraphVO(String[] legendArr, List<ScatterGraph> arrGraph, int graphBG)
	{
		this(legendArr, arrGraph);
		this.graphBG = graphBG;
	}
	
	public ScatterGraphVO(String[] legendArr, List<ScatterGraph> arrGraph,
										int paddingTop, int paddingBottom, int paddingLeft, int paddingRight,
										int marginTop, int marginRight, int maxValueX, int maxValueY, 
										int incrementX, int incrementY)
	{
		super(paddingBottom, paddingTop, paddingLeft, paddingRight, marginTop, marginRight);
		this.maxValueX = maxValueX;
		this.maxValueY = maxValueY;		
		this.incrementX = incrementX;
		this.incrementY = incrementY;
		this.legendArr = legendArr;
		this.arrGraph = arrGraph;
	}
	
	public ScatterGraphVO(String[] legendArr, List<ScatterGraph> arrGraph, int graphBG,
			int paddingTop, int paddingBottom, int paddingLeft, int paddingRight,
			int marginTop, int marginRight, int maxValueX, int maxValueY, 
			int incrementX, int incrementY)
	{
		super(paddingBottom, paddingTop, paddingLeft, paddingRight, marginTop, marginRight);
		this.maxValueX = maxValueX;
		this.maxValueY = maxValueY;
		this.incrementX = incrementX;
		this.incrementY = incrementY;
		this.legendArr = legendArr;
		this.arrGraph = arrGraph;
		this.graphBG = graphBG;
	}

	public int getMaxValueX() {
		return maxValueX;
	}

	public void setMaxValueX(int maxValueX) {
		this.maxValueX = maxValueX;
	}
	
	public int getMaxValueY() {
		return maxValueY;
	}

	public void setMaxValueY(int maxValueY) {
		this.maxValueY = maxValueY;
	}

	public int getIncrementX() {
		return incrementX;
	}

	public void setIncrementX(int incrementX) {
		this.incrementX = incrementX;
	}
	
	public int getIncrementY() {
		return incrementY;
	}

	public void setIncrementY(int incrementY) {
		this.incrementY = incrementY;
	}
	
	

	public GraphAnimation getAnimation() {
		return animation;
	}

	public void setAnimation(GraphAnimation animation) {
		this.animation = animation;
	}

	public String[] getLegendArr() {
		return legendArr;
	}

	public void setLegendArr(String[] legendArr) {
		this.legendArr = legendArr;
	}

	public List<ScatterGraph> getArrGraph() {
		return arrGraph;
	}

	public void setArrGraph(List<ScatterGraph> arrGraph) {
		this.arrGraph = arrGraph;
	}

	public int getGraphBG() {
		return graphBG;
	}

	public void setGraphBG(int graphBG) {
		this.graphBG = graphBG;
	}

	public boolean isDrawRegion() {
		return isDrawRegion;
	}

	public void setDrawRegion(boolean isDrawRegion) {
		this.isDrawRegion = isDrawRegion;
	}
	
	
	
	
	
}
