package com.handstudio.android.hzgrapherlib.vo.radargraph;

import android.graphics.Color;

public class RadarGraph {
	private String name = null;
	private int color = Color.BLUE;
	private float[] coordinateArr = null;
	private int bitmapResource = -1;
	
	public RadarGraph(String name, int color, float[] coordinateArr) {
		this.name = name;
		this.color = color;
		this.setCoordinateArr(coordinateArr);
	}
	
	public RadarGraph(String name, int color, float[] coordinateArr, int bitmapResource) {
		this.name = name;
		this.color = color;
		this.setCoordinateArr(coordinateArr);
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
