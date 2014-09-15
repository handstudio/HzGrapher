package com.handstudio.android.hzgrapher;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.graphview.BarGraphView;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.bargraph.BarGraph;
import com.handstudio.android.hzgrapherlib.vo.bargraph.BarGraphVO;

public class BarGraphActivity extends Activity{

	public static final String TAG = "BAR_GRAPH_ACTIVITY";
	private ViewGroup layoutGraphView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView ( R.layout.activity_graph );
		
		layoutGraphView = (ViewGroup) findViewById ( R.id.layoutGraphView );
		layoutGraphView.addView (new BarGraphView(this , createBarGraphVO()));
	}
	
	private BarGraphVO createBarGraphVO (){
		BarGraphVO vo = null;
		
		String[] legendArr = {"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT"};
		
		//android
		float[] graph1 = {100,200,100,400,500,400,300,200,100,0};
//		//ios
//		float[] graph2 = {50,300,250,200,250,300,350,50,450,200};
//		//tizen
//		float[] graph3 = {500,400,50,200,100,0,100,200,100,400}; 
		
		List<BarGraph> arrGraph = new ArrayList<BarGraph>();
		arrGraph.add(new BarGraph("android", Color.GREEN, graph1));
//		arrGraph.add(new BarGraph("ios", Color.GRAY, graph2));
//		arrGraph.add(new BarGraph("tizen", Color.RED, graph3));
		
		int paddingTop = BarGraphVO.DEFAULT_PADDING;
		int paddingBottom = BarGraphVO.DEFAULT_PADDING;
		int paddingLeft = BarGraphVO.DEFAULT_PADDING;
		int paddingRight = BarGraphVO.DEFAULT_PADDING;
		int marginTop = BarGraphVO.DEFAULT_MARGIN_TOP;
		int marginRight = BarGraphVO.DEFAULT_MARGIN_RIGHT;
		int minValueX = 0;
		int minValueY = 0;
		int maxValueX = 1000;
		int maxValueY = 1000;
		int incrementX = 100;
		int incrementY = 100;
		int barWidth = 50;
		
		vo = new BarGraphVO(legendArr, arrGraph, 
				paddingTop, paddingBottom, paddingLeft, paddingRight,
				marginTop, marginRight,
				minValueX, minValueY, maxValueX, maxValueY,
				incrementX, incrementY,
				barWidth,
				-1);
		
		vo.setGraphNameBox(new GraphNameBox());
		vo.setAnimation(new GraphAnimation(GraphAnimation.LINEAR_ANIMATION, GraphAnimation.DEFAULT_DURATION));
		vo.setAnimationShow(true);
		
		return vo;
	}
}
