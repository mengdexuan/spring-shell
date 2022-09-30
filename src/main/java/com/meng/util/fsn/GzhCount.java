package com.meng.util.fsn;

/**
 * @author mengdexuan on 2019/7/13 18:48.
 */
public class GzhCount {

	//冠字号码位数（默认10位）
	public static Integer gzhCount = 10;

	//根据冠字号码位数，确定偏移量
	public static Integer position(){
		return (gzhCount - 10)*2;
	}

}
