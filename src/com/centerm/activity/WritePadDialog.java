package com.centerm.activity;

import java.sql.PreparedStatement;
import java.util.ArrayList;

import com.handwritten.R;




import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;


public class WritePadDialog extends Dialog {

	  static final int[] COLORS = new int[] {
	        Color.BLACK, Color.RED, Color.YELLOW, Color.GREEN,
	        Color.CYAN, Color.BLUE, Color.MAGENTA,
	    };
	Context context;
	LayoutParams p ;
	DialogListener dialogListener;
	float pressure = 3.0f;
	float prPressure = 3.0f;
	private final Rect mInvalidRect = new Rect();
	int i = 0;
	private float mCurveEndX;
	private float mCurveEndY;
	private float mX;
	private float mY;
	private boolean isDrawing;
	private int index = 0;
	
	public WritePadDialog(Context context,DialogListener dialogListener) {
		super(context);
		this.context = context;
		this.dialogListener = dialogListener;
	}

	static final int BACKGROUND_COLOR = Color.WHITE;

	static final int BRUSH_COLOR = Color.BLACK;

	PaintView mView;

	/** The index of the current color to use. */
	int mColorIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.write_pad);
		
		p = getWindow().getAttributes();  //获取对话框当前的参数值   
		p.height = 300;//(int) (d.getHeight() * 0.4);   //高度设置为屏幕的0.4 
		p.width = 600;//(int) (d.getWidth() * 0.6);    //宽度设置为屏幕的0.6		   
		getWindow().setAttributes(p);     //设置生效
		
		
		mView = new PaintView(context);
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.tablet_view);
		frameLayout.addView(mView);
		mView.requestFocus();
		/*mView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.i("setOnTouchListener","point count"+ event.getPointerCount()+", x= "+event.getX()+",y="+event.getY()+",history count="+event.getHistorySize() );
				return false;
			}
		});*/
		Button btnClear = (Button) findViewById(R.id.tablet_clear);
		btnClear.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				 mView.clear();
			}
		});

		Button btnOk = (Button) findViewById(R.id.tablet_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					dialogListener.refreshActivity(mView.getCachebBitmap());
					WritePadDialog.this.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		Button btnCancel = (Button)findViewById(R.id.tablet_cancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				cancel();
			}
		});
	}
	

	/**
	 * This view implements the drawing canvas.
	 * 
	 * It handles all of the input events and drawing functions.
	 */
	class PaintView extends View {
		private Paint paint;
		private Canvas cacheCanvas;
		private Bitmap cachebBitmap;
		private Path path;
		BitmapShader bitmapShader;

		public Bitmap getCachebBitmap() {
			return cachebBitmap;
		}

		public PaintView(Context context) {
			super(context);					
			init();			
		}

		private void init(){
			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setStrokeWidth(3);
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(Color.BLACK);
			paint.setDither(true);	
			path = new Path();
			
			cachebBitmap = Bitmap.createBitmap(p.width, (int)(p.height*0.8), Config.ARGB_8888);			
			cacheCanvas = new Canvas(cachebBitmap);
			cacheCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
			cacheCanvas.drawColor(Color.WHITE);
			//构造渲染器BitmapShader  
	        bitmapShader = new BitmapShader(cachebBitmap,Shader.TileMode.MIRROR,Shader.TileMode.REPEAT);  
		}
		public void clear() {
			if (cacheCanvas != null) {
				
				paint.setColor(BACKGROUND_COLOR);
				cacheCanvas.drawPaint(paint);
				paint.setColor(Color.BLACK);
				cacheCanvas.drawColor(Color.WHITE);
				invalidate();			
			}
		}

		
		
		@Override
		protected void onDraw(Canvas canvas) {
			//canvas.drawColor(BRUSH_COLOR);
			//paint.setStrokeWidth(pressure);
			Log.i("mark", "onDraw");
			canvas.drawBitmap(cachebBitmap, 0, 0, null);
			canvas.drawPath(path, paint);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			
			int curW = cachebBitmap != null ? cachebBitmap.getWidth() : 0;
			int curH = cachebBitmap != null ? cachebBitmap.getHeight() : 0;
			if (curW >= w && curH >= h) {
				return;
			}

			if (curW < w)
				curW = w;
			if (curH < h)
				curH = h;

			Bitmap newBitmap = Bitmap.createBitmap(curW, curH, Bitmap.Config.ARGB_8888);
			Canvas newCanvas = new Canvas();
			newCanvas.setBitmap(newBitmap);
			if (cachebBitmap != null) {
				newCanvas.drawBitmap(cachebBitmap, 0, 0, null);
			}
			cachebBitmap = newBitmap;
			cacheCanvas = newCanvas;
		}

		private float cur_x, cur_y;

		/*@Override
		public boolean onTouchEvent(MotionEvent event) {
			
			/*float x = event.getX();
			float y = event.getY();
			pressure = event.getPressure();

			
			
			paint.setStrokeWidth(pressure+ (i++));
			Log.i("mark", "ontouchevent");
			//paint.setColor(Color.BLACK);
			//invalidate();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				Log.i("mark", "ACTION_DOWN");
				cur_x = x;
				cur_y = y;
				path.moveTo(cur_x, cur_y);
				break;
			}

			case MotionEvent.ACTION_MOVE: {
				Log.i("mark", "ACTION_MOVE");
				pressure = event.getPressure();
				path.quadTo(cur_x, cur_y, x, y);
				cur_x = x;
				cur_y = y;
				break;
			}

			case MotionEvent.ACTION_UP: {
				Log.i("mark", "ACTION_UP");
				cacheCanvas.drawPath(path, paint);
				path.reset();
				break;
			}
			}

			invalidate();

			return true;
		}*/

		@Override
		protected void drawableStateChanged() {
			Log.i("mark", "drawableStateChanged");
			super.drawableStateChanged();
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent event) {
			Log.i("mark", "dispatchTouchEvent"+event.getPressure());
			return super.dispatchTouchEvent(event);
		}

		@Override
		protected void dispatchDraw(Canvas canvas) {
			super.dispatchDraw(canvas);
		}

		@Override
		public boolean dispatchUnhandledMove(View focused, int direction) {
			Log.i("mark", "dispatchUnhandledMove");
			return super.dispatchUnhandledMove(focused, direction);
		}

		@Override
		public ArrayList<View> getTouchables() {
			// TODO Auto-generated method stub
			return super.getTouchables();
		}

		/*@Override
		public boolean onFilterTouchEventForSecurity(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();
			pressure = event.getPressure(); //需要进行增量计算

			
			paint.setStrokeWidth(pressure*5);
			Log.i("mark", "ontouchevent");
			//paint.setColor(Color.BLACK);
			//invalidate();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				Log.i("mark", "ACTION_DOWN");
				cur_x = x;
				cur_y = y;
				path.moveTo(cur_x, cur_y);
				mInvalidRect.set((int) x, (int) y, (int) x, (int) y);
				break;
			}

			case MotionEvent.ACTION_MOVE: {
				Log.i("mark", "ACTION_MOVE");
				path.lineTo(x, y);//(cur_x, cur_y, x, y);
				cur_x = x;
				cur_y = y;
				cacheCanvas.drawPath(path, paint);
				invalidate();
				break;
			}

			case MotionEvent.ACTION_UP: {
				Log.i("mark", "ACTION_UP");
				path.lineTo(x, y);
				cacheCanvas.drawPath(path, paint);
				path.reset();
				break;
			}
			}

			invalidate();
			return super.onFilterTouchEventForSecurity(event);
		}*/

		
		
		@Override
		public boolean onTrackballEvent(MotionEvent event) {
			// TODO Auto-generated method stub
			Log.i("mark", "onTrackballEvent");
			return super.onTrackballEvent(event);
		}

		
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {

			//printSamples(event);
			// TODO Auto-generated method stub
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				pressure = event.getPressure();
				touchDown(event);
				cacheCanvas.drawPath(path, paint);
				invalidate();
				return true;

			case MotionEvent.ACTION_MOVE:
				if (isDrawing) {
					Rect rect = touchMove(event);
					/*if (rect != null) {
						invalidate(rect);
					}*/
					invalidate();
					return true;
				}
				break;
			case MotionEvent.ACTION_UP:
				if (isDrawing) {
					touchUp(event);
					invalidate();
					return true;
				}
				break;
			}
			return super.onTouchEvent(event);
		}
		
		
		private void touchDown(MotionEvent event) {
			isDrawing = true;
			path.reset();
			float x = event.getX();
			float y = event.getY();

			mX = x;
			mY = y;
			//paint.setShader(bitmapShader);
			paint.setStrokeWidth(pressure*10);
			path.moveTo(x, y);

			mInvalidRect.set((int) x, (int) y, (int) x, (int) y);
			mCurveEndX = x;
			mCurveEndY = y;
		}

		@SuppressLint("NewApi")
		private Rect touchMove(MotionEvent event) {
			Rect areaToRefresh = null;
			Log.i("mark", "move");
			float x = event.getX();
			float y = event.getY();
			Log.i("point", "curx=" +x+", cur y= "+y+", prex ="+mX+", prey ="+mY);
			
			
			//Shader mshader = new BitmapShader(bitmap, tileX, tileY)
			//Shader mShader = new LinearGradient(mX,mY, x, y, null, null,Shader.TileMode.REPEAT);
			/*float dpressure = Math.abs( pressure - prPressure ); //压力差值
			int historysize = event.getHistorySize();
			Log.i("pointcount", ""+event.getPointerCount() );
			Log.i("history pointcount", ""+event.getHistorySize() );
			Log.i("get size", ""+event.getSize() );
			//Log.i("get size", ""+event.transform(matrix) );
			if( event.getPointerCount() > 1)
			{
				int actionId = event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK;
				int pointIndex = event.findPointerIndex(actionId);
				float k = 0.2f,add = 0.2f;
				if( (pressure - prPressure) < 0 )
				{
					 k = -0.2f;
					 add = -0.2f;
				}
				for( int j = 0; j < historysize; j++ )
				{
					float presr = event.getHistoricalPressure(pointIndex, j);
					float x1 = event.getHistoricalX(pointIndex, j);
					float y1 = event.getHistoricalY(pointIndex, j);
					float size = event.getHistoricalSize(pointIndex);
					
					
					if( k < (pressure - prPressure)*5 )
					{
						paint.setStrokeWidth( prPressure*5+ k );
						cacheCanvas.drawPoint( x1, y1, paint );
						k += add;
					}
					else
					{
						x = x1;
						y = y1;
						break;
					}
				}
			}*/
			
			if( x == mX && mY == y )
			{
				/*areaToRefresh = mInvalidRect;
				areaToRefresh.set((int) x, (int) y,
						(int) x, (int) y);
				areaToRefresh.union((int) x, (int) y, (int) x, (int) y);*/
				return areaToRefresh;
			}
			try{
			 final int N = event.getHistorySize();
             final int P = event.getPointerCount();
             Log.i("mark", "history size"+ N+"pointer count" + P);
             for (int i = 0; i < N; i++) {
                 for (int j = 0; j < P; j++) {
                	prPressure = pressure;
         			pressure = event.getHistoricalPressure(j, i);
                   Log.i("witerpad xxxx", "getToolType = "+ event.getToolType(j)
                             +",getHistoricalX = "+event.getHistoricalX(j, i)
                             +",getHistoricalY ="+event.getHistoricalY(j, i)
                             +",getHistoricalPressure="+event.getHistoricalPressure(j, i)
                             +",getHistoricalTouchMajor"+event.getHistoricalTouchMajor(j, i)
                             +",getHistoricalTouchMinor"+event.getHistoricalTouchMinor(j, i)
                             +",getHistoricalOrientation"+event.getHistoricalOrientation(j, i)
                             +",getHistoricalAxisValue"+event.getHistoricalAxisValue(MotionEvent.AXIS_DISTANCE, j, i)
                             +",getHistoricalAxisValue"+event.getHistoricalAxisValue(MotionEvent.AXIS_TILT, j, i) );
                   areaToRefresh = Beziercurve(mX, mY, event.getHistoricalX(j, i), event.getHistoricalY(j, i) );
                 }
             }
             for (int j = 0; j < P; j++) {
            		prPressure = pressure;
         			pressure = event.getHistoricalPressure(j);
            	  Log.i("witerpad", "getToolType = "+ event.getToolType(j)
                          +",getHistoricalX = "+event.getHistoricalX(j)
                          +",getHistoricalY ="+event.getHistoricalY(j)
                          +",getHistoricalPressure="+event.getHistoricalPressure(j)
                          +",getHistoricalTouchMajor"+event.getHistoricalTouchMajor(j)
                          +",getHistoricalTouchMinor"+event.getHistoricalTouchMinor(j)
                          +"getHistoricalOrientation"+event.getHistoricalOrientation(j)
                          +",getHistoricalAxisValue"+event.getHistoricalAxisValue(MotionEvent.AXIS_DISTANCE, j)
                          +",getHistoricalAxisValue"+event.getHistoricalAxisValue(MotionEvent.AXIS_TILT, j) );
            	  areaToRefresh = Beziercurve(mX, mY, event.getHistoricalX(j), event.getHistoricalY(j) );
             }
			}
			catch ( Exception e)
			{
				e.printStackTrace();
			}
			//paint.setShader(bitmapShader);
			/*prPressure = pressure;
			pressure = event.getPressure();
			paint.setStrokeWidth( ( ( pressure + prPressure ) * 5/2) );
			
			//paint.setShader(mShader);
			

			final float previousX = mX;
			final float previousY = mY;

			
			final float dx = Math.abs(x - previousX);
			final float dy = Math.abs(y - previousY);

			if ( dx >= 1 || dy >= 1 ) {
				areaToRefresh = mInvalidRect;
				areaToRefresh.set((int) mCurveEndX, (int) mCurveEndY,
						(int) mCurveEndX, (int) mCurveEndY);

				// 设置贝塞尔曲线的操作点为起点和终点的一半
				float cX = mCurveEndX = (x + previousX) / 2;
				float cY = mCurveEndY = (y + previousY) / 2;

				
				// mGesturePaint.setStrokeWidth(mPresure);
				// 实现绘制c；previousX, previousY为操作点，cX, cY为终点
				path.quadTo(previousX, previousY, cX, cY);
				// mPath.lineTo(x, y);

				// union with the control point of the new curve
				
				//areaToRefresh矩形扩大了border(宽和高扩大了两倍border)， border值由设置手势画笔粗细值决定
				 
				areaToRefresh.union( (int) previousX, (int) previousY,(int) previousX, (int) previousY );
			
				//areaToRefresh.union((int) x, (int) y, (int) x, (int) y);
				 

				// union with the end point of the new curve
				areaToRefresh.union((int) cX, (int) cY, (int) cX, (int) cY);

				// 第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
				
				
				cacheCanvas.drawPath(path, paint);
				invalidate();
				
				path.rewind();
				path.moveTo(mX, mY);

				mX = x;
				mY = y;
				// mPath.reset();
				//drawCanvas();
			}
			*/
			return areaToRefresh;
		}
		/*void printSamples(MotionEvent ev) {
		     final int historySize = ev.getHistorySize();
		     Log.i("historySize", ""+historySize);
		     final int pointerCount = ev.getPointerCount();
		     for (int h = 0; h < historySize; h++) {
		         Log.i("At time ",""+ev.getHistoricalEventTime(h) );
		         for (int p = 0; p < pointerCount; p++) {
		        	 Log.i("  pointer %d: (%f,%f)",""+
		                 ev.getPointerId(p)+":("+ev.getHistoricalX(p, h)+","+ev.getHistoricalY(p, h));
		         }
		     }
		     Log.i("At time :", ""+ev.getEventTime());
		     for (int p = 0; p < pointerCount; p++) {
		    	 Log.i("  pointer %d: (%f,%f)",""+
		             ev.getPointerId(p)+":(" +ev.getX(p)+","+ev.getY(p));
		     }
		 }*/

		private void touchUp(MotionEvent event) {
			cacheCanvas.drawPath(path, paint);
			isDrawing = false;
		}
		
		private Rect Beziercurve(float previousX, float previousY, float currX, float currY)
		{
			Rect areaToRefresh = null;
			paint.setStrokeWidth( ( ( pressure + prPressure ) * 5/2) );
			
			final float dx = Math.abs(currX - previousX);
			final float dy = Math.abs(currY - previousY);

			if ( dx >= 3 || dy >= 3 ) {
				/*areaToRefresh = mInvalidRect;
				areaToRefresh.set((int) mCurveEndX, (int) mCurveEndY,
						(int) mCurveEndX, (int) mCurveEndY);*/
				if( index >= COLORS.length )
				{
					index = 0;
				}
				paint.setColor(COLORS[index]);
				index++;
				paint.setAlpha(Math.min((int)(pressure * 255), 255));
				// 设置贝塞尔曲线的操作点为起点和终点的一半
				float cX = mCurveEndX = (currX + previousX) / 2;
				float cY = mCurveEndY = (currY + previousY) / 2;

				
				// mGesturePaint.setStrokeWidth(mPresure);
				// 实现绘制c；previousX, previousY为操作点，cX, cY为终点
				path.quadTo(previousX, previousY, cX, cY);
				// mPath.lineTo(x, y);

				// union with the control point of the new curve
				/*
				 * areaToRefresh矩形扩大了border(宽和高扩大了两倍border)， border值由设置手势画笔粗细值决定
				 */
				//areaToRefresh.union((int) previousX, (int) previousY,(int) previousX, (int) previousY);
				/*
				 * areaToRefresh.union((int) x, (int) y, (int) x, (int) y);
				 */

				// union with the end point of the new curve
				//areaToRefresh.union((int) cX, (int) cY, (int) cX, (int) cY);

				// 第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
				
				
				cacheCanvas.drawPath(path, paint);
				invalidate();

				mX = cX;
				mY = cY;
				path.rewind();
				path.moveTo(cX, cY);
			}
			return areaToRefresh;
			
		}
		
		
	}
	
	

}
