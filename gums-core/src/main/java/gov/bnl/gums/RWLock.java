package gov.bnl.gums;

import org.apache.log4j.Logger;

public class RWLock
{
	private int givenLocks;
	private int waitingWriters;
	private Logger log = Logger.getLogger(RWLock.class);
		
	private Object mutex;
	
	public RWLock()
	{
		mutex = new Object();
		givenLocks = 0;
		waitingWriters = 0;
	}
	
	public void getReadLock()
	{
		synchronized(mutex)
		{
			while((givenLocks == -1) || (waitingWriters != 0))
				try {
					mutex.wait();
				} catch (InterruptedException e) {
					log.error(e);
				}
			
			givenLocks++;
		}
	}
	
	public void getWriteLock()
	{
		synchronized(mutex)
		{
			waitingWriters++;
			while(givenLocks != 0) {
				try {
					mutex.wait();
				} catch (InterruptedException e) {
					log.error(e);
				}
			}
			
			waitingWriters--;
			givenLocks = -1;
		}
	}
	
	
	public void releaseLock()
	{	
		synchronized(mutex)
		{
			if(givenLocks == 0)
				return;
				
			if(givenLocks == -1)
				givenLocks = 0;
			else
				givenLocks--;
			
			mutex.notifyAll();
		}
	}


}



