package org.toolup.process.business;

import java.util.Date;

public class ProcessExecResult {
	
	private int returnCode;
	private Date startTime;
	private Date endTime;
	
	public Date getStartTime() {
		return startTime;
	}
	public ProcessExecResult startTime(Date startTime) {
		this.startTime = startTime;
		return this;
	}
	public Date getEndTime() {
		return endTime;
	}
	public ProcessExecResult endTime(Date endTime) {
		this.endTime = endTime;
		return this;
	}
	public int getReturnCode() {
		return returnCode;
	}
	public ProcessExecResult returnCode(int returnCode) {
		this.returnCode = returnCode;
		return this;
	}

	@Override
	public String toString() {
		return "ProcessExecResult [returnCode=" + returnCode + "]";
	}
	
	

}
