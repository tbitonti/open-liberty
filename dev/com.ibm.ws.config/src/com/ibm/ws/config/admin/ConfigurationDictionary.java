/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.config.admin;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.osgi.framework.Filter;

import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.wsspi.kernel.service.utils.OnErrorUtil.OnError;
import com.ibm.wsspi.kernel.service.utils.SerializableProtectedString;

//@formatter:off
@Trivial
public class ConfigurationDictionary extends Dictionary<String, Object> implements Serializable {
    private static final long serialVersionUID = 7966152868712543805L;

    /** Supported simple types. */
    private static final List<Class<?>> simpleTypes =
        Arrays.asList( String.class,
                       Integer.class, Long.class, Float.class, Double.class,
                       Byte.class, Short.class, Character.class, Boolean.class );

    /** Supported primitive array types. */
    private static final List<Class<?>> primitiveArrayTypes =
        Arrays.asList( long[].class, int[].class, short[].class,
                       char[].class, byte[].class,
                       double[].class, float[].class,
                       boolean[].class );

    /** Supported simple array types. */
    private static final List<Class<?>> simpleArrayTypes =
        Arrays.asList( String[].class,
                       Integer[].class, Long[].class, Float[].class, Double[].class,
                       Byte[].class, Short[].class, Character[].class, Boolean[].class );

    /** IBM extended types. */
    private static final List<Class<?>> extendedTypes =
        Arrays.asList( SerializableProtectedString.class, OnError.class );

    private static final Set<Class<?>> supportedTypes;

    static {
        supportedTypes = new HashSet<Class<?>>(
                        simpleTypes.size() +
                        primitiveArrayTypes.size() +
                        simpleArrayTypes.size() +
                        extendedTypes.size() );

        supportedTypes.addAll(simpleTypes);
        supportedTypes.addAll(primitiveArrayTypes);
        supportedTypes.addAll(simpleArrayTypes);
        supportedTypes.addAll(extendedTypes);
    }

    /**
     * Validate a candidate element value.
     *
     * The value must be of a supported type, or must be
     * a collection of values of simple types, or must be
     * a mapping of strings to simple types.
     *
     * @param value A candidate element value.
     *
     * @throws IllegalArgumentException Thrown if the value is not valid.
     */
    private static void validateValue(Object value) {
        Class<?> valueClass = value.getClass();

        if ( supportedTypes.contains(valueClass) ) {
            return;
        }

        if ( value instanceof Collection<?> ) {
            for ( Object elementValue : (Collection<?>) value ) {
                Class<?> elementClass = elementValue.getClass();
                if ( !simpleTypes.contains(elementClass) ) {
                    throw new IllegalArgumentException( elementClass.getName() + " in " + valueClass.getName() );
                }
            }
            return;
        }

        if ( value instanceof Map ) {
            ((Map<?, ?>) value).forEach( (key, element) -> {
                Class<?> keyClass = key.getClass();
                if (keyClass != String.class) {
                    throw new IllegalArgumentException( keyClass.getName() + " in " + valueClass.getName() );
                }

                Class<?> elementClass = element.getClass();
                if (!simpleTypes.contains(elementClass)) {
                    throw new IllegalArgumentException( elementClass.getName() + " in " + valueClass.getName() );
                }
            });
            return;
        }

        throw new IllegalArgumentException( valueClass.getName() );
    }

    //

    static final Comparator<String> CASE_INSENSITIVE = new CaseInsensitive();

    @Trivial
    static class CaseInsensitive implements Comparator<String>, Serializable {
        private static final long serialVersionUID = 7962325242424955159L;

        @Override
        public int compare(String s1, String s2) {
            if ( s1 == s2 ) {
                return 0;
            }
            return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
        }
    };

    //

    public ConfigurationDictionary() {
        // EMPTY
    }

    protected final Map<String, Object> properties =
        Collections.synchronizedMap(new TreeMap<String, Object>(CASE_INSENSITIVE));

    @Override
    public Object get(Object key) {
        return properties.get(key);
    }

