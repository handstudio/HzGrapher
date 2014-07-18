package com.handstudio.android.hzgrapherlib.graphview;

import java.util.ArrayList;
import java.util.WeakHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
import com.handstudio.android.hzgrapherlib.canvas.GraphCanvasWrapper;
import com.handstudio.android.hzgrapherlib.error.ErrorCode;
import com.handstudio.android.hzgrapherlib.error.ErrorDetector;
import com.handstudio.android.hzgrapherlib.path.GraphPath;
import com.handstudio.android.hzgrapherlib.util.Spline;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.curvegraph.CurveGraphVO;

public class CurveCompareGraphView extends SurfaceView implements Callback{

	public static final String TAG = "CurveComapreGraphView";
	private SurfaceHolder mHolder;
	private DrawThread mDrawThread;
	
	private CurveGraphVO mCurveGraphVO = null;
	private Spline spline = null;
	
	//Constructor
	public CurveCompareGraphView(Context context, CurveGraphVO vo) {
		super(context);
		mCurveGraphVO = vo;
		initView(context, vo);
	}
	
	private void initView(Context context, CurveGraphVO vo) {
		ErrorCode ec = ErrorDetector.checkLineCompareGraphObject(vo);
		ec.printError();
		
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
		Bitmap b = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
		SurfaceHolder mHolder;
		Context mCtx;
		
		boolean isRun = true;
		boolean isDirty = true;
		
		Matrix matrix = new Matrix();
		
		int height = getHeight();
		int width = getWidth();
		
		//graph length
		int xLength = width - (mCurveGraphVO.getPaddingLeft() + mCurveGraphVO.getPaddingRight() + mCurveGraphVO.getMarginRight());
		int yLength = height - (mCurveGraphVO.getPaddingBottom() + mCurveGraphVO.getPaddingTop() + mCurveGraphVO.getMarginTop());
		
		//chart length
		int chartXLength = width - (mCurveGraphVO.getPaddingLeft() + mCurveGraphVO.getPaddingRight());
		int chartYLength = height - (mCurveGraphVO.getPaddingBottom() + mCurveGraphVO.getPaddingTop());
		
		Paint p = new Paint();
		Paint pCircle = new Paint();
		Paint pLine = new Paint();
		Paint pBaseLine = new Paint();
		Paint pBaseLineX = new Paint();
		Paint pMarkText = new Paint();
		
		//animation
		float anim = 0.0f;
		boolean isAnimation = false;
		long animStartTime = -1;
		int animationType = 0;
		
		WeakHashMap<Integer, Bitmap> arrIcon = new WeakHashMap<Integer, Bitmap>();
		Bitmap bg = null;
		public DrawThread(SurfaceHolder holder, Context context) {
			mHolder = holder;
			mCtx = context;
			
			int size = mCurveGraphVO.getArrGraph().size();
			for (int i = 0; i < size; i++) {
				int bitmapResource = mCurveGraphVO.getArrGraph().get(i).getBitmapResource();
				if(bitmapResource != -1){
					arrIcon.put(i, BitmapFactory.decodeResource(getResources(), bitmapResource));
				}else{
					if(arrIcon.get(i) != null){
						arrIcon.remove(i);
					}
				}
			}
			int bgResource = mCurveGraphVO.getGraphBG();
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
				graphCanvasWrapper = new GraphCanvasWrapper(canvas, width, height, mCurveGraphVO.getPaddingLeft(), mCurveGraphVO.getPaddingBottom());
				
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

							//x coord dot line
							drawBaseLine(graphCanvasWrapper);
							
							//y coord
							graphCanvasWrapper.drawLine(0, 0, 0, chartYLength, pBaseLine);
							
							//x coord
							graphCanvasWrapper.drawLine(0, 0, chartXLength, 0, pBaseLine);
							
							//x, y coord mark
							drawXMark(graphCanvasWrapper);
							drawYMark(graphCanvasWrapper);
							
							//x, y coord text
							drawXText(graphCanvasWrapper);
							drawYText(graphCanvasWrapper);
							
							//Graph
							drawGraphRegion(graphCanvasWrapper);
							drawGraph(graphCanvasWrapper);
							
							drawGraphName(canvas);
							
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if(graphCanvasWrapper.getCanvas() != null){
								mHolder.unlockCanvasAndPost(graphCanvasWrapper.getCanvas());
							}
						}
						
					}
				}
			}
		}
		
		/**
		 * time calculate
		 */
		private void calcTimePass(){
			if(isAnimation){
				long curTime = System.currentTimeMillis();
				long gapTime = curTime - animStartTime;
				long animDuration = mCurveGraphVO.getAnimation().getDuration();
				if(gapTime >= animDuration)
					gapTime = animDuration;
				
				anim = (float) gapTime / (float) animDuration;
			}else{
				isDirty = false;
			}
		}

		/**
		 * draw graph name
		 */
		private void drawGraphName(Canvas canvas) {
			GraphNameBox gnb = mCurveGraphVO.getGraphNameBox();
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
				
				
				int graphSize = mCurveGraphVO.getArrGraph().size();
				for (int i = 0; i < graphSize; i++) {
					
					String text = mCurveGraphVO.getArrGraph().get(i).getName();
					Rect rect = new Rect();
					pNameText.getTextBounds(text, 0, text.length(), rect);
					
					if(rect.width() > maxTextWidth){
						maxTextWidth = rect.width();
						maxTextHeight = rect.height();
					}
					
					mCurveGraphVO.getArrGraph().get(i).getName();
					
				}
				mCurveGraphVO.getArrGraph().get(0).getName();
				nameboxWidth = 1 * maxTextWidth + nameboxTextIconMargin + nameboxIconWidth;
				int maxCellHight = maxTextHeight;
				if(nameboxIconHeight > maxTextHeight){
					maxCellHight = nameboxIconHeight;
				}
				nameboxHeight = graphSize * maxCellHight + (graphSize-1) * nameboxIconMargin;
				
				canvas.drawRect(width - (nameboxMarginRight + nameboxWidth) - nameboxPadding*2,
						nameboxMarginTop, width - nameboxMarginRight, nameboxMarginTop + nameboxHeight + nameboxPadding*2, nameRextPaint);
				
				for (int i = 0; i < graphSize; i++) {
					
					pIcon.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());
					canvas.drawRect(width - (nameboxMarginRight + nameboxWidth) - nameboxPadding,
							nameboxMarginTop + (maxCellHight * i) + nameboxPadding + (nameboxIconMargin * i), 
							width - (nameboxMarginRight + maxTextWidth) - nameboxPadding - nameboxTextIconMargin, 
							nameboxMarginTop + maxCellHight * (i+1) + nameboxPadding + nameboxIconMargin * i, pIcon);
					
					String text = mCurveGraphVO.getArrGraph().get(i).getName();
					canvas.drawText(text, width - (nameboxMarginRight + maxTextWidth) - nameboxPadding, 
							nameboxMarginTop + maxTextHeight/2 + maxCellHight * i + maxCellHight/2 + nameboxPadding  + nameboxIconMargin * i, pNameText);
				}
			}
		}

		/**
		 * check graph line animation
		 */
		private void isAnimation() {
			if(mCurveGraphVO.getAnimation() != null){
				isAnimation = true;
			}else{
				isAnimation = false;
			}
			animationType = mCurveGraphVO.getAnimation().getAnimation();
		}
		
		/**
		 * draw Base Line
		 */
		private void drawBaseLine(GraphCanvasWrapper graphCanvas) {
			for (int i = 1; mCurveGraphVO.getIncrement() * i <= mCurveGraphVO.getMaxValue(); i++) {
				
				float y = yLength * mCurveGraphVO.getIncrement() * i/mCurveGraphVO.getMaxValue();
				
				graphCanvas.drawLine(0, y, chartXLength, y, pBaseLineX);
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
		 * draw Graph Region
		 */
		private void drawGraphRegion(GraphCanvasWrapper graphCanvas) {
			
			if (isAnimation){
				drawGraphCompareRegionWithAnimation(graphCanvas);
			}else{
				drawGraphCompareRegionWithoutAnimation(graphCanvas);
			}
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
			for (int i = 0; i < mCurveGraphVO.getArrGraph().size(); i++) {
				GraphPath curvePath = new GraphPath(width, height, mCurveGraphVO.getPaddingLeft(), mCurveGraphVO.getPaddingBottom());

				boolean firstSet = false;
				float xGap = xLength/(mCurveGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
				
				float[] x = setAxisX(xGap, i);
				float[] y = setAxisY(i);
				// Creates a monotone cubic spline from a given set of control points.
				spline = Spline.createMonotoneCubicSpline(x, y);
				
				p.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());

				Bitmap icon = arrIcon.get(i);
				
				// draw line
				for (float j = x[0]; j < x[x.length-1]; j++) {
					if (!firstSet) {
						curvePath.moveTo(j, spline.interpolate(j));
						firstSet = true;
					} else 
						curvePath.lineTo((j+1), spline.interpolate((j+1)));
				}
				
				// draw point
				for (int j = 0; j < mCurveGraphVO.getArrGraph().get(i).getCoordinateArr().length; j++) {
					float pointX = xGap * j;
					float pointY = yLength * mCurveGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mCurveGraphVO.getMaxValue();

					if(icon == null) 
						graphCanvas.drawCircle(pointX, pointY, 4, pCircle);
					else
						graphCanvas.drawBitmapIcon(icon, pointX, pointY, null);
					
				}
				
				graphCanvas.getCanvas().drawPath(curvePath, p);
			}
		}
		
		/**
		 *	draw graph with animation 
		 */
		private void drawGraphWithAnimation(GraphCanvasWrapper graphCanvas) {
			//for draw animation
			for (int i = 0; i < mCurveGraphVO.getArrGraph().size(); i++) {
				GraphPath curvePath = new GraphPath(width, height, mCurveGraphVO.getPaddingLeft(), mCurveGraphVO.getPaddingBottom());

				boolean firstSet = false;
				float xGap = xLength/(mCurveGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
				float pointNum = (mCurveGraphVO.getArrGraph().get(0).getCoordinateArr().length * anim) / 1;
				
				float[] x = setAxisX(xGap, i);
				float[] y = setAxisY(i);
				// Creates a monotone cubic spline from a given set of control points.
				spline = Spline.createMonotoneCubicSpline(x, y);
				
				p.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());

				Bitmap icon = arrIcon.get(i);

				// draw line
				for (float j = x[0]; j <= x[x.length-1]; j++) {
					if (!firstSet) {
						curvePath.moveTo(j, spline.interpolate(j));
						firstSet = true;
					} else 
						curvePath.lineTo(((j) * anim), spline.interpolate(((j) * anim)));
				}
				
				graphCanvas.getCanvas().drawPath(curvePath, p);

				// draw point
				for (int j = 0; j < pointNum+1; j++) {
					if(j < mCurveGraphVO.getArrGraph().get(i).getCoordinateArr().length){
						if(icon == null)
							graphCanvas.drawCircle(x[j], y[j], 4, pCircle);
						else
							graphCanvas.drawBitmapIcon(icon, x[j], y[j], null);
					}
				}
				
				if (anim==1)
					isDirty = false;
			}
		}
		
		/**
		 * draw graph compare region without animation
		 */
		private void drawGraphCompareRegionWithoutAnimation(GraphCanvasWrapper graphCanvas) {
			Canvas c = new Canvas(b);
			b.eraseColor(Color.TRANSPARENT);
			
			Paint pBg = new Paint();
			pBg.setFlags(Paint.ANTI_ALIAS_FLAG);
			pBg.setAntiAlias(true); //text anti alias
			pBg.setFilterBitmap(true); // bitmap anti alias
			pBg.setStyle(Style.FILL);
			
			ArrayList<GraphPath> arrCurveBgPath = new ArrayList<GraphPath>();
			
			for (int i = 0; i < mCurveGraphVO.getArrGraph().size(); i++) {
				GraphPath regionPath = new GraphPath(width, height, mCurveGraphVO.getPaddingLeft(), mCurveGraphVO.getPaddingBottom());

				boolean firstSet = false;
				float xGap = xLength/(mCurveGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
				
				float[] x = setAxisX(xGap, i);
				float[] y = setAxisY(i);
				// Creates a monotone cubic spline from a given set of control points.
				spline = Spline.createMonotoneCubicSpline(x, y);
				
				p.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());

				// draw Region
				for (float j = x[0]; j < x[x.length-1]; j++) {
					if (!firstSet) {
						regionPath.moveTo(j, spline.interpolate(j));
						firstSet = true;
					} else 
						regionPath.lineTo((j+1), spline.interpolate((j+1)));
				}
				
				regionPath.lineTo(x[x.length-1], 0);
				regionPath.lineTo(0, 0);
				arrCurveBgPath.add(regionPath);
			}

			pBg.setColor(mCurveGraphVO.getArrGraph().get(0).getColor());
			pBg.setAlpha(255);
			c.drawPath(arrCurveBgPath.get(0), pBg);
			pBg.setXfermode(new PorterDuffXfermode(Mode.XOR));
			pBg.setColor(mCurveGraphVO.getArrGraph().get(1).getColor());
			pBg.setAlpha(255);
			c.drawPath(arrCurveBgPath.get(1), pBg);
			graphCanvas.getCanvas().drawBitmap(b, 0, 0, null);
		}

		/**
		 * draw graph compare region with animation 
		 */
		private void drawGraphCompareRegionWithAnimation(GraphCanvasWrapper graphCanvas) {
			Canvas c = new Canvas(b);
			b.eraseColor(Color.TRANSPARENT);
			
			Paint pBg = new Paint();
			pBg.setFlags(Paint.ANTI_ALIAS_FLAG);
			pBg.setAntiAlias(true); //text anti alias
			pBg.setFilterBitmap(true); // bitmap anti alias
			pBg.setStyle(Style.FILL);
			
			ArrayList<GraphPath> arrCurveBgPath = new ArrayList<GraphPath>();
			
			for (int i = 0; i < mCurveGraphVO.getArrGraph().size(); i++) {
				GraphPath regionPath = new GraphPath(width, height, mCurveGraphVO.getPaddingLeft(), mCurveGraphVO.getPaddingBottom());

				boolean firstSet = false;
				float xGap = xLength/(mCurveGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
				
				float moveX = 0;
				float[] x = setAxisX(xGap, i);
				float[] y = setAxisY(i);
				// Creates a monotone cubic spline from a given set of control points.
				spline = Spline.createMonotoneCubicSpline(x, y);
				
				p.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());
				pCircle.setColor(mCurveGraphVO.getArrGraph().get(i).getColor());
				
				// draw Region
				for (float j = x[0]; j <= x[x.length-1]; j++) {
					if (!firstSet) {
						regionPath.moveTo(j, spline.interpolate(j));
						firstSet = true;
					} else { 
						moveX = j * anim;
						regionPath.lineTo(moveX, spline.interpolate(moveX));
					}
				}

				if (animationType == GraphAnimation.CURVE_REGION_ANIMATION_1) {
					moveX += xGap * anim;
					if(moveX >= xLength){
						moveX = xLength;
					}	
				}
				
				regionPath.lineTo(moveX, 0);
				regionPath.lineTo(0, 0);
				
				arrCurveBgPath.add(regionPath);
			}
			
			pBg.setColor(mCurveGraphVO.getArrGraph().get(0).getColor());
			pBg.setAlpha(255);
			c.drawPath(arrCurveBgPath.get(0), pBg);
			pBg.setXfermode(new PorterDuffXfermode(Mode.XOR));
			pBg.setColor(mCurveGraphVO.getArrGraph().get(1).getColor());
			pBg.setAlpha(255);
			c.drawPath(arrCurveBgPath.get(1), pBg);
			graphCanvas.getCanvas().drawBitmap(b, 0, 0, null);
		}
		
		/**
		 * draw X Mark
		 */
		private void drawXMark(GraphCanvasWrapper graphCanvas) {
			float x = 0;
			
			float xGap = xLength/(mCurveGraphVO.getArrGraph().get(0).getCoordinateArr().length-1);
			for (int i = 0; i < mCurveGraphVO.getArrGraph().get(0).getCoordinateArr().length; i++) {
			        x = xGap * i;
			        
			        graphCanvas.drawLine(x, 0, x, -10, pBaseLine);
			}
		}
		
		/**
		 * draw Y Mark
		 */
		private void drawYMark(GraphCanvasWrapper canvas) {
			for (int i = 0; mCurveGraphVO.getIncrement() * i <= mCurveGraphVO.getMaxValue(); i++) {
				float y = yLength * mCurveGraphVO.getIncrement() * i/mCurveGraphVO.getMaxValue();
				
				canvas.drawLine(0, y, -10, y, pBaseLine);
			}
		}
		
		/**
		 * draw X Text
		 */
		private void drawXText(GraphCanvasWrapper graphCanvas) {
			float x = 0;
			
			float xGap = xLength/(mCurveGraphVO.getArrGraph().get(0).getCoordinateArr().length-1);
			for (int i = 0; i < mCurveGraphVO.getLegendArr().length; i++) {
			        x = xGap * i;
			        
			        String text = mCurveGraphVO.getLegendArr()[i];
			        pMarkText.measureText(text);
			        pMarkText.setTextSize(20);
					Rect rect = new Rect();
					pMarkText.getTextBounds(text, 0, text.length(), rect);
					
			    graphCanvas.drawText(text, x -(rect.width()/2), -(20 + rect.height()), pMarkText);
			}
		}
		
		/**
		 * draw Y Text
		 */
		private void drawYText(GraphCanvasWrapper graphCanvas) {
			for (int i = 0; mCurveGraphVO.getIncrement() * i <= mCurveGraphVO.getMaxValue(); i++) {
				
				String mark = Float.toString(mCurveGraphVO.getIncrement() * i);
				float y = yLength * mCurveGraphVO.getIncrement() * i/mCurveGraphVO.getMaxValue();
				pMarkText.measureText(mark);
				pMarkText.setTextSize(20);
				Rect rect = new Rect();
				pMarkText.getTextBounds(mark, 0, mark.length(), rect);
//				Log.e(TAG, "rect = height()" + rect.height());
//				Log.e(TAG, "rect = width()" + rect.width());
				graphCanvas.drawText(mark, -(rect.width() + 20), y-rect.height()/2, pMarkText);
			}
		}

		/**
		 * set point X Coordinate  
		 */
		private float[] setAxisX(float xGap, int graphNum){
			float[] axisX = new float[mCurveGraphVO.getArrGraph().get(graphNum).getCoordinateArr().length];
			
			for (int i = 0; i < mCurveGraphVO.getArrGraph().get(graphNum).getCoordinateArr().length; i++)
				axisX[i] = xGap*i;
			
			return axisX;
		}
		
		/**
		 * set point Y Coordinate
		 */
		private float[] setAxisY(int graphNum){
			float[] axisY = new float[mCurveGraphVO.getArrGraph().get(graphNum).getCoordinateArr().length];

			for (int i = 0; i < mCurveGraphVO.getArrGraph().get(graphNum).getCoordinateArr().length; i++)
				axisY[i] = yLength*mCurveGraphVO.getArrGraph().get(graphNum).getCoordinateArr()[i]/mCurveGraphVO.getMaxValue();;
			
			return axisY;
		}

	}
}
