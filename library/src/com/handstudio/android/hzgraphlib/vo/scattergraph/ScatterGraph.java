package com.handstudio.android.hzgraphlib.vo.scattergraph;

import java.util.ArrayList;

public class ScatterGraph 
{
	public static final String TAG = ScatterGraph.class.getSimpleName();
	
	private String name = null;
	private int color = -1;
	private float[] coordinateArr = null;
	private int bitmapResource = -1;
	
	
	public ScatterGraph(String name, int color, float[] coordinateArr)
	{
		this.name = name;
		this.color = color;
		this.coordinateArr = coordinateArr;
	}
	
	
	public ScatterGraph(String name, int color, float[] coordinateArr, int bitmapResource)
	{
		this(name, color, coordinateArr);
		this.bitmapResource = bitmapResource;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public int getColor() {
		return color;
	}


	public void setColor(int color) {
		this.color = color;
	}


	public float[] getCoordinateArr() {
		return coordinateArr;
	}


	public void setCoordinateArr(float[] coordinateArr) {
		this.coordinateArr = coordinateArr;
	}


	public int getBitmapResource() {
		return bitmapResource;
	}


	public void setBitmapResource(int bitmapResource) {
		this.bitmapResource = bitmapResource;
	}
	
	
}
