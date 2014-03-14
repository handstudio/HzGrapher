package com.handstudio.android.hzgrapherlib.animation;


public class GraphAnimation {
	public static final int LINEAR_ANIMATION = 1;
	
	public static final int CURVE_REGION_ANIMATION_1 = 2;
	public static final int CURVE_REGION_ANIMATION_2 = 3;
	
	public static final int DEFAULT_DURATION = 2000;
	
	private int animation = LINEAR_ANIMATION;
	private int duration = DEFAULT_DURATION;
	
	public GraphAnimation() {
		
	}
	
	public GraphAnimation(int animation, int duration) {
		super();
		this.animation = animation;
		this.duration = duration;
	}
	public int getAnimation() {
		return animation;
	}
	public void setAnimation(int animation) {
		this.animation = animation;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
}
