package com.handstudio.android.hzgrapherlib.graphview;

import java.util.WeakHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
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
import com.handstudio.android.hzgrapherlib.util.Converter;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraphVO;

public class RadarGraphView extends SurfaceView implements Callback{

	public static final String TAG = "RadarGraphView";
	private SurfaceHolder mHolder;
	private DrawThread mDrawThread;
	
	private LineGraphVO mRadarGraphVO = null;
	
	private int fieldCount = 8;
	private int chartSize = 300;
	
	
	//Constructor
	public RadarGraphView(Context context, LineGraphVO vo) {
		super(context);
		mRadarGraphVO = vo;
		initView(context, vo);
	}
	
	public RadarGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context, attrs, 0);
	}
	
	public RadarGraphView(Context context, AttributeSet attrs, int defStyle) {
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
		int xLength = width - (mRadarGraphVO.getPaddingLeft() + mRadarGraphVO.getPaddingRight() + mRadarGraphVO.getMarginRight());
		int yLength = height - (mRadarGraphVO.getPaddingBottom() + mRadarGraphVO.getPaddingTop() + mRadarGraphVO.getMarginTop());
		
		//chart length
		int chartXLength = width - (mRadarGraphVO.getPaddingLeft() + mRadarGraphVO.getPaddingRight());
		int chartYLength = height - (mRadarGraphVO.getPaddingBottom() + mRadarGraphVO.getPaddingTop());
		
		//chartCenter
		PointF chartCenter = new PointF(width/2, height/2);
		
		Paint p = new Paint();
		Paint pCircle = new Paint();
		Paint pCrossLine = new Paint();
		Paint pBaseLine = new Paint();
		Paint pMarkText = new Paint();
		
		//animation
		float anim = 0.0f;
		boolean isAnimation = false;
		boolean isDrawRegion = false;
		long animStartTime = -1;
		
		WeakHashMap<Integer, Bitmap> arrIcon = new WeakHashMap<Integer, Bitmap>();
		Bitmap bg = null;
		public DrawThread(SurfaceHolder holder, Context context) {
			mHolder = holder;
			mCtx = context;
			
			int size = mRadarGraphVO.getArrGraph().size();
			for (int i = 0; i < size; i++) {
				int bitmapResource = mRadarGraphVO.getArrGraph().get(i).getBitmapResource();
				if(bitmapResource != -1){
					arrIcon.put(i, BitmapFactory.decodeResource(getResources(), bitmapResource));
				}else{
					if(arrIcon.get(i) != null){
						arrIcon.remove(i);
					}
				}
			}
			int bgResource = mRadarGraphVO.getGraphBG();
			if(bgResource != -1){
				Bitmap tempBg = BitmapFactory.decodeResource(getResources(), bgResource);
				bg = Bitmap.createScaledBitmap(tempBg, width, height, true);
				tempBg.recycle();
			}
		}
		
		public void setRunFlag(boolean bool){
			isRun = bool;
		}
		
		@Override
		public void run() {
			Canvas canvas = null;
			GraphCanvasWrapper graphCanvasWrapper = null;
			Log.e(TAG,"height = " + height);
			Log.e(TAG,"width = " + width);
			
			setPaint();
			isAnimation();
			isDrawRegion();
			
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
				
				canvas = mHolder.lockCanvas();
				graphCanvasWrapper = new GraphCanvasWrapper(canvas, width, height, mRadarGraphVO.getPaddingLeft(), mRadarGraphVO.getPaddingBottom());
				
				synchronized(mHolder){
					synchronized (touchLock) {
						
						try {
							//bg color
							canvas.drawColor(Color.WHITE);
							if(bg != null){
								canvas.drawBitmap(bg, 0, 0, null);
							}

							//TODO draw cross line
							drawCrossLine(graphCanvasWrapper);
							
							//TODO x coord dot line
							drawBaseLine(graphCanvasWrapper);
							
//							//TODO draw outline
							
							//TODO draw text
							drawYText(graphCanvasWrapper);
							
							//Graph
//							drawGraphRegion(graphCanvasWrapper);
//							drawGraph(graphCanvasWrapper);
							
							
							drawGraphName(canvas);
							
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
				calcTimePass();
			}
			
			
		}
		
		private void calcTimePass(){
			long curTime = System.currentTimeMillis();
			long gapTime = curTime - animStartTime;
			long animDuration = mRadarGraphVO.getAnimation().getDuration();
			if(gapTime >= animDuration){
				gapTime = animDuration;
				isDirty = false;
			}
			
			anim = mRadarGraphVO.getArrGraph().get(0).getCoordinateArr().length * (float)gapTime/(float)animDuration;
			
//			Log.e(TAG,"curTime = " + curTime + " , animStartTime = " + animStartTime);
//			Log.e(TAG,"anim = " + anim + " , gapTime = " + gapTime);
		}

		private void drawGraphName(Canvas canvas) {
			GraphNameBox gnb = mRadarGraphVO.getGraphNameBox();
			if(gnb != null){
				int nameboxWidth = 0;
				int nameboxHeight = 0;
				
				int nameboxIconWidth = gnb.getNameboxIconWidth();
				int nameboxIconHeight = gnb.getNameboxIconHeight();
				
				int nameboxMarginTop = gnb.getNameboxMarginTop();
				int nameboxMarginRight = gnb.getNameboxMarginRight();
				int nameboxPadding = gnb.getNameboxPadding();
				
				int nameboxTextIconMargin = gnb.getNameboxIconMargin();
				int nameboxIconMargin = gnb.getNameboxIconMargin();
				int nameboxTextSize = gnb.getNameboxTextSize(); 
				
				int maxTextWidth = 0;
				int maxTextHeight = 0;
				
				Paint nameRextPaint = new Paint();
				nameRextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
				nameRextPaint.setAntiAlias(true); //text anti alias
				nameRextPaint.setFilterBitmap(true); // bitmap anti alias
				nameRextPaint.setColor(Color.BLUE);
				nameRextPaint.setStrokeWidth(3);
				nameRextPaint.setStyle(Style.STROKE);
				
				Paint pIcon = new Paint();
				pIcon.setFlags(Paint.ANTI_ALIAS_FLAG);
				pIcon.setAntiAlias(true); //text anti alias
				pIcon.setFilterBitmap(true); // bitmap anti alias
				pIcon.setColor(Color.BLUE);
				pIcon.setStrokeWidth(3);
				pIcon.setStyle(Style.FILL_AND_STROKE);
				
				
				Paint pNameText = new Paint();
				pNameText.setFlags(Paint.ANTI_ALIAS_FLAG);
				pNameText.setAntiAlias(true); //text anti alias
				pNameText.setTextSize(nameboxTextSize);
				pNameText.setColor(Color.BLACK); 
				
				
				int graphSize = mRadarGraphVO.getArrGraph().size();
				for (int i = 0; i < graphSize; i++) {
					
					
					String text = mRadarGraphVO.getArrGraph().get(i).getName();
					Rect rect = new Rect();
					pNameText.getTextBounds(text, 0, text.length(), rect);
					
					if(rect.width() > maxTextWidth){
						maxTextWidth = rect.width();
						maxTextHeight = rect.height();
					}
					
					mRadarGraphVO.getArrGraph().get(i).getName();
					
				}
				mRadarGraphVO.getArrGraph().get(0).getName();
				nameboxWidth = 1 * maxTextWidth + nameboxTextIconMargin + nameboxIconWidth;
				int maxCellHight = maxTextHeight;
				if(nameboxIconHeight > maxTextHeight){
					maxCellHight = nameboxIconHeight;
				}
				nameboxHeight = graphSize * maxCellHight + (graphSize-1) * nameboxIconMargin;
				
				canvas.drawRect(width - (nameboxMarginRight + nameboxWidth) - nameboxPadding*2,
						nameboxMarginTop, width - nameboxMarginRight, nameboxMarginTop + nameboxHeight + nameboxPadding*2, nameRextPaint);
				
				for (int i = 0; i < graphSize; i++) {
					
					pIcon.setColor(mRadarGraphVO.getArrGraph().get(i).getColor());
					canvas.drawRect(width - (nameboxMarginRight + nameboxWidth) - nameboxPadding,
							nameboxMarginTop + (maxCellHight * i) + nameboxPadding + (nameboxIconMargin * i), 
							width - (nameboxMarginRight + maxTextWidth) - nameboxPadding - nameboxTextIconMargin, 
							nameboxMarginTop + maxCellHight * (i+1) + nameboxPadding + nameboxIconMargin * i, pIcon);
					
					String text = mRadarGraphVO.getArrGraph().get(i).getName();
					canvas.drawText(text, width - (nameboxMarginRight + maxTextWidth) - nameboxPadding, 
							nameboxMarginTop + maxTextHeight/2 + maxCellHight * i + maxCellHight/2 + nameboxPadding  + nameboxIconMargin * i, pNameText);
				}
			}
		}

		/**
		 * check graph line animation
		 */
		private void isAnimation() {
			if(mRadarGraphVO.getAnimation() != null){
				isAnimation = true;
			}else{
				isAnimation = false;
			}
		}
		
		/**
		 * check graph line region animation
		 */
		private void isDrawRegion() {
			if(mRadarGraphVO.isDrawRegion()){
				isDrawRegion = true;
			}else{
				isDrawRegion = false;
			}
		}
		
		private void drawCrossLine(GraphCanvasWrapper graphCanvas) {
			PointF dot = new PointF(chartCenter.x + 00, chartCenter.y - chartSize);
			for (int i = 0; i < fieldCount; i++) {
				float radAngle = (float) (Converter.DegreeToRadian(360/fieldCount * (i))); // use radian
				PointF rotateDot = getRotatePoint(dot, radAngle);
				
				graphCanvas.getCanvas().drawLine(chartCenter.x, chartCenter.y,
						rotateDot.x, rotateDot.y, pCrossLine);
			}
			
//			graphCanvas.getCanvas().drawCircle(chartCenter.x, chartCenter.y, 4, pCircle);
		}

		/**
		 * calc rotated point
		 * @param dot
		 * @param radAngle
		 */
		private PointF getRotatePoint(PointF dot, float radAngle) {
			PointF rotateDot = new PointF();
			rotateDot.x = (float) (chartCenter.x + (dot.x-chartCenter.x)*Math.cos(radAngle) - (dot.y-chartCenter.y)*Math.sin(radAngle));
			rotateDot.y = (float) (chartCenter.y + (dot.x-chartCenter.x)*Math.sin(radAngle) + (dot.y-chartCenter.y)*Math.cos(radAngle));
			return rotateDot;
		}
		

		private void drawBaseLine(GraphCanvasWrapper graphCanvas) {
//			for (int i = 1; mRadarGraphVO.getIncrement() * i <= mRadarGraphVO.getMaxValue(); i++) {
//				
//				float y = yLength * mRadarGraphVO.getIncrement() * i/mRadarGraphVO.getMaxValue();
//				
//				graphCanvas.drawLine(0, y, chartXLength, y, pBaseLine);
//			}
			
			int baselineCount = 5;
			PointF dot[] = {
					new PointF(chartCenter.x + 00, chartCenter.y - chartSize / baselineCount * 1),
					new PointF(chartCenter.x + 00, chartCenter.y - chartSize / baselineCount * 2),
					new PointF(chartCenter.x + 00, chartCenter.y - chartSize / baselineCount * 3),
					new PointF(chartCenter.x + 00, chartCenter.y - chartSize / baselineCount * 4),
					new PointF(chartCenter.x + 00, chartCenter.y - chartSize / baselineCount * 5)};
			Path linePath[] = new Path[baselineCount];
			
			for (int i = 0; i <= fieldCount; i++) {
				float radAngle = (float) (Converter.DegreeToRadian(360/fieldCount * (i))); // use radian
				for (int j = 0; j < baselineCount; j++) {
					PointF rotateDot = getRotatePoint(dot[j], radAngle);
					if(i == 0){
						linePath[j] = new Path();
						linePath[j].moveTo(rotateDot.x, rotateDot.y);
					}else{
						linePath[j].lineTo(rotateDot.x, rotateDot.y);
					}
					graphCanvas.getCanvas().drawPath(linePath[j], pBaseLine);
				}
//				graphCanvas.getCanvas().drawLine(chartCenter.x, chartCenter.y,
//						rotateDot.x, rotateDot.y, pCrossLine);
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
			
			pCrossLine = new Paint();
			pCrossLine.setFlags(Paint.ANTI_ALIAS_FLAG);
			pCrossLine.setAntiAlias(true); //text anti alias
			pCrossLine.setFilterBitmap(true); // bitmap anti alias
			pCrossLine.setColor(Color.GRAY);
			pCrossLine.setStrokeWidth(3);
			
			pBaseLine = new Paint();
			pBaseLine.setFlags(Paint.ANTI_ALIAS_FLAG);
			pBaseLine.setAntiAlias(true); //text anti alias
			pBaseLine.setFilterBitmap(true); // bitmap anti alias
			pBaseLine.setColor(0xffcccccc);
			pBaseLine.setStrokeWidth(3);
			pBaseLine.setStyle(Style.STROKE);
			pBaseLine.setPathEffect(new DashPathEffect(new float[] {10,5}, 0));
			
			pMarkText = new Paint();
			pMarkText.setFlags(Paint.ANTI_ALIAS_FLAG);
			pMarkText.setAntiAlias(true); //text anti alias
			pMarkText.setColor(Color.BLACK); 
		}

		
		/**
		 * draw Graph
		 */
		private void drawGraph(GraphCanvasWrapper graphCanvas) {
			
			if (isAnimation){
				drawGraphWithAnimation(graphCanvas);
			}else{
				drawGraphWithoutAnimation(graphCanvas);
			}
		}
		
		/**
		 *	draw graph without animation 
		 */
		private void drawGraphWithoutAnimation(GraphCanvasWrapper graphCanvas) {
			
			for (int i = 0; i < mRadarGraphVO.getArrGraph().size(); i++) {
				GraphPath linePath = new GraphPath(width, height, mRadarGraphVO.getPaddingLeft(), mRadarGraphVO.getPaddingBottom());
				GraphPath regionPath = new GraphPath(width, height, mRadarGraphVO.getPaddingLeft(), mRadarGraphVO.getPaddingBottom());
				boolean firstSet = false;
				float x = 0;
				float y = 0;
				p.setColor(mRadarGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mRadarGraphVO.getArrGraph().get(i).getColor());
				float xGap = xLength/(mRadarGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
				
				Bitmap icon = arrIcon.get(i);
				
				for (int j = 0; j < mRadarGraphVO.getArrGraph().get(i).getCoordinateArr().length; j++) {
					if(j < mRadarGraphVO.getArrGraph().get(i).getCoordinateArr().length){
						
						if (!firstSet) {
							
							x = xGap * j ;
							y = yLength * mRadarGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mRadarGraphVO.getMaxValue();
							
							linePath.moveTo(x, y);
							
							firstSet = true;
						} else {
							x = xGap * j;
							y = yLength * mRadarGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mRadarGraphVO.getMaxValue();
							
							linePath.lineTo(x, y);
						}
						
						if(icon == null){
							graphCanvas.drawCircle(x, y, 4, pCircle);
						}else{
							graphCanvas.drawBitmapIcon(icon, x, y, null);
						}
					}
				}
				
				graphCanvas.getCanvas().drawPath(linePath, p);
			}
		}

		/**
		 *	draw graph with animation 
		 */
		private void drawGraphWithAnimation(GraphCanvasWrapper graphCanvas) {
			//for draw animation
			float prev_x = 0;
			float prev_y = 0;
			
			float next_x = 0;
			float next_y = 0;
			
			float value = 0;
			float mode = 0;
			
			for (int i = 0; i < mRadarGraphVO.getArrGraph().size(); i++) {
				GraphPath linePath = new GraphPath(width, height, mRadarGraphVO.getPaddingLeft(), mRadarGraphVO.getPaddingBottom());
				GraphPath regionPath = new GraphPath(width, height, mRadarGraphVO.getPaddingLeft(), mRadarGraphVO.getPaddingBottom());
				boolean firstSet = false;
				float x = 0;
				float y = 0;
				p.setColor(mRadarGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mRadarGraphVO.getArrGraph().get(i).getColor());
				float xGap = xLength/(mRadarGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
				
				Bitmap icon = arrIcon.get(i);
				value = anim/1;
				mode = anim %1;
				
				for (int j = 0; j < value+1; j++) {
					if(j < mRadarGraphVO.getArrGraph().get(i).getCoordinateArr().length){
						
						if (!firstSet) {
							
							x = xGap * j ;
							y = yLength * mRadarGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mRadarGraphVO.getMaxValue();
							
							linePath.moveTo(x, y);
							
							firstSet = true;
						} else {
							x = xGap * j;
							y = yLength * mRadarGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mRadarGraphVO.getMaxValue();
							
							if( j > value ){
								next_x = x - prev_x;
								next_y = y - prev_y;
								
								linePath.lineTo(prev_x + next_x * mode, prev_y + next_y * mode);
							}else{
								linePath.lineTo(x, y);
							}
						}
						
						if(icon == null){
							graphCanvas.drawCircle(x, y, 4, pCircle);
						}else{
							graphCanvas.drawBitmapIcon(icon, x, y, null);
						}
						prev_x = x;
						prev_y = y;
					}
				}
				
				graphCanvas.getCanvas().drawPath(linePath, p);
			}
		}
		
		/**
		 * draw Y Text
		 */
		private void drawYText(GraphCanvasWrapper graphCanvas) {
//			for (int i = 0; mRadarGraphVO.getIncrement() * i <= mRadarGraphVO.getMaxValue(); i++) {
//				
//				String mark = Float.toString(mRadarGraphVO.getIncrement() * i);
//				float y = yLength * mRadarGraphVO.getIncrement() * i/mRadarGraphVO.getMaxValue();
//				pMarkText.measureText(mark);
//				pMarkText.setTextSize(20);
//				Rect rect = new Rect();
//				pMarkText.getTextBounds(mark, 0, mark.length(), rect);
////				Log.e(TAG, "rect = height()" + rect.height());
////				Log.e(TAG, "rect = width()" + rect.width());
//				graphCanvas.drawText(mark, -(rect.width() + 20), y-rect.height()/2, pMarkText);
//			}
		}
	}
}
