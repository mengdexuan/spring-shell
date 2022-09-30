package com.meng.util.fsn;

import java.util.ArrayList;


public class FsnInfo implements Comparable<FsnInfo> {
	private int[] headStart;
	private int[] headString;
	private long counter;
	private int[] headEnd;
	private String filePath;
	private ArrayList<FsnRow> fsnRows;
	private String fsnFileName;
	private String fsnFileNameWidthEx;

	private boolean permitException = false;
	private boolean analysisExcepted = false;
	private String analysisErrorMsg = "";
	private long size = 0;


	public FsnInfo() {

	}

	public FsnInfo(String filePath) throws Exception {
		if (filePath == null || "".equals(filePath)) {
			throw new Exception("The name of the file can not be empty\n" +
					"\n");
		}
		setFilePath(filePath);
	}

	

	public String getFsnFileName() {
		return fsnFileName;
	}

	public void setFsnFileName(String fsnFileName) {
		this.fsnFileName = fsnFileName;

	}

	

	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean isPermitException() {
		return permitException;
	}

	public void setPermitException(boolean permitException) {
		this.permitException = permitException;
	}

	public ArrayList<FsnRow> getFsnRows() {
		return fsnRows;
	}

	public void setFsnRows(ArrayList<FsnRow> fsnRows) {
		this.fsnRows = fsnRows;
	}

	public int[] getHeadStart() {
		return headStart;
	}

	public void setHeadStart(int[] headStart) {
		this.headStart = headStart;
	}

	public int[] getHeadString() {
		return headString;
	}

	public void setHeadString(int[] headString) {
		this.headString = headString;
	}

	public long getCounter() {
		return counter;
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}

	public int[] getHeadEnd() {
		return headEnd;
	}

	public void setHeadEnd(int[] headEnd) {
		this.headEnd = headEnd;
	}

	
	public boolean isAnalysisExcepted() {
		return analysisExcepted;
	}

	public void setAnalysisExcepted(boolean analysisExcepted) {
		this.analysisExcepted = analysisExcepted;
	}

	public String getAnalysisErrorMsg() {
		return analysisErrorMsg;
	}

	public void setAnalysisErrorMsg(String analysisErrorMsg) {
		this.analysisErrorMsg = analysisErrorMsg;
	}
	public int getFsnRecordCount() {
		if (fsnRows != null) {
			return fsnRows.size();
		} else {
			return 0;
		}
	}

	public String getFsnFileNameWidthEx() {
		return fsnFileNameWidthEx;
	}

	public void setFsnFileNameWidthEx(String fsnFileNameWidthEx) {
		this.fsnFileNameWidthEx = fsnFileNameWidthEx;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	@Override

	public String toString() {
		return "FsnInfo{" +
				"  \ncounter=" + counter +
				", \nfilePath='" + filePath + '\'' +
				", \nfsnRows=" + fsnRows +
				", \nfsnFileName='" + fsnFileName + '\'' +
				", \nfsnFileNameWidthEx='" + fsnFileNameWidthEx + '\'' +
				", \npermitException=" + permitException +
				", \nanalysisExcepted=" + analysisExcepted +
				", \nanalysisErrorMsg='" + analysisErrorMsg + '\'' +
				", \nsize=" + size +
				", \nheadStart=" + this.getHeadStart() +
				", \nheadString=" + this.getHeadString() +
				", \nheadEnd=" + this.getHeadEnd() +
				'}';
	}

	@Override
	public int compareTo(FsnInfo o) {
		return this.fsnFileName.compareTo(o.fsnFileName);
	}

}
