package com.handstudio.android.hzgrapherlib.vo.linegraph;

import android.graphics.Color;

public class LineGraph {
	private int color = Color.BLUE;
	private float[] coordinateArr = null;
	private int bitmapResource = -1;
	
	public LineGraph(int color, float[] coordinateArr) {
		this.color = color;
		this.setCoordinateArr(coordinateArr);
	}
	
	public LineGraph(int color, float[] coordinateArr, int bitmapResource) {
		this.color = color;
		this.setCoordinateArr(coordinateArr);
		this.bitmapResource = bitmapResource;
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
