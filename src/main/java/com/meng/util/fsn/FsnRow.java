package com.meng.util.fsn;

import java.util.Arrays;

public class FsnRow {
	private String dateStr;  //验钞启动日期
	private String timeStr;  //验钞启动时间
	private String dateTime;  //验钞启动日期+时间
	private int tfFlag;  //真假币标志
	private String errorCode;  //表示最多3组假币特征码
	private String moneyFlag;  //币种标志
	private int  ver;  //年版或版本号标志
	private int valuta;  //币值
	private int charNum;  //冠字号码字符数；
	private String sno;  //存放识别的冠字号码
	private String machineSno;  //机具编号
	private int reserve1;  //保留字1
	private int recordNum;  //冠字号信息在Fsn文件中的行号
	private byte[] imageBytes;  //冠字号信息的图片数据字节数组
	private String imgStr; //图片压缩后的字符串
	private String accNo=""; //冠字号信息产生的账号

	public String getDateStr() {
		return dateStr;
	}
	public void setDateStr(String dateStr) {
		this.dateStr = dateStr;
	}
	public String getTimeStr() {
		return timeStr;
	}
	public void setTimeStr(String timeStr) {
		this.timeStr = timeStr;
	}
	public String getDateTime() {
		return dateTime;
	}
	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	public int getTfFlag() {
		return tfFlag;
	}
	public void setTfFlag(int tfFlag) {
		this.tfFlag = tfFlag;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getMoneyFlag() {
		return moneyFlag;
	}
	public void setMoneyFlag(String moneyFlag) {
		this.moneyFlag = moneyFlag;
	}
	public int getVer() {
		return ver;
	}
	public void setVer(int ver) {
		this.ver = ver;
	}
	public int getValuta() {
		return valuta;
	}
	public void setValuta(int valuta) {
		this.valuta = valuta;
	}
	public int getCharNum() {
		return charNum;
	}
	public void setCharNum(int charNum) {
		this.charNum = charNum;
	}
	public String getSno() {
		return sno;
	}
	public void setSno(String sno) {
		this.sno = sno;
	}
	public String getMachineSno() {
		return machineSno;
	}
	public void setMachineSno(String machineSno) {
		this.machineSno = machineSno;
	}
	public int getReserve1() {
		return reserve1;
	}
	public void setReserve1(int reserve1) {
		this.reserve1 = reserve1;
	}
	public int getRecordNum() {
		return recordNum;
	}
	public void setRecordNum(int recordNum) {
		this.recordNum = recordNum;
	}
	public byte[] getImageBytes() {
		return imageBytes;
	}
	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}
	public String getAccNo() {
		return accNo;
	}
	public void setAccNo(String accNo) {
		this.accNo = accNo;
	}
	public String getImgStr() {
		return imgStr;
	}
	public void setImgStr(String imgStr) {
		this.imgStr = imgStr;
	}

	@Override
	public String toString() {
		return "\n FsnRow{" +
				"dateStr='" + dateStr + '\'' +
				", timeStr='" + timeStr + '\'' +
				", dateTime='" + dateTime + '\'' +
				", tfFlag=" + tfFlag +
				", errorCode='" + errorCode + '\'' +
				", moneyFlag='" + moneyFlag + '\'' +
				", ver=" + ver +
				", valuta=" + valuta +
				", charNum=" + charNum +
				", sno='" + sno + '\'' +
				", machineSno='" + machineSno + '\'' +
				", reserve1=" + reserve1 +
				", recordNum=" + recordNum +
				", imageBytes=" + Arrays.toString(imageBytes) +
				", imgStr='" + imgStr + '\'' +
				", accNo='" + accNo + '\'' +
				'}';
	}
}