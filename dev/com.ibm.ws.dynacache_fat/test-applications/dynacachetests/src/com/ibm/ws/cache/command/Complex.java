// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.command;



public class Complex implements java.io.Serializable{
	
	public String ticker = null;
	
    public void setTicker(String ticker){
    	this.ticker = ticker;	
    }
    public String getTicker(){
    	return ticker;	
    }
    
    public Complex getComplex(){
    	return this;
    }
}
