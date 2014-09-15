package com.handstudio.android.hzgrapher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	}
	
	public void onClick(View v){
		switch (v.getId()) {
		case R.id.btnLineGraph:
			startActivity(LineGraphActivity.class);
			break;
			
		case R.id.btnLineGraphWithRegion:
			startActivity(LineGraphWithRegionActivity.class);
			break;
			
		case R.id.btnCompareGraph:
			startActivity(LineCompareGraphActivity.class);
			break;
			
		case R.id.btnCircleGraph:
			startActivity(CircleGraphActivity.class);
			break;

		case R.id.btnRadarGraph:
			startActivity(RadarGraphActivity.class);
			break;
			
		case R.id.btnBubbleGraph:
			startActivity(BubbleGraphActivity.class);
			break;

		case R.id.btnCurveGraph:
			startActivity(CurveGraphActivity.class);
			break;
			
		case R.id.btnCurveGraphWithRegion:
			startActivity(CurveGraphWithRegionActivity.class);
			break;
			
		case R.id.btnCurveCompareGraph:
			startActivity(CurveCompareGraphActivity.class);
			break;
			
		case R.id.btnPieGraph:
			startActivity(PieGraphActivity.class);
			break;
			
		case R.id.btnScatterGraph:
			startActivity(ScatterGraphActivity.class);
			break;
			
		case R.id.btnBarGraph:
			startActivity(BarGraphActivity.class);
			break;
			
		default:
			break;
		}
	}

	private void startActivity(Class<?> cls) {
		Intent i = new Intent(this, cls);
		startActivity(i);
	}
}
