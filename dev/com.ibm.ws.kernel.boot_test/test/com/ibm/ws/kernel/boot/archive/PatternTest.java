/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.kernel.boot.archive;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

/**
 * Simple verification that java pattern matching does what
 * we expect for directory matching.
 */
public class PatternTest {
    public static final String CANDIDATE_ROOT = "/";

    public static final String CANDIDATE_CHILD_A = "/a.xml";
    public static final String CANDIDATE_CHILD_B = "/b.props";
    public static final String CANDIDATE_CHILD_C = "/c";
    
    public static final String CANDIDATE_GRANDCHILD_AA = "/a/a.xml";    
    public static final String CANDIDATE_GRANDCHILD_AB = "/a/b.props";    
    public static final String CANDIDATE_GRANDCHILD_AC = "/a/c";    
    
    public static final String CANDIDATE_GRANDCHILD_BA = "/b/a.xml";    
    public static final String CANDIDATE_GRANDCHILD_BB = "/b/b.props";    
    public static final String CANDIDATE_GRANDCHILD_BC = "/b/c";    
    
    public static final String CANDIDATE_GRANDCHILD_CA = "/c/a.xml";    
    public static final String CANDIDATE_GRANDCHILD_CB = "/c/b.props";    
    public static final String CANDIDATE_GRANDCHILD_CC = "/c/c";        
    
    public static final String CANDIDATE_GRANDCHILD_AAA = "/a/a/a.xml";    
    public static final String CANDIDATE_GRANDCHILD_AAB = "/a/a/b.props";    
    public static final String CANDIDATE_GRANDCHILD_AAC = "/a/a/c";    

    public static final String CANDIDATE_GRANDCHILD_ABA = "/a/b/a.xml";    
    public static final String CANDIDATE_GRANDCHILD_ABB = "/a/b/b.props";    
    public static final String CANDIDATE_GRANDCHILD_ABC = "/a/b/c";    
    
    public static final String CANDIDATE_GRANDCHILD_ACA = "/a/c/a.xml";    
    public static final String CANDIDATE_GRANDCHILD_ACB = "/a/c/b.props";    
    public static final String CANDIDATE_GRANDCHILD_ACC = "/a/c/c";    
    
    public static final String CANDIDATE_GRANDCHILD_BAA = "/b/a/a.xml";    
    public static final String CANDIDATE_GRANDCHILD_BAB = "/b/a/b.props";    
    public static final String CANDIDATE_GRANDCHILD_BAC = "/b/a/c";    
    
    public static final String CANDIDATE_GRANDCHILD_BBA = "/b/b/a.xml";    
    public static final String CANDIDATE_GRANDCHILD_BBB = "/b/b/b.props";    
    public static final String CANDIDATE_GRANDCHILD_BBC = "/b/b/c";    

    public static final String CANDIDATE_GRANDCHILD_BCA = "/b/c/a.xml";    
    public static final String CANDIDATE_GRANDCHILD_BCB = "/b/c/b.props";    
    public static final String CANDIDATE_GRANDCHILD_BCC = "/b/c/c";    
    
    public static final String CANDIDATE_GRANDCHILD_CAA = "/c/a/a.xml";    
    public static final String CANDIDATE_GRANDCHILD_CAB = "/c/a/b.props";    
    public static final String CANDIDATE_GRANDCHILD_CAC = "/c/a/c";    
    
    public static final String CANDIDATE_GRANDCHILD_CBA = "/c/b/a.xml";    
    public static final String CANDIDATE_GRANDCHILD_CBB = "/c/b/b.props";    
    public static final String CANDIDATE_GRANDCHILD_CBC = "/c/b/c";    
    
    public static final String CANDIDATE_GRANDCHILD_CCA = "/c/c/a.xml";            
    public static final String CANDIDATE_GRANDCHILD_CCB = "/c/c/b.props";            
    public static final String CANDIDATE_GRANDCHILD_CCC = "/c/c/c";                

