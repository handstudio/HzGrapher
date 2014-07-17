package com.handstudio.android.hzgrapherlib.graphview;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.handstudio.android.hzgrapherlib.canvas.GraphCanvasWrapper;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgraphlib.vo.scattergraph.ScatterGraph;
import com.handstudio.android.hzgraphlib.vo.scattergraph.ScatterGraphVO;

public class ScatterGraphView extends SurfaceView implements Callback
{
	public static final String TAG = ScatterGraphView.class.getSimpleName();
	
	private static final Object touchLock = new Object();
	
	private Context mContext;
	private DrawThread mDrawThread;
	private SurfaceHolder mSurfaceHolder;
	private ScatterGraphVO mScatterGraphVO = null;
	
	
	public ScatterGraphView(Context context, ScatterGraphVO vo) 
	{
		super(context);
		mContext = context;
		mScatterGraphVO = vo;
		initView();
	}
	
	
	private void initView()
	{
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		
	}
	
	

	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		if(mDrawThread == null){
			mDrawThread = new DrawThread(mSurfaceHolder, mContext);
		}
		mDrawThread.start();
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
	{
		
	}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		if(mDrawThread != null){
			mDrawThread.isRunning = false;
			if(mDrawThread.isAlive()){
				Thread thread = mDrawThread;
				thread.interrupt();
				mDrawThread = null;
			}
		}
	}
			
	
	
	class DrawThread extends Thread
	{
		SurfaceHolder mHolder;
		Context mContext;
		
		boolean isRunning = true;
		boolean isDirty = true;
		
		Matrix matrix = new Matrix();
		
		int width = getWidth();
		int height = getHeight();
		
		int xLength = width - (mScatterGraphVO.getPaddingLeft() + mScatterGraphVO.getPaddingRight() + mScatterGraphVO.getMarginRight());
		int yLength = height - (mScatterGraphVO.getPaddingBottom() + mScatterGraphVO.getPaddingTop() + mScatterGraphVO.getMarginTop());
		
		int chartXLength = width - (mScatterGraphVO.getPaddingLeft() + mScatterGraphVO.getPaddingRight());
		int chartYLength = height - (mScatterGraphVO.getPaddingBottom() + mScatterGraphVO.getPaddingTop());		
		
		Paint pPoint;
		Paint pBaseLine;
		Paint pBaseLineD;
		Paint pMarkText;
		
		float anim = 0.0f;
		boolean isAnimation = false;
		boolean isDrawRegion = false;
		long animStartTime = -1;
		
		WeakHashMap<Integer, Bitmap> arrIcon = new WeakHashMap<Integer, Bitmap>();
		Bitmap bg = null;
		
		
		public DrawThread(SurfaceHolder holder, Context context)
		{
			mHolder = holder;
			mContext = context;
			
			int size = mScatterGraphVO.getArrGraph().size();
			for(int i=0; i<size; i++){
				int bitmapResource = mScatterGraphVO.getArrGraph().get(i).getBitmapResource();
				if(bitmapResource != -1){
					arrIcon.put(i, BitmapFactory.decodeResource(getResources(), bitmapResource));
				} else{
					if(arrIcon.get(i) != null){
						arrIcon.remove(i);
					}
				}
			}
			int bgResource = mScatterGraphVO.getGraphBG();
			if(bgResource != -1){
				Bitmap tempBg = BitmapFactory.decodeResource(getResources(), bgResource);
				bg = Bitmap.createScaledBitmap(tempBg, width, height, true);
				tempBg.recycle();
			}
		}


		@Override
		public void run() 
		{			
			Canvas canvas = null;
			GraphCanvasWrapper graphCanvasWrapper = null;
			
			setPaint();
			isAnimation();
						
			animStartTime = System.currentTimeMillis();
			
			while(isRunning){
				
				if(!isDirty){
					try{
						sleep(300);
					} catch(InterruptedException e){
						e.printStackTrace();
					}
					continue;
				}
				
				canvas = mHolder.lockCanvas();
				graphCanvasWrapper = new GraphCanvasWrapper(canvas, width, height, mScatterGraphVO.getPaddingLeft(), mScatterGraphVO.getPaddingBottom());
				
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
								drawGraph(graphCanvasWrapper);
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
		
		
		
		private void setPaint()
		{
			pPoint = new Paint();
			pPoint.setFlags(Paint.ANTI_ALIAS_FLAG);
			pPoint.setAntiAlias(true);
			pPoint.setFilterBitmap(true);
			pPoint.setColor(Color.BLUE);
			pPoint.setStrokeWidth(2);
			pPoint.setStyle(Style.FILL_AND_STROKE);
			
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
		
		
		
		private void drawBaseLine(GraphCanvasWrapper graphCanvas)
		{
			graphCanvas.drawLine(0, 0, chartXLength, 0, pBaseLine);
			graphCanvas.drawLine(0, 0, 0, chartYLength, pBaseLine);
					
		}
		
		
		private void drawBaseMark(GraphCanvasWrapper graphCanvas)
		{
			for (int i = 1; mScatterGraphVO.getIncrementX() * i <= mScatterGraphVO.getMaxValueX(); i++) {	
				float y = yLength * mScatterGraphVO.getIncrementX() * i/mScatterGraphVO.getMaxValueX();
				graphCanvas.drawLine(0, y, -10, y, pBaseLine);
			}
			
			for (int i = 1; mScatterGraphVO.getIncrementY() * i <= mScatterGraphVO.getMaxValueY(); i++) {	
				float x = xLength * mScatterGraphVO.getIncrementY() * i / mScatterGraphVO.getMaxValueY();
				graphCanvas.drawLine(x, 0, x, -10, pBaseLine);
			}
			
		}
		
		
		private void drawBaseText(GraphCanvasWrapper graphCanvas)
		{
			for (int i = 0; mScatterGraphVO.getIncrementY() * i <= mScatterGraphVO.getMaxValueY(); i++) 
			{
				float x = xLength * mScatterGraphVO.getIncrementY() * i/mScatterGraphVO.getMaxValueY();
		        String mark = Float.toString(mScatterGraphVO.getIncrementY() * i);
		        pMarkText.measureText(mark);
		        pMarkText.setTextSize(20);
				Rect rect = new Rect();
				pMarkText.getTextBounds(mark, 0, mark.length(), rect);
			    graphCanvas.drawText(mark, x -(rect.width()/2), -(20 + rect.height()), pMarkText);
			}
			
			for (int i = 0; mScatterGraphVO.getIncrementX() * i <= mScatterGraphVO.getMaxValueX(); i++) 
			{	
				String mark = Float.toString(mScatterGraphVO.getIncrementX() * i);
				float y = yLength * mScatterGraphVO.getIncrementX() * i/mScatterGraphVO.getMaxValueX();
				pMarkText.measureText(mark);
				pMarkText.setTextSize(20);
				Rect rect = new Rect();
				pMarkText.getTextBounds(mark, 0, mark.length(), rect);
				graphCanvas.drawText(mark, -(rect.width() + 20), y-rect.height()/2, pMarkText);
			}
		}
		
		
		private void drawBaseLineGuide(GraphCanvasWrapper graphCanvas)
		{
			for (int i = 1; mScatterGraphVO.getIncrementX() * i <= mScatterGraphVO.getMaxValueX(); i++) {	
				float y = yLength * mScatterGraphVO.getIncrementX() * i/mScatterGraphVO.getMaxValueX();
				graphCanvas.drawLine(0, y, chartXLength, y, pBaseLineD);
			}
			
			for (int i = 1; mScatterGraphVO.getIncrementY() * i <= mScatterGraphVO.getMaxValueY(); i++) {	
				float x = xLength * mScatterGraphVO.getIncrementY() * i / mScatterGraphVO.getMaxValueY();
				graphCanvas.drawLine(x, 0, x, chartYLength, pBaseLineD);
			}			
		}
		
		
		private void drawGraphWithAnimation(GraphCanvasWrapper graphCanvas)
		{
			getRandomIndex();
			
//			Log.i(TAG, "indexList.size() = " + indexList.size());
					
			for(int i=0; i<indexList.size(); i++){
				ScatterGraph graph = mScatterGraphVO.getArrGraph().get(indexList.get(i));
				float[] graphCoor = graph.getCoordinateArr();
				float x = (graphCoor[0] * xLength) / mScatterGraphVO.getMaxValueY();
				float y = (graphCoor[1] * yLength) / mScatterGraphVO.getMaxValueX();
				int color = graph.getColor();
				pPoint.setColor(color);
				graphCanvas.drawCircle(x, y, 5, pPoint);
			}
			
			if(indexList.size() == 100
					&& mScatterGraphVO.getArrGraph().size() == 100){
				isAnimation = false;
			}
		}
		
		
		private void drawGraph(GraphCanvasWrapper graphCanvas)
		{
//			Log.i(TAG, "mScatterGraphVO.getArrGraph().size() = " + mScatterGraphVO.getArrGraph().size());
			
			for(int i=0; i<mScatterGraphVO.getArrGraph().size(); i++)
			{
				ScatterGraph graph = mScatterGraphVO.getArrGraph().get(i);
				float[] graphCoor = graph.getCoordinateArr();
				float x = (graphCoor[0] * xLength) / mScatterGraphVO.getMaxValueY();
				float y = (graphCoor[1] * yLength) / mScatterGraphVO.getMaxValueX();
				int color = graph.getColor();
				pPoint.setColor(color);
				graphCanvas.drawCircle(x, y, 5, pPoint);
			}
		}
		
		
		private void drawGraphName(Canvas canvas) {
			GraphNameBox gnb = mScatterGraphVO.getGraphNameBox();
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
				
				
				int graphSize = mScatterGraphVO.getLegendArr().length;
				for (int i = 0; i < graphSize; i++) {					
					String text = mScatterGraphVO.getLegendArr()[i];
					Rect rect = new Rect();
					pNameText.getTextBounds(text, 0, text.length(), rect);
					if(rect.width() > maxTextWidth){
						maxTextWidth = rect.width();
						maxTextHeight = rect.height();
					}
					mScatterGraphVO.getArrGraph().get(i).getName();	
				}
				nameboxWidth = 1 * maxTextWidth + nameboxTextIconMargin + nameboxIconWidth;
				int maxCellHight = maxTextHeight;
				if(nameboxIconHeight > maxTextHeight){
					maxCellHight = nameboxIconHeight;
				}
				nameboxHeight = graphSize * maxCellHight + (graphSize-1) * nameboxIconMargin;
				canvas.drawRect(width - (nameboxMarginRight + nameboxWidth) - nameboxPadding*2,
						nameboxMarginTop, width - nameboxMarginRight, nameboxMarginTop + nameboxHeight + nameboxPadding*2, nameRextPaint);
				
				for (int i = 0; i < graphSize; i++) 
				{
					for(int j=0; j<mScatterGraphVO.getArrGraph().size(); j++){
						ScatterGraph graph = mScatterGraphVO.getArrGraph().get(j);
						String name = graph.getName();
						if(!name.equals(mScatterGraphVO.getLegendArr()[i])){
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
			}
		}
		
		
		
		ArrayList<Integer> indexList = new ArrayList<Integer>();
		private int getRandomIndex()
		{
			int index = -1;
			int size = mScatterGraphVO.getArrGraph().size();
			
			Random random = new Random();
			int rNum = random.nextInt(size);
			
			if(!isDuplicate(rNum)){
				indexList.add(rNum);
				index = rNum;
				Log.e(TAG, "rNum = " + rNum);
				return index;
			}
			return getRandomIndex();
		}
		
		
		private boolean isDuplicate(int rNum)
		{
			boolean result = false;
			
			if(indexList.size() == 0){
				return false;
			}
			
			for(int i=0; i<indexList.size(); i++){
				int pNum = indexList.get(i);
				if(pNum == rNum){
					result = true;
				} else{
					result = false;
				}
			}
			return result;
		}
			
		
		private void isAnimation() 
		{
			if(mScatterGraphVO.getAnimation() != null){
				isAnimation = true;
			}else{
				isAnimation = false;
			}
		}

	
	
//		private void calcTimePass(){
//			if(isAnimation){
//				long curTime = System.currentTimeMillis();
//				long gapTime = curTime - animStartTime;
//				long animDuration = mScatterGraphVO.getAnimation().getDuration();
//				if(gapTime >= animDuration){
//					gapTime = animDuration;
//					isDirty = false;
//				}
//				
//				anim = mScatterGraphVO.getArrGraph().get(0).getCoordinateArr().size() * (float)gapTime/(float)animDuration;
//			}else{
//				isDirty = false;
//			}
//			
//	//		Log.e(TAG,"curTime = " + curTime + " , animStartTime = " + animStartTime);
//	//		Log.e(TAG,"anim = " + anim + " , gapTime = " + gapTime);
//		}
//		
//		
//		private void drawGraphName(Canvas canvas) {
//			GraphNameBox gnb = mScatterGraphVO.getGraphNameBox();
//			if(gnb != null){
//				int nameboxWidth = 0;
//				int nameboxHeight = 0;
//				
//				int nameboxIconWidth = gnb.getNameboxIconWidth();
//				int nameboxIconHeight = gnb.getNameboxIconHeight();
//				
//				int nameboxMarginTop = gnb.getNameboxMarginTop();
//				int nameboxMarginRight = gnb.getNameboxMarginRight();
//				int nameboxPadding = gnb.getNameboxPadding();
//				
//				int nameboxTextIconMargin = gnb.getNameboxIconMargin();
//				int nameboxIconMargin = gnb.getNameboxIconMargin();
//				int nameboxTextSize = gnb.getNameboxTextSize(); 
//				
//				int maxTextWidth = 0;
//				int maxTextHeight = 0;
//				
//				Paint nameRextPaint = new Paint();
//				nameRextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
//				nameRextPaint.setAntiAlias(true); //text anti alias
//				nameRextPaint.setFilterBitmap(true); // bitmap anti alias
//				nameRextPaint.setColor(Color.BLUE);
//				nameRextPaint.setStrokeWidth(3);
//				nameRextPaint.setStyle(Style.STROKE);
//				
//				Paint pIcon = new Paint();
//				pIcon.setFlags(Paint.ANTI_ALIAS_FLAG);
//				pIcon.setAntiAlias(true); //text anti alias
//				pIcon.setFilterBitmap(true); // bitmap anti alias
//				pIcon.setColor(Color.BLUE);
//				pIcon.setStrokeWidth(3);
//				pIcon.setStyle(Style.FILL_AND_STROKE);
//				
//				
//				Paint pNameText = new Paint();
//				pNameText.setFlags(Paint.ANTI_ALIAS_FLAG);
//				pNameText.setAntiAlias(true); //text anti alias
//				pNameText.setTextSize(nameboxTextSize);
//				pNameText.setColor(Color.BLACK); 
//				
//				
//				int graphSize = mScatterGraphVO.getArrGraph().size();
//				for (int i = 0; i < graphSize; i++) {
//					
//					
//					String text = mScatterGraphVO.getArrGraph().get(i).getName();
//					Rect rect = new Rect();
//					pNameText.getTextBounds(text, 0, text.length(), rect);
//					
//					if(rect.width() > maxTextWidth){
//						maxTextWidth = rect.width();
//						maxTextHeight = rect.height();
//					}
//					
//					mScatterGraphVO.getArrGraph().get(i).getName();
//					
//				}
//				mScatterGraphVO.getArrGraph().get(0).getName();
//				nameboxWidth = 1 * maxTextWidth + nameboxTextIconMargin + nameboxIconWidth;
//				int maxCellHight = maxTextHeight;
//				if(nameboxIconHeight > maxTextHeight){
//					maxCellHight = nameboxIconHeight;
//				}
//				nameboxHeight = graphSize * maxCellHight + (graphSize-1) * nameboxIconMargin;
//				
//				canvas.drawRect(width - (nameboxMarginRight + nameboxWidth) - nameboxPadding*2,
//						nameboxMarginTop, width - nameboxMarginRight, nameboxMarginTop + nameboxHeight + nameboxPadding*2, nameRextPaint);
//				
//				for (int i = 0; i < graphSize; i++) {
//					
//					pIcon.setColor(mScatterGraphVO.getArrGraph().get(i).getColor());
//					canvas.drawRect(width - (nameboxMarginRight + nameboxWidth) - nameboxPadding,
//							nameboxMarginTop + (maxCellHight * i) + nameboxPadding + (nameboxIconMargin * i), 
//							width - (nameboxMarginRight + maxTextWidth) - nameboxPadding - nameboxTextIconMargin, 
//							nameboxMarginTop + maxCellHight * (i+1) + nameboxPadding + nameboxIconMargin * i, pIcon);
//					
//					String text = mScatterGraphVO.getArrGraph().get(i).getName();
//					canvas.drawText(text, width - (nameboxMarginRight + maxTextWidth) - nameboxPadding, 
//							nameboxMarginTop + maxTextHeight/2 + maxCellHight * i + maxCellHight/2 + nameboxPadding  + nameboxIconMargin * i, pNameText);
//				}
//			}
//		}
//		
//		
//		
//		private void drawBaseLine(GraphCanvasWrapper graphCanvas) {
//			for (int i = 1; mScatterGraphVO.getIncrement() * i <= mScatterGraphVO.getMaxValueX(); i++) {
//				
//				float y = yLength * mScatterGraphVO.getIncrement() * i/mScatterGraphVO.getMaxValueX();
//				
//				graphCanvas.drawLine(0, y, chartXLength, y, pBaseLineX);
//			}
//		}
//		
//		
//		private void drawXMark(GraphCanvasWrapper graphCanvas) {
//			float x = 0;
//			float y = 0;
//			
//	//		float xGap = xLength/(mScatterGraphVO.getArrGraph().get(0).getCoordinateArr().length-1);
//			float xGap = xLength/(mScatterGraphVO.getLegendArr().length);
//			for (int i = 0; i < mScatterGraphVO.getLegendArr().length; i++) {
//			        x = xGap * i;
//			        y = yLength * mScatterGraphVO.getArrGraph().get(0).getCoordinateArr()[i]/mScatterGraphVO.getMaxValue();
//			        
//			    graphCanvas.drawLine(x, 0, x, -10, pBaseLine);
//			}
//		}
//		
//		private void drawYMark(GraphCanvasWrapper canvas) {
//			for (int i = 0; mScatterGraphVO.getIncrement() * i <= mScatterGraphVO.getMaxValue(); i++) {
//				
//				float y = yLength * mScatterGraphVO.getIncrement() * i/mScatterGraphVO.getMaxValue();
//				
//				canvas.drawLine(0, y, -10, y, pBaseLine);
//			}
//		}
//		
//		
//		private void drawXText(GraphCanvasWrapper graphCanvas) {
//			float x = 0;
//			float y = 0;
//			
//			float xGap = xLength/(mScatterGraphVO.getArrGraph().get(0).getCoordinateArr().length-1);
//			for (int i = 0; i < mScatterGraphVO.getLegendArr().length; i++) {
//			        x = xGap * i;
//			        
//			        String text = mScatterGraphVO.getLegendArr()[i];
//			        pMarkText.measureText(text);
//			        pMarkText.setTextSize(20);
//					Rect rect = new Rect();
//					pMarkText.getTextBounds(text, 0, text.length(), rect);
//					
//			    graphCanvas.drawText(text, x -(rect.width()/2), -(20 + rect.height()), pMarkText);
//			}
//		}
//		
//		/**
//		 * draw Y Text
//		 */
//		private void drawYText(GraphCanvasWrapper graphCanvas) {
//			for (int i = 0; mScatterGraphVO.getIncrement() * i <= mScatterGraphVO.getMaxValue(); i++) {
//				
//				String mark = Float.toString(mScatterGraphVO.getIncrement() * i);
//				float y = yLength * mScatterGraphVO.getIncrement() * i/mScatterGraphVO.getMaxValue();
//				pMarkText.measureText(mark);
//				pMarkText.setTextSize(20);
//				Rect rect = new Rect();
//				pMarkText.getTextBounds(mark, 0, mark.length(), rect);
//	//			Log.e(TAG, "rect = height()" + rect.height());
//	//			Log.e(TAG, "rect = width()" + rect.width());
//				graphCanvas.drawText(mark, -(rect.width() + 20), y-rect.height()/2, pMarkText);
//			}
//		}
//		
//		
//	
//		private void drawGraphRegion(GraphCanvasWrapper graphCanvas) {
//			if(isDrawRegion){
//				if (isAnimation){
//					drawGraphRegionWithAnimation(graphCanvas);
//				}else{
//					drawGraphRegionWithoutAnimation(graphCanvas);
//				}
//			}
//		}
//		
//		private void drawGraph(GraphCanvasWrapper graphCanvas) {
//			
//			if (isAnimation){
//				drawGraphWithAnimation(graphCanvas);
//			}else{
//				drawGraphWithoutAnimation(graphCanvas);
//			}
//		}
//		
//		
//		
//		private void drawGraphRegionWithoutAnimation(GraphCanvasWrapper graphCanvas) 
//		{			
//			boolean isDrawRegion = mScatterGraphVO.isDrawRegion();
//			
//			for (int i = 0; i < mScatterGraphVO.getArrGraph().size(); i++) {
//				GraphPath regionPath = new GraphPath(width, height, mScatterGraphVO.getPaddingLeft(), mScatterGraphVO.getPaddingBottom());
//				boolean firstSet = false;
//				float x = 0;
//				float y = 0;
//				pPoint.setColor(mScatterGraphVO.getArrGraph().get(i).getColor());
//				float xGap = xLength/(mScatterGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
//				
//				for (int j = 0; j < mScatterGraphVO.getArrGraph().get(i).getCoordinateArr().length; j++) {
//					if(j < mScatterGraphVO.getArrGraph().get(i).getCoordinateArr().length){
//						
//						if (!firstSet) {
//							
//							x = xGap * j ;
//							y = yLength * mScatterGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mScatterGraphVO.getMaxValue();
//							
//							regionPath.moveTo(x, 0);
//							regionPath.lineTo(x, y);
//							
//							firstSet = true;
//						} else {
//							x = xGap * j;
//							y = yLength * mScatterGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mScatterGraphVO.getMaxValue();
//							
//							regionPath.lineTo(x, y);
//						}
//					}
//				}
//				
//				if(isDrawRegion){
//					regionPath.lineTo(x, 0);
//					regionPath.lineTo(0, 0);
//					
//					Paint pBg = new Paint();
//					pBg.setFlags(Paint.ANTI_ALIAS_FLAG);
//					pBg.setAntiAlias(true); //text anti alias
//					pBg.setFilterBitmap(true); // bitmap anti alias
//					pBg.setStyle(Style.FILL);
//					pBg.setColor(mScatterGraphVO.getArrGraph().get(i).getColor());
//					graphCanvas.getCanvas().drawPath(regionPath, pBg);
//				}
//			}
//		}
//		
//		
//		private void drawGraphWithoutAnimation(GraphCanvasWrapper graphCanvas) 
//		{	
//			for (int i = 0; i < mScatterGraphVO.getArrGraph().size(); i++) {
//				GraphPath linePath = new GraphPath(width, height, mScatterGraphVO.getPaddingLeft(), mScatterGraphVO.getPaddingBottom());
//				GraphPath regionPath = new GraphPath(width, height, mScatterGraphVO.getPaddingLeft(), mScatterGraphVO.getPaddingBottom());
//				boolean firstSet = false;
//				float x = 0;
//				float y = 0;
//				pPoint.setColor(mScatterGraphVO.getArrGraph().get(i).getColor());
//				float xGap = xLength/(mScatterGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
//				
//				Bitmap icon = arrIcon.get(i);
//				
//				for (int j = 0; j < mScatterGraphVO.getArrGraph().get(i).getCoordinateArr().length; j++) {
//					if(j < mScatterGraphVO.getArrGraph().get(i).getCoordinateArr().length){
//						
//						if (!firstSet) {
//							
//							x = xGap * j ;
//							y = yLength * mScatterGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mScatterGraphVO.getMaxValue();
//							
//							linePath.moveTo(x, y);
//							
//							firstSet = true;
//						} else {
//							x = xGap * j;
//							y = yLength * mScatterGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mScatterGraphVO.getMaxValue();
//							
//							linePath.lineTo(x, y);
//						}
//						
//						if(icon == null){
//							graphCanvas.drawCircle(x, y, 4, pPoint);
//						}else{
//							graphCanvas.drawBitmapIcon(icon, x, y, null);
//						}
//					}
//				}
//			}
//		}
//		
//		
//		private void drawGraphWithAnimation(GraphCanvasWrapper graphCanvas) {
//			//for draw animation
//			float prev_x = 0;
//			float prev_y = 0;
//			
//			float next_x = 0;
//			float next_y = 0;
//			
//			float value = 0;
//			float mode = 0;
//			
//			for (int i = 0; i < mScatterGraphVO.getArrGraph().size(); i++) {
//				GraphPath linePath = new GraphPath(width, height, mScatterGraphVO.getPaddingLeft(), mScatterGraphVO.getPaddingBottom());
//				GraphPath regionPath = new GraphPath(width, height, mScatterGraphVO.getPaddingLeft(), mScatterGraphVO.getPaddingBottom());
//				boolean firstSet = false;
//				float x = 0;
//				float y = 0;
//				
//				pPoint.setColor(mScatterGraphVO.getArrGraph().get(i).getColor());
//				float xGap = xLength/(mScatterGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
//				
//				Bitmap icon = arrIcon.get(i);
//				value = anim/1;
//				mode = anim %1;
//				
//				for (int j = 0; j < value+1; j++) {
//					if(j < mScatterGraphVO.getArrGraph().get(i).getCoordinateArr().length){
//						
//						if (!firstSet) {
//							
//							x = xGap * j ;
//							y = yLength * mScatterGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mScatterGraphVO.getMaxValue();
//							
//							linePath.moveTo(x, y);
//							
//							firstSet = true;
//						} else {
//							x = xGap * j;
//							y = yLength * mScatterGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mScatterGraphVO.getMaxValue();
//							
//							if( j > value && mode != 0){
//								next_x = x - prev_x;
//								next_y = y - prev_y;
//								
//								linePath.lineTo(prev_x + next_x * mode, prev_y + next_y * mode);
//							}else{
//								linePath.lineTo(x, y);
//							}
//						}
//						
//						if(icon == null){
//							graphCanvas.drawCircle(x, y, 4, pPoint);
//						}else{
//							graphCanvas.drawBitmapIcon(icon, x, y, null);
//						}
//						prev_x = x;
//						prev_y = y;
//					}
//				}
//			}
//		}
//		
//		
//		private void drawGraphRegionWithAnimation(GraphCanvasWrapper graphCanvas) {
//			//for draw animation
//			float prev_x = 0;
//			float prev_y = 0;
//			
//			float next_x = 0;
//			float next_y = 0;
//			
//			int value = 0;
//			float mode = 0;
//			
//			boolean isDrawRegion = mScatterGraphVO.isDrawRegion();
//			
//			for (int i = 0; i < mScatterGraphVO.getArrGraph().size(); i++) {
//				GraphPath regionPath = new GraphPath(width, height, mScatterGraphVO.getPaddingLeft(), mScatterGraphVO.getPaddingBottom());
//				boolean firstSet = false;
//				float x = 0;
//				float y = 0;
//				pPoint.setColor(mScatterGraphVO.getArrGraph().get(i).getColor());
//				float xGap = xLength/(mScatterGraphVO.getArrGraph().get(i).getCoordinateArr().length-1);
//				
//				value = (int) (anim/1);
//				mode = anim %1;
//				
//				boolean isFinish = false;
//				for (int j = 0; j <= value+1; j++) {
//					if(j < mScatterGraphVO.getArrGraph().get(i).getCoordinateArr().length){
//						
//						if (!firstSet) {
//							
//							x = xGap * j ;
//							y = yLength * mScatterGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mScatterGraphVO.getMaxValue();
//							
//							regionPath.moveTo(x, 0);
//							regionPath.lineTo(x, y);
//							
//							firstSet = true;
//						} else {
//							x = xGap * j;
//							y = yLength * mScatterGraphVO.getArrGraph().get(i).getCoordinateArr()[j]/mScatterGraphVO.getMaxValue();
//							
//							if( j > value){
//								next_x = x - prev_x;
//								next_y = y - prev_y;
//								regionPath.lineTo(prev_x + next_x * mode, prev_y + next_y * mode);
//							}else{
//								regionPath.lineTo(x, y);
//							}
//						}
//						
//						prev_x = x;
//						prev_y = y;
//					}
//				}
//				isFinish = true;
//				
//				if(isDrawRegion){
//					float x_bg = prev_x + next_x * mode;
//					if(x_bg >= xLength){
//						x_bg = xLength;
//					}
//					regionPath.lineTo(x_bg, 0);
//					regionPath.lineTo(0, 0);
//					
//					Paint pBg = new Paint();
//					pBg.setFlags(Paint.ANTI_ALIAS_FLAG);
//					pBg.setAntiAlias(true); //text anti alias
//					pBg.setFilterBitmap(true); // bitmap anti alias
//					pBg.setStyle(Style.FILL);
//					pBg.setColor(mScatterGraphVO.getArrGraph().get(i).getColor());
//					graphCanvas.getCanvas().drawPath(regionPath, pBg);
//				}
//			}
//		}
//		
//		
//		private void setPaint()
//		{
//			pPoint = new Paint();
//			pPoint.setFlags(Paint.ANTI_ALIAS_FLAG);
//			pPoint.setAntiAlias(true);
//			pPoint.setFilterBitmap(true);
//			pPoint.setColor(Color.BLUE);
//			pPoint.setStrokeWidth(2);
//			pPoint.setStyle(Style.FILL_AND_STROKE);
//			
//			pBaseLine = new Paint();
//			pBaseLine.setFlags(Paint.ANTI_ALIAS_FLAG);
//			pBaseLine.setAntiAlias(true);
//			pBaseLine.setFilterBitmap(true);
//			pBaseLine.setColor(Color.GRAY);
//			pBaseLine.setStrokeWidth(3);
//			
//			pBaseLineX = new Paint();
//			pBaseLineX.setFlags(Paint.ANTI_ALIAS_FLAG);
//			pBaseLineX.setAntiAlias(true);
//			pBaseLineX.setFilterBitmap(true);
//			pBaseLineX.setColor(0xffcccccc);
//			pBaseLineX.setStrokeWidth(1);
//			pBaseLineX.setStyle(Style.STROKE);
//			pBaseLineX.setPathEffect(new DashPathEffect(new float[] {10,5}, 0));
//			
//			pBaseLineY = new Paint();
//			pBaseLineY.setFlags(Paint.ANTI_ALIAS_FLAG);
//			pBaseLineY.setAntiAlias(true);
//			pBaseLineY.setFilterBitmap(true);
//			pBaseLineY.setColor(0xffcccccc);
//			pBaseLineY.setStrokeWidth(1);
//			pBaseLineY.setStyle(Style.STROKE);
//			pBaseLineY.setPathEffect(new DashPathEffect(new float[] {10,5}, 0));
//			
//			pMarkText = new Paint();
//			pMarkText.setFlags(Paint.ANTI_ALIAS_FLAG);
//			pMarkText.setAntiAlias(true);
//			pMarkText.setColor(Color.BLACK); 
//			
//		}
//		
//		
//		private void isAnimation() 
//		{
//			if(mScatterGraphVO.getAnimation() != null){
//				isAnimation = true;
//			}else{
//				isAnimation = false;
//			}
//		}
//		
//		
//		private void isDrawRegion() {
//			if(mScatterGraphVO.isDrawRegion()){
//				isDrawRegion = true;
//			}else{
//				isDrawRegion = false;
//			}
//		}
//		
//		
//	}
		
		
		
	}
}
