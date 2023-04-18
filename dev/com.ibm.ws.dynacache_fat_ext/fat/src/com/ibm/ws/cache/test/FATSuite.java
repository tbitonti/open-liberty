/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2012, 2023
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.ibm.ws.cache.test;

import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.ws.cache.async.ProgrammaticServletCachingTest;
import com.ibm.ws.cache.htod.EvictionTest;
import com.ibm.ws.cache.htod.HTODTest1;
import com.ibm.ws.cache.htod.HTODTest2;
import com.ibm.ws.cache.htod.HTODTest3;
import com.ibm.ws.cache.htod.MBeanCounterTest;
import com.ibm.ws.cache.pmi.HTODPMITest;
import com.ibm.ws.cache.servlet.DiscardTest;
import com.ibm.ws.cache.servlet.DoNotCacheTest;
import com.ibm.ws.cache.servlet.PageOneTest;
import com.ibm.ws.cache.servlet.RangeAttrTest;
import com.ibm.ws.cache.servlet.RangeTest;
import com.ibm.ws.cache.servlet.SaveCookieTest;
import com.ibm.ws.cache.servlet.SkipCacheAttributeTest;
import com.ibm.ws.cache.servlet.StaticCacheTest;
import com.ibm.ws.cache.webservices.WebServiceTests;

import componenttest.rules.repeater.EmptyAction;
import componenttest.rules.repeater.FeatureReplacementAction;
import componenttest.rules.repeater.JakartaEE9PublishedAppsRule;
import componenttest.rules.repeater.RepeatTests;

/**
 * Collection of all Dynacache tests
 */
@RunWith(Suite.class)
@SuiteClasses({

                DoNotCacheTest.class,
                RangeAttrTest.class,
                RangeTest.class,
                SaveCookieTest.class,
                SkipCacheAttributeTest.class,
                StaticCacheTest.class,
                DiscardTest.class,
                PageOneTest.class,
                ProgrammaticServletCachingTest.class,
                EvictionTest.class,
                HTODTest1.class,
                HTODTest2.class,
                HTODTest3.class,
                MBeanCounterTest.class,
                HTODPMITest.class,
                WebServiceTests.class
})
public class FATSuite {

    public static RepeatTests repeatTests = RepeatTests.with(new EmptyAction().fullFATOnly()).andWith(FeatureReplacementAction.EE9_FEATURES().removeFeature("restConnector-1.0").addFeature("restConnector-2.0").alwaysAddFeature("servlet-5.0").conditionalFullFATOnly(FeatureReplacementAction.GREATER_THAN_OR_EQUAL_JAVA_11)).andWith(FeatureReplacementAction.EE10_FEATURES().removeFeature("restConnector-1.0").addFeature("restConnector-2.0").alwaysAddFeature("servlet-6.0"));

    @ClassRule
    public static RuleChain rule = RuleChain.outerRule(repeatTests).around(new JakartaEE9PublishedAppsRule());
}
