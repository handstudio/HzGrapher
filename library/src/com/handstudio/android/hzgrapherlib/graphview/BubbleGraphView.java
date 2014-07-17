package com.handstudio.android.hzgrapherlib.graphview;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.handstudio.android.hzgrapherlib.canvas.GraphCanvasWrapper;
import com.handstudio.android.hzgrapherlib.error.ErrorCode;
import com.handstudio.android.hzgrapherlib.error.ErrorDetector;
import com.handstudio.android.hzgrapherlib.util.EuclidLine;
import com.handstudio.android.hzgrapherlib.util.EuclidPoint;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.bubblegraph.BubbleGraph;
import com.handstudio.android.hzgrapherlib.vo.bubblegraph.BubbleGraphVO;

public class BubbleGraphView extends SurfaceView implements Callback
{
	public static final String TAG = "BUBBLE_GRAPH_VIEW";
	
	private BubbleGraphVO mBubbleGraphVO = null;
	private DrawThread mDrawThread = null;
	private	Context mContext = null;
	
	public BubbleGraphView ( Context ctx , BubbleGraphVO vo ) 
	{
		super( ctx );
		mBubbleGraphVO = vo;
		initView ( ctx , vo );
	}
	
	@Override
	public void surfaceDestroyed ( SurfaceHolder holder )
	{
		if ( mDrawThread != null )
		{
			mDrawThread.setRunFlag(false);
			mDrawThread = null;
		}
	}
	
