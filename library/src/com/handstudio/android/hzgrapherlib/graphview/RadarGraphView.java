package com.handstudio.android.hzgrapherlib.graphview;

import java.util.WeakHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.handstudio.android.hzgrapherlib.error.ErrorCode;
import com.handstudio.android.hzgrapherlib.error.ErrorDetector;
import com.handstudio.android.hzgrapherlib.util.Converter;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.radargraph.RadarGraphVO;

public class RadarGraphView extends SurfaceView implements Callback{

	public static final String TAG = "RadarGraphView";
	private SurfaceHolder mHolder;
	private DrawThread mDrawThread;
	
	private RadarGraphVO mRadarGraphVO = null;
	
	public static final float MAX_VALUE = 100;
	private int fieldCount = 8;
	private int baselineCount = 5;
	
	
	//Constructor
	public RadarGraphView(Context context, RadarGraphVO vo) {
		super(context);
		mRadarGraphVO = vo;
		initView(context, vo);
	}
	
	private void initView(Context context, RadarGraphVO vo) {
		ErrorCode ec = ErrorDetector.checkGraphObject(vo);
		ec.printError();
		
		mHolder = getHolder();
		mHolder.addCallback(this);
		
		fieldCount = vo.getArrGraph().get(0).getCoordinateArr().length;
		baselineCount = vo.getMaxValue() / vo.getIncrement();
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
		
		float chartSize = 0;
		
		//chartCenter
		PointF chartCenter = new PointF(width/2, height/2);
		
		Paint pGraphColor = new Paint();
		Paint pGraphRegionColor = new Paint();
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
			
			getChartSize();
		}

		private void getChartSize() {
			if(chartXLength <= chartYLength){
				chartSize = chartXLength/2; 
			}else{
				chartSize = chartYLength/2;
			}
		}
		
		public void setRunFlag(boolean bool){
			isRun = bool;
		}
		
