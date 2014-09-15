package com.handstudio.android.hzgrapherlib.vo.bargraph;

import android.graphics.Color;

public class BarGraph 
{
	private String name = null;
	private int color = Color.BLUE;
	private float[] coordinateArr = null;
	
	public BarGraph ( String name , int color , float[] coordinateArr)
	{
		this.setName(name);
		this.setColor(color);
		this.setCoordinateArr(coordinateArr);
	}
	
	public String getName () {
		return this.name; 
	}
	
	public void setName ( String name ) {
		this.name = name; 
	}
	
	public int getColor () {
		return this.color; 
	}
	
	public void setColor ( int color ) {
		this.color = color; 
	}
	
	public float[] getCoordinateArr () {
		return this.coordinateArr; 
	}
	
	public void setCoordinateArr ( float[] coordArr ) 
	{
		this.coordinateArr = coordArr; 
	}	
}
