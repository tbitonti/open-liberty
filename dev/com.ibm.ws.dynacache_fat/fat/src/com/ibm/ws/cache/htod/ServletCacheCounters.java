// 1.3, 3/13/06
// IBM Confidential OCO Source Material
// 5724-I63, 5724-H88, 5655-N01, 5733-W60 (C) COPYRIGHT International Business Machines Corp. 1997, 2005
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
//
package com.ibm.ws.cache.htod;

import java.lang.reflect.Field;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.ibm.ws.cache.TestConfig;
import com.ibm.ws.cache.servlet.ServletTestCase;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

/**
 * This helper class is a simple collection of numbers.
 * The numbers correspond to those numbers received as responses from
 * the MBeanCounterTestServlet running with Dynacache in an AppServer.
 * Utility methods are provided hererin to transfer or evaluate values.
 * 
 * Credits: These counters and methods originally came from MBeanCounterTest.
 * They were extracted to a separate class to make them usable by other classes.
 */
public class ServletCacheCounters extends ServletTestCase {

    //-------------------------------------------------------------------------
    // Member variables.
    //-------------------------------------------------------------------------

    private long sMemEntriesSize = 0;
    private long sDiskEntriesSize = 0;
    private long sActualDiskEntriesSize = 0;
    private long sDiskDepIdSize = 0;
    private long sDiskTemplateSize = 0;
    private long sTotalCacheDataDiskSize = 0;
    private long sCacheHits = 0;
    private long sCacheMisses = 0;
    private long sRemoves = 0;
    private long sLruRemoves = 0;
    private long sExplicitInvalidationsFromMemory = 0;
    private long sExplicitInvalidationsFromDisk = 0;
    private long sExplicitInvalidationsLocal = 0;
    private long sExplicitInvalidationsRemote = 0;
    private long sTimeoutInvalidationsFromMemory = 0;
    private long sTimeoutInvalidationsFromDisk = 0;
    private long sPendingRemovalSizeDisk = 0;
    private long sDepIdsBufferedSizeDisk = 0;
    private long sDepIdsOffloadedToDisk = 0;
    private long sDepIdBasedInvalidationsFromDisk = 0;
    private long sTemplatesBufferedSizeDisk = 0;
    private long sTemplatesOffloadedToDisk = 0;
    private long sTemplateBasedInvalidationsFromDisk = 0;
    private long sObjectsReadFromDisk = 0;
    private long sObjectsReadFromDisk4K = 0;
    private long sObjectsReadFromDisk40K = 0;
    private long sObjectsReadFromDisk400K = 0;
    private long sObjectsReadFromDisk4000K = 0;
    private long sObjectsReadFromDiskSize = 0;
    private long sObjectsWriteToDisk = 0;
    private long sObjectsWriteToDisk4K = 0;
    private long sObjectsWriteToDisk40K = 0;
    private long sObjectsWriteToDisk400K = 0;
    private long sObjectsWriteToDisk4000K = 0;
    private long sObjectsWriteToDiskSize = 0;
    private long sObjectsDeleteFromDisk = 0;
    private long sObjectsDeleteFromDisk4K = 0;
    private long sObjectsDeleteFromDisk40K = 0;
    private long sObjectsDeleteFromDisk400K = 0;
    private long sObjectsDeleteFromDisk4000K = 0;
    private long sObjectsDeleteFromDiskSize = 0;
    private long sPushPullTableSize = 0;
    private long sRemoteInvalidationNotifications = 0;
    private long sRemoteUpdateNotifications = 0;
    private long sRemoteObjectUpdates = 0;
    private long sRemoteObjectUpdateSize = 0;
    private long sRemoteObjectHits = 0;
    private long sRemoteObjectFetchSize = 0;
    private long sRemoteObjectMisses = 0;
    private long sInvalidationLocal = 0;
    private long sInvalidationLocalExplicit = 0;
    private long sInvalidationLocalLRU = 0;
    private long sInvalidationLocalTimeout = 0;
    private long sInvalidationLocalDiskTimeout = 0;
    private long sInvalidationLocalClearAll = 0;
    private long sInvalidationRemote = 0;
    private long sInvalidationRemoteExplicit = 0;
    private long sInvalidationRemoteLRU = 0;
    private long sInvalidationRemoteTimeout = 0;
    private long sInvalidationRemoteDiskTimeout = 0;
    private long sInvalidationRemoteClearAll = 0;

    // Note: Whenever you add new members here, be sure to
    // propagate them to the supporting methods below.

    //-------------------------------------------------------------------------
    // Public methods.
    //-------------------------------------------------------------------------

