package com.xli2017.main;

import java.util.EventListener;

/**
 * Interface works with RunningEvent
 * @see RunningEvent.java
 * @author xli2017
 *
 */
public interface RunningListener extends EventListener
{
	public void RunningEvent(RunningEvent event);
}
