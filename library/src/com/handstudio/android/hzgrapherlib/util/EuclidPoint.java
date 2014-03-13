package com.handstudio.android.hzgrapherlib.util;

public class EuclidPoint 
{
	private float mX = 0.0f;
	private float mY = 0.0f;
		
	public EuclidPoint ( float x , float y )
	{
		mX = x; mY = y;
	}
	
	public float getX () { return mX; }
	public float getY () { return mY; }
	
	public void setX ( float x ) { mX = x; }
	public void setY ( float y ) { mY = y; }
}
