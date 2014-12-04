/**
 * Implementation of a set-associative cache.
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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;


/**
 * A set-associate cache which has a fixed maximum number of sets (numSets).
 * Each set has a maximum number of elements (MAX_ELEMS_PER_SET).
 * If a set is full and another entry is added, an entry is dropped based on the eviction policy.
 */
public class KVCache implements KeyValueInterface {
    private int numSets = 100;
    private int maxElemsPerSet = 10;
    

    ArrayList<LinkedList<Entry>> cache;
    WriteLock[] locks;
    
    // Added 4 Dec 2014
    
    private class Entry
    {
    	public String key;
    	public String value;
    	public boolean isReferenced;
    	public boolean isValid;
    	
    	public Entry(String key, String value)
    	{
	    	this.key = key;
	    	this.value = value;
	    	this.isValid = true;
	    	this.isReferenced = true;
	    }

    	public String toString()
    	{
    		return "(" + key + ", " + value + ")";
    	}
    }
    
    //--

    /**
     * Creates a new LRU cache.
     * @param cacheSize    the maximum number of entries that will be kept in this cache.
     */
    public KVCache(int numSets, int maxElemsPerSet) {
        this.numSets = numSets;
        this.maxElemsPerSet = maxElemsPerSet;
        
        // Added 4 Dec 2014
        
        cache = new ArrayList<LinkedList<Entry>>(numSets);
        locks = new WriteLock[numSets];
        
        for (int i = 0; i < numSets; i++)
        {
        	ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        	locks[i] = lock.writeLock();
        	cache.add(new LinkedList<Entry>());
        }
        
        //--
    }

    /**
     * Retrieves an entry from the cache.
     * Assumes the corresponding set has already been locked for writing.
     * @param key the key whose associated value is to be returned.
     * @return the value associated to this key, or null if no value with this key exists in the cache.
     */
    public String get(String key) {
        // Must be called before anything else
        AutoGrader.agCacheGetStarted(key);
        AutoGrader.agCacheGetDelay();

        // Added 4 Dec 2014
        
        int set = getSetId(key);
        LinkedList<Entry> tempCache = cache.get(set);
        Entry result = null;
        
        for (Entry entry : tempCache)
        {
        	if (entry.key.equals(key))
        	{
        		result = entry;
        		break;
        	}
        }
        
        if (result != null)
        {
        	result.isReferenced = true;
        }
        //--

        // Must be called before returning
        AutoGrader.agCacheGetFinished(key);
        return null;
    }

    /**
     * Adds an entry to this cache.
     * If an entry with the specified key already exists in the cache, it is replaced by the new entry.
     * If the cache is full, an entry is removed from the cache based on the eviction policy
     * Assumes the corresponding set has already been locked for writing.
     * @param key    the key with which the specified value is to be associated.
     * @param value    a value to be associated with the specified key.
     * @return true is something has been overwritten
     */
    public void put(String key, String value) {
        // Must be called before anything else
        AutoGrader.agCachePutStarted(key, value);
        AutoGrader.agCachePutDelay();

        // Added 4 Dec 2014
        int set = getSetId(key);
        LinkedList<Entry> tempCache = cache.get(set);
        Entry result = null;
        
        for (Entry entry : tempCache)
        {
	        if (entry.key.equals(key))
	        {
		        result = entry;
		        break;
	        }
        }
        
        if (result != null)
        {
	        result.value = value;
	        result.isReferenced = true;
        }
        else
        {
        	Entry newCacheEntry = new Entry(key, value);
	        if (tempCache.size()==this.maxElemsPerSet)
	        {
		        while(true) 
		        {
			        Entry evict = tempCache.pop();
			        if (evict.isReferenced)
			        {
				        evict.isReferenced = false;
				        tempCache.addLast(evict);
			        }
			        else break;
		        }
	        }
	        tempCache.addLast(newCacheEntry);
        }
        //--

        // Must be called before returning
        AutoGrader.agCachePutFinished(key, value);
    }

