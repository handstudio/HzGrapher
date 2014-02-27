package com.handstudio.android.hzgrapherlib.path;

import android.graphics.Path;
import android.graphics.PointF;

import com.handstudio.android.hzgrapherlib.util.MatrixTranslator;

public class GraphPath extends Path {
	
	private MatrixTranslator mMt;
	
	public GraphPath(int width, int height, int paddingLeft, int paddingBottom) {
		mMt = new MatrixTranslator(width, height, paddingLeft, paddingBottom);
	}
	
	@Override
	public void moveTo(float x, float y) {
		super.moveTo(mMt.calcX(x), mMt.calcY(y));
	}
	
	public void moveTo(PointF point) {
		super.moveTo(mMt.calcX(point.x), mMt.calcY(point.y));
	}
	
	@Override
	public void lineTo(float x, float y) {
		super.lineTo(mMt.calcX(x), mMt.calcY(y));
	}
	
	public void lineTo(PointF point) {
		super.lineTo(mMt.calcX(point.x), mMt.calcY(point.y));
	}
}
