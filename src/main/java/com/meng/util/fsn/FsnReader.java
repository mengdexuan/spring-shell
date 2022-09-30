package com.meng.util.fsn;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


public class FsnReader {
	public static int intstep=2;
	public static int stringstep=4;
	public static int fsnHeadLengh=32;
	public static int fsnPerBodyNoImg=100;
	public static int fsnPerBodyImg=1644;
	public int filePos=0;
	public FsnInfo fsnInfo;
	public String fsnFilePath ;
	
	
	public FsnReader(String filePath){
		this.fsnFilePath=filePath;
	}
	
	public int parseInt(byte[]  fsnBytes){
		int ret=(int)MessageUtil.demarshallintLittle(fsnBytes,filePos,intstep);
		this.filePos +=intstep;
		return ret;
	}
	
	public long parseLong(byte[]  fsnBytes){
		long ret=MessageUtil.demarshallintLittle(fsnBytes,filePos,stringstep);
		this.filePos +=stringstep;
		return ret;
	}
	
	public byte[] parseByte(byte[]  fsnBytes,int length){
		byte[] ret=MessageUtil.convertByteMarshall(fsnBytes,filePos,length);
		this.filePos +=length;
		return ret;
	}
	
	public void setHead(byte[]  fsnBytes) throws Exception {
		this.filePos=0;
		if(this.fsnInfo ==null){
			this.fsnInfo =new FsnInfo(this.fsnFilePath);
		}
		int[] headStart = new int[4];
		for (int i = 0; i < 4; i++) {
			headStart[i] = parseInt(fsnBytes);
		}
		fsnInfo.setHeadStart(headStart);
		int[] headString = new int[6];
		for (int i = 0; i < 6; i++) {
			headString[i] = parseInt(fsnBytes);
		}
		fsnInfo.setHeadString(headString);

		long counter =parseLong(fsnBytes);
		
		fsnInfo.setCounter(counter);
		int[] headEnd = new int[4];
		for (int i = 0; i < 4; i++) {
			headEnd[i] =  parseInt(fsnBytes);
		}
		fsnInfo.setHeadEnd(headEnd);
	}
	
	public FsnRow getSnoExpImg(byte[] fsnBytes) {
		FsnRow body = new FsnRow();
		int data = parseInt(fsnBytes);
		int time = parseInt(fsnBytes);
		int y = data >> 9;
		int m = (data - (y << 9)) >> 5;
		int d = data - (y << 9) - (m << 5);
		int hh = time >> 11;
		int mm = (time - (hh << 11)) >> 5;
		int ss = (time - (hh << 11) - (mm << 5)) << 1;
		StringBuffer DateBuf=new StringBuffer();
		StringBuffer TimeBuf=new StringBuffer();
		
		DateBuf.append( y + 1980);
		DateBuf.append( "-");
		DateBuf.append( MessageUtil.numAddZero(m,2) );
		DateBuf.append( "-");
		DateBuf.append( MessageUtil.numAddZero(d,2) );
		
		TimeBuf.append(MessageUtil.numAddZero(hh,2) );
		TimeBuf.append(":");
		TimeBuf.append(MessageUtil.numAddZero(mm,2) );
		TimeBuf.append(":");
		TimeBuf.append(MessageUtil.numAddZero(ss,2 ));
		
		body.setDateStr(DateBuf.toString());
		body.setTimeStr( TimeBuf.toString());
		
		body.setDateStr(DateBuf.toString());
		body.setTimeStr( TimeBuf.toString());
		

		body.setDateTime( body.getDateStr() + " " + body.getTimeStr());
		body.setTfFlag( parseInt(fsnBytes));
		StringBuffer errorCode = new StringBuffer();
		for (int i = 0; i < 3; i++) {
			int code = parseInt(fsnBytes);
			if(i==0){
				errorCode.append(code);
			}else{
				errorCode.append(":") ;
				errorCode.append(code);
			}
		}
		
		body.setErrorCode(errorCode.toString());
		String moneyFlag = "";
		for (int i = 0; i < 4; i++) {
			int flag = parseInt(fsnBytes);
			if (flag != 0) {
				moneyFlag += (char) flag;
			}
		}
		body.setMoneyFlag( moneyFlag);
		int ver = parseInt(fsnBytes);
		body.setVer(ver);
		body.setValuta(parseInt(fsnBytes));
		body.setCharNum(parseInt(fsnBytes));
		StringBuffer no = new StringBuffer();
		for (int i = 0; i < 12; i++) {
			int notemp = parseInt(fsnBytes);
			if (notemp != 0) {
				no.append( (char) notemp);
			}
		}
		body.setSno(no.toString());
		StringBuffer machineSNo =new StringBuffer();
		for (int i = 0; i < 24; i++) {
			int msno = parseInt(fsnBytes);
			if (msno != 0) {
				machineSNo.append( (char) msno);
			}
		}
		body.setMachineSno( machineSNo.toString());
		body.setReserve1( parseInt(fsnBytes));
		return body;
	}
	
