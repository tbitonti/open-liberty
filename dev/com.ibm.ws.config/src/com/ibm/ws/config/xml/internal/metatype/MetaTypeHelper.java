/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.ws.config.xml.internal.metatype;

import java.util.ArrayList;
import java.util.List;

import com.ibm.websphere.ras.annotation.Trivial;

//@formatter:off
public class MetaTypeHelper {
    /**
     * Parse a list of configuration values.
     *
     * Special characters are commas (element separators), backward
     * slashes (escape characters), and whitespace.
     *
     * A comma which is escaped is treated as a regular character.
     *
     * Whitespace at the beginning or at the end of a value is ignored.
     *
     * An empty string is parsed as a singleton list containing an empty
     * string.  A trailing comma is parsed as having an empty string
     * following the comma.  Adjacent commas are permitted and are parsed
     * as an empty string between the commas.
     *
     * @param value The value which is to be parsed.
     *
     * @return The value parsed into a list.
     */
    @Trivial
    public static final List<String> parseValue(String value) {
        List<String> values = new ArrayList<String>();

        int length = value.length();

        StringBuilder builder = new StringBuilder(length);

        for ( int charNo = 0; charNo < length; charNo++ ) {
            char ch = value.charAt(charNo);

            if ( ch == ',' ) {
                values.add(builder.toString());
                builder.setLength(0);

            } else if ( ch == '\\') {
                if ( charNo + 1 < length ) {
                    builder.append( value.charAt(++charNo) ); // add next
                } else {
                    // last character - ignore
                }

            } else if ( Character.isWhitespace(ch) ) {
                if ( builder.length() == 0 ) {
                    continue; // Leading ... ignore
                }
                charNo += consumeWhitespace(value, charNo, builder) - 1;

            } else {
                builder.append(ch);
            }
        }
        values.add( builder.toString() );
        return values;
    }

    @Trivial
    private static int consumeWhitespace(String value, int start, StringBuilder builder) {
        int whitespace = 0;
        for ( int charNo = start, length = value.length(); charNo < length; charNo++ ) {
            char ch = value.charAt(charNo);

            if ( Character.isWhitespace(ch) ) {
                whitespace++; // Accumulate ...
                continue;

            } else {
                if ( ch != ',' ) {
                    // Interior white space ... copy it ...
                    for ( int spaceNo = 0; spaceNo < whitespace; spaceNo++ ) {
                        builder.append( value.charAt(start + spaceNo) );
                    }
                } else {
                    // Trailing ... discard the accumulation ...
                }
                break; // ... then resume in the outer loop.
            }
        }

        return whitespace;
    }

    /**
     * Escape a single value.
     *
     * <code>parseValue(escapeValue(s)).get(0).equals(s)</code>
     * should always be true.
     *
     * @param value The value which is to be escaped.
     *
     * @return The value, escaped.
     *
     */
    @Trivial
    public static String escapeValue(String value) {
        int length = value.length();

        // Quick check one: An empty string has nothing to escape.

        if ( length == 0 ) {
            return value;
        }

        // Quick check two: Return as-is a string with no escapable
        // content.  That is, with no leading whitespace, no trailing
        // whitespace, and no special characters.

        boolean hasSpecial =
            ( Character.isWhitespace( value.charAt(0) ) ||
              Character.isWhitespace( value.charAt(length - 1) ) );

        if ( !hasSpecial) {
            for ( int charNo = 0; charNo < length; charNo++ ) {
                char c = value.charAt(charNo);
                if ( (c == ',') || (c == '\\') ) {
                    hasSpecial = true;
                    break;
                }
            }
        }

        if ( !hasSpecial ) {
            return value;
        }

        // Conservative allocation: Assume every character is escaped.
        StringBuilder builder = new StringBuilder( length * 2 );

        int begin = 0;
        int end = length;

        // Escape leading whitespace.
        char c;
        while ( (begin < end) && Character.isWhitespace(c = value.charAt(begin)) ) {
            builder.append('\\');
            builder.append(c);
            begin++;
        }

        // Demarcate trailing whitespace.
        while ( (end > begin) && Character.isWhitespace(value.charAt(end - 1)) ) {
            end--;
        }

        // Escape embedded special characters.
        //
        // Optimized use of append: Assume that escape characters are sparse.
        // The trade-off is between creating substrings while having fewer
        // append calls, versus between not creating a substring but invoking
        // append on every character.

        for ( int charNo = begin; charNo < end; charNo++ ) {
            c = value.charAt(charNo);
            if ( (c == '\\') || (c == ',') ) {
                builder.append(value, begin, charNo);
                builder.append('\\');
                begin = charNo;
            }
        }
        builder.append(value, begin, end);

        // Escape trailing whitespace.
        for ( int charNo = end; charNo < length; charNo++ ) {
            builder.append('\\');
            builder.append( value.charAt(charNo) );
        }

        return builder.toString();
    }
 // @formatter:on
}
