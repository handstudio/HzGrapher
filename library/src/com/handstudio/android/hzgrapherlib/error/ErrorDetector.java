package com.handstudio.android.hzgrapherlib.error;

import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraphVO;
import com.handstudio.android.hzgrapherlib.vo.radargraph.RadarGraphVO;

public class ErrorDetector {
	public static ErrorCode checkGraphObject(LineGraphVO lineGraphVO){
		//1. vo check
		if(lineGraphVO == null){
			return ErrorCode.GRAPH_VO_IS_EMPTY;
		}
		
		//2. legend and graph size check
		int legendSize = lineGraphVO.getLegendArr().length;
		for (int i = 0; i < lineGraphVO.getArrGraph().size(); i++) {
			if(legendSize !=lineGraphVO.getArrGraph().get(i).getCoordinateArr().length){
				return ErrorCode.INVALIDATE_GRAPH_AND_LEGEND_SIZE;
			}
		}
		
		return ErrorCode.NOT_ERROR;
	}
	
	public static ErrorCode checkLineCompareGraphObject(LineGraphVO lineGraphVO){
		//1. vo check
		if(lineGraphVO == null){
			return ErrorCode.GRAPH_VO_IS_EMPTY;
		}

		
		//2 graph size must be 2
		if(lineGraphVO.getArrGraph().size() != 2){
			return ErrorCode.LINE_COMPARE_GRAPH_SIZE_MUST_BE_2;
		}
				
		//3. legend and graph size check
		int legendSize = lineGraphVO.getLegendArr().length;
		for (int i = 0; i < lineGraphVO.getArrGraph().size(); i++) {
			if(legendSize !=lineGraphVO.getArrGraph().get(i).getCoordinateArr().length){
				return ErrorCode.INVALIDATE_GRAPH_AND_LEGEND_SIZE;
			}
		}
		
		return ErrorCode.NOT_ERROR;
	}
	
	public static ErrorCode checkGraphObject(RadarGraphVO radarGraphVO){
		//1. vo check
		if(radarGraphVO == null){
			return ErrorCode.GRAPH_VO_IS_EMPTY;
		}
		
		//2. legend and graph size check
		int legendSize = radarGraphVO.getLegendArr().length;
		for (int i = 0; i < radarGraphVO.getArrGraph().size(); i++) {
			if(legendSize !=radarGraphVO.getArrGraph().get(i).getCoordinateArr().length){
				return ErrorCode.INVALIDATE_GRAPH_AND_LEGEND_SIZE;
			}
		}
		
		return ErrorCode.NOT_ERROR;
	}
}