    /**
     * Re-initializes all counter values to zeroes.
     */
    public void zero() {
        sMemEntriesSize = 0;
        sDiskEntriesSize = 0;
        sActualDiskEntriesSize = 0;
        sDiskDepIdSize = 0;
        sDiskTemplateSize = 0;
        sTotalCacheDataDiskSize = 0;
        sCacheHits = 0;
        sCacheMisses = 0;
        sRemoves = 0;
        sLruRemoves = 0;
        sExplicitInvalidationsFromMemory = 0;
        sExplicitInvalidationsFromDisk = 0;
        sExplicitInvalidationsLocal = 0;
        sExplicitInvalidationsRemote = 0;
        sTimeoutInvalidationsFromMemory = 0;
        sTimeoutInvalidationsFromDisk = 0;
        sPendingRemovalSizeDisk = 0;
        sDepIdsBufferedSizeDisk = 0;
        sDepIdsOffloadedToDisk = 0;
        sDepIdBasedInvalidationsFromDisk = 0;
        sTemplatesBufferedSizeDisk = 0;
        sTemplatesOffloadedToDisk = 0;
        sTemplateBasedInvalidationsFromDisk = 0;
        sObjectsReadFromDisk = 0;
        sObjectsReadFromDisk4K = 0;
        sObjectsReadFromDisk40K = 0;
        sObjectsReadFromDisk400K = 0;
        sObjectsReadFromDisk4000K = 0;
        sObjectsReadFromDiskSize = 0;
        sObjectsWriteToDisk = 0;
        sObjectsWriteToDisk4K = 0;
        sObjectsWriteToDisk40K = 0;
        sObjectsWriteToDisk400K = 0;
        sObjectsWriteToDisk4000K = 0;
        sObjectsWriteToDiskSize = 0;
        sObjectsDeleteFromDisk = 0;
        sObjectsDeleteFromDisk4K = 0;
        sObjectsDeleteFromDisk40K = 0;
        sObjectsDeleteFromDisk400K = 0;
        sObjectsDeleteFromDisk4000K = 0;
        sObjectsDeleteFromDiskSize = 0;
        sPushPullTableSize = 0;
        sRemoteInvalidationNotifications = 0;
        sRemoteUpdateNotifications = 0;
        sRemoteObjectUpdates = 0;
        sRemoteObjectUpdateSize = 0;
        sRemoteObjectHits = 0;
        sRemoteObjectFetchSize = 0;
        sRemoteObjectMisses = 0;
        sInvalidationLocal = 0;
        sInvalidationLocalExplicit = 0;
        sInvalidationLocalLRU = 0;
        sInvalidationLocalTimeout = 0;
        sInvalidationLocalDiskTimeout = 0;
        sInvalidationLocalClearAll = 0;
        sInvalidationRemote = 0;
        sInvalidationRemoteExplicit = 0;
        sInvalidationRemoteLRU = 0;
        sInvalidationRemoteTimeout = 0;
        sInvalidationRemoteDiskTimeout = 0;
        sInvalidationRemoteClearAll = 0;
    }

