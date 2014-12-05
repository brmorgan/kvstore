/**
 * Persistent Key-Value storage layer. Current implementation is transient,
 * but assume to be backed on disk when you do your project.
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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * This is a dummy KeyValue Store. Ideally this would go to disk,
 * or some other backing store. For this project, we simulate the disk like
 * system using a manual delay.
 *
 *
 *
 */
public class KVStore implements KeyValueInterface {
    private Dictionary<String, String> store     = null;

    public KVStore() {
        resetStore();
    }

    private void resetStore() {
        store = new Hashtable<String, String>();
    }

    public void put(String key, String value) throws KVException {
        AutoGrader.agStorePutStarted(key, value);

        try {
            putDelay();
            store.put(key, value);
        } finally {
            AutoGrader.agStorePutFinished(key, value);
        }
    }

    public String get(String key) throws KVException {
        AutoGrader.agStoreGetStarted(key);

        try {
            getDelay();
            String retVal = this.store.get(key);
            if (retVal == null) {
                KVMessage msg = new KVMessage("resp", "key \"" + key + "\" does not exist in store");
                throw new KVException(msg);
            }
            return retVal;
        } finally {
            AutoGrader.agStoreGetFinished(key);
        }
    }

    public void del(String key) throws KVException {
        AutoGrader.agStoreDelStarted(key);

        try {
            delDelay();
            if(key != null)
                this.store.remove(key);
        } finally {
            AutoGrader.agStoreDelFinished(key);
        }
    }

    private void getDelay() {
        AutoGrader.agStoreDelay();
    }

    private void putDelay() {
        AutoGrader.agStoreDelay();
    }

    private void delDelay() {
        AutoGrader.agStoreDelay();
    }

    public String toXML() throws KVException 
    {
    	// Added 4 Dec 2014
    	
    	DocumentBuilder builder;
    	try 
    	{
    		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    	} 
    	catch (Exception e) 
    	{
    		throw new KVException (new KVMessage("Error: DocumentBuilder error"));
    	}
    	
    	Document doc = builder.newDocument();
    	doc.setXmlStandalone(true);
    	Element root = doc.createElement("KVStore");
    	doc.appendChild(root);
    	Enumeration ePairs = store.keys();
    	
    	while(ePairs.hasMoreElements()) 
    	{
	    	String eKey = (String) ePairs.nextElement();
	    	String eValue = store.get(eKey);
	    	Element child = doc.createElement("KVPair");
	    	Element key = doc.createElement("Key");
	    	Element value = doc.createElement("Value");
	    	
	    	key.setTextContent(eKey);
	    	value.setTextContent(eValue);
	    	child.appendChild(key);
	    	child.appendChild(value);
	    	root.appendChild(child);
    	}
    	try 
    	{
	    	Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    	StreamResult result = new StreamResult(new StringWriter());
	    	DOMSource source = new DOMSource(doc);
	    	
	    	transformer.transform(source, result);
	    	return result.getWriter().toString();
    	} 
    	catch (Exception e) 
    	{
    		throw new KVException (new KVMessage("Error: Transformer error"));
    	}
    	
    	//--
    }

    public void dumpToFile(String fileName) throws KVException 
    {
        // Added 4 Dec 2014
    	
    	String dump = toXML();
    	
    	try
    	{
	    	BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
	    	out.write(dump);
	    	out.close();
    	}
    	
    	catch (Exception e) 
    	{
    		throw new KVException (new KVMessage("Error: dumpToFile error"));
    	}
    	
    	//--
    }

    /**
     * Replaces the contents of the store with the contents of a file
     * written by dumpToFile; the previous contents of the store are lost.
     * @param fileName the file to be read.
     * @throws KVException 
     */
    public void restoreFromFile(String fileName) throws KVException 
    {
        // Added 4 Dec 2014
    	
    	DocumentBuilder builder;
    	try 
    	{
    		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    	} 
    	catch (Exception e) 
    	{
    		throw new KVException (new KVMessage("Unknown Error: DocumentBuilder error"));
    	}
    	try 
    	{
	    	Document doc = builder.parse(fileName);
	    	NodeList pairs = doc.getElementsByTagName("KVPair");
	    	
	    	for(int i = 0; i<pairs.getLength(); i++) 
	    	{
	    		store.put(pairs.item(i).getFirstChild().getTextContent(), pairs.item(i).getLastChild().getTextContent());
	    	}
    	} 
    	catch (Exception e) 
    	{
    		throw new KVException (new KVMessage("Unknown Error: restoreFromFile"));
    	} 
    	//--
    }
}
