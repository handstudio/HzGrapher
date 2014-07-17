package com.handstudio.android.hzgrapherlib.vo.bubblegraph;

import android.graphics.Color;

import com.handstudio.android.hzgrapherlib.util.EuclidPoint;

public class BubbleGraph 
{
	private String name = null;
	private int color = Color.BLUE;
	private float[] coordinateArr = null;
	private float[] sizeArr = null;
	
	public BubbleGraph ( String name , int color , float[] coordinateArr , float[] sizeArr )
	{
		this.setName(name);
		this.setColor(color);
		this.setCoordinateArr(coordinateArr);
		this.setSizeArr(sizeArr);
	}
	
	public String getName () { return this.name; }
	public void setName ( String name ) { this.name = name; }
	
	public int getColor () { return this.color; }
	public void setColor ( int color ) { this.color = color; }
	
	public float[] getCoordinateArr () { return this.coordinateArr; }
	public void setCoordinateArr ( float[] coordArr ) 
	{
		this.coordinateArr = coordArr; 
	}
	
	public float[] getSizeArr () { return this.sizeArr; }
	public void setSizeArr ( float[] sizeArr ) { this.sizeArr = sizeArr; }
	
	public float getCoordinateOfFloatIndex ( float idx )
	{
		int curIdx = (int)idx;
		int nextIdx = (int)(idx)+1;
		float curPosY = this.coordinateArr[curIdx];
		float nextPosY = this.coordinateArr[nextIdx];
		
		float m = (nextPosY-curPosY)/((float)nextIdx-(float)curIdx);
		float ret = m*idx-m*(float)curIdx + curPosY;
		return ret;
	}
}