		@Override
		public void run() {
			Canvas canvas = null;
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
				
				try {
					Thread.sleep(0000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				calcTimePass();
				
				synchronized(mHolder){
					synchronized (touchLock) {
						
						try {
							//bg color
							canvas.drawColor(Color.WHITE);
							if(bg != null){
								canvas.drawBitmap(bg, 0, 0, null);
							}

							//TODO draw cross line
							drawCrossLine(canvas);
							
							//TODO x coord dot line
							drawBaseLine(canvas);
							drawBaseLineText(canvas);
							
//							//TODO draw outline
							
							//TODO draw text
							drawLegendText(canvas);
							
							//Graph
							drawGraphRegion(canvas);
							drawGraph(canvas);
							
							
							drawGraphName(canvas);
							
//							isDirty = false;
							

						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if(canvas != null){
								mHolder.unlockCanvasAndPost(canvas);
							}
						}
						
					}
				}
			}
			
			
		}
		
		private void calcTimePass(){
			if(isAnimation){
				long curTime = System.currentTimeMillis();
				long gapTime = curTime - animStartTime;
				long animDuration = mRadarGraphVO.getAnimation().getDuration();
				if(gapTime >= animDuration){
					gapTime = animDuration;
					isDirty = false;
				}
				
				anim = mRadarGraphVO.getArrGraph().get(0).getCoordinateArr().length * (float)gapTime/(float)animDuration;
//				anim = anim+0.01f;
			}else{
				isDirty = false;
			}
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
		
		private void drawCrossLine(Canvas canvas) {
			PointF dot = new PointF(chartCenter.x + 00, chartCenter.y - chartSize);
			for (int i = 0; i < fieldCount; i++) {
				float radAngle = (float) (Converter.DegreeToRadian(360/fieldCount * (i))); // use radian
				PointF rotateDot = getRotatePoint(dot, radAngle);
				
				canvas.drawLine(chartCenter.x, chartCenter.y,
						rotateDot.x, rotateDot.y, pCrossLine);
			}
			
//			canvas.drawCircle(chartCenter.x, chartCenter.y, 4, pCircle);
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
		

		private void drawBaseLine(Canvas canvas) {
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
					canvas.drawPath(linePath[j], pBaseLine);
				}
			}
		}
		
		private void drawBaseLineText(Canvas canvas) {
			
			PointF dot[] = {
					new PointF(chartCenter.x + 00, chartCenter.y - chartSize / baselineCount * 1),
					new PointF(chartCenter.x + 00, chartCenter.y - chartSize / baselineCount * 2),
					new PointF(chartCenter.x + 00, chartCenter.y - chartSize / baselineCount * 3),
					new PointF(chartCenter.x + 00, chartCenter.y - chartSize / baselineCount * 4),
					new PointF(chartCenter.x + 00, chartCenter.y - chartSize / baselineCount * 5)};
			
			for (int i = 0; i < baselineCount; i++) {
				PointF rotateDot = dot[i];

				String mark = Integer.toString(mRadarGraphVO.getIncrement() * (i+1));
				pMarkText.measureText(mark);
				pMarkText.setTextSize(20);
				Rect rect = new Rect();
				pMarkText.getTextBounds(mark, 0, mark.length(), rect);
				canvas.drawText(mark, rotateDot.x -(rect.width() + 20), rotateDot.y + rect.height()/2, pMarkText);
			}
		}

		/**
		 * set graph line color
		 */
		private void setPaint() {
			pGraphColor = new Paint();
			pGraphColor.setFlags(Paint.ANTI_ALIAS_FLAG);
			pGraphColor.setAntiAlias(true); //text anti alias
			pGraphColor.setFilterBitmap(true); // bitmap anti alias
			pGraphColor.setColor(Color.BLUE);
			pGraphColor.setStrokeWidth(3);
			pGraphColor.setStyle(Style.STROKE);
			
			pGraphRegionColor = new Paint();
			pGraphRegionColor.setFlags(Paint.ANTI_ALIAS_FLAG);
			pGraphRegionColor.setAntiAlias(true); //text anti alias
			pGraphRegionColor.setFilterBitmap(true); // bitmap anti alias
			pGraphRegionColor.setColor(Color.BLUE);
			pGraphRegionColor.setAlpha(127);
			pGraphRegionColor.setStrokeWidth(1);
			pGraphRegionColor.setStyle(Style.FILL_AND_STROKE);
			
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
		 * draw Graph Region
		 */
		private void drawGraphRegion(Canvas canvas) {
			if(isDrawRegion){
				if (isAnimation){
					drawGraphRegionWithAnimation(canvas);
				}else{
					drawGraphRegionWithoutAnimation(canvas);
				}
			}
		}
		
		/**
		 * draw Graph
		 */
		private void drawGraph(Canvas canvas) {
			
			if (isAnimation){
				drawGraphWithAnimation(canvas);
			}else{
				drawGraphWithoutAnimation(canvas);
			}
		}
		
		/**
		 *	draw graph region without animation 
		 */
		private void drawGraphRegionWithoutAnimation(Canvas canvas) {
			
			for (int i = 0; i < mRadarGraphVO.getArrGraph().size(); i++) {
				float[] graph = mRadarGraphVO.getArrGraph().get(i).getCoordinateArr();
				pGraphRegionColor.setColor(mRadarGraphVO.getArrGraph().get(i).getColor());
				pGraphRegionColor.setAlpha(127);
				Path lineRegionPath = new Path();
				
				for (int j = 0; j < fieldCount; j++) {

					PointF dot = new PointF(chartCenter.x + 00, chartCenter.y - graph[j % fieldCount] / MAX_VALUE * chartSize);
					float radAngle = (float) (Converter.DegreeToRadian(360/fieldCount * (j % fieldCount))); // use radian
					PointF rotateDot = getRotatePoint(dot, radAngle);
					
					if (j == 0) {
						lineRegionPath.moveTo(rotateDot.x, rotateDot.y);
					} else {
						lineRegionPath.lineTo(rotateDot.x, rotateDot.y);
					}
				}
				canvas.drawPath(lineRegionPath, pGraphRegionColor);
			}
		}
		
		/**
		 *	draw graph without animation 
		 */
		private void drawGraphWithoutAnimation(Canvas canvas) {
			
			for (int i = 0; i < mRadarGraphVO.getArrGraph().size(); i++) {
				float[] graph = mRadarGraphVO.getArrGraph().get(i).getCoordinateArr();
				pGraphColor.setColor(mRadarGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mRadarGraphVO.getArrGraph().get(i).getColor());
				Bitmap icon = arrIcon.get(i);
				Path linePath = new Path();
				
				PointF first = new PointF();
				for (int j = 0; j <= fieldCount; j++) {
					
					PointF dot = new PointF(chartCenter.x + 00, chartCenter.y - graph[j % fieldCount] / MAX_VALUE * chartSize);
					float radAngle = (float) (Converter.DegreeToRadian(360/fieldCount * (j % fieldCount))); // use radian
					PointF rotateDot = getRotatePoint(dot, radAngle);
					
					if(j< fieldCount){
						if(icon == null){
							canvas.drawCircle(rotateDot.x, rotateDot.y, 4, pCircle);
						}else{
							canvas.drawBitmap(icon, rotateDot.x - icon.getWidth()/2,
									rotateDot.y - icon.getHeight()/2, null);
						}
					}

					if (j == 0) {
						first.x = rotateDot.x;
						first.y = rotateDot.y;
						linePath.moveTo(rotateDot.x, rotateDot.y);
					}else{
						linePath.lineTo(rotateDot.x, rotateDot.y);
					}
				}
				canvas.drawPath(linePath, pGraphColor);
			}
		}

		/**
		 *	draw graph region with animation 
		 */
		private void drawGraphRegionWithAnimation(Canvas canvas) {
			//for draw animation
			float prev_x = 0;
			float prev_y = 0;
			
			float next_x = 0;
			float next_y = 0;
			
			int value = 0;
			float mode = 0;
			
			for (int i = 0; i < mRadarGraphVO.getArrGraph().size(); i++) {
				float[] graph = mRadarGraphVO.getArrGraph().get(i).getCoordinateArr();
				pGraphRegionColor.setColor(mRadarGraphVO.getArrGraph().get(i).getColor());
				pGraphRegionColor.setAlpha(127);
				Path lineRegionPath = new Path();
				
				value = (int) (anim/1);
				mode = anim %1;
				
//				Log.e("", "value = " + value + "\t ,mode = " + mode);
				
				for (int j = 0; j <=  value+1; j++) {

					PointF dot = new PointF(chartCenter.x + 00, chartCenter.y - graph[j % fieldCount] / MAX_VALUE * chartSize);
					float radAngle = (float) (Converter.DegreeToRadian(360/fieldCount * (j % fieldCount))); // use radian
					PointF rotateDot = getRotatePoint(dot, radAngle);
					
					if (j == 0) {
						lineRegionPath.moveTo(rotateDot.x, rotateDot.y);
					} else {
						if( j > value){
							next_x = rotateDot.x - prev_x;
							next_y = rotateDot.y - prev_y;
							
							lineRegionPath.lineTo(prev_x + next_x * mode, prev_y + next_y * mode);
							lineRegionPath.lineTo(chartCenter.x, chartCenter.y);
						}else{
							lineRegionPath.lineTo(rotateDot.x, rotateDot.y);
						}
					}
					prev_x = rotateDot.x;
					prev_y = rotateDot.y;
				}
				canvas.drawPath(lineRegionPath, pGraphRegionColor);
			}
		}
		
		/**
		 *	draw graph with animation 
		 */
		private void drawGraphWithAnimation(Canvas canvas) {
			//for draw animation
			float prev_x = 0;
			float prev_y = 0;
			
			float next_x = 0;
			float next_y = 0;
			
			float value = 0;
			float mode = 0;
			
			for (int i = 0; i < mRadarGraphVO.getArrGraph().size(); i++) {
				float[] graph = mRadarGraphVO.getArrGraph().get(i).getCoordinateArr();
				pGraphColor.setColor(mRadarGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mRadarGraphVO.getArrGraph().get(i).getColor());
				Bitmap icon = arrIcon.get(i);
				Path linePath = new Path();
				
				PointF first = new PointF();
				
				value = anim/1;
				mode = anim %1;
				
				for (int j = 0; j < value+1; j++) {
					
					PointF dot = new PointF(chartCenter.x + 00, chartCenter.y - graph[j % fieldCount] / MAX_VALUE * chartSize);
					float radAngle = (float) (Converter.DegreeToRadian(360/fieldCount * (j % fieldCount))); // use radian
					PointF rotateDot = getRotatePoint(dot, radAngle);
										
					if(j< fieldCount){
						if(icon == null){
							canvas.drawCircle(rotateDot.x, rotateDot.y, 4, pCircle);
						}else{
							canvas.drawBitmap(icon, rotateDot.x - icon.getWidth()/2,
									rotateDot.y - icon.getHeight()/2, null);
						}
					}

					if (j == 0) {
						first.x = rotateDot.x;
						first.y = rotateDot.y;
						linePath.moveTo(rotateDot.x, rotateDot.y);
					}else{
						if( j > value){
							next_x = rotateDot.x - prev_x;
							next_y = rotateDot.y - prev_y;
							
							linePath.lineTo(prev_x + next_x * mode, prev_y + next_y * mode);
						}else{
							linePath.lineTo(rotateDot.x, rotateDot.y);
						}
					}
					prev_x = rotateDot.x;
					prev_y = rotateDot.y;
				}
				canvas.drawPath(linePath, pGraphColor);
			}
		}
		
		/**
		 * draw Legend Text
		 */
		private void drawLegendText(Canvas canvas) {

			for (int j = 0; j < fieldCount; j++) {

				PointF textPoint = new PointF(chartCenter.x + 00, chartCenter.y - chartSize);
				float radAngle = (float) (Converter.DegreeToRadian(360/fieldCount * (j))); // use radian
				PointF rotateDot = getRotatePoint(textPoint, radAngle);

				String legend = mRadarGraphVO.getLegendArr()[j];
				pMarkText.measureText(legend);
				pMarkText.setTextSize(20);
				Rect rect = new Rect();
				pMarkText.getTextBounds(legend, 0, legend.length(), rect);

				if(radAngle >= Converter.DegreeToRadian(180)){
					canvas.drawText(legend, rotateDot.x -(rect.width() + 20), rotateDot.y + rect.height(), pMarkText);
				}else{
					canvas.drawText(legend, rotateDot.x + (20), rotateDot.y - rect.height()/2, pMarkText);
				}
			}

		}
	}
}
