package com.handstudio.android.hzgrapherlib.util;

import android.graphics.PointF;


public class IntersectFinder {
	public static PointF getIntersectPoint(PointF p1, PointF p2, PointF p3, PointF p4)
	{
		PointF result = new PointF();
		double t;
		double s;     
		double under = (p4.y-p3.y)*(p2.x-p1.x)-(p4.x-p3.x)*(p2.y-p1.y); 
		if(under==0) 
			return null;
		
		double _t = (p4.x-p3.x)*(p1.y-p3.y) - (p4.y-p3.y)*(p1.x-p3.x); 
		double _s = (p2.x-p1.x)*(p1.y-p3.y) - (p2.y-p1.y)*(p1.x-p3.x);    
		t = _t/under; 
		s = _s/under;
		
		if(t<0.0 || t>1.0 || s<0.0 || s>1.0) 
			return null;
		
		if( (_t == 0) && (_s == 0)) {
			return null;
		}
		
		result.x = (float) (p1.x + t * (float)(p2.x-p1.x)); 
		result.y = (float) (p1.y + t * (float)(p2.y-p1.y));
		
		return result; 
	}
}
