/**
 * Slave Server component of a KeyValue store
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

import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * This class defines the slave key value servers. Each individual KVServer
 * would be a fully functioning Key-Value server. For Project 3, you would
 * implement this class. For Project 4, you will have a Master Key-Value server
 * and multiple of these slave Key-Value servers, each of them catering to a
 * different part of the key namespace.
 *
 */
public class KVServer implements KeyValueInterface {
    private KVStore dataStore = null;
    private KVCache dataCache = null;

    private static final int MAX_KEY_SIZE = 256;
    private static final int MAX_VAL_SIZE = 256 * 1024;

    /**
     * @param numSets number of sets in the data Cache.
     */
    public KVServer(int numSets, int maxElemsPerSet) {
        dataStore = new KVStore();
        dataCache = new KVCache(numSets, maxElemsPerSet);

        AutoGrader.registerKVServer(dataStore, dataCache);
    }

    public void put(String key, String value) throws KVException 
    {
        // Must be called before anything else
        AutoGrader.agKVServerPutStarted(key, value);

        // Added 4 Dec 2014
        
        WriteLock cacheWL = dataCache.getWriteLock(key);
        
        try 
	    {
	        cacheWL.lock();
	        synchronized(dataStore) 
	        {
		        try 
		        {
			        dataStore.put(key, value);
			        dataCache.put(key, value);
		        } 
		        catch (KVException e) 
		        {
		        	throw new KVException(new KVMessage("resp", "IO Error"));
		        }
	        }
        } 
        finally 
        {
	        AutoGrader.agKVServerPutFinished(key, value);
	        cacheWL.unlock();
        }
        
        //--

        // Must be called before return or abnormal exit
        AutoGrader.agKVServerPutFinished(key, value);
    }

    public String get (String key) throws KVException 
    {
        // Must be called before anything else
        AutoGrader.agKVServerGetStarted(key);

        // Added 4 Dec 2014
        
        WriteLock cacheWL = dataCache.getWriteLock(key);
        String result = null;
        
        try 
        {
        	cacheWL.lock();
	        result = dataCache.get(key);
	        if (result == null) 
	        {
		        synchronized (dataStore) 
		        {
		        	try
		        	{
		        		result = dataStore.get(key);
		        	}
		        	catch(Exception e)
		        	{
		        		throw new KVException(new KVMessage("resp", "Error: Entry not found!"));
		        	}
			        dataCache.put(key, result);
		        }
	        }
        } 
        finally 
        {
        	// Must be called before returning
        	AutoGrader.agKVServerGetFinished(key);
        	cacheWL.unlock();
        }
        return result;
        
        // --
    }

    public void del (String key) throws KVException 
    {
        // Must be called before anything else
        AutoGrader.agKVServerDelStarted(key);

        // Added 4 Dec 2014
        
        WriteLock cacheWL = dataCache.getWriteLock(key);
        try 
        {
	        cacheWL.lock();
	        synchronized (dataStore) 
	        {
	        	try
	        	{
	        		dataStore.get(key);
	        	}
	        	catch(Exception e)
	        	{
	        		throw new KVException(new KVMessage("resp", "Error: Entry not found!"));
	        	}
	        	
		        try 
		        {
			        dataStore.del(key);
			        dataCache.del(key);
		        } 
		        catch (Exception e) 
		        {
		        	throw new KVException(new KVMessage("resp", "Error: IO Error"));
		        }
	        }
        } 
        finally 
        {
        	AutoGrader.agKVServerDelFinished(key);
        	cacheWL.unlock();
        }
        
        //--

        // Must be called before return or abnormal exit
        AutoGrader.agKVServerDelFinished(key);
    }
}
