package com.meng.util.fsn;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MessageUtil {


	public static long demarshallintBig(byte[] buf, int ofs, int width) {
		long l = 0;
		for (int i = 0; i < width; i++) {
			l <<= 8;
			l |= (buf[ofs + i] & 0xFF);
		}
		return l;
	}

	public static long demarshallintLittle(byte[] buf, int ofs, int width) {
		long l = 0;
		for (int i = (width - 1); i >= 0; i--) {
			l <<= 8;
			l |= (buf[ofs + i] & 0xFF);
		}
		return l;
	}

	
	  /** 字节数组高低端字节序转换
	   * @param buf  待转换数组
	   * @param ofs  待转换字节数组的字节起始位置
	   * @param width 待转换的长度
	   * @return 转换后的字节数组
	   */
	public static byte[] convertByteMarshall(byte[] buf, int ofs,int width) {
		byte[] ret; 
		ret = new byte[width];
		for (int i =0; i <width; i++) { 
			ret[i]=buf[(ofs+width-1)-i]; 
		} return ret; 
	}
	 
	/**
	 * 从字节数组中拷贝截取部分字节数组，并做高地位转换
	 * 
	 * @param buf
	 *            源字节数据
	 * @param ofs
	 *            拷贝起始位置
	 * @param width
	 *            拷贝字节数
	 * @return
	 */
	public static byte[] byteCopy(byte[] buf, int ofs, int width) {
		byte[] ret = new byte[width];
		 for (int i = 0; i < width; i++) {
			ret[i] = buf[ofs+i];
		}
		//System.arraycopy(buf, ofs, ret, 0, width);
		return ret;
	}

	/**
	 * byte数组连接
	 * 
	 * @param buf1
	 *            源byte数组
	 * @param buf2
	 *            连接的byte数组
	 * @return
	 */
	public static byte[] arraycat(byte[] buf1, byte[] buf2) {
		byte[] bufret = null;
		int len1 = 0;
		int len2 = 0;
		if (buf1 != null) {
			len1 = buf1.length;
		}
		if (buf2 != null) {
			len2 = buf2.length;
		}
		if (len1 + len2 > 0) {
			bufret = new byte[len1 + len2];
		}
		if (len1 > 0) {
			System.arraycopy(buf1, 0, bufret, 0, len1);
		}
		if (len2 > 0) {
			System.arraycopy(buf2, 0, bufret, len1, len2);
		}
		return bufret;
	}
	
	/**
	 * 字节数组拷贝替换，将buf2数组的内容替换到buf1中起始位置为buf1Pos的字节，长度为buf2的长度
	 * @param buf1 复制到buf1数组中的数组
	 * @param buf2 被复制的数组
	 * @param buf1Pos 从复制到数组的第几位开始复制
	 * @return
	 */
	
	public static byte[] arrayCopy(byte[] buf1, byte[] buf2,int buf1Pos) {
		byte[] bufret = null;
		int len1 = 0;
		int len2 = 0;
		if (buf1 != null) {
			len1 = buf1.length;
		}
		if (buf2 != null) {
			len2 = buf2.length;
		}
		
		if (len1 > 0 && len2>0 && len1>=(buf1Pos+len2)) {
			System.arraycopy(buf2, 0, buf1, buf1Pos, len2);
		}
		
		return buf1;

	}
	
	

	public static byte[] short2Bytes(int num) {
		byte[] byteNum = new byte[2];
		for (int ix = 0; ix < 2; ix++) {
			int offset = ix  * 8;
			byteNum[ix] = (byte) ((num >> offset) & 0xff);
		}
		return byteNum;
	}
	
	public static byte[] int2Bytes(long num) {
		byte[] byteNum = new byte[4];
		for (int ix = 0; ix < 4; ix++) {
			int offset = ix * 8;
			byteNum[ix] = (byte) ((num >> offset) & 0xff);
		}
		

		
		return byteNum;
	}

	public static int bytes2Int(byte[] byteNum) {
		int num = 0;
		for (int ix = 0; ix < 4; ++ix) {
			num <<= 8;
			num |= (byteNum[ix] & 0xff);
		}
		return num;
	}

	public static byte int2OneByte(int num) {
		return (byte) (num & 0x000000ff);
	}

	public static int oneByte2Int(byte byteNum) {
		// 针对正数的int
		return byteNum > 0 ? byteNum : (128 + (128 + byteNum));
	}

	public static byte[] long2Bytes(long num) {
		byte[] byteNum = new byte[8];
		for (int ix = 0; ix < 8; ++ix) {
			int offset = 64 - (ix + 1) * 8;
			byteNum[ix] = (byte) ((num >> offset) & 0xff);
		}
		return byteNum;
	}

	public static long bytes2Long(byte[] byteNum) {
		long num = 0;
		for (int ix = 0; ix < 8; ++ix) {
			num <<= 8;
			num |= (byteNum[ix] & 0xff);
		}
		return num;
	}

	

	private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public static String toBinaryString(byte b) {
		int u = toUnsigned(b);
		return new String(new char[] { DIGITS[(u >>> 7) & 0x1],
				DIGITS[(u >>> 6) & 0x1], DIGITS[(u >>> 5) & 0x1],
				DIGITS[(u >>> 4) & 0x1], DIGITS[(u >>> 3) & 0x1],
				DIGITS[(u >>> 2) & 0x1], DIGITS[(u >>> 1) & 0x1],
				DIGITS[u & 0x1] });
	}

	public static String toBinaryString(byte... bytes) {
		char[] buffer = new char[bytes.length * 8];
		for (int i = 0, j = 0; i < bytes.length; ++i) {
			int u = toUnsigned(bytes[i]);
			buffer[j++] = DIGITS[(u >>> 7) & 0x1];
			buffer[j++] = DIGITS[(u >>> 6) & 0x1];
			buffer[j++] = DIGITS[(u >>> 5) & 0x1];
			buffer[j++] = DIGITS[(u >>> 4) & 0x1];
			buffer[j++] = DIGITS[(u >>> 3) & 0x1];
			buffer[j++] = DIGITS[(u >>> 2) & 0x1];
			buffer[j++] = DIGITS[(u >>> 1) & 0x1];
			buffer[j++] = DIGITS[u & 0x1];
		}
		return new String(buffer);
	}
	
		
	//返回int数值的二进制字符串
	public static String toBinaryString(int num) {
		byte[] numbyte=int2Bytes(num);
		return toBinaryString(numbyte);
	}

	public static String toHexString(byte b) {
		int u = toUnsigned(b);
		return new String(new char[] { DIGITS[u >>> 4], DIGITS[u & 0xf] });
	}

	public static String toHexString(byte... bytes) {
		char[] buffer = new char[bytes.length * 2];
		for (int i = 0, j = 0; i < bytes.length; ++i) {
			int u = toUnsigned(bytes[i]);
			buffer[j++] = DIGITS[u >>> 4];
			buffer[j++] = DIGITS[u & 0xf];
		}
		return new String(buffer);
	}

	private static int toUnsigned(byte b) {
		return b < 0 ? b + 256 : b;
	}
	
	/**
	 * 反格式化byte
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] hex2byte(String s) {
		byte[] src = s.toLowerCase().getBytes();
		byte[] ret = new byte[src.length / 2];
		for (int i = 0; i < src.length; i += 2) {
			byte hi = src[i];
			byte low = src[i + 1];
			hi = (byte) ((hi >= 'a' && hi <= 'f') ? 0x0a + (hi - 'a')
					: hi - '0');
			low = (byte) ((low >= 'a' && low <= 'f') ? 0x0a + (low - 'a')
					: low - '0');
			ret[i / 2] = (byte) (hi << 4 | low);
		}
		return ret;
	}

	/**
	 * 格式化byte
	 * 
	 * @param b
	 * @return
	 */
	public static String byte2hex(byte[] b) {
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
				'B', 'C', 'D', 'E', 'F' };
		char[] out = new char[b.length * 2];
		for (int i = 0; i < b.length; i++) {
			byte c = b[i];
			out[i * 2] = Digit[(c >>> 4) & 0X0F];
			out[i * 2 + 1] = Digit[c & 0X0F];
		}

		return new String(out);
	}
	
	/**
	 * 得文件字节流，ByteArrayOutputStream方式
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray(String filename) throws IOException {

		File f = new File(filename);
		if (!f.exists()) {
			throw new FileNotFoundException(filename);
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(f));
			int buf_size = 1024;
			byte[] buffer = new byte[buf_size];
			int len = 0;
			while (-1 != (len = in.read(buffer, 0, buf_size))) {
				bos.write(buffer, 0, len);
			}
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			bos.close();
		}
	}

	/**
	 * 字节数组内容写入文件
	 * @param content
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static boolean writeFile(byte[] content, String fileName) throws Exception {
		boolean ret=false;
		String filepath= getFilePath(fileName) ;
		File file = new File(filepath);

		if (!file.exists()) {

        	Boolean isOk = file.mkdirs();
			if (!isOk) {
				throw new Exception("Failed to create a folder:" + filepath);
			}
		}

		synchronized (MessageUtil.class){
			FileOutputStream out = new FileOutputStream(fileName);
			try {
				IoUtil.write(out,true,content);
				ret = true;
			} catch (IORuntimeException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	/**
	 * 从文件路径中获取文件名，包括文件后缀，非File方式
	 * @param filePath
	 * @return
	 */
	public static String getFileNameWithEx(String filePath){
		String ret="";
		try{
			if(filePath!=null){
				ret=filePath.trim().substring(filePath.lastIndexOf("\\")+1);
				ret=ret.substring(ret.lastIndexOf("/")+1);
			}
		}catch(Exception e){
			
		}
		return ret;
	}
	
	/**
	 * 从文件路径中获取文件名后缀名，不包括"."，非File方式
	 * @param filePath
	 * @return
	 */
	public static String getFileNameEx(String filePath){
		String ret=getFileNameWithEx(filePath);
		try{
			if(filePath!=null){
				filePath=filePath.trim();
				int fileNameLen=filePath.length();
				int lastDotPos=filePath.lastIndexOf(".");
				if(lastDotPos>-1){
					lastDotPos ++; //向后移动一个位置，不取.
					ret=Substring(filePath, lastDotPos, fileNameLen-lastDotPos);
				}
			}
		}catch(Exception e){
			
		}
		return ret;
	}
	
	/**
	 * 从文件名中获取文件路径，不包括文件名，非File方式
	 * @param fileName 文件名
	 * @return
	 */
	public static String getFilePath(String fileName){
		String ret="";
		try{
			if(fileName!=null){
				String fn=getFileNameWithEx(fileName);
				ret=fileName.trim().substring(0,fileName.lastIndexOf(fn));
			}
		}catch(Exception e){
			
		}
		return ret;
	}
	
	/**
	 * @Description: 安全字符串截取函数，如果不合法，则返回“”空

	 * @param str
	 *            :源字符串
	 * @param slen
	 *            ：截取开始的位置，从0开始
	 * @param len
	 *            ：截取的长度
	 * @return String
	 * @throws
	 */
	public static String Substring(String str, int slen, int len) {
		String ret = "";
		if(str==null){
			str="";
		}
		String instr =str;
		int strlen = instr.length();
		int endlen = slen + len;
		if (slen < 0) {
			slen = 0;
		}
		if (strlen >= slen) { // 起始位置小于等于字符串长度，才有可能返回数据
			if (endlen > strlen) { // 终止位置如果大于字符串长度
				endlen = strlen;
			}
			ret = instr.substring(slen, endlen);
		}
		return ret;
	}
	
	/**
	 * num数字型前补零
	 * 
	 * @param fieldValue
	 *            字符串值
	 * @param len
	 *            总长度
	 * @return
	 */
	public static String numAddZero(String fieldValue, int len) {
		int seq_len = 0;
		StringBuffer ret = new StringBuffer();
		if (null != fieldValue && !"".equals(fieldValue)) {
			seq_len = fieldValue.length();
		}

		for (int i = 0; i < len - seq_len; i++) {
			ret.append(0);
		}
		ret.append(fieldValue);
		return ret.toString();
	}
	
	public static String numAddZero(int fieldValue, int len) {

		StringBuffer ret = new StringBuffer();
		int seq_len = IntLen(fieldValue);

		for (int i = 0; i < len - seq_len; i++) {
			ret.append(0);
		}
		ret.append(fieldValue);
		return ret.toString();
	}
	
	/**
	 * @Description: 计算int型数据的字符数
	 * @param value
	 * @return int
	 * @throws
	 */
	public static int IntLen(int value) {
		int ret = 1;
		if (value < 0) {
			value = -value;
		}
		int shang = value / 10; // 除以10，取商
		if (shang == 0) {
			return ret;
		} else if (shang > 0 && shang <= 9) {
			return ret + 1;
		} else {
			return ret + IntLen(shang);
		}
	}
	
	public static Calendar getCalendar(String date,String format){
		SimpleDateFormat sdf=new SimpleDateFormat(format);
		Calendar ca=Calendar.getInstance();
		try {
			ca.setTime(sdf.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ca;
	}

}
