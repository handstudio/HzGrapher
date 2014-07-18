package com.handstudio.android.hzgrapher;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;

import com.handstudio.android.hzgrapherlib.graphview.BubbleGraphView;
import com.handstudio.android.hzgrapherlib.vo.bubblegraph.BubbleGraph;
import com.handstudio.android.hzgrapherlib.vo.bubblegraph.BubbleGraphVO;

public class BubbleGraphActivity extends Activity 
{
	public static final String TAG = "BUBBLE_GRAPH_ACTIVITY";
	private ViewGroup layoutGraphView = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView ( R.layout.activity_graph );
	    
	    layoutGraphView = (ViewGroup) findViewById ( R.id.layoutGraphView );
	    layoutGraphView.addView (new BubbleGraphView (this , createBubbleGraphVO() ));
	}
	
	private BubbleGraphVO createBubbleGraphVO ()
	{
		BubbleGraphVO ret = null;
		
		String[] legendArr = {"2008","2009","2010","2011","2012"};
		ret = new BubbleGraphVO ( legendArr );
		
		//ret.setGraphBG(R.drawable.back);
		ret.setAnimationDuration(1000);
		
		ret.setIsLineShow(true);
		ret.setIsAnimaionShow(true);
		
		float[] coordArr = {20.0f , 35.0f , 50.0f , 104.0f , 50.0f};
		float[] sizeArr = {20.0f , 15.0f , 20.0f , 25.0f , 30.0f}; 
		ret.add ( new BubbleGraph("Github" , Color.rgb(255,45,2) , coordArr , sizeArr) );
		
		float[] coordArr2 = {30.0f , 40.0f , 15.0f , 21.0f , 80.0f};
		float[] sizeArr2 = {20.0f , 25.0f , 33.0f , 25.0f , 30.0f};
		ret.add( new BubbleGraph("SourceForge" , Color.CYAN , coordArr2 , sizeArr2) );
		
		float[] coordArr3 = {84.0f , 60.0f , 75.0f , 88.0f , 92.0f};
		float[] sizeArr3 = {15.0f , 60.0f , 20.0f , 23.0f , 25.0f};
		ret.add( new BubbleGraph("Google group" , Color.YELLOW , coordArr3 , sizeArr3) );
		
		/*
		float[] coordArr4 = {30.0f , 50.0f , 65.0f , 33.0f , 120.0f};
		float[] sizeArr4 = {15.0f , 25.0f , 20.0f , 23.0f , 25.0f};
		list.add( new BubbleGraph("Google group" , Color.GREEN , coordArr4 , sizeArr4) );
		
		float[] coordArr5 = {20.0f , 50.0f , 35.0f , 60.0f , 22.0f};
		float[] sizeArr5 = {15.0f , 25.0f , 20.0f , 23.0f , 25.0f};
		list.add( new BubbleGraph("Google group" , Color.MAGENTA , coordArr5 , sizeArr5) );
		
		float[] coordArr6 = {90.0f , 35.0f , 64.0f , 40.0f , 53.0f};
		float[] sizeArr6 = {15.0f , 25.0f , 20.0f , 23.0f , 25.0f};
		list.add( new BubbleGraph("Google group" , Color.DKGRAY , coordArr6 , sizeArr6) );
		
		float[] coordArr7 = {64.0f , 40.0f , 30.0f , 70.0f , 10.0f};
		float[] sizeArr7 = {15.0f , 25.0f , 20.0f , 23.0f , 25.0f};
		list.add( new BubbleGraph("Google group" , Color.BLACK , coordArr7 , sizeArr7) );
		*/
		
		return ret;
	}
}
