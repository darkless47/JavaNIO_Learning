package com.xli2017.main;

import java.util.EventObject;

/**
 * A customized event for control the running status of threads
 * @author xli2017
 *
 */
public class RunningEvent extends EventObject
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8370782444619949185L;
	
	/** The running for main panel thread and screen capture thread */
	private boolean runningStatus = false;

	/**
	 * Constructor
	 * @param source
	 */
	public RunningEvent(Object source)
	{
		super(source);
	}
	
	/**
	 * Set the running status
	 * @param b true for running; false for stopped
	 * @see getRunningStatus() for get
	 */
	public void setRunningStatus(boolean b)
	{
		this.runningStatus = b;
	}
	
	/**
	 * Get current running status
	 * @return true for running; false for stopped
	 * @see setRunningStatus(boolean b) for set
	 */
	public boolean getRunningStatus()
	{
		return this.runningStatus;
	}

}
