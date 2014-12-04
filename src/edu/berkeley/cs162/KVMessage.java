/**
 * XML Parsing library for the key-value store
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

import java.io.FilterInputStream;
import java.io.InputStream;
import java.net.Socket;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import java.io.IOException;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import java.io.StringWriter;
import java.io.Writer;
import java.io.OutputStreamWriter;

/**
 * This is the object that is used to generate messages the XML based messages
 * for communication between clients and servers.
 */
public class KVMessage {
    private String msgType = null;
    private String key = null;
    private String value = null;
    private String message = null;

    public final String getKey() {
        return key;
    }

    public final void setKey(String key) {
        this.key = key;
    }

    public final String getValue() {
        return value;
    }

    public final void setValue(String value) {
        this.value = value;
    }

    public final String getMessage() {
        return message;
    }

    public final void setMessage(String message) {
        this.message = message;
    }

    public String getMsgType() {
        return msgType;
    }

    /* Solution from http://weblogs.java.net/blog/kohsuke/archive/2005/07/socket_xml_pitf.html */
    private class NoCloseInputStream extends FilterInputStream {
        public NoCloseInputStream(InputStream in) {
            super(in);
        }

        public void close() {} // ignore close
    }

    /***
     *
     * @param msgType
     * @throws KVException of type "resp" with message "Message format incorrect" if msgType is unknown
     */
    public KVMessage(String msgType) throws KVException 
    {
        // Added 4 Dec 2014
    	
    	if (msgType.equals("getreq") || msgType.equals("putreq") || msgType.equals("delreq") || msgType.equals("resp")) 
    	{
    		this.msgType = msgType;
    	} 
    	else 
    	{
    		throw new KVException(new KVMessage("resp", "Message format incorrect"));
    		
    	}
    	//--
    }

    public KVMessage(String msgType, String message) throws KVException 
    {
        // Added 4 Dec 2014
    	
    	this(msgType);
    	this.message = message;
    	
    	//--
    }

     /***
     * Parse KVMessage from socket's input stream
     * @param sock Socket to receive from
     * @throws KVException if there is an error in parsing the message. The exception should be of type "resp and message should be :
     * a. "XML Error: Received unparseable message" - if the received message is not valid XML.
     * b. "Network Error: Could not receive data" - if there is a network error causing an incomplete parsing of the message.
     * c. "Message format incorrect" - if there message does not conform to the required specifications. Examples include incorrect message type.
     */
    public KVMessage(InputStream input) throws KVException 
    {
        // Added 4 Dec 2014
    	
    	DocumentBuilder builder;
    	Document doc;
    	
    	try
    	{
    		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    	}
    	catch(Exception e)
    	{
    		throw new KVException (new KVMessage("resp", "Error: DocumentBuilder error"));
    	}
    	
    	try 
    	{
    		doc = builder.parse(new NoCloseInputStream(input));
    		doc.setXmlStandalone(true);
    	}
    	catch (IOException e) 
    	{
    		throw new KVException(new KVMessage("resp", "Could not receive data"));
    	} 
    	catch (SAXException e) 
    	{
    		throw new KVException(new KVMessage("resp", "Received unparseable message"));
    	}
    	
    	try
    	{
    		Element root = doc.getDocumentElement();
    		String msgType = root.getAttribute("type");
    		
    		if(msgType.equals("putreq"))
    		{
    			Node key = root.getFirstChild();
    			Node value = root.getLastChild();
    			
    			if(key == null || key.getTextContent() == null)
    			{
    				throw new KVException (new KVMessage("resp", "Message format incorrect"));
    			}
    			if(value == null || value.getTextContent()==null)
    			{
    				throw new KVException (new KVMessage("resp", "Message format incorrect"));
    			}
    			
    			this.msgType = "putreq";
    			this.key = key.getTextContent();
    			this.value = value.getTextContent();
    		}
    		else if(msgType.equals("getreq"))
    		{
    			Node key = root.getFirstChild();
    			
    			 if(key == null || key.getTextContent()==null)
    			 {
    				 throw new KVException (new KVMessage("resp", "Message format incorrect"));
    			 }
    			 
    			 this.msgType = "getreq";
    			 this.key = key.getTextContent();
    		}
    		else if(msgType.equals("delreq"))
    		{
    			Node key = root.getFirstChild();
    			
    			if(key == null || key.getTextContent()==null)
    			{
    				throw new KVException (new KVMessage("resp", "Message format incorrect"));
    			}
    			
    			this.msgType = "delreq";
    			this.key = key.getTextContent();
    		}
    		else if(msgType.equals("resp"))
    		{
    			if(((Element)root.getFirstChild()).getTagName().equals("Key"))
    			{
    				Node key = root.getFirstChild();
    				Node value = root.getLastChild();
    				
    				if(key.getTextContent() == null)
    				{
    					throw new KVException (new KVMessage("resp", "Message format incorrect"));
    				}
    				if(value.getTextContent() == null)
    				{
    					throw new KVException (new KVMessage("resp", "Message format incorrect"));
    				}
    				
    				this.msgType = "resp";
    				this.key = key.getTextContent();
    				this.value = value.getTextContent();
    			}
    			else
    			{
    				Node respmsg = root.getFirstChild();
    				this.msgType = "resp";
    				this.message = respmsg.getTextContent();
    			}
    		}
    		else
    		{
    			throw new KVException (new KVMessage("resp", "Message format incorrect"));
    		}
    	}
    	catch (Exception e) 
    	{
    		throw new KVException (new KVMessage("resp", "Message format incorrect"));
    	}
    	
    	//--
    }