    /**
     * Assimilates values from a result string into this object.
     * The result string must be in the format of a response from
     * the Dynacache MBeanCounterTestServlet.
     */
    public void parse(String result) {

        System.out.println("***************************************************");

        // Warning: wide lines...
        sMemEntriesSize = parseLongValue(result, "sMemEntriesSize", sMemEntriesSize);
        sDiskEntriesSize = parseLongValue(result, "sDiskEntriesSize", sDiskEntriesSize);
        sActualDiskEntriesSize = parseLongValue(result, "sActualDiskEntriesSize", sActualDiskEntriesSize);
        sDiskDepIdSize = parseLongValue(result, "sDiskDepIdSize", sDiskDepIdSize);
        sDiskTemplateSize = parseLongValue(result, "sDiskTemplateSize", sDiskTemplateSize);
        sTotalCacheDataDiskSize = parseLongValue(result, "sTotalCacheDataDiskSize", sTotalCacheDataDiskSize);
        sCacheHits = parseLongValue(result, "sCacheHits", sCacheHits);
        sCacheMisses = parseLongValue(result, "sCacheMisses", sCacheMisses);
        sRemoves = parseLongValue(result, "sRemoves", sRemoves);
        sLruRemoves = parseLongValue(result, "sLruRemoves", sLruRemoves);
        sExplicitInvalidationsFromMemory = parseLongValue(result, "sExplicitInvalidationsFromMemory", sExplicitInvalidationsFromMemory);
        sExplicitInvalidationsFromDisk = parseLongValue(result, "sExplicitInvalidationsFromDisk", sExplicitInvalidationsFromDisk);
        sExplicitInvalidationsLocal = parseLongValue(result, "sExplicitInvalidationsLocal", sExplicitInvalidationsLocal);
        sExplicitInvalidationsRemote = parseLongValue(result, "sExplicitInvalidationsRemote", sExplicitInvalidationsRemote);
        sTimeoutInvalidationsFromMemory = parseLongValue(result, "sTimeoutInvalidationsFromMemory", sTimeoutInvalidationsFromMemory);
        sTimeoutInvalidationsFromDisk = parseLongValue(result, "sTimeoutInvalidationsFromDisk", sTimeoutInvalidationsFromDisk);
        sPendingRemovalSizeDisk = parseLongValue(result, "sPendingRemovalSizeDisk", sPendingRemovalSizeDisk);
        sDepIdsBufferedSizeDisk = parseLongValue(result, "sDepIdsBufferedSizeDisk", sDepIdsBufferedSizeDisk);
        sDepIdsOffloadedToDisk = parseLongValue(result, "sDepIdsOffloadedToDisk", sDepIdsOffloadedToDisk);
        sDepIdBasedInvalidationsFromDisk = parseLongValue(result, "sDepIdBasedInvalidationsFromDisk", sDepIdBasedInvalidationsFromDisk);
        sTemplatesBufferedSizeDisk = parseLongValue(result, "sTemplatesBufferedSizeDisk", sTemplatesBufferedSizeDisk);
        sTemplatesOffloadedToDisk = parseLongValue(result, "sTemplatesOffloadedToDisk", sTemplatesOffloadedToDisk);
        sTemplateBasedInvalidationsFromDisk = parseLongValue(result, "sTemplateBasedInvalidationsFromDisk", sTemplateBasedInvalidationsFromDisk);
        sObjectsReadFromDisk = parseLongValue(result, "sObjectsReadFromDisk", sObjectsReadFromDisk);
        sObjectsReadFromDisk4K = parseLongValue(result, "sObjectsReadFromDisk4K", sObjectsReadFromDisk4K);
        sObjectsReadFromDisk40K = parseLongValue(result, "sObjectsReadFromDisk40K", sObjectsReadFromDisk40K);
        sObjectsReadFromDisk400K = parseLongValue(result, "sObjectsReadFromDisk400K", sObjectsReadFromDisk400K);
        sObjectsReadFromDisk4000K = parseLongValue(result, "sObjectsReadFromDisk4000K", sObjectsReadFromDisk4000K);
        sObjectsReadFromDiskSize = parseLongValue(result, "sObjectsReadFromDiskSize", sObjectsReadFromDiskSize);
        sObjectsWriteToDisk = parseLongValue(result, "sObjectsWriteToDisk", sObjectsWriteToDisk);
        sObjectsWriteToDisk4K = parseLongValue(result, "sObjectsWriteToDisk4K", sObjectsWriteToDisk4K);
        sObjectsWriteToDisk40K = parseLongValue(result, "sObjectsWriteToDisk40K", sObjectsWriteToDisk40K);
        sObjectsWriteToDisk400K = parseLongValue(result, "sObjectsWriteToDisk400K", sObjectsWriteToDisk400K);
        sObjectsWriteToDisk4000K = parseLongValue(result, "sObjectsWriteToDisk4000K", sObjectsWriteToDisk4000K);
        sObjectsWriteToDiskSize = parseLongValue(result, "sObjectsWriteToDiskSize", sObjectsWriteToDiskSize);
        sObjectsDeleteFromDisk = parseLongValue(result, "sObjectsDeleteFromDisk", sObjectsDeleteFromDisk);
        sObjectsDeleteFromDisk4K = parseLongValue(result, "sObjectsDeleteFromDisk4K", sObjectsDeleteFromDisk4K);
        sObjectsDeleteFromDisk40K = parseLongValue(result, "sObjectsDeleteFromDisk40K", sObjectsDeleteFromDisk40K);
        sObjectsDeleteFromDisk400K = parseLongValue(result, "sObjectsDeleteFromDisk400K", sObjectsDeleteFromDisk400K);
        sObjectsDeleteFromDisk4000K = parseLongValue(result, "sObjectsDeleteFromDisk4000K", sObjectsDeleteFromDisk4000K);
        sObjectsDeleteFromDiskSize = parseLongValue(result, "sObjectsDeleteFromDiskSize", sObjectsDeleteFromDiskSize);
        sPushPullTableSize = parseLongValue(result, "sPushPullTableSize", sPushPullTableSize);
        sRemoteInvalidationNotifications = parseLongValue(result, "sRemoteInvalidationNotifications", sRemoteInvalidationNotifications);
        sRemoteUpdateNotifications = parseLongValue(result, "sRemoteUpdateNotifications", sRemoteUpdateNotifications);
        sRemoteObjectUpdates = parseLongValue(result, "sRemoteObjectUpdates", sRemoteObjectUpdates);
        sRemoteObjectUpdateSize = parseLongValue(result, "sRemoteObjectUpdateSize", sRemoteObjectUpdateSize);
        sRemoteObjectHits = parseLongValue(result, "sRemoteObjectHits", sRemoteObjectHits);
        sRemoteObjectFetchSize = parseLongValue(result, "sRemoteObjectFetchSize", sRemoteObjectFetchSize);
        sRemoteObjectMisses = parseLongValue(result, "sRemoteObjectMisses", sRemoteObjectMisses);

        if (true == TestConfig.getDistributed()) {

            sInvalidationLocal = parseLongValue(result, "sInvalidationLocal", sInvalidationLocal);
            sInvalidationLocalExplicit = parseLongValue(result, "sInvalidationLocalExplicit", sInvalidationLocalExplicit);
            sInvalidationLocalLRU = parseLongValue(result, "sInvalidationLocalLRU", sInvalidationLocalLRU);
            sInvalidationLocalTimeout = parseLongValue(result, "sInvalidationLocalTimeout", sInvalidationLocalTimeout);
            sInvalidationLocalDiskTimeout = parseLongValue(result, "sInvalidationLocalDiskTimeout", sInvalidationLocalDiskTimeout);
            sInvalidationLocalClearAll = parseLongValue(result, "sInvalidationLocalClearAll", sInvalidationLocalClearAll);
            sInvalidationRemote = parseLongValue(result, "sInvalidationRemote", sInvalidationRemote);
            sInvalidationRemoteExplicit = parseLongValue(result, "sInvalidationRemoteExplicit", sInvalidationRemoteExplicit);
            sInvalidationRemoteLRU = parseLongValue(result, "sInvalidationRemoteLRU", sInvalidationRemoteLRU);
            sInvalidationRemoteTimeout = parseLongValue(result, "sInvalidationRemoteTimeout", sInvalidationRemoteTimeout);
            sInvalidationRemoteDiskTimeout = parseLongValue(result, "sInvalidationRemoteDiskTimeout", sInvalidationRemoteDiskTimeout);
            sInvalidationRemoteClearAll = parseLongValue(result, "sInvalidationRemoteClearAll", sInvalidationRemoteClearAll);

        }

    }

