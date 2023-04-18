// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.command;


public interface Quote {
    public void setTicker(String ticker);
    public String getTicker();
    public double getPrice();
    public void setPrice(double price);
}
