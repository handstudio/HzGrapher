package com.handstudio.android.hzgrapher;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.graphview.RadarGraphView;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.radargraph.RadarGraph;
import com.handstudio.android.hzgrapherlib.vo.radargraph.RadarGraphVO;

public class RadarGraphActivity extends Activity {

	private ViewGroup layoutGraphView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
		
		layoutGraphView = (ViewGroup) findViewById(R.id.layoutGraphView);

		
		setRadarGraph();
		
	}

	private void setRadarGraph() {
		//all setting
		RadarGraphVO vo = makeLineGraphAllSetting();
		
		//default setting
//		LineGraphVO vo = makeRadarGraphDefaultSetting();
		
		layoutGraphView.addView(new RadarGraphView(this, vo));
	}
	
	/**
	 * make simple line graph
	 * @return
	 */
	private RadarGraphVO makeRadarGraphDefaultSetting() {
		
		String[] legendArr 	= {"1 best","2 worst","3 long long long ","4","5 asdgasdgasdga", "sdg"};
		float[] graph1 		= {100,90,80,70,90, 70};
		float[] graph2 		= {70,50,80,40,90, 88};
		float[] graph3 		= {20,70,90,90,90, 17};
		
		List<RadarGraph> arrGraph 		= new ArrayList<RadarGraph>();
		arrGraph.add(new RadarGraph("android", 0xaa66ff33, graph1));
		arrGraph.add(new RadarGraph("ios", 0xaa00ffff, graph2));
		arrGraph.add(new RadarGraph("tizen", 0xaaff0066, graph3));
		
		RadarGraphVO vo = new RadarGraphVO(legendArr, arrGraph);
		return vo;
	}

	/**
	 * make line graph using options
	 * @return
	 */
	private RadarGraphVO makeLineGraphAllSetting() {
		//BASIC LAYOUT SETTING
		//padding
		int paddingBottom 	= RadarGraphVO.DEFAULT_PADDING;
		int paddingTop 		= RadarGraphVO.DEFAULT_PADDING;
		int paddingLeft 	= RadarGraphVO.DEFAULT_PADDING;
		int paddingRight 	= RadarGraphVO.DEFAULT_PADDING;

		//graph margin
		int marginTop 		= RadarGraphVO.DEFAULT_MARGIN_TOP;
		int marginRight 	= RadarGraphVO.DEFAULT_MARGIN_RIGHT;

		//max value
		int maxValue 		= RadarGraphVO.DEFAULT_MAX_VALUE;

		//increment
		int increment 		= RadarGraphVO.DEFAULT_INCREMENT;
		
		//GRAPH SETTING
		String[] legendArr 	= {"Android","Java","C++","Python","Objective c", "Spring Framework"};
		float[] graph1 		= {100,90,80,70,90, 70};
		float[] graph2 		= {70,50,80,40,90, 88};
		float[] graph3 		= {20,70,90,90,90, 17};
		
		List<RadarGraph> arrGraph 		= new ArrayList<RadarGraph>();
		
		arrGraph.add(new RadarGraph("android", 0xaa66ff33, graph1, R.drawable.ic_launcher));
		arrGraph.add(new RadarGraph("ios", 0xaa00ffff, graph2));
		arrGraph.add(new RadarGraph("tizen", 0xaaff0066, graph3));
		
		RadarGraphVO vo = new RadarGraphVO(
				paddingBottom, paddingTop, paddingLeft, paddingRight,
				marginTop, marginRight, maxValue, increment, legendArr, arrGraph);
		
		//set animation
		vo.setAnimation(new GraphAnimation(GraphAnimation.LINEAR_ANIMATION, GraphAnimation.DEFAULT_DURATION*3));
		//set graph name box
		vo.setGraphNameBox(new GraphNameBox());
		//set draw graph region
		vo.setDrawRegion(true);
		
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
