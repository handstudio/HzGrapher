package com.handstudio.android.hzgrapherlib.error;

import android.util.Log;

public class ErrorCode {
	public static final String TAG = "ErrorCode";
	
	private int code;
	private String message;
	
	private ErrorCode(int code, String message){
		this.code = code;
		this.message = message;
	}
	
	public void printError(){
		Log.e(TAG, "code = " + code + " , message = " + message);
	} 
	
	//COMMON
	public static final ErrorCode NOT_ERROR 						= new ErrorCode(0x00000000		,"NOT_ERROR");
	public static final ErrorCode GRAPH_VO_IS_EMPTY	 				= new ErrorCode(0x00000001		,"GRAPH_VO_IS_EMPTY");
	public static final ErrorCode GRAPH_VO_SIZE_ZERO	 			= new ErrorCode(0x00000002		,"GRAPH_VO_SIZE_ZERO");
	public static final ErrorCode INVALIDATE_GRAPH_AND_LEGEND_SIZE 	= new ErrorCode(0x00000003		,"INVALIDATE_GRAPH_AND_LEGEND_SIZE");
	public static final ErrorCode NOT_MATCH_GRAPH_SIZE 				= new ErrorCode(0x00000004		,"NOT_MATCH_GRAPH_SIZE");
	
	//line compare graph
	public static final ErrorCode LINE_COMPARE_GRAPH_SIZE_MUST_BE_2	= new ErrorCode(0x00010001		,"LINE_COMPARE_GRAPH_SIZE_MUST_BE_2");
}
