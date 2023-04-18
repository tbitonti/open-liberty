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

import com.ibm.ws.cache.cachemonitor.CacheContents;
import com.ibm.ws.cache.command.CommandTest;
import com.ibm.ws.cache.servlet.MiscTest;

import componenttest.rules.repeater.EmptyAction;
import componenttest.rules.repeater.FeatureReplacementAction;
import componenttest.rules.repeater.JakartaEE9PublishedAppsRule;
import componenttest.rules.repeater.RepeatTests;

/**
 * Collection of a few fast example tests
 */
@RunWith(Suite.class)
@SuiteClasses({ CommandTest.class,
                CacheContents.class,
                MiscTest.class
})
public class FATSuiteLite {

    public static RepeatTests repeatTests = RepeatTests.with(new EmptyAction().fullFATOnly()).andWith(FeatureReplacementAction.EE9_FEATURES().removeFeature("restConnector-1.0").addFeature("restConnector-2.0").alwaysAddFeature("servlet-5.0").conditionalFullFATOnly(FeatureReplacementAction.GREATER_THAN_OR_EQUAL_JAVA_11)).andWith(FeatureReplacementAction.EE10_FEATURES().removeFeature("restConnector-1.0").addFeature("restConnector-2.0").alwaysAddFeature("servlet-6.0"));

    @ClassRule
    public static RuleChain rule = RuleChain.outerRule(repeatTests).around(new JakartaEE9PublishedAppsRule());
}