	@Override
	public void surfaceCreated ( SurfaceHolder holder )
	{
		Log.i(TAG, "SurfaceCreated!");
		
		if ( mDrawThread == null )
		{
			mDrawThread = new DrawThread ( holder , mBubbleGraphVO , this.mContext );
			mDrawThread.start();	
		}
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
	{
		
	}
	
	public void setBubbleGraphVO ( BubbleGraphVO vo ) { mBubbleGraphVO = vo; }
	
	private void initView ( Context ctx , BubbleGraphVO vo )
	{
		ErrorCode ec = ErrorDetector.checkGraphObject(vo);
		ec.printError();
		
		SurfaceHolder holder = this.getHolder();
		holder.addCallback(this);
		mContext = ctx;
	}
	
	private class DrawThread extends Thread 
	{
		private class CircleAnim
		{
			public float mCurrent = 0.0f;
			public float mMax = 0.0f;
		}
		
		private Context 			mCtx = null;
		private SurfaceHolder 		mHolder = null;
		private BubbleGraphVO 		mVO = null;
		private boolean 			mIsRun = true;
		private CircleAnim[][] 		mCircleAnim = null;
		
		private EuclidPoint[][][]	mCircleOuterPoint = null;
		
		private Paint 				mPaintBubble = null;
		private Paint 				mPaintAxisLine = null;
		private Paint				mPaintGuideLine = null;
		private Paint				mPaintAxisMarker = null;
		private Paint 				mPaintAxisText = null;
		
		public DrawThread ( SurfaceHolder holder , BubbleGraphVO vo , Context ctx )
		{
			mCtx = ctx;
			mHolder = holder;
			mVO = vo;
		}

		@Override
		public void run ()
		{	
			Canvas canvas = null;
			GraphCanvasWrapper gcw = null;
			float horizontalThreshold = 0.0f;
			
			initCircleAnimation ();
			initPaints ();
			
			int width = getWidth();
			int height = getHeight();
			
			//chart length
			int chartXLength = width - (mVO.getPaddingLeft() + mVO.getPaddingRight());
			int chartYLength = height - (mVO.getPaddingBottom() + mVO.getPaddingTop());
			
			initClosePoints ( chartXLength , chartYLength );	
			
			long startTick = Calendar.getInstance().getTimeInMillis();
			
			Bitmap bitBackground = null;
			Bitmap bitTemp = null;
			if ( mVO.getGraphBG() != -1 )
			{
				bitTemp = BitmapFactory.decodeResource ( mCtx.getResources(), mVO.getGraphBG() );
				bitBackground = Bitmap.createScaledBitmap( bitTemp , width , height , true);
			}
			
			while ( true )
			{
				if ( mIsRun == false ) { break; }
				
				canvas = mHolder.lockCanvas();
				if ( canvas == null ) { continue; }
				gcw = new GraphCanvasWrapper ( canvas , width , height , mVO.getPaddingLeft() , mVO.getPaddingBottom() );
				
				try
				{
					synchronized ( mHolder )
					{
						if ( bitBackground == null ) { canvas.drawColor(Color.WHITE); }
						else { canvas.drawBitmap(bitBackground, 0, 0 , null); }
						
						drawBaseline ( gcw , chartXLength , chartYLength );
						
						if ( mVO.isAnimationShow() == true )
						{
							drawGraphWithAnimation ( gcw , chartXLength , chartYLength , horizontalThreshold );
						}
						else 
						{
							drawGraphWithoutAnimation ( gcw , chartXLength , chartYLength );
						}
						
						drawAxisMark ( gcw , chartXLength , chartYLength );
						drawAxisValue ( gcw , chartXLength , chartYLength );
						drawAxisLine ( gcw , chartXLength , chartYLength );
						drawGraphName ( gcw.getCanvas() , width , height );
					}
				}
				finally
				{
					if ( canvas != null )
					{
						mHolder.unlockCanvasAndPost(canvas);
					}
				}
				
				long difference = Calendar.getInstance().getTimeInMillis() - startTick;
				horizontalThreshold = ((float)mVO.get(0).getCoordinateArr().length / 
										(float)mVO.getAnimationDuration())*(float)difference;
			}
		}
		
		public void setRunFlag ( boolean isRun ) { mIsRun = isRun; }
		
		private void initClosePoints ( int width , int height )
		{
			int graphCount = mVO.size();
			int legendCount = mVO.getLegendArr().length;
			
			mCircleOuterPoint = new EuclidPoint[graphCount][][];
			
			float perX = (float)width/(float)mVO.getLegendArr().length;
			float maxValue = mVO.getMaxCoordinate();
			float minValue = mVO.getMinCoordinate();
			
			int i; int j;
			for ( i = 0 ; i < graphCount ; i++ )
			{	
				mCircleOuterPoint[i] = new EuclidPoint[legendCount][];
				
				float[] coordsArr = mVO.get(i).getCoordinateArr();
				float[] sizeArr = mVO.get(i).getSizeArr();
				
				for ( j = 0 ; j < legendCount ; j++ )
				{
					mCircleOuterPoint[i][j] = new EuclidPoint[2];
					mCircleOuterPoint[i][j][0] = null;
					mCircleOuterPoint[i][j][1] = null;
					
					float rad = getPixelFromCircleRadius ( sizeArr[j] , width );
					float circleX = (float)j*perX;
					float circleY = (coordsArr[j]-minValue)*(float)height / (maxValue-minValue);
					
					if ( j > 0 )
					{
						float prevX = (float)(j-1) * perX;
						float prevY = (float)(coordsArr[j-1]-minValue)*(float)height / (maxValue-minValue);
						
						EuclidPoint pt = 
								new EuclidLine ( new EuclidPoint ( circleX , circleY ) , 
													new EuclidPoint ( prevX , prevY ) ).getPointOfLine(true, rad);
						mCircleOuterPoint[i][j][0] = new EuclidPoint ( pt.getX() , pt.getY() );
					}
					
					if ( j < legendCount - 1 )
					{
						float nextX = (float)(j+1) * perX;
						float nextY = (coordsArr[j+1]-minValue)*(float)height / (maxValue-minValue);
						
						EuclidPoint pt = 
								new EuclidLine ( new EuclidPoint ( circleX , circleY ) , 
													new EuclidPoint ( nextX , nextY ) ).getPointOfLine(false, rad);
						mCircleOuterPoint[i][j][1] = new EuclidPoint ( pt.getX() , pt.getY() ); 
					}
				}
				
				if ( i == 0 ) 
				{
					for ( j = 0 ; j < legendCount ; j++ )
					{
						String expr = ""; 
						EuclidPoint ptPrev = mCircleOuterPoint[i][j][0];
						EuclidPoint ptNext = mCircleOuterPoint[i][j][1];
						
						if ( ptPrev == null ) { expr += "PREV:NULL\n"; }
						else 
						{
							expr += "PREV:X:" + Float.toString(ptPrev.getX()) + "/" + "Y:" + Float.toString(ptPrev.getY()) + "\n";
						}
						
						if ( ptNext == null ) { expr += "NEXT:NULL\n"; }
						else 
						{
							expr += "NEXT:X:" + Float.toString(ptNext.getX()) + "/" + "Y:" + Float.toString(ptNext.getY()) + "\n";
						}
						
						Log.i(TAG , expr);
					}
				}
			}
		}
		
		private void initCircleAnimation ()
		{
			mCircleAnim = new CircleAnim [mVO.size()][];
			int i; int j;
			
			for ( i = 0 ; i < mCircleAnim.length ; i++ )
				
			{
				mCircleAnim[i] = new CircleAnim[mVO.get(i).getSizeArr().length];
				for ( j = 0 ; j < mCircleAnim[i].length ; j++ )
				{
					mCircleAnim[i][j] = new CircleAnim ();
					mCircleAnim[i][j].mCurrent = 0.0f;
					mCircleAnim[i][j].mMax = mVO.get(i).getSizeArr()[j];
				}
			}
		}
		
		private void initPaints ()
		{
			mPaintBubble = new Paint ();
			mPaintBubble.setFlags(Paint.ANTI_ALIAS_FLAG);
			mPaintBubble.setAntiAlias(true);
			mPaintBubble.setColor(Color.GRAY);
			mPaintBubble.setStrokeWidth(3.0f);
			mPaintBubble.setAlpha((int)(256*(1-50)));
			
			mPaintAxisLine = new Paint ();
			mPaintAxisLine.setFlags(Paint.ANTI_ALIAS_FLAG);
			mPaintAxisLine.setAntiAlias(true);
			mPaintAxisLine.setColor(Color.GRAY);
			mPaintAxisLine.setStrokeWidth(3.0f);
			
			mPaintGuideLine = new Paint ();
			mPaintGuideLine.setFlags(Paint.ANTI_ALIAS_FLAG);
			mPaintGuideLine.setPathEffect(new DashPathEffect(new float[] {5,10}, 0));
			mPaintGuideLine.setColor(Color.rgb(210, 210, 210));
			mPaintGuideLine.setStrokeWidth(3.0f);
			
			mPaintAxisMarker = new Paint ();
			mPaintAxisMarker.setFlags(Paint.ANTI_ALIAS_FLAG);
			mPaintAxisMarker.setColor(Color.GRAY);
			mPaintAxisMarker.setAntiAlias(true);
			mPaintAxisMarker.setStrokeWidth(3.0f);
			
			mPaintAxisText = new Paint ();
			mPaintAxisText.setFlags(Paint.ANTI_ALIAS_FLAG);
			mPaintAxisText.setAntiAlias(true);
			mPaintAxisText.setColor(Color.rgb(30, 30, 30));
		}
		
		private boolean drawGraphWithAnimation ( GraphCanvasWrapper gcw , int width , int height , float threshold )
		{
			int i; int j;
			for ( i = 0 ; i < mVO.size() ; i++ )
			{
				BubbleGraph bg = mVO.get(i);
				float[] coordsArr = bg.getCoordinateArr();
				float maxValue = mVO.getMaxCoordinate();
				float minValue = mVO.getMinCoordinate();
				
				mPaintBubble.setColor(bg.getColor());
				mPaintBubble.setAlpha(150);
				
				float perX = (float)width/(float)coordsArr.length;				
				int curIdx = (int)threshold;
				
				// draw animated lines
				if ( curIdx < coordsArr.length-1 && mVO.isLineShow() == true )
				{
					gcw.drawLine ( curIdx*perX , 
								(coordsArr[curIdx]-minValue)*height / (maxValue-minValue) , 
								perX*threshold , 
								(bg.getCoordinateOfFloatIndex(threshold)-minValue)*height / (maxValue-minValue) , mPaintBubble );
				}
				
				if ( curIdx < coordsArr.length )
				{
					float rad = getPixelFromCircleRadius ( mCircleAnim[i][curIdx].mCurrent , width );
					gcw.drawCircle ( curIdx*perX , (coordsArr[curIdx]-minValue)*height / (maxValue-minValue) , 
						 	rad , mPaintBubble );
					
					if ( mCircleAnim[i][curIdx].mCurrent <= mCircleAnim[i][curIdx].mMax )
					{
						 mCircleAnim[i][curIdx].mCurrent += 1.0f;
					}
				}
				
				
				int loopLimit = curIdx;
				if ( curIdx >= coordsArr.length ) { loopLimit = coordsArr.length; }
				
				// draw prev animated line
				for ( j = 0 ; j < loopLimit ; j++ )
				{
					if ( loopLimit == coordsArr.length && j < coordsArr.length-1 )
					{
						EuclidPoint ptPrev = mCircleOuterPoint[i][j][1];
						EuclidPoint ptNext = mCircleOuterPoint[i][j+1][0];
						
						if ( ptPrev != null && ptNext != null )
						{
							gcw.drawLine( ptPrev.getX() , ptPrev.getY() , ptNext.getX() , ptNext.getY() , mPaintBubble );
						}
					}
					
					else if ( j < coordsArr.length-1 && mVO.isLineShow() == true )
					{	
						gcw.drawLine( j*perX , (coordsArr[j]-minValue)*height / (maxValue-minValue) , 
								(j+1)*perX , (coordsArr[j+1]-minValue)*height / (maxValue-minValue) , mPaintBubble );
					}
					
					if ( j < coordsArr.length )
					{
						float rad = getPixelFromCircleRadius ( mCircleAnim[i][j].mCurrent , width );
						gcw.drawCircle ( j*perX , (coordsArr[j]-minValue)*height / (maxValue-minValue) , rad , mPaintBubble );
			
						if ( mCircleAnim[i][j].mCurrent <= mCircleAnim[i][j].mMax )
						{
							 mCircleAnim[i][j].mCurrent += 1.0f;
						}
					}
				}
			}
			
			return false;
		}
		
		private void drawGraphWithoutAnimation ( GraphCanvasWrapper gcw , int width , int height )
		{
			int i; int j;
			
			for ( i = 0 ; i < mVO.size() ; i++ )
			{
				BubbleGraph bg = mVO.get(i);
				float[] coordsArr = bg.getCoordinateArr();
				float[] sizeArr = bg.getSizeArr();
				float maxValue = mVO.getMaxCoordinate();
				float minValue = mVO.getMinCoordinate();
				
				mPaintBubble.setColor(bg.getColor());
				mPaintBubble.setAlpha(150);
				
				float perX = (float)width/(float)coordsArr.length;
				if ( coordsArr.length != sizeArr.length ) { continue; }
				
				for ( j = 0 ; j < coordsArr.length ; j++ )
				{	
					float rad = getPixelFromCircleRadius ( sizeArr[j] , width );
					
					float circleX = j*perX;
					float circleY = (coordsArr[j]-minValue)*height / (maxValue-minValue);
					
					gcw.drawCircle ( circleX , circleY , rad , mPaintBubble );
					
					if ( j < coordsArr.length - 1 )
					{
						EuclidPoint ptPrev = mCircleOuterPoint[i][j][1];
						EuclidPoint ptNext = mCircleOuterPoint[i][j+1][0];
						
						if ( ptPrev != null && ptNext != null )
						{
							gcw.drawLine( ptPrev.getX() , ptPrev.getY() , ptNext.getX() , ptNext.getY() , mPaintBubble );
						}
					}
				}
			}
		}
		
		private float getPixelFromCircleRadius ( float rad , int width )
		{
			float ret = 0.0f;
			ret = ((rad*((float)width/(float)mVO.get(0).getCoordinateArr().length)) / mVO.getMaxSize())/(float)2; 
			return ret;
		}
		
		private void drawBaseline ( GraphCanvasWrapper gcw , int width , int height )
		{
			int i;
			int totalSize = mVO.get(0).getCoordinateArr().length;
			float yPos = 0.0f;
			float perY = (float)height/(float)totalSize;
			
			for ( i = 0 ; i < totalSize ; i++ )
			{
				yPos += perY;
				gcw.drawLine(0.0f, yPos , width, yPos , mPaintGuideLine);
			}
		}
		
		
		
		private void drawGraphName(Canvas canvas, int width , int height) 
		{
			GraphNameBox gnb = mVO.getGraphNameBox();
			
			if(gnb != null)
			{
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
				
				int graphSize = mVO.size();
				
				for (int i = 0; i < graphSize; i++) 
				{	
					String text = mVO.get(i).getName();
					Rect rect = new Rect();
					pNameText.getTextBounds(text, 0, text.length(), rect);
					
					if(rect.width() > maxTextWidth){
						maxTextWidth = rect.width();
						maxTextHeight = rect.height();
					}
					mVO.get(i).getName();
				}
				
				mVO.get(0).getName();
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
					pIcon.setColor(mVO.get(i).getColor());
					canvas.drawRect(width - (nameboxMarginRight + nameboxWidth) - nameboxPadding,
							nameboxMarginTop + (maxCellHight * i) + nameboxPadding + (nameboxIconMargin * i), 
							width - (nameboxMarginRight + maxTextWidth) - nameboxPadding - nameboxTextIconMargin, 
							nameboxMarginTop + maxCellHight * (i+1) + nameboxPadding + nameboxIconMargin * i, pIcon);
					
					String text = mVO.get(i).getName();
					canvas.drawText(text, width - (nameboxMarginRight + maxTextWidth) - nameboxPadding, 
							nameboxMarginTop + maxTextHeight/2 + maxCellHight * i + maxCellHight/2 + nameboxPadding  + nameboxIconMargin * i, pNameText);
				}
			}
		}
		
		private void drawAxisMark ( GraphCanvasWrapper gcw , int width , int height )
		{
			int i;
			int totalSizeX = mVO.get(0).getCoordinateArr().length;
			float perX = (float)width / (float)totalSizeX;
			
			// draw X axis mark first 
			for ( i = 0 ; i < totalSizeX ; i++ )
			{
				gcw.drawLine( (float)(i)*perX , -10.0f , (float)(i)*perX , 0.0f , mPaintAxisMarker );
			}
			
			int totalSizeY = mVO.getTotalCountOfItem() / mVO.size();
			float perY = (float)height / (float)totalSizeY;
			
			// draw Y axis mark 
			for ( i = 0 ; i <= totalSizeY ; i++ )
			{
				gcw.drawLine ( -10.0f , (float)(i)*perY , 0.0f , (float)(i)*perY , mPaintAxisMarker );
			}
		}
		
		private void drawAxisValue ( GraphCanvasWrapper gcw , int width , int height )
		{
			int i;
			int totalSizeX = mVO.getLegendArr().length;
			float perX = (float)width / (float)totalSizeX;
			
			// draw X axis text first 
			for ( i = 0 ; i < mVO.getLegendArr().length ; i++ )
			{
				String text = mVO.getLegendArr()[i];
				mPaintAxisText.measureText(text);
				mPaintAxisText.setTextSize(20);
				Rect rect = new Rect ();
				mPaintAxisText.getTextBounds(text, 0, text.length(), rect);
				
				gcw.drawText(text, i*perX - (rect.width()/2) , -(20+rect.height()) , mPaintAxisText);
			}
			
			int totalSizeY = mVO.getTotalCountOfItem() / mVO.size();
			float perY = (float)height / (float)totalSizeY;
			
			float max = mVO.getMaxCoordinate();
			float min = mVO.getMinCoordinate();
			float valuePerY = (max-min)/totalSizeY;
			float cur = min;
			
			// draw Y axis mark 
			for ( i = 0 ; i <= totalSizeY ; i++ )
			{
				String text = String.format("%.1f", cur);
				mPaintAxisText.measureText(text);
				mPaintAxisText.setTextSize(20);
				Rect rect = new Rect ();
				mPaintAxisText.getTextBounds(text, 0, text.length(), rect);
				
				gcw.drawText(text, -(20+rect.width()) , i*perY - (rect.height()/2) , mPaintAxisText);
				cur += valuePerY;
			}
		}
		
		private void drawAxisLine ( GraphCanvasWrapper gcw , int width , int height )
		{
			gcw.drawLine(0.0f, 0.0f, width, 0.0f, mPaintAxisLine);
			gcw.drawLine(0.0f, 0.0f, 0.0f, height, mPaintAxisLine);
		}
	}
}
