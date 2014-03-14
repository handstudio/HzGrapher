package com.handstudio.android.hzgrapher;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.graphview.CurveGraphView;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.curvegraph.CurveGraph;
import com.handstudio.android.hzgrapherlib.vo.curvegraph.CurveGraphVO;

public class CurveGraphWithRegionActivity extends Activity {

	private ViewGroup layoutGraphView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
		
		layoutGraphView = (ViewGroup) findViewById(R.id.layoutGraphView);

		setCurveGraph();
	}

	private void setCurveGraph() {
		//all setting
		CurveGraphVO vo = makeCurveGraphAllSetting();
		
		//default setting
//		CurveGraphVO vo = makeCurveGraphDefaultSetting();
		
		layoutGraphView.addView(new CurveGraphView(this, vo));
	}
	
	/**
	 * make simple Curve graph
	 * @return
	 */
	private CurveGraphVO makeCurveGraphDefaultSetting() {
		
		String[] legendArr 	= {"1","2","3","4","5"};
		float[] graph1 		= {500,100,300,200,100};
		float[] graph2 		= {000,100,200,100,200};
		float[] graph3 		= {200,500,300,400,000};
		
		List<CurveGraph> arrGraph 		= new ArrayList<CurveGraph>();
		arrGraph.add(new CurveGraph("android", 0xaa66ff33, graph1));
		arrGraph.add(new CurveGraph("ios", 0xaa00ffff, graph2));
		arrGraph.add(new CurveGraph("tizen", 0xaaff0066, graph3));
		
		CurveGraphVO vo = new CurveGraphVO(legendArr, arrGraph);
		return vo;
	}

	/**
	 * make Curve graph using options
	 * @return
	 */
	private CurveGraphVO makeCurveGraphAllSetting() {
		//BASIC LAYOUT SETTING
		//padding
		int paddingBottom 	= CurveGraphVO.DEFAULT_PADDING;
		int paddingTop 		= CurveGraphVO.DEFAULT_PADDING;
		int paddingLeft 	= CurveGraphVO.DEFAULT_PADDING;
		int paddingRight 	= CurveGraphVO.DEFAULT_PADDING;

		//graph margin
		int marginTop 		= CurveGraphVO.DEFAULT_MARGIN_TOP;
		int marginRight 	= CurveGraphVO.DEFAULT_MARGIN_RIGHT;

		//max value
		int maxValue 		= CurveGraphVO.DEFAULT_MAX_VALUE;

		//increment
		int increment 		= CurveGraphVO.DEFAULT_INCREMENT;
		
		//GRAPH SETTING
		String[] legendArr 	= {"1","2","3","4","5"};
		float[] graph1 		= {500,100,300,200,100};
		float[] graph2 		= {000,100,200,100,200};
		float[] graph3 		= {200,500,300,400,000};
		
		List<CurveGraph> arrGraph 		= new ArrayList<CurveGraph>();
		
		arrGraph.add(new CurveGraph("android", 0xaa66ff33, graph1, R.drawable.ic_launcher));
		arrGraph.add(new CurveGraph("ios", 0xaa00ffff, graph2));
		arrGraph.add(new CurveGraph("tizen", 0xaaff0066, graph3));
		
		CurveGraphVO vo = new CurveGraphVO(
				paddingBottom, paddingTop, paddingLeft, paddingRight,
				marginTop, marginRight, maxValue, increment, legendArr, arrGraph);
		
		//set animation
		vo.setAnimation(new GraphAnimation(GraphAnimation.CURVE_REGION_ANIMATION_2, GraphAnimation.DEFAULT_DURATION));
		//set graph name box
		vo.setGraphNameBox(new GraphNameBox());
		//set draw graph region
		vo.setDrawRegion(true);
		
		//use icon
//		arrGraph.add(new Graph(0xaa66ff33, graph1, R.drawable.icon1));
//		arrGraph.add(new Graph(0xaa00ffff, graph2, R.drawable.icon2));
//		arrGraph.add(new Graph(0xaaff0066, graph3, R.drawable.icon3));
		
//		CurveGraphVO vo = new CurveGraphVO(
//				paddingBottom, paddingTop, paddingLeft, paddingRight,
//				marginTop, marginRight, maxValue, increment, legendArr, arrGraph, R.drawable.bg);
		return vo;
	}
}