    /**
     * Generate the XML representation for this message.
     * @return the XML String
     * @throws KVException if not enough data is available to generate a valid KV XML message
     */
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
    	
    	Element root = doc.createElement("KVMessage");
    	root.setAttribute("type", this.msgType);
    	doc.appendChild(root);
    	
    	if (this.msgType == "putreq") 
    	{
    		if(this.key==null||this.value==null)
    		{
    			throw new KVException (new KVMessage("resp", "Message format incorrect"));
    		}
	    	Element key = doc.createElement("Key");
	    	Element value = doc.createElement("Value");
	    	
	    	key.setTextContent(this.key);
	    	value.setTextContent(this.value);
	    	
	    	root.appendChild(key);
	    	root.appendChild(value);
    	} 
    	else if (this.msgType.equals("getreq") || this.msgType.equals("delreq")) 
    	{
	    	if(this.key==null)
	    	{
	    		throw new KVException (new KVMessage("resp", "Message format incorrect"));
	    	}
	    	
	    	Element key = doc.createElement("Key");
	    	key.setTextContent(this.key);
	    	root.appendChild(key);
    	} 
    	else if (this.msgType.equals("resp")) 
    	{
	    	if(this.key == null && this.value == null) 
	    	{
		    	Element message = doc.createElement("Message");
		    	message.setTextContent(this.message);
		    	root.appendChild(message);
	    	} 
	    	else if (this.key != null && this.value != null) 
	    	{
		    	Element key = doc.createElement("Key");
		    	Element value = doc.createElement("Value");
		    	key.setTextContent(this.key);
		    	value.setTextContent(this.value);
		    	root.appendChild(key);
		    	root.appendChild(value);
	    	} 
	    	else 
	    	{
	    		throw new KVException (new KVMessage("resp", "Message format incorrect"));
	    	}
    	} 
    	else 
    	{
    		throw new KVException (new KVMessage("resp", "Message format incorrect"));
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
    		throw new KVException (new KVMessage("resp", "Error: Transformer error"));
    	}
    	
    	//--
    }

    public void sendMessage(Socket sock) throws KVException 
    {
        // Added 4 Dec 2014
    	
    	String xml = this.toXML();
    	Writer out = null;
    	
    	try 
    	{
    		out = new OutputStreamWriter(sock.getOutputStream());
    		out.write(xml);
    		out.flush();
    	} 
    	catch (Exception e) 
    	{
    		throw new KVException(new KVMessage("resp", "Error: Could not write to socket"));
    	} 
    	finally 
    	{
    		try 
    		{
    			sock.shutdownOutput();
    		} 
    		catch (IOException e) 
    		{
    			throw new KVException(new KVMessage("resp", "Error: Could Not Close socket Output"));
    		}
    	}
    	}
    	
    	//--
    }
