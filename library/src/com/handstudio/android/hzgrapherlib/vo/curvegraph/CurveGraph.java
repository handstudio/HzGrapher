package com.handstudio.android.hzgrapherlib.vo.curvegraph;

import android.graphics.Color;

public class CurveGraph {
	private String name = null;
	private int color = Color.BLUE;
	private float[] coordinateArr = null;
	private int bitmapResource = -1;

	public CurveGraph(String name, int color, float[] coordinateArr) {
		this.name = name;
		this.color = color;
		this.setCoordinateArr(coordinateArr);
	}

	public CurveGraph(String name, int color, float[] coordinateArr,
			int bitmapResource) {
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
