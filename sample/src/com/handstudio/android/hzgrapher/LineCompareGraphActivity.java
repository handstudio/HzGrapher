package com.handstudio.android.hzgrapher;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.graphview.LineCompareGraphView;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraph;
import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraphVO;

public class LineCompareGraphActivity extends Activity {

	private ViewGroup layoutGraphView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
		
		layoutGraphView = (ViewGroup) findViewById(R.id.layoutGraphView);

		
		setLineCompareGraph();
		
	}

	private void setLineCompareGraph() {
		//all setting
		LineGraphVO vo = makeLineCompareGraphAllSetting();
		
		//default setting
//		LineGraphVO vo = makeLineCompareGraphDefaultSetting();
		
		layoutGraphView.addView(new LineCompareGraphView(this, vo));
	}
	
	/**
	 * make simple line graph
	 * @return
	 */
	private LineGraphVO makeLineCompareGraphDefaultSetting() {
		
		String[] legendArr 	= {"1","2","3","4","5"};
		float[] graph1 		= {500,100,300,200,100};
		float[] graph2 		= {000,100,200,100,200};
		
		List<LineGraph> arrGraph 		= new ArrayList<LineGraph>();
		arrGraph.add(new LineGraph("android", 0xaa66ff33, graph1));
		arrGraph.add(new LineGraph("ios", 0xaa00ffff, graph2));
		
		LineGraphVO vo = new LineGraphVO(legendArr, arrGraph);
		return vo;
	}

	/**
	 * make line graph using options
	 * @return
	 */
	private LineGraphVO makeLineCompareGraphAllSetting() {
		//BASIC LAYOUT SETTING
		//padding
		int paddingBottom 	= LineGraphVO.DEFAULT_PADDING;
		int paddingTop 		= LineGraphVO.DEFAULT_PADDING;
		int paddingLeft 	= LineGraphVO.DEFAULT_PADDING;
		int paddingRight 	= LineGraphVO.DEFAULT_PADDING;

		//graph margin
		int marginTop 		= LineGraphVO.DEFAULT_MARGIN_TOP;
		int marginRight 	= LineGraphVO.DEFAULT_MARGIN_RIGHT;

		//max value
		int maxValue 		= LineGraphVO.DEFAULT_MAX_VALUE;

		//increment
		int increment 		= LineGraphVO.DEFAULT_INCREMENT;
		
		//GRAPH SETTING
		String[] legendArr 	= {"1","2","3","4","5"};
		float[] graph1 		= {500,100,300,200,100};
		float[] graph2 		= {000,300,200,100,200};
		
		List<LineGraph> arrGraph 		= new ArrayList<LineGraph>();
		
		arrGraph.add(new LineGraph("android", 0xaa66ff33, graph1, R.drawable.ic_launcher));
		arrGraph.add(new LineGraph("ios", 0xaa00ffff, graph2));
		
		LineGraphVO vo = new LineGraphVO(
				paddingBottom, paddingTop, paddingLeft, paddingRight,
				marginTop, marginRight, maxValue, increment, legendArr, arrGraph);
		
		//set animation
		vo.setAnimation(new GraphAnimation(GraphAnimation.LINEAR_ANIMATION, GraphAnimation.DEFAULT_DURATION));
		//set graph name box
		vo.setGraphNameBox(new GraphNameBox());
		
		//use icon
//		arrGraph.add(new Graph(0xaa66ff33, graph1, R.drawable.icon1));
//		arrGraph.add(new Graph(0xaa00ffff, graph2, R.drawable.icon2));
//		arrGraph.add(new Graph(0xaaff0066, graph3, R.drawable.icon3));
		
//		LineGraphVO vo = new LineGraphVO(
//				paddingBottom, paddingTop, paddingLeft, paddingRight,
//				marginTop, marginRight, maxValue, increment, legendArr, arrGraph, R.drawable.bg);
		return vo;
	}
}