    public static final String[] CANDIDATES = {
            CANDIDATE_ROOT,

            CANDIDATE_CHILD_A, CANDIDATE_CHILD_B, CANDIDATE_CHILD_C,

            CANDIDATE_GRANDCHILD_AA, CANDIDATE_GRANDCHILD_AB, CANDIDATE_GRANDCHILD_AC,
            CANDIDATE_GRANDCHILD_BA, CANDIDATE_GRANDCHILD_BB, CANDIDATE_GRANDCHILD_BC,
            CANDIDATE_GRANDCHILD_CA, CANDIDATE_GRANDCHILD_CB, CANDIDATE_GRANDCHILD_CC,
        
            CANDIDATE_GRANDCHILD_AAA, CANDIDATE_GRANDCHILD_AAB, CANDIDATE_GRANDCHILD_AAC,
            CANDIDATE_GRANDCHILD_ABA, CANDIDATE_GRANDCHILD_ABB, CANDIDATE_GRANDCHILD_ABC,
            CANDIDATE_GRANDCHILD_ACA, CANDIDATE_GRANDCHILD_ACB, CANDIDATE_GRANDCHILD_ACC,
        
            CANDIDATE_GRANDCHILD_BAA, CANDIDATE_GRANDCHILD_BAB, CANDIDATE_GRANDCHILD_BAC,
            CANDIDATE_GRANDCHILD_BBA, CANDIDATE_GRANDCHILD_BBB, CANDIDATE_GRANDCHILD_BBC,
            CANDIDATE_GRANDCHILD_BCA, CANDIDATE_GRANDCHILD_BCB, CANDIDATE_GRANDCHILD_BCC,
        
            CANDIDATE_GRANDCHILD_CAA, CANDIDATE_GRANDCHILD_CAB, CANDIDATE_GRANDCHILD_CAC,
            CANDIDATE_GRANDCHILD_CBA, CANDIDATE_GRANDCHILD_CBB, CANDIDATE_GRANDCHILD_CBC,
            CANDIDATE_GRANDCHILD_CCA, CANDIDATE_GRANDCHILD_CCB, CANDIDATE_GRANDCHILD_CCC
    };

    public static final String EXPR_ROOT = Pattern.quote("/");
    public static final String EXPR_CHILD_A = Pattern.quote("/a/");
    public static final String EXPR_GRANDCHILD_AB = Pattern.quote("/a/b/");

    public static final String EXPR_EXT_XML = "\\.xml$";
    public static final String EXPR_EXT_PROPS = "\\.props$";

    public static final String[] EXPRESSIONS = {
            EXPR_ROOT,
            EXPR_CHILD_A,
            EXPR_GRANDCHILD_AB,
            EXPR_EXT_XML,
            EXPR_EXT_PROPS
    };

    public static class TestCase {
        public static final boolean MATCH_ALL = true;
        public static final boolean MATCH_NONE = true;

        public final String expression;

        public final String[] candidates;

        public final boolean matchAll;
        public final boolean matchNone;
        public final Set<String> matches;

        public TestCase(
            String expression,
            String[] candidates,
            boolean matchAll, boolean matchNone, String... matches) {

            this.expression = expression;

            this.candidates = candidates;

            this.matchAll = matchAll;
            this.matchNone = matchNone;

            if ( matches.length == 0 ) {
                this.matches = null;
            } else {
                this.matches = new HashSet<String>(matches.length);
                for ( String match : matches ) {
                    this.matches.add(match);
                }
            }
        }
        
        public String verify() {
            System.out.println("Pattern [ " + expression + " ]");

            Pattern pattern = Pattern.compile(expression);

            String missingMatch = null;
            String extraMatch = null;

            for ( String candidate : candidates ) {
                Matcher matcher = pattern.matcher(candidate);
                boolean found = matcher.find();
                    
                if ( found ) {
                    if ( !matchAll && (matchNone || !matches.contains(candidate)) ) {
                        if ( extraMatch == null ) {
                            extraMatch = candidate;
                        }
                    }
                } else {
                    if ( !matchNone && (matchAll || matches.contains(candidate)) ) {
                        if ( missingMatch == null ) {
                            missingMatch = candidate;
                        }
                    }
                }
                String foundText = (found ? "PRESENT" : "ABSENT");

                System.out.println("  [ " + candidate + " ]: " + foundText); 
            }
                
            if ( missingMatch != null ) {
                if ( extraMatch != null ) {
                    return "Pattern [ " + expression + " ] should match [ " + missingMatch + " ] and should not match [ " + extraMatch + " ]";
                } else {
                    return "Pattern [ " + expression + " ] should match [ " + missingMatch + " ]";
                }
            } else if ( extraMatch != null ) {
                return "Pattern [ " + expression + " ] should not match [ " + extraMatch + " ]";
            } else {
                return null;
            }
        }
    }

    public static final TestCase TEST_ROOT =
        new TestCase(EXPR_ROOT, CANDIDATES, TestCase.MATCH_ALL, !TestCase.MATCH_NONE);

