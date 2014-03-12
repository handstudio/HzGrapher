package com.handstudio.android.hzgrapherlib.graphview;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.handstudio.android.hzgrapherlib.canvas.GraphCanvasWrapper;
import com.handstudio.android.hzgrapherlib.util.Converter;
import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
import com.handstudio.android.hzgrapherlib.vo.circlegraph.CircleGraph;
import com.handstudio.android.hzgrapherlib.vo.circlegraph.CircleGraphVO;

public class CircleGraphView extends SurfaceView implements Callback{

	public static final String TAG = "CircleGraphView";

	private SurfaceHolder mHolder;
	private DrawThread mDrawThread;
	private CircleGraphVO mCircleGraphVO = null;

	public CircleGraphView(Context context) {
		super(context);
		initView(context);
	}

	//Constructor
	public CircleGraphView(Context context, CircleGraphVO vo) {
		super(context);
		mCircleGraphVO = vo;
		initView(context, vo);
	}

	public CircleGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context, attrs, 0);
	}

	public CircleGraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		initView(context, attrs, defStyle);
	}

	private void initView(Context context) {
		mHolder = getHolder();
		mHolder.addCallback(this);
	}

	private void initView(Context context, CircleGraphVO vo) {
		mHolder = getHolder();
		mHolder.addCallback(this);
	}


	private void initView(Context context, AttributeSet attrs, int defStyle) {
		mHolder = getHolder();
		mHolder.addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {}

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
			}
			return true;
		}else if(action == MotionEvent.ACTION_MOVE){
			synchronized (touchLock) {
			}
			return true;
		}else if(action == MotionEvent.ACTION_UP){
			synchronized (touchLock) {
			}
			return true;
		}

		return super.onTouchEvent(event);
	}

	class DrawThread extends Thread{

		private int 		mCircleGraphIndex 	=0;

		private boolean 	isRun 				= true;
		private boolean 	isAnimation 		= false;
		private boolean 	isDirty 			= true;

		private int 		height 				= getHeight();
		private int 		width 				= getWidth();
		private int 		radius 				= (width -mCircleGraphVO.getPaddingLeft() - mCircleGraphVO.getPaddingRight())/2;

		private long 		animStartTime 		= -1;
		private float 		startAngle 			= 0;
		private float 		anim 				= 0.0f;
		private float 		total 				= calculateDataTotal(mCircleGraphVO.getArrGraph());
		private float 		mSweep[] 			= new float[mCircleGraphVO.getArrGraph().size()];

		Matrix matrix = new Matrix();
		//chartCenter
		PointF chartCenter = new PointF(width/2 + mCircleGraphVO.getCenterX(), height/2 + mCircleGraphVO.getCenterY());

		public DrawThread(SurfaceHolder holder, Context context) {
			mHolder = holder;
		}

		public void setRunFlag(boolean bool){
			isRun = bool;
		}

		@Override
		public void run() {
			super.run();

			Canvas canvas = null;
			GraphCanvasWrapper graphCanvasWrapper = null;
			
			isAnimation();
			animStartTime = System.currentTimeMillis();

			mCircleGraphVO.setArrGraph(calculateData(mCircleGraphVO.getArrGraph()));

			if(width < height){
				radius 	= (width - (mCircleGraphVO.getPaddingLeft() + mCircleGraphVO.getPaddingRight()))/2;
			}else{
				radius 	= (height -(mCircleGraphVO.getPaddingTop() + mCircleGraphVO.getPaddingBottom()))/2;
			}
			
			
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
				if( canvas != null){
					canvas.drawColor(Color.WHITE);
				}
				graphCanvasWrapper = new GraphCanvasWrapper(canvas, width, height, mCircleGraphVO.getPaddingLeft(), mCircleGraphVO.getPaddingBottom());
				calcTimePass();

				synchronized(mHolder){
					synchronized (touchLock) {

						try {
							// all draw circle
							drawCircle(canvas ,graphCanvasWrapper, total);
							
							if(anim == 1){
								// all draw Line
								drawLine(canvas , mCircleGraphVO.getArrGraph());
								// all draw Text
								drawText(canvas , mCircleGraphVO.getArrGraph());
							}
							
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

		private void drawCircle(Canvas canvas , GraphCanvasWrapper graphCanvasWrapper,float total) {
			for (int i = 0; i < mCircleGraphVO.getArrGraph().size(); i++) {
				this.mCircleGraphIndex = i;

				Paint paint = setPaint(mCircleGraphVO.getArrGraph().get(i).getColor());
				//draw rectF
				RectF mBigOval = new RectF(chartCenter.x-radius, chartCenter.y-radius, chartCenter.x+radius, chartCenter.y+radius);

				if(isAnimation){
					//draw circle
					calculateDrawTime(i);
					graphCanvasWrapper.drawArc(mBigOval, startAngle, mSweep[i], true, paint);
				}else{
					graphCanvasWrapper.drawArc(mBigOval, startAngle, mCircleGraphVO.getArrGraph().get(i).getAngleDegree(), true, paint);
				}
				
				startAngle = startAngle + mCircleGraphVO.getArrGraph().get(i).getAngleDegree();
			}
		}

		private void calculateDrawTime(int i) {
			mSweep[i] = mCircleGraphVO.getArrGraph().get(i).getAngleDegree() * anim;
		}




		private void drawText(Canvas canvas , List<CircleGraph> circleGraphList) {
			PointF dot = new PointF(chartCenter.x, chartCenter.y);

			float preDegree =0;

			for (CircleGraph circleGraph : circleGraphList) {
				Paint textPaint = setTextPaint(mCircleGraphVO.getTextColor() , mCircleGraphVO.getTextSize());

				float radAngle = (float) (Converter.DegreeToRadian((float) preDegree + (circleGraph.getAngleDegree()/2))); // use radian
				preDegree = preDegree + circleGraph.getAngleDegree();

				float rate = getCircleRate(circleGraph.getAngleArr());
				String rateText = changeRateText(rate);
				PointF rotateDot = getRotatePoint(dot, radAngle,textPaint,rateText);
				canvas.drawText(rateText, rotateDot.x, rotateDot.y, textPaint);
			}
		}

		private void drawLine(Canvas canvas , List<CircleGraph> circleGraphList) {
			PointF dot = new PointF(chartCenter.x, chartCenter.y);
			
			float preDegree =0;
			
			for (CircleGraph circleGraph : circleGraphList) {
				Paint linePaint = setLinePaint(mCircleGraphVO.getLineColor());

				float radAngle = (float) (Converter.DegreeToRadian((float) preDegree + (circleGraph.getAngleDegree()))); // use radian
				preDegree = preDegree + circleGraph.getAngleDegree();
				PointF rotateDot = getLineRotatePoint(dot, radAngle);
				canvas.drawLine(chartCenter.x, chartCenter.y, rotateDot.x, rotateDot.y, linePaint);
			}
		}
		
		private String changeRateText(float rate){

			if(String.valueOf(rate).substring(3,4).equals("0")){
				return String.valueOf((int) rate+"%");
			}else{
				String rateText = String.valueOf(Math.round(rate*10)*0.1);
				if(rateText.length() > 5){
					return (String.valueOf(Math.round(rate*10)*0.1)).substring(0,4)+"%";
				}else{
					return (String.valueOf(Math.round(rate*10)*0.1))+"%";
				}
			}
		}

		private float getCircleRate(float angleArr){
			return (angleArr/total * 100);
		}

		/**
		 * calc rotated point
		 * @param dot
		 * @param radAngle
		 */
		private PointF getRotatePoint(PointF dot, float radAngle, Paint textPaint, String rateText) {
			PointF rotateDot = new PointF();
			Rect rect = new Rect();
			textPaint.getTextBounds(rateText, 0, rateText.length(), rect);
			rotateDot.x = (float) (chartCenter.x + radius/1.4 *Math.cos(radAngle) - rect.width()/2);
			rotateDot.y = (float) (chartCenter.y + radius/1.4 *Math.sin(radAngle) + rect.height()/2);
			return rotateDot;
		}
		
		private PointF getLineRotatePoint(PointF dot, float radAngle) {
			PointF rotateDot = new PointF();
			rotateDot.x = (float) (chartCenter.x + radius *Math.cos(radAngle));
			rotateDot.y = (float) (chartCenter.y + radius *Math.sin(radAngle));
			return rotateDot;
		}


		private void calcTimePass(){

			if(isAnimation){
				long curTime = System.currentTimeMillis();
				long gapTime = (long) Math.ceil(curTime - animStartTime);
				long animDuration = mCircleGraphVO.getAnimation().getDuration();
				if(gapTime > animDuration){
					gapTime = animDuration;
					isDirty = false;
				}

				anim =  (float)gapTime/(float)animDuration;

			}else{
				isDirty = false;
			}

		}

		/**
		 * check graph line animation
		 */
		private void isAnimation() {
			if(mCircleGraphVO.getAnimation() != null){
				isAnimation = true;
			}else{
				isAnimation = false;
			}
		}


		private Paint setPaint(int color) {
			Paint paint = new Paint();
			paint.setFlags(Paint.ANTI_ALIAS_FLAG);
			paint.setAntiAlias(true); //text anti alias
			paint.setFilterBitmap(true); // bitmap anti alias
			paint.setColor(color);
			paint.setStrokeWidth(3);
			paint.setTextSize(20);
			return paint;
		}

		private Paint setTextPaint(int color , int textSize) {
			Paint paint = new Paint();
			Rect rect = new Rect();

			paint.setFlags(Paint.ANTI_ALIAS_FLAG);
			paint.setAntiAlias(true); //text anti alias
			paint.setFilterBitmap(true); // bitmap anti alias
			paint.setColor(color);
			paint.setStrokeWidth(3);
			paint.setTextSize(textSize);
			return paint;
		}
		
		private Paint setLinePaint(int color) {
			Paint paint = new Paint();
			paint.setFlags(Paint.ANTI_ALIAS_FLAG);
			paint.setAntiAlias(true); //text anti alias
			paint.setFilterBitmap(true); // bitmap anti alias
			paint.setColor(color);
			paint.setStrokeWidth(2);
			return paint;
		}

		private float calculateDataTotal(List<CircleGraph> circleGraphList) {  
			float total = 0;  
			
			for (CircleGraph circleGraph : circleGraphList) {
				total += circleGraph.getAngleArr();
			}
			return total;  
		}

		private List<CircleGraph> calculateData(List<CircleGraph> circleGraphList) {  
			float total = 0;  
			total = calculateDataTotal(circleGraphList);
			
			for (CircleGraph circleGraph : circleGraphList) {
				circleGraph.setAngleDegree((360 * (circleGraph.getAngleArr() / total)));
			}
			return circleGraphList;  
		} 

		private void drawGraphName(Canvas canvas) {
			GraphNameBox gnb = mCircleGraphVO.getGraphNameBox();
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


				int graphSize = mCircleGraphVO.getArrGraph().size();
				for (int i = 0; i < graphSize; i++) {


					String text = mCircleGraphVO.getArrGraph().get(i).getName();
					Rect rect = new Rect();
					pNameText.getTextBounds(text, 0, text.length(), rect);

					if(rect.width() > maxTextWidth){
						maxTextWidth = rect.width();
						maxTextHeight = rect.height();
					}

					mCircleGraphVO.getArrGraph().get(i).getName();

				}
				mCircleGraphVO.getArrGraph().get(0).getName();
				nameboxWidth = 1 * maxTextWidth + nameboxTextIconMargin + nameboxIconWidth;
				int maxCellHight = maxTextHeight;
				if(nameboxIconHeight > maxTextHeight){
					maxCellHight = nameboxIconHeight;
				}
				nameboxHeight = graphSize * maxCellHight + (graphSize-1) * nameboxIconMargin;

				if(canvas != null){
					canvas.drawRect(width - (nameboxMarginRight + nameboxWidth) - nameboxPadding*2,
							nameboxMarginTop, width - nameboxMarginRight, nameboxMarginTop + nameboxHeight + nameboxPadding*2, nameRextPaint);

					for (int i = 0; i < graphSize; i++) {

						pIcon.setColor(mCircleGraphVO.getArrGraph().get(i).getColor());
						canvas.drawRect(width - (nameboxMarginRight + nameboxWidth) - nameboxPadding,
								nameboxMarginTop + (maxCellHight * i) + nameboxPadding + (nameboxIconMargin * i), 
								width - (nameboxMarginRight + maxTextWidth) - nameboxPadding - nameboxTextIconMargin, 
								nameboxMarginTop + maxCellHight * (i+1) + nameboxPadding + nameboxIconMargin * i, pIcon);

						String text = mCircleGraphVO.getArrGraph().get(i).getName();
						canvas.drawText(text, width - (nameboxMarginRight + maxTextWidth) - nameboxPadding, 
								nameboxMarginTop + maxTextHeight/2 + maxCellHight * i + maxCellHight/2 + nameboxPadding  + nameboxIconMargin * i, pNameText);
					}
				}
			}
		}

	}

}