    /**
     * Checks individual value for an exact match.
     * Generates asserts on mismatches.
     */
    public void check(String m, String name, long expected) {
        System.out.println("ServletCacheCounters/check(exact): " + name + " expected=" + expected + " actual=" + getFieldValue(name));
        String err = m + "Error. Unexpected value. name:<" + name + ">";

        // Warning: wide lines...
        if (name.equals("sMemEntriesSize")) {
            TestCase.assertEquals(err, expected, sMemEntriesSize);
        }
        if (name.equals("sDiskEntriesSize")) {
            TestCase.assertEquals(err, expected, sDiskEntriesSize);
        }
        if (name.equals("sActualDiskEntriesSize")) {
            TestCase.assertEquals(err, expected, sActualDiskEntriesSize);
        }
        if (name.equals("sDiskDepIdSize")) {
            TestCase.assertEquals(err, expected, sDiskDepIdSize);
        }
        if (name.equals("sDiskTemplateSize")) {
            TestCase.assertEquals(err, expected, sDiskTemplateSize);
        }
        if (name.equals("sTotalCacheDataDiskSize")) {
            TestCase.assertEquals(err, expected, sTotalCacheDataDiskSize);
        }
        if (name.equals("sCacheHits")) {
            TestCase.assertEquals(err, expected, sCacheHits);
        }
        if (name.equals("sCacheMisses")) {
            TestCase.assertEquals(err, expected, sCacheMisses);
        }
        if (name.equals("sRemoves")) {
            TestCase.assertEquals(err, expected, sRemoves);
        }
        if (name.equals("sLruRemoves")) {
            TestCase.assertEquals(err, expected, sLruRemoves);
        }
        if (name.equals("sExplicitInvalidationsFromMemory")) {
            TestCase.assertEquals(err, expected, sExplicitInvalidationsFromMemory);
        }
        if (name.equals("sExplicitInvalidationsFromDisk")) {
            TestCase.assertEquals(err, expected, sExplicitInvalidationsFromDisk);
        }
        if (name.equals("sExplicitInvalidationsLocal")) {
            TestCase.assertEquals(err, expected, sExplicitInvalidationsLocal);
        }
        if (name.equals("sExplicitInvalidationsRemote")) {
            TestCase.assertEquals(err, expected, sExplicitInvalidationsRemote);
        }
        if (name.equals("sTimeoutInvalidationsFromMemory")) {
            TestCase.assertEquals(err, expected, sTimeoutInvalidationsFromMemory);
        }
        if (name.equals("sTimeoutInvalidationsFromDisk")) {
            TestCase.assertEquals(err, expected, sTimeoutInvalidationsFromDisk);
        }
        if (name.equals("sPendingRemovalSizeDisk")) {
            TestCase.assertEquals(err, expected, sPendingRemovalSizeDisk);
        }
        if (name.equals("sDepIdsBufferedSizeDisk")) {
            TestCase.assertEquals(err, expected, sDepIdsBufferedSizeDisk);
        }
        if (name.equals("sDepIdsOffloadedToDisk")) {
            TestCase.assertEquals(err, expected, sDepIdsOffloadedToDisk);
        }
        if (name.equals("sDepIdBasedInvalidationsFromDisk")) {
            TestCase.assertEquals(err, expected, sDepIdBasedInvalidationsFromDisk);
        }
        if (name.equals("sTemplatesBufferedSizeDisk")) {
            TestCase.assertEquals(err, expected, sTemplatesBufferedSizeDisk);
        }
        if (name.equals("sTemplatesOffloadedToDisk")) {
            TestCase.assertEquals(err, expected, sTemplatesOffloadedToDisk);
        }
        if (name.equals("sTemplateBasedInvalidationsFromDisk")) {
            TestCase.assertEquals(err, expected, sTemplateBasedInvalidationsFromDisk);
        }
        if (name.equals("sObjectsReadFromDisk")) {
            TestCase.assertEquals(err, expected, sObjectsReadFromDisk);
        }
        if (name.equals("sObjectsReadFromDisk4K")) {
            TestCase.assertEquals(err, expected, sObjectsReadFromDisk4K);
        }
        if (name.equals("sObjectsReadFromDisk40K")) {
            TestCase.assertEquals(err, expected, sObjectsReadFromDisk40K);
        }
        if (name.equals("sObjectsReadFromDisk400K")) {
            TestCase.assertEquals(err, expected, sObjectsReadFromDisk400K);
        }
        if (name.equals("sObjectsReadFromDisk4000K")) {
            TestCase.assertEquals(err, expected, sObjectsReadFromDisk4000K);
        }
        if (name.equals("sObjectsReadFromDiskSize")) {
            TestCase.assertEquals(err, expected, sObjectsReadFromDiskSize);
        }
        if (name.equals("sObjectsWriteToDisk")) {
            TestCase.assertEquals(err, expected, sObjectsWriteToDisk);
        }
        if (name.equals("sObjectsWriteToDisk4K")) {
            TestCase.assertEquals(err, expected, sObjectsWriteToDisk4K);
        }
        if (name.equals("sObjectsWriteToDisk40K")) {
            TestCase.assertEquals(err, expected, sObjectsWriteToDisk40K);
        }
        if (name.equals("sObjectsWriteToDisk400K")) {
            TestCase.assertEquals(err, expected, sObjectsWriteToDisk400K);
        }
        if (name.equals("sObjectsWriteToDisk4000K")) {
            TestCase.assertEquals(err, expected, sObjectsWriteToDisk4000K);
        }
        if (name.equals("sObjectsWriteToDiskSize")) {
            TestCase.assertEquals(err, expected, sObjectsWriteToDiskSize);
        }
        if (name.equals("sObjectsDeleteFromDisk")) {
            TestCase.assertEquals(err, expected, sObjectsDeleteFromDisk);
        }
        if (name.equals("sObjectsDeleteFromDisk4K")) {
            TestCase.assertEquals(err, expected, sObjectsDeleteFromDisk4K);
        }
        if (name.equals("sObjectsDeleteFromDisk40K")) {
            TestCase.assertEquals(err, expected, sObjectsDeleteFromDisk40K);
        }
        if (name.equals("sObjectsDeleteFromDisk400K")) {
            TestCase.assertEquals(err, expected, sObjectsDeleteFromDisk400K);
        }
        if (name.equals("sObjectsDeleteFromDisk4000K")) {
            TestCase.assertEquals(err, expected, sObjectsDeleteFromDisk4000K);
        }
        if (name.equals("sObjectsDeleteFromDiskSize")) {
            TestCase.assertEquals(err, expected, sObjectsDeleteFromDiskSize);
        }
        if (name.equals("sPushPullTableSize")) {
            TestCase.assertEquals(err, expected, sPushPullTableSize);
        }
        if (name.equals("sRemoteInvalidationNotifications")) {
            TestCase.assertEquals(err, expected, sRemoteInvalidationNotifications);
        }
        if (name.equals("sRemoteUpdateNotifications")) {
            TestCase.assertEquals(err, expected, sRemoteUpdateNotifications);
        }
        if (name.equals("sRemoteObjectUpdates")) {
            TestCase.assertEquals(err, expected, sRemoteObjectUpdates);
        }
        if (name.equals("sRemoteObjectUpdateSize")) {
            TestCase.assertEquals(err, expected, sRemoteObjectUpdateSize);
        }
        if (name.equals("sRemoteObjectHits")) {
            TestCase.assertEquals(err, expected, sRemoteObjectHits);
        }
        if (name.equals("sRemoteObjectFetchSize")) {
            TestCase.assertEquals(err, expected, sRemoteObjectFetchSize);
        }
        if (name.equals("sRemoteObjectMisses")) {
            TestCase.assertEquals(err, expected, sRemoteObjectMisses);
        }
        if (name.equals("sInvalidationLocal")) {
            TestCase.assertEquals(err, expected, sInvalidationLocal);
        }
        if (name.equals("sInvalidationLocalExplicit")) {
            TestCase.assertEquals(err, expected, sInvalidationLocalExplicit);
        }
        if (name.equals("sInvalidationLocalLRU")) {
            TestCase.assertEquals(err, expected, sInvalidationLocalLRU);
        }
        if (name.equals("sInvalidationLocalTimeout")) {
            TestCase.assertEquals(err, expected, sInvalidationLocalTimeout);
        }
        if (name.equals("sInvalidationLocalDiskTimeout")) {
            TestCase.assertEquals(err, expected, sInvalidationLocalDiskTimeout);
        }
        if (name.equals("sInvalidationLocalClearAll")) {
            TestCase.assertEquals(err, expected, sInvalidationLocalClearAll);
        }
        if (name.equals("sInvalidationRemote")) {
            TestCase.assertEquals(err, expected, sInvalidationRemote);
        }
        if (name.equals("sInvalidationRemoteExplicit")) {
            TestCase.assertEquals(err, expected, sInvalidationRemoteExplicit);
        }
        if (name.equals("sInvalidationRemoteLRU")) {
            TestCase.assertEquals(err, expected, sInvalidationRemoteLRU);
        }
        if (name.equals("sInvalidationRemoteTimeout")) {
            TestCase.assertEquals(err, expected, sInvalidationRemoteTimeout);
        }
        if (name.equals("sInvalidationRemoteDiskTimeout")) {
            TestCase.assertEquals(err, expected, sInvalidationRemoteDiskTimeout);
        }
        if (name.equals("sInvalidationRemoteClearAll")) {
            TestCase.assertEquals(err, expected, sInvalidationRemoteClearAll);
        }
    }

