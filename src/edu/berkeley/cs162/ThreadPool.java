/**
 * A simple thread pool implementation
 *
 * @author Mosharaf Chowdhury (http://www.mosharaf.com)
 * @author Prashanth Mohan (http://www.cs.berkeley.edu/~prmohan)
 *
 * Copyright (c) 2012, University of California at Berkeley
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of University of California, Berkeley nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.cs162;

import java.util.LinkedList;

public class ThreadPool {
    /**
     * Set of threads in the threadpool
     */
    protected Thread threads[] = null;
    LinkedList<Runnable> jobs;

    /**
     * Initialize the number of threads required in the threadpool.
     *
     * @param size  How many threads in the thread pool.
     */
    public ThreadPool(int size)
    {
        // Added 4 Dec 2014
    	
    	threads = new Thread[size];
    	jobs = new LinkedList<Runnable>();
    	
    	for (int i = 0; i < size; i++)
    	{
    		threads[i] = new WorkerThread(this);
    		threads[i].start();
    	}
    	
    	//--
    }

    /**
     * Add a job to the queue of tasks that has to be executed. As soon as a thread is available,
     * it will retrieve tasks from this queue and start processing.
     * @param r job that has to be executed asynchronously
     * @throws InterruptedException
     */
    public void addToQueue(Runnable r) throws InterruptedException
    {
        // Added 4 Dec 2014
    	
    	jobs.push(r);
    	notify();
    	
    	//--
    }
    
    /**
     * Block until a job is available in the queue and retrieve the job
     * @return A runnable task that has to be executed
     * @throws InterruptedException
     */
    public synchronized Runnable getJob() throws InterruptedException 
    {
        // Added 4 Dec 2014
    	
    	if (jobs.size() == 0) 
    	{
    		this.wait();
    	}
    		
    	return jobs.pop();
    	
    	//--
    }
}

/**
 * The worker threads that make up the thread pool.
 */
class WorkerThread extends Thread 
{
	ThreadPool owner;
	
    /**
     * The constructor.
     *
     * @param o the thread pool
     */
    WorkerThread(ThreadPool o)
    {
        // Added 4 Dec 2014
    	
    	owner = o;
    	
    	//
    }

    /**
     * Scan for and execute tasks.
     */
    public void run()
    {
    	while(true)
    	{
    		try
    		{
    			owner.getJob().run();
    		}
    		catch(InterruptedException e)
    		{
    			return;
    		}
    	}
    }
}
