package com.handstudio.android.hzgrapher;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.graphview.CircleGraphView;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.circlegraph.CircleGraphVO;
import com.handstudio.android.hzgrapherlib.vo.circlegraph.CircleGraph;

public class CircleGraphActivity extends Activity {

	private ViewGroup layoutGraphView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
		
		layoutGraphView = (ViewGroup) findViewById(R.id.layoutGraphView);
		
		setCircleGraph();
	}
	
	private void setCircleGraph() {
		
		CircleGraphVO vo = makeLineGraphAllSetting();
		
		layoutGraphView.addView(new CircleGraphView(this,vo));
	}
	
	/**
	 * make line graph using options
	 * @return
	 */
	private CircleGraphVO makeLineGraphAllSetting() {
		//BASIC LAYOUT SETTING
		//padding
		int paddingBottom 	= CircleGraphVO.DEFAULT_PADDING;
		int paddingTop 		= CircleGraphVO.DEFAULT_PADDING;
		int paddingLeft 	= CircleGraphVO.DEFAULT_PADDING;
		int paddingRight 	= CircleGraphVO.DEFAULT_PADDING;

		//graph margin
		int marginTop 		= CircleGraphVO.DEFAULT_MARGIN_TOP;
		int marginRight 	= CircleGraphVO.DEFAULT_MARGIN_RIGHT;

		// radius setting
		int radius = 130;
		
		List<CircleGraph> arrGraph 	= new ArrayList<CircleGraph>();
		
		arrGraph.add(new CircleGraph("android", Color.parseColor("#3366CC"), 1));
		arrGraph.add(new CircleGraph("ios", Color.parseColor("#DC3912"), 1));
		arrGraph.add(new CircleGraph("tizen", Color.parseColor("#FF9900"), 1));
		arrGraph.add(new CircleGraph("HTML", Color.parseColor("#109618"), 1));
		arrGraph.add(new CircleGraph("C", Color.parseColor("#990099"), 3));

		CircleGraphVO vo = new CircleGraphVO(paddingBottom, paddingTop, paddingLeft, paddingRight,marginTop, marginRight,radius, arrGraph);
		
		// circle Line 
		vo.setLineColor(Color.WHITE);
		
		// set text setting
		vo.setTextColor(Color.WHITE);
		vo.setTextSize(20);
		
		// set circle center move X ,Y
		vo.setCenterX(0);
		vo.setCenterY(0);
		
		//set animation
		vo.setAnimation(new GraphAnimation(GraphAnimation.LINEAR_ANIMATION, 2000));
		//set graph name box
		
		GraphNameBox graphNameBox = new GraphNameBox();
		
		// nameBox 
		graphNameBox.setNameboxMarginTop(25);
		graphNameBox.setNameboxMarginRight(25);
		
		vo.setGraphNameBox(graphNameBox);

		return vo;
	}
	
}
