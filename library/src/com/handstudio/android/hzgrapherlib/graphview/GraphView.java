package com.handstudio.android.hzgrapherlib.graphview;

import java.util.WeakHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.handstudio.android.hzgrapherlib.canvas.GraphCanvasWrapper;
import com.handstudio.android.hzgrapherlib.path.GraphPath;
import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraphVO;

public class GraphView extends SurfaceView implements Callback{

	public static final String TAG = "ChartView";
	private SurfaceHolder mHolder;
	private DrawThread mDrawThread;
	
	private LineGraphVO mLineGraphVO = null;
	
	
	//Constructor
	public GraphView(Context context, LineGraphVO vo) {
		super(context);
		mLineGraphVO = vo;
		initView(context, vo);
	}
	
	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context, attrs, 0);
	}
	
	public GraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		initView(context, attrs, defStyle);
	}
	
	private void initView(Context context, LineGraphVO vo) {
		mHolder = getHolder();
		mHolder.addCallback(this);
	}
	

	private void initView(Context context, AttributeSet attrs, int defStyle) {
		mHolder = getHolder();
		mHolder.addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(mDrawThread == null){
			mDrawThread = new DrawThread(mHolder, getContext());
			mDrawThread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if(mDrawThread != null){
			mDrawThread.setRunFlag(false);
			mDrawThread = null;
		}
		
	}
	
	private static final Object touchLock = new Object(); // touch synchronize
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		
		if(mDrawThread == null ){
			return false;
		}
		
		if(action == MotionEvent.ACTION_DOWN){
			synchronized (touchLock) {
				mDrawThread.isDirty = true;
	        }
			return true;
		}else if(action == MotionEvent.ACTION_MOVE){
			synchronized (touchLock) {
				mDrawThread.isDirty = true;
	        }
			return true;
		}else if(action == MotionEvent.ACTION_UP){
			synchronized (touchLock) {
				mDrawThread.isDirty = true;
	        }
			return true;
		}
		
		return super.onTouchEvent(event);
	}
	
	class DrawThread extends Thread{
		SurfaceHolder mHolder;
		Context mCtx;
		
		boolean isRun = true;
		boolean isDirty = true;
		
		Matrix matrix = new Matrix();
		
		int height = getHeight();
		int width = getWidth();
		
		//graph length
		int xLength = width - (mLineGraphVO.getPaddingLeft() + mLineGraphVO.getPaddingRight() + mLineGraphVO.getMarginRight());
		int yLength = height - (mLineGraphVO.getPaddingBottom() + mLineGraphVO.getPaddingTop() + mLineGraphVO.getMarginTop());
		
		//chart length
		int chartXLength = width - (mLineGraphVO.getPaddingLeft() + mLineGraphVO.getPaddingRight());
		int chartYLength = height - (mLineGraphVO.getPaddingBottom() + mLineGraphVO.getPaddingTop());
		
		Paint p = new Paint();
		Paint pCircle = new Paint();
		Paint pLine = new Paint();
		Paint pBaseLine = new Paint();
		Paint pBaseLineX = new Paint();
		Paint pMarkText = new Paint();
		
		WeakHashMap<Integer, Bitmap> arrIcon = new WeakHashMap<Integer, Bitmap>();
		Bitmap bg = null;
		public DrawThread(SurfaceHolder holder, Context context) {
			mHolder = holder;
			mCtx = context;
			
			int size = mLineGraphVO.getArrGraph().size();
			for (int i = 0; i < size; i++) {
				int bitmapResource = mLineGraphVO.getArrGraph().get(i).getBitmapResource();
				if(bitmapResource != -1){
					arrIcon.put(i, BitmapFactory.decodeResource(getResources(), bitmapResource));
				}else{
					if(arrIcon.get(i) != null){
						arrIcon.remove(i);
					}
				}
			}
			int bgResource = mLineGraphVO.getGraphBG();
			if(bgResource != -1){
				Bitmap tempBg = BitmapFactory.decodeResource(getResources(), bgResource);
				bg = Bitmap.createScaledBitmap(tempBg, width, height, true);
				tempBg.recycle();
			}
		}
		
		public void setRunFlag(boolean bool){
			isRun = bool;
		}
		
		private float anim = 0.0f;
		private boolean isAnimation = false;
		private long animStartTime = -1;
		
		@Override
		public void run() {
			GraphCanvasWrapper graphCanvasWrapper = null;
			Log.e(TAG,"height = " + height);
			Log.e(TAG,"width = " + width);
			
			setPaint();
			isAnimation();
			
			animStartTime = System.currentTimeMillis();
			
			while(isRun){
			
				//draw only on dirty mode
				if(!isDirty){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					continue;
				}
				
				graphCanvasWrapper = new GraphCanvasWrapper(mHolder.lockCanvas(), width, height, mLineGraphVO.getPaddingLeft(), mLineGraphVO.getPaddingBottom());
				
				synchronized(mHolder){
					synchronized (touchLock) {
						
						try {
							//bg color
							graphCanvasWrapper.drawColor(Color.WHITE);
							if(bg != null){
								graphCanvasWrapper.drawBitmap(bg, 0, 0, null);
							}

							//TODO x coord dot line
							drawBaseLine(graphCanvasWrapper);
							
							//y coord
							graphCanvasWrapper.drawLine(0, 0, 0, chartYLength, pBaseLine);
							
							//x coord
							graphCanvasWrapper.drawLine(0, 0, chartXLength, 0, pBaseLine);
							
							//TODO x, y coord mark
							drawXMark(graphCanvasWrapper);
							drawYMark(graphCanvasWrapper);
							
							//TODO x, y coord text
							drawXText(graphCanvasWrapper);
							drawYText(graphCanvasWrapper);
							
							//TODO chart
							drawGraph(graphCanvasWrapper);
							
//							isDirty = false;
							

						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if(graphCanvasWrapper.getCanvas() != null){
								mHolder.unlockCanvasAndPost(graphCanvasWrapper.getCanvas());
							}
						}
						
					}
				}
				
				try {
					Thread.sleep(0000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		/**
		 * check graph line animation
		 */
		private void isAnimation() {
			if(mLineGraphVO.getAnimation() != null){
				isAnimation = true;
			}else{
				isAnimation = false;
			}
		}

		private void drawBaseLine(GraphCanvasWrapper canvas) {
			for (int i = 1; mLineGraphVO.getIncrement() * i <= mLineGraphVO.getMaxValue(); i++) {
				
				float y = yLength * mLineGraphVO.getIncrement() * i/mLineGraphVO.getMaxValue();
				
				canvas.drawLine(0, y, chartXLength, y, pBaseLineX);
			}
		}

		/**
		 * set graph line color
		 */
		private void setPaint() {
			p = new Paint();
			p.setFlags(Paint.ANTI_ALIAS_FLAG);
			p.setAntiAlias(true); //text anti alias
			p.setFilterBitmap(true); // bitmap anti alias
			p.setColor(Color.BLUE);
			p.setStrokeWidth(3);
			p.setStyle(Style.STROKE);
			
			pCircle = new Paint();
			pCircle.setFlags(Paint.ANTI_ALIAS_FLAG);
			pCircle.setAntiAlias(true); //text anti alias
			pCircle.setFilterBitmap(true); // bitmap anti alias
			pCircle.setColor(Color.BLUE);
			pCircle.setStrokeWidth(3);
			pCircle.setStyle(Style.FILL_AND_STROKE);
			
			pLine = new Paint();
			pLine.setFlags(Paint.ANTI_ALIAS_FLAG);
			pLine.setAntiAlias(true); //text anti alias
			pLine.setFilterBitmap(true); // bitmap anti alias
			pLine.setShader(new LinearGradient(0, 300f, 0, 0f, Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));
			
			pBaseLine = new Paint();
			pBaseLine.setFlags(Paint.ANTI_ALIAS_FLAG);
			pBaseLine.setAntiAlias(true); //text anti alias
			pBaseLine.setFilterBitmap(true); // bitmap anti alias
			pBaseLine.setColor(Color.GRAY);
			pBaseLine.setStrokeWidth(3);
			
			pBaseLineX = new Paint();
			pBaseLineX.setFlags(Paint.ANTI_ALIAS_FLAG);
			pBaseLineX.setAntiAlias(true); //text anti alias
			pBaseLineX.setFilterBitmap(true); // bitmap anti alias
			pBaseLineX.setColor(0xffcccccc);
			pBaseLineX.setStrokeWidth(3);
			pBaseLineX.setStyle(Style.STROKE);
			pBaseLineX.setPathEffect(new DashPathEffect(new float[] {10,5}, 0));
			
			pMarkText = new Paint();
			pMarkText.setFlags(Paint.ANTI_ALIAS_FLAG);
			pMarkText.setAntiAlias(true); //text anti alias
			pMarkText.setColor(Color.BLACK); 
		}

		/**
		 * draw Graph
		 */
		private void drawGraph(GraphCanvasWrapper canvas) {
			
			if (isAnimation){
				drawGraphWithAnimation(canvas);
			}else{
				drawGraphWithoutAnimation(canvas);
			}
		}
		
		/**
		 *	draw graph without animation 
		 */
		private void drawGraphWithoutAnimation(GraphCanvasWrapper canvas) {
			
			for (int i = 0; i < mLineGraphVO.getArrGraph().size(); i++) {
				GraphPath linePath = new GraphPath(width, height, mLineGraphVO.getPaddingLeft(), mLineGraphVO.getPaddingBottom());
				boolean firstSet = false;
				float x = 0;
				float y = 0;
				p.setColor(mLineGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mLineGraphVO.getArrGraph().get(i).getColor());
				float xGap = xLength/(mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
				
				Bitmap icon = arrIcon.get(i);
				
				for (int j = 0; j < mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length; j++) {
					if(j < mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length){
						
						if (!firstSet) {
							
							x = xGap * j ;
							y = yLength * mLineGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mLineGraphVO.getMaxValue();
							
							linePath.moveTo(x, y);
							
							firstSet = true;
						} else {
							x = xGap * j;
							y = yLength * mLineGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mLineGraphVO.getMaxValue();
							
							linePath.lineTo(x, y);
						}
						
						if(icon == null){
							canvas.drawCircle(x, y, 4, pCircle);
						}else{
							canvas.drawBitmapIcon(icon, x, y, null);
						}
					}
				}
				
				anim += 0.01f;
				if(anim >= mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length-1){
					anim = mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length;
				}

				canvas.drawPath(linePath, p);
			}
		}

		/**
		 *	draw graph with animation 
		 */
		private void drawGraphWithAnimation(GraphCanvasWrapper canvas) {
			//for draw animation
			float prev_x = 0;
			float prev_y = 0;
			
			float next_x = 0;
			float next_y = 0;
			
			float value = 0;
			float mode = 0;
			
			for (int i = 0; i < mLineGraphVO.getArrGraph().size(); i++) {
				GraphPath linePath = new GraphPath(width, height, mLineGraphVO.getPaddingLeft(), mLineGraphVO.getPaddingBottom());
				boolean firstSet = false;
				float x = 0;
				float y = 0;
				p.setColor(mLineGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mLineGraphVO.getArrGraph().get(i).getColor());
				float xGap = xLength/(mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
				
				Bitmap icon = arrIcon.get(i);
				value = anim/1;
				mode = anim %1;
				
				for (int j = 0; j < value+1; j++) {
					if(j < mLineGraphVO.getArrGraph().get(i).getCoordinateArr().length){
						
						if (!firstSet) {
							
							x = xGap * j ;
							y = yLength * mLineGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mLineGraphVO.getMaxValue();
							
							linePath.moveTo(x, y);
							
							firstSet = true;
						} else {
							x = xGap * j;
							y = yLength * mLineGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mLineGraphVO.getMaxValue();
							
							if( j > value ){
								next_x = x - prev_x;
								next_y = y - prev_y;
								
								linePath.lineTo(prev_x + next_x * mode, prev_y + next_y * mode);
							}else{
								linePath.lineTo(x, y);
							}
						}
						
						if(icon == null){
							canvas.drawCircle(x, y, 4, pCircle);
						}else{
							canvas.drawBitmapIcon(icon, x, y, null);
						}
						prev_x = x;
						prev_y = y;
					}
				}
				
				canvas.drawPath(linePath, p);
			}
			long curTime = System.currentTimeMillis();
			long gapTime = curTime - animStartTime;
			long animDuration = mLineGraphVO.getAnimation().getDuration();
			if(gapTime >= animDuration){
				gapTime = animDuration;
				isDirty = false;
			}
			
			anim = mLineGraphVO.getArrGraph().get(0).getCoordinateArr().length * (float)gapTime/(float)animDuration;
			
//			Log.e(TAG,"curTime = " + curTime + " , animStartTime = " + animStartTime);
//			Log.e(TAG,"anim = " + anim + " , gapTime = " + gapTime);
		}
		
		/**
		 * draw X Mark
		 */
		private void drawXMark(GraphCanvasWrapper canvas) {
			float x = 0;
			float y = 0;
			
			float xGap = xLength/(mLineGraphVO.getArrGraph().get(0).getCoordinateArr().length-1);
			for (int i = 0; i < mLineGraphVO.getArrGraph().get(0).getCoordinateArr().length; i++) {
			        x = xGap * i;
			        y = yLength * mLineGraphVO.getArrGraph().get(0).getCoordinateArr()[i]/mLineGraphVO.getMaxValue();
			        
			    canvas.drawLine(x, 0, x, -10, pBaseLine);
			}
		}
		
		/**
		 * draw Y Mark
		 */
		private void drawYMark(GraphCanvasWrapper canvas) {
			for (int i = 0; mLineGraphVO.getIncrement() * i <= mLineGraphVO.getMaxValue(); i++) {
				
				float y = yLength * mLineGraphVO.getIncrement() * i/mLineGraphVO.getMaxValue();
				
				canvas.drawLine(0, y, -10, y, pBaseLine);
			}
		}
		
		/**
		 * draw X Text
		 */
		private void drawXText(GraphCanvasWrapper canvas) {
			float x = 0;
			float y = 0;
			
			float xGap = xLength/(mLineGraphVO.getArrGraph().get(0).getCoordinateArr().length-1);
			for (int i = 0; i < mLineGraphVO.getLegendArr().length; i++) {
			        x = xGap * i;
			        
			        String text = mLineGraphVO.getLegendArr()[i];
			        pMarkText.measureText(text);
			        pMarkText.setTextSize(20);
					Rect rect = new Rect();
					pMarkText.getTextBounds(text, 0, text.length(), rect);
					
			    canvas.drawText(text, x -(rect.width()/2), -(20 + rect.height()), pMarkText);
			}
		}
		
		/**
		 * draw Y Text
		 */
		private void drawYText(GraphCanvasWrapper canvas) {
			for (int i = 0; mLineGraphVO.getIncrement() * i <= mLineGraphVO.getMaxValue(); i++) {
				
				String mark = Float.toString(mLineGraphVO.getIncrement() * i);
				float y = yLength * mLineGraphVO.getIncrement() * i/mLineGraphVO.getMaxValue();
				pMarkText.measureText(mark);
				pMarkText.setTextSize(20);
				Rect rect = new Rect();
				pMarkText.getTextBounds(mark, 0, mark.length(), rect);
//				Log.e(TAG, "rect = height()" + rect.height());
//				Log.e(TAG, "rect = width()" + rect.width());
				canvas.drawText(mark, -(rect.width() + 20), y-rect.height()/2, pMarkText);
			}
		}
	}
}
