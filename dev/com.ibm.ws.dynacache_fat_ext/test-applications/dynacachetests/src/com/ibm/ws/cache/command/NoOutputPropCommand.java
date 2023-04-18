// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.command;

import java.util.Random;

import com.ibm.websphere.command.CacheableCommandImpl;
import com.ibm.websphere.servlet.cache.DynamicCacheAccessor;


public class NoOutputPropCommand extends CacheableCommandImpl implements Quote
{
    static public final long serialVersionUID = -5573075254048153742L;

    static public Random rand = new Random();

    static {
       try {
          DynamicCacheAccessor.getCache().invalidateByTemplate(NoOutputPropCommand.class.getName(),true);
       } catch (Exception ex) {
       }
    }

    private String articleKey = null;
    private String ticker = null;
    private double price = 0.0;

    public boolean isReadyToCallExecute()
    {
        if (ticker == null) {
            return false;
        }
        return true;
    }

    public void performExecute()
         throws Exception
    {
        //System.out.println("executing lookup for ticker :"+ticker);
        /*
        try {
           // simulate business logic with a combination of cpu utilization
           // and wait time
           for(int i=0;i<5000;i++)
              rand.nextInt(1000);
           Thread.sleep(25);
        } catch (Exception ex) {
        }
        */
        randomPrice();
    }

    // method used to generate random prices
    public void randomPrice() {
        price = Math.abs(rand.nextInt()%15000)/100.0;
    }

    //input
    public void setTicker(String ticker)
    {
        this.ticker = ticker;
    }

    public String getTicker() {
       return ticker;
    }


    //output
    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public String toString() {
       return super.toString()+" ticker="+ticker+" price="+price;
    }

}
