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
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.handstudio.android.hzgrapherlib.canvas.GraphCanvasWrapper;
import com.handstudio.android.hzgrapherlib.error.ErrorCode;
import com.handstudio.android.hzgrapherlib.error.ErrorDetector;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.bargraph.BarGraph;
import com.handstudio.android.hzgrapherlib.vo.bargraph.BarGraphVO;

public class BarGraphView extends SurfaceView implements Callback{

	public static final String TAG = "BarGraphView";
	
	private Context mContext;
	private SurfaceHolder mHolder;
	private DrawThread mDrawThread;	
	private BarGraphVO mBarGraphVO = null;
	
	//Constructor
	public BarGraphView(Context context, BarGraphVO vo) {
		super(context);
		Log.i(TAG, "BarGraphView generator.");
		
		mContext = context;
		mBarGraphVO = vo;
		initView(context, vo);
	}
	
	private void initView(Context context, BarGraphVO vo) {
		ErrorCode ec = ErrorDetector.checkGraphObject(vo);
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
		Log.i(TAG, "surfaceCreated()");
		if(mDrawThread == null){
			mDrawThread = new DrawThread(mHolder, getContext());
			mDrawThread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "surfaceDestroyed()");
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
		int xLength = width - (mBarGraphVO.getPaddingLeft() + mBarGraphVO.getPaddingRight() + mBarGraphVO.getMarginRight());
		int yLength = height - (mBarGraphVO.getPaddingBottom() + mBarGraphVO.getPaddingTop() + mBarGraphVO.getMarginTop());
		
		//chart length
		int chartXLength = width - (mBarGraphVO.getPaddingLeft() + mBarGraphVO.getPaddingRight());
		int chartYLength = height - (mBarGraphVO.getPaddingBottom() + mBarGraphVO.getPaddingTop());
		
		Paint p = new Paint();
		Paint pCircle = new Paint();
		Paint pLine = new Paint();
		Paint pBaseLine = new Paint();
		Paint pBaseLineD = new Paint();
		Paint pBaseLineX = new Paint();
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
			
			int bgResource = mBarGraphVO.getGraphBG();
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
			animStartTime = System.currentTimeMillis();
			
			setPaint();
			isAnimation();
			
			while (isRun) {
				if(!isDirty){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					continue;
				}
								
				canvas = mHolder.lockCanvas();
				graphCanvasWrapper = new GraphCanvasWrapper(canvas, width, height, mBarGraphVO.getPaddingLeft(), mBarGraphVO.getPaddingBottom());
				
				synchronized (mHolder) {
					synchronized (touchLock) {
						try{
							canvas.drawColor(Color.WHITE);
							if(bg != null){
								canvas.drawBitmap(bg, 0, 0, null);
							}
														
							drawBaseLine(graphCanvasWrapper);
							drawBaseMark(graphCanvasWrapper);
							drawBaseText(graphCanvasWrapper);
							drawBaseLineGuide(graphCanvasWrapper);
							drawGraphName(canvas);
							
							if(isAnimation){
								drawGraphWithAnimation(graphCanvasWrapper);
							} else{
								drawGraphWithoutAnimation(graphCanvasWrapper);
							}												
						} catch(Exception e){
							e.printStackTrace();
						} finally{
							if(graphCanvasWrapper.getCanvas() != null){
								mHolder.unlockCanvasAndPost(canvas);
							}
						}
					}
				}				
			}
		}
		