    // Note the extra 'BAA', 'BAB', 'BAC', 'CAA', 'CAB', and 'CAC' matches.
    //
    // This predicts the problem of matching against full paths, which is expressed
    // in the directory pattern implementation, which matches against full paths.

    public static final TestCase TEST_CHILD_A =    
        new TestCase(EXPR_CHILD_A, CANDIDATES, !TestCase.MATCH_ALL, !TestCase.MATCH_NONE,
                CANDIDATE_GRANDCHILD_AA, CANDIDATE_GRANDCHILD_AB, CANDIDATE_GRANDCHILD_AC,

                CANDIDATE_GRANDCHILD_AAA, CANDIDATE_GRANDCHILD_AAB, CANDIDATE_GRANDCHILD_AAC,
                CANDIDATE_GRANDCHILD_ABA, CANDIDATE_GRANDCHILD_ABB, CANDIDATE_GRANDCHILD_ABC,
                CANDIDATE_GRANDCHILD_ACA, CANDIDATE_GRANDCHILD_ACB, CANDIDATE_GRANDCHILD_ACC,

                CANDIDATE_GRANDCHILD_BAA, CANDIDATE_GRANDCHILD_BAB, CANDIDATE_GRANDCHILD_BAC, 
                CANDIDATE_GRANDCHILD_CAA, CANDIDATE_GRANDCHILD_CAB, CANDIDATE_GRANDCHILD_CAC);
            
    public static final TestCase TEST_GRANDCHILD_AB =
        new TestCase(EXPR_GRANDCHILD_AB, CANDIDATES, !TestCase.MATCH_ALL, !TestCase.MATCH_NONE,
                CANDIDATE_GRANDCHILD_ABA, CANDIDATE_GRANDCHILD_ABB, CANDIDATE_GRANDCHILD_ABC);
            
    public static final TestCase TEST_EXT_XML =
        new TestCase(EXPR_EXT_XML, CANDIDATES, !TestCase.MATCH_ALL, !TestCase.MATCH_NONE,
                CANDIDATE_CHILD_A,
                CANDIDATE_GRANDCHILD_AA, CANDIDATE_GRANDCHILD_BA, CANDIDATE_GRANDCHILD_CA,
                CANDIDATE_GRANDCHILD_AAA, CANDIDATE_GRANDCHILD_ABA, CANDIDATE_GRANDCHILD_ACA,
                CANDIDATE_GRANDCHILD_BAA, CANDIDATE_GRANDCHILD_BBA, CANDIDATE_GRANDCHILD_BCA,
                CANDIDATE_GRANDCHILD_CAA, CANDIDATE_GRANDCHILD_CBA, CANDIDATE_GRANDCHILD_CCA);

    public static final TestCase TEST_EXT_PROPS =
        new TestCase(EXPR_EXT_PROPS, CANDIDATES, !TestCase.MATCH_ALL, !TestCase.MATCH_NONE,
                CANDIDATE_CHILD_B,
                CANDIDATE_GRANDCHILD_AB, CANDIDATE_GRANDCHILD_BB, CANDIDATE_GRANDCHILD_CB,
                CANDIDATE_GRANDCHILD_AAB, CANDIDATE_GRANDCHILD_ABB, CANDIDATE_GRANDCHILD_ACB,
                CANDIDATE_GRANDCHILD_BAB, CANDIDATE_GRANDCHILD_BBB, CANDIDATE_GRANDCHILD_BCB,
                CANDIDATE_GRANDCHILD_CAB, CANDIDATE_GRANDCHILD_CBB, CANDIDATE_GRANDCHILD_CCB);

    public static final TestCase[] TEST_CASES = new TestCase[] {
            TEST_ROOT, TEST_CHILD_A, TEST_GRANDCHILD_AB,
            TEST_EXT_XML, TEST_EXT_PROPS
    };
    
    public void test(TestCase testCase) {
        String failureReason = testCase.verify();
        if ( failureReason != null ) {
            Assert.fail(failureReason);
        }
    }
    
    @Test
    public void testRoot() {
        test(TEST_ROOT);
    }
    
    @Test
    public void testChildA() {
        test(TEST_CHILD_A);
    }
    
    @Test
    public void testGrandchildAB() {
        test(TEST_GRANDCHILD_AB);
    }
    
    @Test
    public void testExtXml() {
        test(TEST_EXT_XML);
    }
    
    @Test
    public void testExtProps() {
        test(TEST_EXT_PROPS);
    }    
}
