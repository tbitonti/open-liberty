// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.command;

import java.util.Random;

import com.ibm.websphere.command.CacheableCommandImpl;
import com.ibm.websphere.command.TargetableCommand;
import com.ibm.websphere.servlet.cache.DynamicCacheAccessor;


public class QuoteCommandComplex extends CacheableCommandImpl implements Quote
{
	static final long serialVersionUID = -5573075254048153742L;

	static protected Random rand = new Random();

	static {
	   try {
		  DynamicCacheAccessor.getCache().invalidateByTemplate(QuoteCommandComplex.class.getName(),true);
	   } catch (Exception ex) {
	   }
	}

	protected String articleKey = null;
	public Complex complex = new Complex();
	//protected String ticker = null;
	protected double price = 0.0;

	public boolean isReadyToCallExecute()
	{
		if (complex.getTicker() == null) {
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


	public void setOutputProperties(TargetableCommand fromCommand)
	{
		if (this == fromCommand) {
			return;
		}
		if (!(fromCommand instanceof QuoteCommandComplex)) {
			throw new IllegalStateException
				("TargetableCommand must be an instance of QuoteCommandComplex");
		}
		QuoteCommandComplex from = (QuoteCommandComplex) fromCommand;
		complex.setTicker(from.getComplex().getTicker());
		this.price = from.price;
	}

	// method used to generate random prices
	public void randomPrice() {
		price = Math.abs(rand.nextInt()%15000)/100.0;
	}


	//input
	public void setTicker(String ticker)
	{
		complex.setTicker(ticker);
	}

	public String getTicker() {
	   return complex.getTicker();
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
	   return super.toString()+" ticker="+complex.getTicker()+" price="+price;
	}
	
	public Complex getComplex(){
		return complex;
	}
}