		private void setPaint(){
			pBaseLine = new Paint();
			pBaseLine.setFlags(Paint.ANTI_ALIAS_FLAG);
			pBaseLine.setAntiAlias(true);
			pBaseLine.setFilterBitmap(true);
			pBaseLine.setColor(Color.GRAY);
			pBaseLine.setStrokeWidth(3);
			
			pBaseLineD = new Paint();
			pBaseLineD.setFlags(Paint.ANTI_ALIAS_FLAG);
			pBaseLineD.setAntiAlias(true);
			pBaseLineD.setFilterBitmap(true);
			pBaseLineD.setColor(0xffcccccc);
			pBaseLineD.setStrokeWidth(1);
			pBaseLineD.setStyle(Style.STROKE);
			pBaseLineD.setPathEffect(new DashPathEffect(new float[] {10,5}, 0));
			
			pMarkText = new Paint();
			pMarkText.setFlags(Paint.ANTI_ALIAS_FLAG);
			pMarkText.setAntiAlias(true);
			pMarkText.setColor(Color.BLACK); 			
		}
		
		private void isAnimation(){
			if(mBarGraphVO.isAnimationShow()){
				isAnimation = true;
			}else {
				isAnimation = false;
			}
		}
		
		private void drawBaseLine(GraphCanvasWrapper graphCanvas){
			graphCanvas.drawLine(0, 0, chartXLength, 0, pBaseLine);
			graphCanvas.drawLine(0, 0, 0, chartYLength, pBaseLine);					
		}
		
		private void drawBaseMark(GraphCanvasWrapper graphCanvas){
			//draw y axis 
			for (int i = 1; mBarGraphVO.getIncrementY() * i <= mBarGraphVO.getMaxValueY(); i++) {	
				float y = yLength * mBarGraphVO.getIncrementY() * i/mBarGraphVO.getMaxValueY();
				graphCanvas.drawLine(0, y, -10, y, pBaseLine);
			}
			
//			for (int i = 1; mBarGraphVO.getIncrementX() * i <= mBarGraphVO.getMaxValueX(); i++) {	
//				float x = xLength * mBarGraphVO.getIncrementX() * i / mBarGraphVO.getMaxValueX();
//				graphCanvas.drawLine(x, 0, x, -10, pBaseLine);
//			}
			
			//draw x axis 
			for (int i = 0; i < mBarGraphVO.getLegendArr().length; i++) {
				float x = xLength * mBarGraphVO.getIncrementX() * (i+1)/mBarGraphVO.getMaxValueX();
				graphCanvas.drawLine(x, 0, x, -10, pBaseLine);
			}
		}
		
		private void drawBaseText(GraphCanvasWrapper graphCanvas){
			//draw X axis 
//			for (int i = 0; mBarGraphVO.getIncrementX() * i <= mBarGraphVO.getMaxValueX(); i++){
//				float x = xLength * mBarGraphVO.getIncrementX() * i/mBarGraphVO.getMaxValueX();
//		        String mark = Float.toString(mBarGraphVO.getIncrementX() * i);
//		        pMarkText.measureText(mark);
//		        pMarkText.setTextSize(20);
//				Rect rect = new Rect();
//				pMarkText.getTextBounds(mark, 0, mark.length(), rect);
//			    graphCanvas.drawText(mark, x -(rect.width()/2), -(20 + rect.height()), pMarkText);
//			}
			for (int i = 0; i < mBarGraphVO.getLegendArr().length; i++) {
				float x = xLength * mBarGraphVO.getIncrementX() * (i+1)/mBarGraphVO.getMaxValueX();
				String mark = mBarGraphVO.getLegendArr()[i];
				pMarkText.measureText(mark);
				pMarkText.setTextSize(20);
				Rect rect = new Rect();
				pMarkText.getTextBounds(mark, 0, mark.length(), rect);
			    graphCanvas.drawText(mark, x -(rect.width()/2), -(20 + rect.height()), pMarkText);
			}			
			
			//draw Y axis 
			for (int i = 0; mBarGraphVO.getIncrementY() * i <= mBarGraphVO.getMaxValueY(); i++){	
				String mark = Float.toString(mBarGraphVO.getIncrementY() * i);
				float y = yLength * mBarGraphVO.getIncrementY() * i/mBarGraphVO.getMaxValueY();
				pMarkText.measureText(mark);
				pMarkText.setTextSize(20);
				Rect rect = new Rect();
				pMarkText.getTextBounds(mark, 0, mark.length(), rect);
				graphCanvas.drawText(mark, -(rect.width() + 20), y-rect.height()/2, pMarkText);
			}
		}
		
