package com.meng.util.fsn;

import java.util.ArrayList;
import java.util.Calendar;


public class FsnWriter {
	public final static int intstep=2;
	public final static int stringstep=4;
	public final static int fsnHeadLengh=32;
	public final static int fsnPerBodyNoImg=100;
	public final static int fsnPerBodyImg=1644;
	public int filePos=0;
	public FsnInfo fm;
	public String fsnFilePath ;
	public byte[]  fsnBytes;
	
	public FsnWriter(String filePath,FsnInfo fm){
		this.fsnFilePath=filePath;
		this.fm=fm;
	}
	
	public void intToUnsignByte(int  fsnBytes){
		byte[] unsignShort=MessageUtil.short2Bytes(fsnBytes);
		MessageUtil.arrayCopy(this.fsnBytes,unsignShort,this.filePos);
		this.filePos +=intstep;
		
	}
	
	public void longToUnsignByte(long  fsnBytes){
		byte[] unsignShort=MessageUtil.int2Bytes(fsnBytes);
		MessageUtil.arrayCopy(this.fsnBytes,unsignShort,this.filePos);
		this.filePos +=stringstep;
	}
	
	
	public void setHead() throws Exception {
		this.filePos=0;
		

		int[] headStartInt=fm.getHeadStart();
		for (int i = 0; i < 4; i++) {
			
			intToUnsignByte(headStartInt[i]);
		}

		int[] headString = fm.getHeadString();
		for (int i = 0; i < 6; i++) {
			intToUnsignByte(headString[i]);
		}
		long count=fm.getCounter();
		if(fm.getFsnRecordCount()>0  && fm.getFsnRecordCount()<count){
			count=fm.getFsnRecordCount();
		}
		longToUnsignByte(count);

		int[] headEnd =fm.getHeadEnd();
		for (int i = 0; i < 4; i++) {
			intToUnsignByte(headEnd[i]);
		}

	}
	
	@SuppressWarnings("deprecation")
	public void setSnoExpImg(FsnRow body) {


		String datetime=body.getDateTime();
		Calendar dt=MessageUtil.getCalendar(datetime, "yyyy-MM-dd HH:mm:ss");

		if(dt!=null){
			int y = dt.get(Calendar.YEAR);
			int m =  dt.get(Calendar.MONTH)+1;
			int d =  dt.get(Calendar.DAY_OF_MONTH);
			int hh =  dt.get(Calendar.HOUR_OF_DAY);
			int mm =  dt.get(Calendar.MINUTE);
			int ss =  dt.get(Calendar.SECOND);
			int date=((y-1980)<<9) + (m<<5) + d;
			int time = (hh<<11) + (mm<<5) + (ss>>1);
			intToUnsignByte(date);
			intToUnsignByte(time);
		}
		

		intToUnsignByte(body.getTfFlag());
		

		String errorCode = body.getErrorCode();
		
		String[] errorCodeByte;
		if(!(errorCode==null||errorCode=="")){
			errorCodeByte=errorCode.split(":");
			int errorCodeByteLen=errorCodeByte.length;
			for (int i = 0; i < errorCodeByteLen; i++){
				intToUnsignByte(Short.valueOf(errorCodeByte[i]));
			}
			for(int i = errorCodeByteLen; i < 3; i++){
				intToUnsignByte(0);
			}
		}
		

		String moneyFlag = body.getMoneyFlag();
		int moneyFlagLen=moneyFlag.length();
		for (int i = 0; i < moneyFlagLen; i++) {
			char flag = moneyFlag.charAt(i);
			intToUnsignByte(flag);  
		}
		
		for (int i = moneyFlagLen; i < 4; i++) {
			intToUnsignByte(0);
		}
		

		int ver = body.getVer();
		intToUnsignByte(ver);

		int valuta =body.getValuta();
		intToUnsignByte(valuta);

		int charNum = body.getCharNum();
		intToUnsignByte(charNum);

		String sno = body.getSno();
		int snoLen=sno.length();
		for (int i = 0; i < snoLen; i++) {
			char no = sno.charAt(i);
			intToUnsignByte(no);  
		}
		for (int i = snoLen; i < 12; i++) {
			
			intToUnsignByte(0);  
		}
		
		String machineSNo = body.getMachineSno();
		int machineSNoLen=machineSNo.length();
		for (int i = 0; i < machineSNoLen; i++) {
			char no = machineSNo.charAt(i);
			intToUnsignByte(no);  
		}
		for (int i = machineSNoLen; i < 24; i++) {
			
			intToUnsignByte(0);  
		}
		
		int reserve1 = body.getReserve1();
		intToUnsignByte(reserve1);

	}
	
	
	
	

	public void setSnoImg(byte[] imgBytes) throws Exception{

		MessageUtil.arrayCopy(this.fsnBytes,imgBytes,this.filePos);
		this.filePos +=imgBytes.length;

	}
	
	public byte[] getFsnByte() throws Exception {
		byte[] ret=null;
		try{
			if(this.fm==null){
				throw new Exception("FSN information can not be empty");
			}
			int size = this.fm.getHeadString()[2] != 0x2D ? fsnPerBodyImg : fsnPerBodyNoImg;
			int counter = this.fm.getFsnRecordCount(); //
			this.fsnBytes=new byte[counter * size + fsnHeadLengh];

			setHead();
			
			ArrayList<FsnRow> list =this.fm.getFsnRows();
				
			for (int i = 0; i < list.size(); i++) {
				FsnRow body = list.get(i);
				try{
					int thisPosSart=i * size + fsnHeadLengh;

					if(filePos<thisPosSart){
						filePos=thisPosSart;
					}

					setSnoExpImg(body);
					if(size!=fsnPerBodyNoImg){

						byte[] imgByte = body.getImageBytes(); //
						setSnoImg(imgByte);
						
					}
					
				}
				catch(Exception e){
					this.fm.setAnalysisExcepted(true);
					this.fm.setAnalysisErrorMsg(e.getMessage());
				}
				
			}
			ret=this.fsnBytes;
		}catch(Exception e){
			this.fm.setAnalysisExcepted(true);
			this.fm.setAnalysisErrorMsg(e.getMessage());
		}
		
		return ret;
	}
	
	public boolean writeFsn() throws Exception {
		boolean ret=false;
		byte[] fsnByte=getFsnByte();
		if(fsnByte!=null){
			ret=MessageUtil.writeFile(fsnByte,this.fsnFilePath);
		}
	
		return ret;
	}
	
	
}
