package com.handstudio.android.hzgrapherlib.util;

public class EuclidLine 
{
	private EuclidPoint mPt1 = null;
	private EuclidPoint mPt2 = null;
	
	public EuclidLine ( EuclidPoint pt1 , EuclidPoint pt2 )
	{
		mPt1 = pt1; mPt2 = pt2;
	}
	
	public float getLength ()
	{
		return (float)Math.sqrt((mPt2.getX()-mPt1.getX())*(mPt2.getX()-mPt1.getX()) + 
				(mPt2.getY()-mPt1.getY())*(mPt2.getY()-mPt1.getY()));
	}
	
	public float getEclination () 
	{
		return 0.0f;
	}
	
	public EuclidPoint getPointOfLine ( boolean startFromPt1 , float distance )
	{
		EuclidPoint ptRet = null;
		double dx = mPt2.getX() - mPt1.getX();
		double dy = mPt2.getY() - mPt1.getY();
		
		double theta = 0;
		if ( startFromPt1 == true ) { theta = Math.atan(dy/dx) + Converter.DegreeToRadian(180.0f); }
		else { theta = Math.atan ( dy / dx ); }
		
		double x = (double)mPt1.getX() + ((double)distance * Math.cos( theta ));
		double y = (double)mPt1.getY() + ((double)distance * Math.sin( theta ));
		
		ptRet = new EuclidPoint ( (float)x , (float)y );
				
		return ptRet;
	}
}