    /**
     * Add a value to this dictionary.
     *
     * The value must be a valid configuration value.
     * See {@link #validateValue(Object)}.
     *
     * @param key The key for the value.
     * @param value The value which is to be stored.
     *
     * @return The value previously stored under the
     *     specified key.  Null if no value was previously
     *     stored.
     *
     * @throws IllegalArgumentException Thrown if the key or
     *     the value is null, or if the value is not valid.
     */
    @Override
    public Object put(String key, Object value) {
        if ( (key == null) || (value == null) ) {
            throw new NullPointerException();
        }
        validateValue(value); // throws IllegalArgumentException

        return properties.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return properties.remove(key);
    }

    //

    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public int size() {
        return properties.size();
    }

    @Override
    public Enumeration<String> keys() {
        return new KeysEnumeration<String>();
    }

    @Override
    public Enumeration<Object> elements() {
        return new ValuesEnumeration<Object>();
    }

    public boolean matches(Filter filter) {
        return filter.matches(properties);
    }

    //

    @Trivial
    private class KeysEnumeration<T> implements Enumeration<String> {
        Iterator<String> iterator = properties.keySet().iterator();

        @Override
        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        @Override
        public String nextElement() {
            return iterator.next();
        }
    }

    @Trivial
    private class ValuesEnumeration<T> implements Enumeration<Object> {
        private final Iterator<Object> iterator = properties.values().iterator();

        @Override
        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        @Override
        public Object nextElement() {
            return iterator.next();
        }
    }

    /**
     * Copy (clone) this dictionary.  Copy array and vector
     * values.  Simply place all other value types.
     *
     * Note: Non-vector collection types, including mappings,
     * are simply placed.  This is probably an oversight.  See
     * {@link #validateValue(Object)}.
     *
     * @return The copied dictionary.
     */
    public ConfigurationDictionary copy() {
        ConfigurationDictionary result = new ConfigurationDictionary();

        // TODO: Should this have cases for Collection and Map?
        //       'validateValue' allows both.

        properties.forEach( (key, value) -> {
            Object copyValue;
            if ( value.getClass().isArray() ) {
                copyValue = copyArray(value);
            } else if (value instanceof Vector) {
                copyValue = ((Vector<?>) value).clone();
            } else {
                copyValue = value;
            }
            // Bypass the validating 'put': The copied value
            // must be valid since it is a copy of a valid value.
            result.properties.put(key, copyValue);
        });
        return result;
    }

    /**
     * Copy an array typed value.
     *
     * Per {@link #validateValue(Object)}, the array element type must be a
     * simple value, and can be safely reused without being copied.
     *
     * @param value An array typed value.
     *
     * @return A copy of the value.
     */
    private Object copyArray(Object value) {
        int arrayLength = Array.getLength(value);
        Object copyOfArray = Array.newInstance(value.getClass().getComponentType(), arrayLength);
        System.arraycopy(value, 0, copyOfArray, 0, arrayLength);
        return copyOfArray;
    }

    /**
     * Print the contents of this dictionary.
     *
     * The format is:
     * <code>
     *   {key=value, key=value, ...}
     * </code>
     *
     * @return A print string for this configuration dictionary.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('{');

        properties.forEach( (key, value) -> {
            if ( builder.length() > 1 ) {
                builder.append(", ");
            }

            builder.append(key);
            builder.append('=');

            if ( (value == null) || !value.getClass().isArray() ) {
                builder.append(value);
            } else {
                appendArray(builder, value);
            }
        });

        return builder.append('}').toString();
    }

    /**
     * Append an array value to a string builder.
     *
     * The value format is:
     * <code>
     *     simpleName[]{ v0, v1, ... }
     * </code>
     *
     * The simple type name is the unqualified array component type
     * name.  For example, <code>Integer</code> for
     * <code>java.lang.Integer</code>.
     *
     * Nested arrays are not supported: The displayed array elements
     * are never nested arrays.
     *
     * @param builder A string builder.
     * @param value An array value.
     */
    private void appendArray(StringBuilder builder, Object value) {
        String name = value.getClass().getComponentType().getName();
        builder.append(name, name.lastIndexOf('.') + 1, name.length());
        builder.append("[]");

        builder.append('{');
        for ( int i = 0, length = Array.getLength(value); i < length; i++ ) {
            if ( i != 0 ) {
                builder.append(", ");
            }
            builder.append( Array.get(value, i) );
        }
        builder.append('}');
    }
}
//@formatter:on