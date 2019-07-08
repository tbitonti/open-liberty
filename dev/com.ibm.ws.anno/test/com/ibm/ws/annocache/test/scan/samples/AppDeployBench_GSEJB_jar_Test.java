/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.annocache.test.scan.samples;

import org.junit.Test;

import com.ibm.ws.annocache.classsource.specification.internal.ClassSourceImpl_Specification_Direct_EJB;
import com.ibm.ws.annocache.test.scan.TestOptions_SuiteCase;
import com.ibm.ws.annocache.test.scan.Test_Base;
import com.ibm.ws.annocache.classsource.internal.ClassSourceImpl_Factory;

public class AppDeployBench_GSEJB_jar_Test extends Test_Base {

    @Override
    public ClassSourceImpl_Specification_Direct_EJB createClassSourceSpecification(ClassSourceImpl_Factory factory) {
        return AppDeployBench_GSEJB_jar_Data.createClassSourceSpecification(factory);
    }

    //

    @Override
    public String getAppName() {
        return AppDeployBench_GSEJB_jar_Data.EAR_NAME;
    }

    @Override
    public String getAppSimpleName() {
        return AppDeployBench_GSEJB_jar_Data.EAR_SIMPLE_NAME;
    }

    
    @Override
    public String getModName() {
        return AppDeployBench_GSEJB_jar_Data.EJBJAR_NAME;
    }

    @Override
    public String getModSimpleName() {
        return AppDeployBench_GSEJB_jar_Data.EJBJAR_SIMPLE_NAME;
    }

    //

    @Test
    public void testAppDeployBench_GSEJB_jar_BASE() throws Exception {
        runSuiteTest( getBaseCase() ); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_SINGLE_JANDEX() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.SINGLE_JANDEX); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_SINGLE_JANDEX_FULL() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.SINGLE_JANDEX_FULL); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_MULTI() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.MULTI); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_MULTI_JANDEX() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.MULTI_JANDEX); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_MULTI_JANDEX_FULL() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.MULTI_JANDEX_FULL); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_SINGLE_WRITE() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.SINGLE_WRITE); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_SINGLE_READ() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.SINGLE_READ); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_MULTI_WRITE() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.MULTI_WRITE); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_MULTI_READ() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.MULTI_READ); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_SINGLE_WRITE_ASYNC() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.SINGLE_WRITE_ASYNC); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_MULTI_WRITE_ASYNC() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.MULTI_WRITE_ASYNC); // 'runSuiteTest' throws Exception
    }

    //

    @Test
    public void testAppDeployBench_GSEJB_jar_SINGLE_WRITE_JANDEX_NOWRITE() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.SINGLE_WRITE_JANDEX_NOWRITE); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_SINGLE_READ_JANDEX_NOWRITE() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.SINGLE_READ_JANDEX_NOWRITE); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_MULTI_WRITE_JANDEX_NOWRITE() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.MULTI_WRITE_JANDEX_NOWRITE); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_MULTI_READ_JANDEX_NOWRITE() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.MULTI_READ_JANDEX_NOWRITE); // 'runSuiteTest' throws Exception
    }

    //

    @Test
    public void testAppDeployBench_GSEJB_jar_SINGLE_WRITE_SEPARATE() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.SINGLE_WRITE_SEPARATE); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_SINGLE_READ_SEPARATE() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.SINGLE_READ_SEPARATE); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_MULTI_WRITE_SEPARATE() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.MULTI_WRITE_SEPARATE); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_MULTI_READ_SEPARATE() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.MULTI_READ_SEPARATE); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_SINGLE_WRITE_JANDEX_FORMAT() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.SINGLE_WRITE_JANDEX_FORMAT); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_SINGLE_READ_JANDEX_FORMAT() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.SINGLE_READ_JANDEX_FORMAT); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_SINGLE_WRITE_BINARY_FORMAT() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.SINGLE_WRITE_BINARY_FORMAT); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_SINGLE_READ_BINARY_FORMAT() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.SINGLE_READ_BINARY_FORMAT); // 'runSuiteTest' throws Exception
    }

    @Test
    public void testAppDeployBench_GSEJB_jar_SINGLE_READ_BINARY_FORMAT_VALID() throws Exception {
        runSuiteTest(TestOptions_SuiteCase.SINGLE_READ_BINARY_FORMAT_VALID); // 'runSuiteTest' throws Exception
    }
}
