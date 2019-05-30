package com.xli2017.main;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A modified ScheduledThreadPoolExecutor
 * Can be paused when necessary and then resume after pause
 * @author Oracle.com
 *
 */
public class PausableThreadPoolExecutor extends ScheduledThreadPoolExecutor
{
	private boolean isPaused;
	private ReentrantLock pauseLock = new ReentrantLock();
	   private Condition unpaused = pauseLock.newCondition();

	   public PausableThreadPoolExecutor(int corePoolSize) { super(corePoolSize); }
	 
	   protected void beforeExecute(Thread t, Runnable r) {
	     super.beforeExecute(t, r);
	     pauseLock.lock();
	     try {
	       while (isPaused) unpaused.await();
	     } catch(InterruptedException ie) {
	       t.interrupt();
	     } finally {
	       pauseLock.unlock();
	     }
	   }
	   
	   /**
	    * Call this to pause a running thread executor
	    * @see resume() for resume a paused thread executor
	    */
	   public void pause() {
	     pauseLock.lock();
	     try {
	       isPaused = true;
	     } finally {
	       pauseLock.unlock();
	     }
	   }
	   
	   /**
	    * Call this to resume the paused thread executor
	    * @see pause() for pause a thread executor
	    */
	   public void resume() {
	     pauseLock.lock();
	     try {
	       isPaused = false;
	       unpaused.signalAll();
	     } finally {
	       pauseLock.unlock();
	     }
	   }
	   
	   /**
	    * Check if executor is paused or not
	    * @return true for paused
	    */
	   public boolean isPaused()
	   {
		   return this.isPaused;
	   }
}