	public BufferedImage getSnoImg(byte[] fsnBytes) {
		int num = parseInt(fsnBytes);
		int height = parseInt(fsnBytes);
		int width = parseInt(fsnBytes);
		BufferedImage image = new BufferedImage(width * num, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.fillRect(0, 0, width * num, height);
		g.setBackground(Color.WHITE);
		g.dispose();
		int i = 0;
		
		while ( i < width * num) {
			
			byte[] pic=parseByte(fsnBytes,4);
			String s=MessageUtil.toBinaryString(pic);  

			for (int j = 0; j < height  && j < s.length() ; j++) {
				if (s.charAt(j) == '1') {
					image.setRGB(i, j, 0xff000000);
				}
			}
			i++;
		}
		return image;

	}
	
	
	public void testSnoImg(byte[] fsnBytes) throws Exception{
		
		int num = parseInt(fsnBytes);
		int height = parseInt(fsnBytes);
		int width = parseInt(fsnBytes);
		int Reserve2 = parseInt(fsnBytes);
		if(num<=0||height<=0||width<=0){
			throw new Exception("\n" +
					"Picture data read failure, length width and character number can not be less than or equal to 0.");
		}
		if(num>12){
			throw new Exception("Picture data read failed, the number of key words can not be more than 12");
		}
		long mutiall = 4 * width * num;
		if (mutiall > fsnPerBodyImg - 108){
			throw new Exception("\n" +
					"The image data read failed, the image length is larger than the image buffer length.");
		}


	}
	
	public FsnInfo readFsn() throws Exception {
		FsnInfo ret=null;
		try{
			this.fsnInfo =new FsnInfo(this.fsnFilePath);
			byte[] fsnbytes =MessageUtil.toByteArray(this.fsnFilePath);
			this.fsnInfo.setSize(fsnbytes.length);
			setHead(fsnbytes);
			long counter = this.fsnInfo.getCounter();
			int size = this.fsnInfo.getHeadString()[2] != 0x2D ? fsnPerBodyImg : fsnPerBodyNoImg;
			
			
//			if (counter * size + fsnHeadLengh == fsnbytes.length) {
				ArrayList<FsnRow> list = new ArrayList<FsnRow>();
				long ValutaSum=0;
				for (int i = 0; i < counter; i++) {
					FsnRow body = new FsnRow();
					boolean noException=false;
					try{
						int thisPosSart=i * size + fsnHeadLengh;

						thisPosSart += (i* GzhCount.position());

						if(filePos<thisPosSart){
							filePos=thisPosSart;
						}

						body = getSnoExpImg(fsnbytes);
						body.setRecordNum(i+1);
						ValutaSum += body.getValuta();
						if(size!=fsnPerBodyNoImg){	
							//校验图片
//							testSnoImg(fsnbytes);
//							byte[] imgbytes=MessageUtil.byteCopy(fsnbytes, thisPosSart+fsnPerBodyNoImg, size-fsnPerBodyNoImg);
//							body.setImageBytes(imgbytes);
//							imgbytes=null;
						}else{
							body.setImageBytes(null);
						}
						noException=true;
						
					}
					catch(Exception e){
						this.fsnInfo.setAnalysisExcepted(true);
						this.fsnInfo.setAnalysisErrorMsg(e.getMessage());
					}
					if(noException){
						list.add(body);	
					}else{
						if(this.fsnInfo.isPermitException()){
							list.add(body);	
						}
					}
				}
				
				this.fsnInfo.setFsnRows(list) ;
			
				
//			}
			
			fsnbytes=null;
			ret=this.fsnInfo;
		}catch(Exception e){
			this.fsnInfo.setAnalysisExcepted(true);
			this.fsnInfo.setAnalysisErrorMsg(e.getMessage());
		}
		
		return ret;
	}


	public FsnInfo readFsn(byte[] fsnbytes) throws Exception {
		FsnInfo ret=null;
		try{
			this.fsnInfo =new FsnInfo("/a");
			this.fsnInfo.setSize(fsnbytes.length);
			setHead(fsnbytes);
			long counter = this.fsnInfo.getCounter();
			int size = this.fsnInfo.getHeadString()[2] != 0x2D ? fsnPerBodyImg : fsnPerBodyNoImg;

			if (counter * size + fsnHeadLengh == fsnbytes.length) {
				ArrayList<FsnRow> list = new ArrayList<FsnRow>();
				long ValutaSum=0;
				for (int i = 0; i < counter; i++) {
					FsnRow body = new FsnRow();
					boolean noException=false;
					try{
						int thisPosSart=i * size + fsnHeadLengh;
						if(filePos<thisPosSart){
							filePos=thisPosSart;
						}

						body = getSnoExpImg(fsnbytes);
						body.setRecordNum(i+1);
						ValutaSum += body.getValuta();
						if(size!=fsnPerBodyNoImg){

							testSnoImg(fsnbytes);

							byte[] imgbytes=MessageUtil.byteCopy(fsnbytes, thisPosSart+fsnPerBodyNoImg, size-fsnPerBodyNoImg);

							body.setImageBytes(imgbytes);
							imgbytes=null;
						}else{
							body.setImageBytes(null);
						}
						noException=true;

					}
					catch(Exception e){
						this.fsnInfo.setAnalysisExcepted(true);
						this.fsnInfo.setAnalysisErrorMsg(e.getMessage());
					}
					if(noException){
						list.add(body);
					}else{
						if(this.fsnInfo.isPermitException()){
							list.add(body);
						}
					}
				}

				this.fsnInfo.setFsnRows(list) ;


			}

			fsnbytes=null;
			ret=this.fsnInfo;
		}catch(Exception e){
			this.fsnInfo.setAnalysisExcepted(true);
			this.fsnInfo.setAnalysisErrorMsg(e.getMessage());
		}

		return ret;
	}



}
