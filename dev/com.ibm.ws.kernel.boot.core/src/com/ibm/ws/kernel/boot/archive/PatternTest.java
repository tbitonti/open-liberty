package com.ibm.ws.kernel.boot.archive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternTest {

    public static final String EXPR_ROOT = Pattern.quote("/");
    public static final String EXPR_CHILD = Pattern.quote("/a");
    public static final String EXPR_GRANDCHILD = Pattern.quote("/a/b");

    public static final String EXPR_EXT_XML = "\\.xml$";
    public static final String EXPR_EXT_PROPS = "\\.props$";

    public static final String[] EXPRESSIONS = {
            EXPR_ROOT,
            EXPR_CHILD,
            EXPR_GRANDCHILD,
            EXPR_EXT_XML,
            EXPR_EXT_PROPS
    };

    //
    
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
    
    public static void testMatches(String expression, String[] candidates) {
        System.out.println("Pattern [ " + expression + " ]");

        Pattern pattern = Pattern.compile(expression);

        for ( String candidate : candidates ) {
            Matcher matcher = pattern.matcher(candidate);
            boolean found = matcher.find();
            String foundText = (found ? "PRESENT" : "ABSENT");

            System.out.println("  [ " + candidate + " ]: " + foundText); 
        }
    }

    public static void main(String[] parms) {
        for ( String expression : EXPRESSIONS ) {
            testMatches(expression, CANDIDATES);
        }
    }
}
