// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.command;

import java.io.Serializable;
import java.util.Random;

import com.ibm.websphere.command.CacheableCommandImpl;
import com.ibm.websphere.command.TargetableCommand;
import com.ibm.websphere.servlet.cache.DynamicCacheAccessor;

public class WatchListCommand extends CacheableCommandImpl implements Serializable
{
    static final long serialVersionUID = -5573075254048153742L;

    // random number generator used for randomly
    // generated watch lists
    static transient protected Random rand = new Random();
    static final protected int MIN_TICKERS = 5;
    static final protected int MAX_TICKERS = 15;

    static {
        try {
            DynamicCacheAccessor.getCache().invalidateByTemplate(WatchListCommand.class.getName(), true);
        } catch (Exception ex) {
        }
    }

    public String userGroup = null;
    public int userNumber = 0;

    String watchList[] = null;

    @Override
    public boolean isReadyToCallExecute()
    {
        if (userGroup == null) {
            return false;
        }
        return true;
    }

    @Override
    public void performExecute()
                    throws Exception
    {
        //System.out.println("WatchListCommand: executing lookup for userid :"+userid);
        /*
         * try {
         * // simulate business logic with a combination of cpu utilization
         * // and wait time
         * for(int i=0;i<5000;i++)
         * rand.nextInt(1000);
         * Thread.sleep(25);
         * } catch (Exception ex) {
         * }
         */
        watchList = randomWatchList();
    }

    // methods used to generate random prices
    protected String[] randomWatchList() {
        int numTickers = MIN_TICKERS + rand.nextInt(MAX_TICKERS - MIN_TICKERS);
        String tickers[] = new String[numTickers];
        for (int i = 0; i < numTickers; i++)
            tickers[i] = randomTicker();
        return tickers;
    }

    static public String randomTicker() {
        // generates 26*26*26 (17576) random tickers
        char ticker[] = new char[3];
        ticker[0] = (char) ('A' + rand.nextInt(26));
        ticker[1] = (char) ('A' + rand.nextInt(26));
        ticker[2] = (char) ('A' + rand.nextInt(26));
        return new String(ticker);
    }

    @Override
    public void setOutputProperties(TargetableCommand fromCommand)
    {
        if (this == fromCommand) {
            return;
        }
        if (!(fromCommand instanceof WatchListCommand)) {
            throw new IllegalStateException("TargetableCommand must be an instance of WatchListCommandCommand");
        }
        WatchListCommand from = (WatchListCommand) fromCommand;
        this.watchList = from.watchList;
    }

    //input
    public int getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(int number) {
        userNumber = number;
    }

    public String getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }

    //output
    public String[] getWatchList()
    {
        return watchList;
    }

    @Override
    public String toString() {
        return super.toString() + " userid=" + userGroup + ":" + userNumber + " watchList=" + watchList;
    }

}