    /**
     * Reflectively look up the value of the attribute
     * represented by the name parameter
     * 
     */
    private long getFieldValue(String name) {
        long actualValue = -1;
        try {
            Field f = this.getClass().getDeclaredField(name);
            actualValue = f.getLong(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return actualValue;
    }

    /**
     * Checks individual value within a specified range.
     * Generates asserts on mismatches.
     */
    public void check(String m, String name, double min, double max) {
        System.out.println("ServletCacheCounters/check(range): for " + name + " min=" + min + " max=" + max + " actual=" + getFieldValue(name));
        String err = m + "Error. Value is out of range. name:<" + name + "> min:<" + min + "> max:<" + max + "> ";

        // Warning: wide lines...
        if (name.equals("sMemEntriesSize")) {
            assertRange(min, max, err, sMemEntriesSize);
        }
        if (name.equals("sDiskEntriesSize")) {
            assertRange(min, max, err, sDiskEntriesSize);
        }
        if (name.equals("sActualDiskEntriesSize")) {
            assertRange(min, max, err, sActualDiskEntriesSize);
        }
        if (name.equals("sDiskDepIdSize")) {
            assertRange(min, max, err, sDiskDepIdSize);
        }
        if (name.equals("sDiskTemplateSize")) {
            assertRange(min, max, err, sDiskTemplateSize);
        }
        if (name.equals("sTotalCacheDataDiskSize")) {
            assertRange(min, max, err, sTotalCacheDataDiskSize);
        }
        if (name.equals("sCacheHits")) {
            assertRange(min, max, err, sCacheHits);
        }
        if (name.equals("sCacheMisses")) {
            assertRange(min, max, err, sCacheMisses);
        }
        if (name.equals("sRemoves")) {
            assertRange(min, max, err, sRemoves);
        }
        if (name.equals("sLruRemoves")) {
            assertRange(min, max, err, sLruRemoves);
        }
        if (name.equals("sExplicitInvalidationsFromMemory")) {
            assertRange(min, max, err, sExplicitInvalidationsFromMemory);
        }
        if (name.equals("sExplicitInvalidationsFromDisk")) {
            assertRange(min, max, err, sExplicitInvalidationsFromDisk);
        }
        if (name.equals("sExplicitInvalidationsLocal")) {
            assertRange(min, max, err, sExplicitInvalidationsLocal);
        }
        if (name.equals("sExplicitInvalidationsRemote")) {
            assertRange(min, max, err, sExplicitInvalidationsRemote);
        }
        if (name.equals("sTimeoutInvalidationsFromMemory")) {
            assertRange(min, max, err, sTimeoutInvalidationsFromMemory);
        }
        if (name.equals("sTimeoutInvalidationsFromDisk")) {
            assertRange(min, max, err, sTimeoutInvalidationsFromDisk);
        }
        if (name.equals("sPendingRemovalSizeDisk")) {
            assertRange(min, max, err, sPendingRemovalSizeDisk);
        }
        if (name.equals("sDepIdsBufferedSizeDisk")) {
            assertRange(min, max, err, sDepIdsBufferedSizeDisk);
        }
        if (name.equals("sDepIdsOffloadedToDisk")) {
            assertRange(min, max, err, sDepIdsOffloadedToDisk);
        }
        if (name.equals("sDepIdBasedInvalidationsFromDisk")) {
            assertRange(min, max, err, sDepIdBasedInvalidationsFromDisk);
        }
        if (name.equals("sTemplatesBufferedSizeDisk")) {
            assertRange(min, max, err, sTemplatesBufferedSizeDisk);
        }
        if (name.equals("sTemplatesOffloadedToDisk")) {
            assertRange(min, max, err, sTemplatesOffloadedToDisk);
        }
        if (name.equals("sTemplateBasedInvalidationsFromDisk")) {
            assertRange(min, max, err, sTemplateBasedInvalidationsFromDisk);
        }
        if (name.equals("sObjectsReadFromDisk")) {
            assertRange(min, max, err, sObjectsReadFromDisk);
        }
        if (name.equals("sObjectsReadFromDisk4K")) {
            assertRange(min, max, err, sObjectsReadFromDisk4K);
        }
        if (name.equals("sObjectsReadFromDisk40K")) {
            assertRange(min, max, err, sObjectsReadFromDisk40K);
        }
        if (name.equals("sObjectsReadFromDisk400K")) {
            assertRange(min, max, err, sObjectsReadFromDisk400K);
        }
        if (name.equals("sObjectsReadFromDisk4000K")) {
            assertRange(min, max, err, sObjectsReadFromDisk4000K);
        }
        if (name.equals("sObjectsReadFromDiskSize")) {
            assertRange(min, max, err, sObjectsReadFromDiskSize);
        }
        if (name.equals("sObjectsWriteToDisk")) {
            assertRange(min, max, err, sObjectsWriteToDisk);
        }
        if (name.equals("sObjectsWriteToDisk4K")) {
            assertRange(min, max, err, sObjectsWriteToDisk4K);
        }
        if (name.equals("sObjectsWriteToDisk40K")) {
            assertRange(min, max, err, sObjectsWriteToDisk40K);
        }
        if (name.equals("sObjectsWriteToDisk400K")) {
            assertRange(min, max, err, sObjectsWriteToDisk400K);
        }
        if (name.equals("sObjectsWriteToDisk4000K")) {
            assertRange(min, max, err, sObjectsWriteToDisk4000K);
        }
        if (name.equals("sObjectsWriteToDiskSize")) {
            assertRange(min, max, err, sObjectsWriteToDiskSize);
        }
        if (name.equals("sObjectsDeleteFromDisk")) {
            assertRange(min, max, err, sObjectsDeleteFromDisk);
        }
        if (name.equals("sObjectsDeleteFromDisk4K")) {
            assertRange(min, max, err, sObjectsDeleteFromDisk4K);
        }
        if (name.equals("sObjectsDeleteFromDisk40K")) {
            assertRange(min, max, err, sObjectsDeleteFromDisk40K);
        }
        if (name.equals("sObjectsDeleteFromDisk400K")) {
            assertRange(min, max, err, sObjectsDeleteFromDisk400K);
        }
        if (name.equals("sObjectsDeleteFromDisk4000K")) {
            assertRange(min, max, err, sObjectsDeleteFromDisk4000K);
        }
        if (name.equals("sObjectsDeleteFromDiskSize")) {
            assertRange(min, max, err, sObjectsDeleteFromDiskSize);
        }
        if (name.equals("sPushPullTableSize")) {
            assertRange(min, max, err, sPushPullTableSize);
        }
        if (name.equals("sRemoteInvalidationNotifications")) {
            assertRange(min, max, err, sRemoteInvalidationNotifications);
        }
        if (name.equals("sRemoteUpdateNotifications")) {
            assertRange(min, max, err, sRemoteUpdateNotifications);
        }
        if (name.equals("sRemoteObjectUpdates")) {
            assertRange(min, max, err, sRemoteObjectUpdates);
        }
        if (name.equals("sRemoteObjectUpdateSize")) {
            assertRange(min, max, err, sRemoteObjectUpdateSize);
        }
        if (name.equals("sRemoteObjectHits")) {
            assertRange(min, max, err, sRemoteObjectHits);
        }
        if (name.equals("sRemoteObjectFetchSize")) {
            assertRange(min, max, err, sRemoteObjectFetchSize);
        }
        if (name.equals("sRemoteObjectMisses")) {
            assertRange(min, max, err, sRemoteObjectMisses);
        }
        if (name.equals("sInvalidationLocal")) {
            assertRange(min, max, err, sInvalidationLocal);
        }
        if (name.equals("sInvalidationLocalExplicit")) {
            assertRange(min, max, err, sInvalidationLocalExplicit);
        }
        if (name.equals("sInvalidationLocalLRU")) {
            assertRange(min, max, err, sInvalidationLocalLRU);
        }
        if (name.equals("sInvalidationLocalTimeout")) {
            assertRange(min, max, err, sInvalidationLocalTimeout);
        }
        if (name.equals("sInvalidationLocalDiskTimeout")) {
            assertRange(min, max, err, sInvalidationLocalDiskTimeout);
        }
        if (name.equals("sInvalidationLocalClearAll")) {
            assertRange(min, max, err, sInvalidationLocalClearAll);
        }
        if (name.equals("sInvalidationRemote")) {
            assertRange(min, max, err, sInvalidationRemote);
        }
        if (name.equals("sInvalidationRemoteExplicit")) {
            assertRange(min, max, err, sInvalidationRemoteExplicit);
        }
        if (name.equals("sInvalidationRemoteLRU")) {
            assertRange(min, max, err, sInvalidationRemoteLRU);
        }
        if (name.equals("sInvalidationRemoteTimeout")) {
            assertRange(min, max, err, sInvalidationRemoteTimeout);
        }
        if (name.equals("sInvalidationRemoteDiskTimeout")) {
            assertRange(min, max, err, sInvalidationRemoteDiskTimeout);
        }
        if (name.equals("sInvalidationRemoteClearAll")) {
            assertRange(min, max, err, sInvalidationRemoteClearAll);
        }
    }

    /**
     * This method checks the counters for all the testcases.
     * It issues asserts if it encounters unexpected values.
     */
    public void check(String m, int valuesize,
                      int expectedreads, int expectedwrites, int expecteddeletes,
                      long totalreadsize, long totalwritesize, long totaldeletesize) {

        // expected read values
        int read4K = 0, read40K = 0, read400K = 0, read4000K = 0;

        if (valuesize <= 4000)
            read4K = expectedreads;
        if (valuesize > 4000 && valuesize <= 40000)
            read40K = expectedreads;
        if (valuesize > 40000 && valuesize <= 400000)
            read400K = expectedreads;
        if (valuesize > 400000 && valuesize <= 4000000)
            read4000K = expectedreads;

        // expected write values
        int write4K = 0, write40K = 0, write400K = 0, write4000K = 0;

        if (valuesize <= 4000)
            write4K = expectedwrites;
        if (valuesize > 4000 && valuesize <= 40000)
            write40K = expectedwrites;
        if (valuesize > 40000 && valuesize <= 400000)
            write400K = expectedwrites;
        if (valuesize > 400000 && valuesize <= 4000000)
            write4000K = expectedwrites;

        // expected delete values
        int delete4K = 0, delete40K = 0, delete400K = 0, delete4000K = 0;

        if (valuesize <= 4000)
            delete4K = expecteddeletes;
        if (valuesize > 4000 && valuesize <= 40000)
            delete40K = expecteddeletes;
        if (valuesize > 40000 && valuesize <= 400000)
            delete400K = expecteddeletes;
        if (valuesize > 400000 && valuesize <= 4000000)
            delete4000K = expecteddeletes;

        TestCase.assertEquals(m + ": sObjectsReadFromDisk != " + expectedreads, expectedreads, this.sObjectsReadFromDisk);
        TestCase.assertEquals(m + ": sObjectsReadFromDisk4K != " + read4K, read4K, this.sObjectsReadFromDisk4K);
        TestCase.assertEquals(m + ": sObjectsReadFromDisk40K != " + read40K, read40K, this.sObjectsReadFromDisk40K);
        TestCase.assertEquals(m + ": sObjectsReadFromDisk400K != " + read400K, read400K, this.sObjectsReadFromDisk400K);
        TestCase.assertEquals(m + ": sObjectsReadFromDisk4000K != " + read4000K, read4000K, this.sObjectsReadFromDisk4000K);
        TestCase.assertEquals(m + ": sObjectsReadFromDiskSize != " + totalreadsize, totalreadsize, this.sObjectsReadFromDiskSize);

        TestCase.assertEquals(m + ": sObjectsWriteToDisk != " + expectedwrites, expectedwrites, this.sObjectsWriteToDisk);
        TestCase.assertEquals(m + ": sObjectsWriteToDisk4K != " + write4K, write4K, this.sObjectsWriteToDisk4K);
        TestCase.assertEquals(m + ": sObjectsWriteToDisk40K != " + write40K, write40K, this.sObjectsWriteToDisk40K);
        TestCase.assertEquals(m + ": sObjectsWriteToDisk400K != " + write400K, write400K, this.sObjectsWriteToDisk400K);
        TestCase.assertEquals(m + ": sObjectsWriteToDisk4000K != " + write4000K, write4000K, this.sObjectsWriteToDisk4000K);
        TestCase.assertEquals(m + ": sObjectsWriteToDiskSize != " + totalwritesize, totalwritesize, this.sObjectsWriteToDiskSize);

        TestCase.assertEquals(m + ": sObjectsDeleteFromDisk != " + expecteddeletes, expecteddeletes, this.sObjectsDeleteFromDisk);
        TestCase.assertEquals(m + ": sObjectsDeleteFromDisk4K != " + expecteddeletes, delete4K, this.sObjectsDeleteFromDisk4K);
        TestCase.assertEquals(m + ": sObjectsDeleteFromDisk40K != " + expecteddeletes, delete40K, this.sObjectsDeleteFromDisk40K);
        TestCase.assertEquals(m + ": sObjectsDeleteFromDisk400K != " + expecteddeletes, delete400K, this.sObjectsDeleteFromDisk400K);
        TestCase.assertEquals(m + ": sObjectsDeleteFromDisk4000K != " + expecteddeletes, delete4000K, this.sObjectsDeleteFromDisk4000K);
        TestCase.assertEquals(m + ": sObjectsDeleteFromDiskSize != " + expecteddeletes, totaldeletesize, this.sObjectsDeleteFromDiskSize);

    }

    //-------------------------------------------------------------------------
    // Private methods.
    //-------------------------------------------------------------------------

    /**
     * Searches the result string for the key string.
     * If found, extracts the associated value and returns it.
     * Otherwise, returns the original value.
     */
    private long parseLongValue(String result, String key, long value) {
        String keyEquals = key + "=";

        int sindex = result.indexOf(keyEquals);
        if (sindex > 0) {
            int eindex = result.indexOf(" ", sindex);
            String temp = result.substring(sindex + keyEquals.length(), eindex);
            try {
                value = Long.parseLong(temp);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //System.out.println(keyEquals + value);
        }
        else {
            System.out.println("Key/value pair not found. key=" + key);
        }
        return value;
    }

    /**
     * Helper method ensures a value is within an expected range.
     * Throws an AssertionFailedError if the value is out of range.
     */
    private void assertRange(double min, double max, String err, long value) {
        if ((value > max) || (value < min)) {
            err += "actual:<" + value + "> (ignore the 999s) ";
            TestCase.assertEquals(err, 999, -999);
        }
    }

    /**
     * Opens an HTTP connection with the AppServer,
     * issues the specified uri request, and
     * returns the response from the server.
     * 
     * Parm m is an eyecatcher for trace messages
     * to identify the method which called this one.
     */
    public WebResponse getresponse(String m, String uri) throws Exception {

        WebResponse resp = null;
        WebConversation wc = new WebConversation();
        System.out.println(m + " sending request: " + uri);
        resp = getWebResponse(wc, uri);
        System.out.println(m + " got response");
        Assert.assertEquals(ServletTestCase.msg(m + ": Response code was not OK", resp), resp.getResponseCode(), 200);
        String s = resp.getText();
        if (EvictionTest.stringin("error", s)) {
            System.out.println(s);
            Assert.fail(m + ": TimeoutInvalidation - " + resp.getURL() + "\n Error occurred during the test");
        }
        int sindex = s.indexOf("Result: ");
        String result = "";
        if (sindex > 0) {
            int eindex = s.indexOf("</body>", sindex);
            result = s.substring(sindex, eindex);
        } else {
            Assert.fail(ServletTestCase.msg(m + ": NO result found in response: ", resp));
        }
        parse(result);
        return resp;
    }
}