		private void drawBaseLineGuide(GraphCanvasWrapper graphCanvas){
			//draw Y axis
			for (int i = 1; mBarGraphVO.getIncrementY() * i <= mBarGraphVO.getMaxValueY(); i++) {	
				float y = yLength * mBarGraphVO.getIncrementY() * i/mBarGraphVO.getMaxValueY();
				graphCanvas.drawLine(0, y, chartXLength, y, pBaseLineD);
			}
			
//			for (int i = 1; mBarGraphVO.getIncrementY() * i <= mBarGraphVO.getMaxValueY(); i++) {	
//				float x = xLength * mBarGraphVO.getIncrementY() * i / mBarGraphVO.getMaxValueY();
//				graphCanvas.drawLine(x, 0, x, chartYLength, pBaseLineD);
//			}			
		}
		
		private void drawGraphName(Canvas canvas) {
			GraphNameBox gnb = mBarGraphVO.getGraphNameBox();
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
				
				int graphSize = mBarGraphVO.getArrGraph().size();
				for (int i = 0; i < graphSize; i++) {					
					String text = mBarGraphVO.getArrGraph().get(i).getName();
					Rect rect = new Rect();
					pNameText.getTextBounds(text, 0, text.length(), rect);
					if(rect.width() > maxTextWidth){
						maxTextWidth = rect.width();
						maxTextHeight = rect.height();
					}
					mBarGraphVO.getArrGraph().get(i).getName();	
				}
				
				nameboxWidth = 1 * maxTextWidth + nameboxTextIconMargin + nameboxIconWidth;
				int maxCellHight = maxTextHeight;
				if(nameboxIconHeight > maxTextHeight){
					maxCellHight = nameboxIconHeight;
				}
				nameboxHeight = graphSize * maxCellHight + (graphSize-1) * nameboxIconMargin;
				canvas.drawRect(width - (nameboxMarginRight + nameboxWidth) - nameboxPadding*2,
						nameboxMarginTop, width - nameboxMarginRight, nameboxMarginTop + nameboxHeight + nameboxPadding*2, nameRextPaint);
				
				for (int i = 0; i < graphSize; i++) {
					BarGraph graph = mBarGraphVO.getArrGraph().get(i);
					
					pIcon.setColor(graph.getColor());
					canvas.drawRect(width - (nameboxMarginRight + nameboxWidth) - nameboxPadding,
							nameboxMarginTop + (maxCellHight * i) + nameboxPadding + (nameboxIconMargin * i), 
							width - (nameboxMarginRight + maxTextWidth) - nameboxPadding - nameboxTextIconMargin, 
							nameboxMarginTop + maxCellHight * (i+1) + nameboxPadding + nameboxIconMargin * i, pIcon);
					String text = graph.getName();
					canvas.drawText(text, width - (nameboxMarginRight + maxTextWidth) - nameboxPadding, 
							nameboxMarginTop + maxTextHeight/2 + maxCellHight * i + maxCellHight/2 + nameboxPadding  + nameboxIconMargin * i, pNameText);

				}
			}
		}
		
		private void drawGraphWithoutAnimation(GraphCanvasWrapper canvas){
			Log.d(TAG, "drawGraphWithoutAnimation");
			Paint barGraphRegionPaint = new Paint();
			barGraphRegionPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
			barGraphRegionPaint.setAntiAlias(true); //text anti alias
			barGraphRegionPaint.setFilterBitmap(true); // bitmap anti alias
			barGraphRegionPaint.setStrokeWidth(0);
			 
			Paint barPercentPaint = new Paint();
			barPercentPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
			barPercentPaint.setAntiAlias(true);
			barPercentPaint.setColor(Color.WHITE); 	
			barPercentPaint.setTextSize(20);
					    
			float yBottom = 0;
			float yBottomOld = 0;
			
			//x축 반복 
			for(int i=0; i< mBarGraphVO.getLegendArr().length; i++){
				float xLeft = xLength * mBarGraphVO.getIncrementX() * (i+1)/mBarGraphVO.getMaxValueX() - mBarGraphVO.getBarWidth() / 2;
				float xRight = xLeft + mBarGraphVO.getBarWidth();
				
				float totalYLength = 0;
				for (int j = 0; j < mBarGraphVO.getArrGraph().size(); j++) {
					totalYLength += yLength * mBarGraphVO.getArrGraph().get(j).getCoordinateArr()[i]/mBarGraphVO.getMaxValueY();
				}
				
				//x축 각 섹션별 반복 
				for (int j = 0; j < mBarGraphVO.getArrGraph().size(); j++) {
					BarGraph graph = mBarGraphVO.getArrGraph().get(j);
					
					yBottomOld = yBottom;
					yBottom += yLength * graph.getCoordinateArr()[i]/mBarGraphVO.getMaxValueY();
					
					barGraphRegionPaint.setColor(mBarGraphVO.getArrGraph().get(j).getColor());
					
					canvas.drawRect(xLeft, yBottomOld, xRight, yBottom, barGraphRegionPaint);
					
					int percentage = (int) (((yBottom - yBottomOld)*100)/totalYLength);
					if(percentage != 0){
						String mark = String.valueOf(percentage)+"%";
						barPercentPaint.measureText(mark);
						Rect rect = new Rect();
						barPercentPaint.getTextBounds(mark, 0, mark.length(), rect);
						canvas.drawText(mark, xRight-((xRight-xLeft)/2)-rect.width()/2, yBottom-((yBottom-yBottomOld)/2)-rect.height()/2, barPercentPaint);
					}
				}			
				
				yBottom = 0;
			}
		}
		
		private void drawGraphWithAnimation(GraphCanvasWrapper canvas){
			Log.d(TAG, "drawGraphWithAnimation");
			Paint barGraphRegionPaint = new Paint();
			barGraphRegionPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
			barGraphRegionPaint.setAntiAlias(true); //text anti alias
			barGraphRegionPaint.setFilterBitmap(true); // bitmap anti alias
			barGraphRegionPaint.setStrokeWidth(0);
			 
//			Paint barPercentPaint = new Paint();
//			barPercentPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
//			barPercentPaint.setAntiAlias(true);
//			barPercentPaint.setColor(Color.WHITE); 	
//			barPercentPaint.setTextSize(20);
			
			long curTime = System.currentTimeMillis();
			long gapTime = curTime - animStartTime;
			long totalAnimDuration = mBarGraphVO.getAnimation().getDuration();
			
			if(gapTime >= totalAnimDuration){
				gapTime = totalAnimDuration;
				isDirty = false;
			}
			
			float yBottomOld = 0;		
			
			//x축 반복 
			for(int i=0; i< mBarGraphVO.getLegendArr().length; i++){				
				float xLeft = xLength * mBarGraphVO.getIncrementX() * (i+1)/mBarGraphVO.getMaxValueX() - mBarGraphVO.getBarWidth() / 2;
				float xRight = xLeft + mBarGraphVO.getBarWidth();
				
				float totalYLength = 0;
				for (int j = 0; j < mBarGraphVO.getArrGraph().size(); j++) {
					totalYLength += yLength * mBarGraphVO.getArrGraph().get(j).getCoordinateArr()[i]/mBarGraphVO.getMaxValueY();
				}
								
				float yGap = (totalYLength / totalAnimDuration) * gapTime;
				Log.d(TAG, "yGap = "+yGap);		
				
				barGraphRegionPaint.setColor(mBarGraphVO.getArrGraph().get(0).getColor());
				canvas.drawRect(xLeft, yBottomOld, xRight, yGap, barGraphRegionPaint);								
			}		
		}
	}
}
