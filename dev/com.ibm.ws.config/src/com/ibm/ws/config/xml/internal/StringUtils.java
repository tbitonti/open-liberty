/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.config.xml.internal;

import java.lang.reflect.Array;
import java.util.List;

import com.ibm.websphere.ras.annotation.Trivial;

//@formatter:off
@Trivial
public class StringUtils {

    // 48-58  0-9
    // 65-90  A-Z
    // 97-122 a-z

    public static final boolean isAlphaNumeric(char c) {
        return ( (c >= '0' && c <= '9') ||
                 (c >= 'A' && c <= 'Z') ||
                 (c >= 'a' && c <= 'z') );
    }

    /**
     * Replace non-alphanumeric characters in a string with underscores.
     *
     * @param name The name which is to be updated.
     *
     * @return The name with non-alphanumeric characters replaced with underscores.
     */
    public static String replaceNonAlpha(String name) {
        StringBuilder builder = null;

        int lastCopied = 0;
        int length = name.length();

        for ( int charNo = 0; charNo < length; charNo++ ) {
            char c = name.charAt(charNo);
            if ( isAlphaNumeric(c) ) {
                continue;
            }

            if ( builder == null ) {
                builder = new StringBuilder(length);
            }
            if ( lastCopied < charNo ) {
                builder.append(name, lastCopied, charNo);
            }
            builder.append('_');

            lastCopied = charNo + 1;
        }

        if ( builder == null ) {
            return name;
        } else {
            if ( lastCopied != length ) {
                builder.append(name, lastCopied, length);
            }
            return builder.toString();
        }
    }

    //

    /**
     * Convert a value to a string value.
     *
     * If the value is a string, answer the string, and do not escape
     * the value.
     *
     * If the value is not a string and is not a collection type,
     * answer the print string of the value.  Do not escape the value.
     *
     * If the value is a collection type (list, or array),
     * create a comma delimited value, with escaped elements.
     *
     * @param value The value which is to be converted.
     *
     * @return The converted value.
     */
    public static String convertToString(Object value) {
        if ( value == null ) {
            return null;

        } else if ( value instanceof String ) {
            return (String) value;

        } else if ( value instanceof List ) {
            return convertToString( (List<?>) value );

        } else if ( value instanceof String[] ) {
            return convertToString( (String[]) value );

        } else if ( value.getClass().isArray() ) {
            return convertArrayToString(value);

        } else {
            return value.toString();
        }
    }

    public static String convertToString(List<?> list) {
        int length = list.size();
        if ( length == 0 ) {
            return EvaluationContext.EMPTY_STRING;

        } else if ( length == 1 ) {
            String strValue = String.valueOf(list.get(0));
            return escapeValue(strValue);

        } else {
            StringBuilder builder = new StringBuilder();
            boolean isFirst = true;
            for ( Object element : list ) {
                if ( isFirst ) {
                    isFirst = false;
                } else {
                    builder.append(", ");
                }
                builder.append( escapeValue( String.valueOf(element) ) );
            }
            return builder.toString();
        }
    }

    public static String convertToString(String[] array) {
        if ( array.length == 0 ) {
            return EvaluationContext.EMPTY_STRING;

        } else if ( array.length == 1 ) {
            return escapeValue(array[0]);

        } else {
            StringBuilder builder = new StringBuilder();

            boolean onFirst = true;
            for ( String element : array ) {
                if ( onFirst ) {
                    onFirst = false;
                } else {
                    builder.append(", ");
                }
                builder.append( escapeValue(element) );
            }

            return builder.toString();
        }
    }

    public static String convertArrayToString(Object value) {
        int length = Array.getLength(value);
        if ( length == 0 ) {
            return EvaluationContext.EMPTY_STRING;

        } else if ( length == 1 ) {
            return escapeValue( String.valueOf(Array.get(value, 0)) );

        } else {
            StringBuilder builder = new StringBuilder();
            boolean onFirst = true;
            for ( int elementNo = 0; elementNo < length; elementNo++ ) {
                if ( onFirst ) {
                    onFirst = false;
                } else {
                    builder.append(", ");
                }
                builder.append( escapeValue( String.valueOf(Array.get(value, elementNo) ) ) );
            }
            return builder.toString();
        }
    }

    /**
     * Escape slashes and/or commas in the given value.
     *
     * @param value
     * @return
     */
    public static final String escapeValue(String value) {
        StringBuilder builder = null;

        int lastCopied = 0;
        int length = value.length();

        for ( int charNo = 0; charNo < length; charNo++ ) {
            char c = value.charAt(charNo);
            if ( (c != ',') && (c != '\\') ) {
                continue;
            }

            if ( builder == null ) {
                builder = new StringBuilder( 2 * length );
            }

            if ( lastCopied < charNo ) {
                builder.append(value, lastCopied, charNo);
            }
            builder.append('\\');
            builder.append(c);

            lastCopied = charNo + 1;
        }

        if ( builder == null ) {
            return value;
        } else {
            if ( lastCopied < length ) {
                builder.append(value, lastCopied, length);
            }
            return builder.toString();
        }
    }
}
// @formatter:on