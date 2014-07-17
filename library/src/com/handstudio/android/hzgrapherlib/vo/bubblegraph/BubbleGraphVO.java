package com.handstudio.android.hzgrapherlib.vo.bubblegraph;

import java.util.ArrayList;
import java.util.List;

import com.handstudio.android.hzgrapherlib.vo.Graph;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraph;

public class BubbleGraphVO extends Graph
{
	private String[] 				legendArr = null;
	private List<BubbleGraph> 		arrGraph = null;
	
	private int 					totalItemCount = 0;
	private float 					maxValue = 0.0f;
	private float 					minValue = Float.MAX_VALUE;
	private float 					maxSizeValue = 0.0f;
	private int 					increment;
	
	private int 					graphBG = -1;
	private long 					animationDuration = 2000;
	
	private boolean 				isLineShow = true;
	private boolean					isAnimaionShow = true;
	
	public BubbleGraphVO ( String[] legendArr )
	{
		this.arrGraph = new ArrayList<BubbleGraph> ();
		this.legendArr = legendArr;
		initVO ();
	}
	
	public void add ( BubbleGraph bg )
	{
		this.arrGraph.add(bg);	
		int i;
		for ( i = 0 ; i < bg.getCoordinateArr().length ; i++ )
		{
			float v = bg.getCoordinateArr()[i];
			float s = bg.getSizeArr()[i];
			
			if ( v < this.minValue ) { this.minValue = v; }
			if ( v > this.maxValue ) { this.maxValue = v; }
			
			if ( s > this.maxSizeValue ) { this.maxSizeValue = s; }
		}
		this.totalItemCount += bg.getCoordinateArr().length;
	}
	
	public BubbleGraph get ( int i ) { return this.arrGraph.get(i); }
	public int size () { return this.arrGraph.size(); }
	
	private void initVO ()
	{
		initDefaultGraphNameBox ();
	}
	
	private void initDefaultGraphNameBox () 
	{
		GraphNameBox gnb = new GraphNameBox ();
		this.setGraphNameBox(gnb);
	}
	
	public long getAnimationDuration () { return this.animationDuration; }
	public void setAnimationDuration ( long tick ) { this.animationDuration = tick; }
	
	public boolean isLineShow () { return this.isLineShow; }
	public void setIsLineShow ( boolean isLineShow ) { this.isLineShow = isLineShow; }
	
	public boolean isAnimationShow () { return this.isAnimaionShow; }
	public void setIsAnimaionShow ( boolean isShow ) { this.isAnimaionShow = isShow; }
	
	public int getTotalCountOfItem () { return this.totalItemCount; }
	public float getMaxCoordinate () { return this.maxValue; }
	public float getMinCoordinate () { return this.minValue; }
	public float getMaxSize () { return this.maxSizeValue; }
	
	public String[] getLegendArr () { return this.legendArr; }
	public void setLegendArr ( String[] legendArr ) { this.legendArr = legendArr; }
	
	public int getIncrement () { return this.increment; }
	public void setIncrement ( int increment ) { this.increment = increment; }
	
	public int getGraphBG () { return this.graphBG; }
	public void setGraphBG ( int graphBG ) { this.graphBG = graphBG; }
	
	public List<BubbleGraph> getArrGraph() {
		return arrGraph;
	}

	public void setArrGraph(List<BubbleGraph> arrGraph) {
		this.arrGraph = arrGraph;
	}
}