    /**
     * Removes an entry from this cache.
     * Assumes the corresponding set has already been locked for writing.
     * @param key    the key with which the specified value is to be associated.
     */
    public void del (String key) {
        // Must be called before anything else
        AutoGrader.agCacheDelStarted(key);
        AutoGrader.agCacheDelDelay();

        // Added 4 Dec 2014
        
        LinkedList<Entry> tempCache = cache.get(getSetId(key));
        for (Entry entry : tempCache) 
        {
        	if (entry.key.equals(key))
        	{
        		tempCache.remove(entry);
        		break;
        	}
        }
        
        //--

        // Must be called before returning
        AutoGrader.agCacheDelFinished(key);
    }

    /**
     * @param key
     * @return    the write lock of the set that contains key.
     */
    public WriteLock getWriteLock(String key) 
    {
        // Added 4 Dec 2014
    	return locks[getSetId(key)];
    }

    /**
     *
     * @param key
     * @return    set of the key
     */
    private int getSetId(String key) {
        return Math.abs(key.hashCode()) % numSets;
    }

    public String toXML() throws KVException
    {
        // Added 4 Dec 2014
    	
    	DocumentBuilder xmlBuilder;
    	try 
    	{
    		xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    	} 
    	catch (Exception e) 
    	{
    		throw new KVException (new KVMessage("Error: DocumentBuilder error"));
    	}
    	
    	Document doc = xmlBuilder.newDocument();
    	doc.setXmlStandalone(true);
    	Element root = doc.createElement("KVCache");
    	doc.appendChild(root);
    	
    	for (int i = 0; i < numSets; i++) 
    	{
	    	Element setElement = doc.createElement("Set");
	    	setElement.setAttribute("Id", Integer.toString(this.getSetId(Integer.toString(i))));
	    	root.appendChild(setElement);
	    	LinkedList<Entry> entries = cache.get(i);
	    	
	    	for (Iterator<Entry> iterator = entries.iterator(); iterator.hasNext();) 
	    	{
		    	Entry entry = iterator.next();
		    	Element cacheEntry = doc.createElement("CacheEntry");
		    	cacheEntry.setAttribute("isReferenced", Boolean.toString(entry.isReferenced));
		    	cacheEntry.setAttribute("isValid", Boolean.toString(entry.isValid));
		    	setElement.appendChild(cacheEntry);
		    	
		    	Element key = doc.createElement("Key");
		    	cacheEntry.appendChild(key);
		    	
		    	Text keyText = doc.createTextNode(entry.key);
		    	key.appendChild(keyText);
		    	
		    	Element value = doc.createElement("Value");
		    	cacheEntry.appendChild(value);
		    	
		    	Text valueText = doc.createTextNode(entry.value);
		    	value.appendChild(valueText);
	    	}
	    	
	    	int sizeOfEntries = entries.size();
	    	
	    	if (sizeOfEntries < maxElemsPerSet) 
	    	{
		    	for (int j = sizeOfEntries; j < maxElemsPerSet; j++) 
		    	{
			    	Element cacheEntry = doc.createElement("CacheEntry");
			    	cacheEntry.setAttribute("isReferenced", Boolean.toString(false));
			    	cacheEntry.setAttribute("isValid", Boolean.toString(false));
			    	setElement.appendChild(cacheEntry);
			    	
			    	Element key = doc.createElement("Key");
			    	cacheEntry.appendChild(key);
			    	
			    	Text keyText = doc.createTextNode("empty or garbage");
			    	key.appendChild(keyText);
			    	
			    	Element valueElement = doc.createElement("Value");
			    	cacheEntry.appendChild(valueElement);
			    	
			    	Text valueText = doc.createTextNode("empty or garbage");
			    	valueElement.appendChild(valueText);
		    	}
	    	}
    	} 
    	try 
    	{
	    	Transformer tf = TransformerFactory.newInstance().newTransformer();
	    	StreamResult result = new StreamResult(new StringWriter());
	    	DOMSource source = new DOMSource(doc);
	    	
	    	tf.transform(source, result);
	    	
	    	return result.getWriter().toString();
	    } 
    	catch (Exception e) 
    	{
	    	throw new KVException(new KVMessage("Error: TransformerFactory error"));
    	}
    	
    	//--
    }
}
