package org.pac4j.core.util;

import lombok.val;
import org.pac4j.core.exception.TechnicalException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class gathers all the utilities methods.
 *
 * @author Jerome Leleu
 * @since 1.4.0
 */
public final class CommonHelper {

    /**
     * Return if the String is not blank.
     *
     * @param s string
     * @return if the String is not blank
     */
    public static boolean isNotBlank(final String s) {
        return s != null && !s.isBlank();
    }

    /**
     * Return if the String is blank.
     *
     * @param s string
     * @return if the String is blank
     */
    public static boolean isBlank(final String s) {
        return !isNotBlank(s);
    }

    /**
     * Compare two String to see if they are equals (both null is ok).
     *
     * @param s1 string
     * @param s2 string
     * @return if two String are equals
     */
    public static boolean areEquals(final String s1, final String s2) {
        return s1 == null ? s2 == null : s1.equals(s2);
    }

    /**
     * Compare two String to see if they are equals ignoring the case and the blank spaces (both null is ok).
     *
     * @param s1 string
     * @param s2 string
     * @return if two String are equals ignoring the case and the blank spaces
     */
    public static boolean areEqualsIgnoreCaseAndTrim(final String s1, final String s2) {
        if (s1 == null && s2 == null) {
            return true;
        } else {
            return s1 != null && s2 != null && s1.trim().equalsIgnoreCase(s2.trim());
        }
    }

    /**
     * Compare two String to see if they are not equals.
     *
     * @param s1 string
     * @param s2 string
     * @return if two String are not equals
     */
    public static boolean areNotEquals(final String s1, final String s2) {
        return !areEquals(s1, s2);
    }

    /**
     * Return if a collection is empty.
     *
     * @param coll a collection
     * @return whether it is empty
     */
    public static boolean isEmpty(final Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    /**
     * Return if a collection is not empty.
     *
     * @param coll a collection
     * @return whether it is not empty
     */
    public static boolean isNotEmpty(final Collection<?> coll) {
        return !isEmpty(coll);
    }

    /**
     * Verify that a boolean is true otherwise throw a {@link TechnicalException}.
     *
     * @param value   the value to be checked for truth
     * @param message the message to include in the exception if the value is false
     */
    public static void assertTrue(final boolean value, final String message) {
        if (!value) {
            throw new TechnicalException(message);
        }
    }

    /**
     * Verify that a String is not blank otherwise throw a {@link TechnicalException}.
     *
     * @param name  name if the string
     * @param value value of the string
     * @param msg   an expanatory message
     */
    public static void assertNotBlank(final String name, final String value, final String msg) {
        assertTrue(!isBlank(value), name + " cannot be blank" + (msg != null ? ": " + msg : Pac4jConstants.EMPTY_STRING));
    }

    /**
     * Verify that a String is not blank otherwise throw a {@link TechnicalException}.
     *
     * @param name  name if the string
     * @param value value of the string
     */
    public static void assertNotBlank(final String name, final String value) {
        assertNotBlank(name, value, null);
    }

    /**
     * Verify that an Object is not <code>null</code> otherwise throw a {@link TechnicalException}.
     *
     * @param name name of the object
     * @param obj  object
     */
    public static void assertNotNull(final String name, final Object obj) {
        assertTrue(obj != null, name + " cannot be null");
    }

    /**
     * Verify that an Object is <code>null</code> otherwise throw a {@link TechnicalException}.
     *
     * @param name name of the object
     * @param obj  object
     */
    public static void assertNull(final String name, final Object obj) {
        assertTrue(obj == null, name + " must be null");
    }

    /**
     * Add a new parameter to an url.
     *
     * @param url   url
     * @param name  name of the parameter
     * @param value value of the parameter
     * @return the new url with the parameter appended
     */
    public static String addParameter(final String url, final String name, final String value) {
        if (url != null) {
            val sb = new StringBuilder();
            sb.append(url);
            if (name != null) {
                if (url.indexOf("?") >= 0) {
                    sb.append("&");
                } else {
                    sb.append("?");
                }
                sb.append(name);
                sb.append("=");
                if (value != null) {
                    sb.append(urlEncode(value));
                }
            }
            return sb.toString();
        }
        return null;
    }

    /**
     * URL encode a text using UTF-8.
     *
     * @param text text to encode
     * @return the encoded text
     */
    public static String urlEncode(final String text) {
        try {
            return URLEncoder.encode(text, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            val message = "Unable to encode text : " + text;
            throw new TechnicalException(message, e);
        }
    }

    /**
     * Return a random string of a certain size.
     *
     * @param size the size
     * @return the random size
     */
    public static String randomString(final int size) {
        val builder = new StringBuilder();
        while (builder.length() < size) {
            val suffix = java.util.UUID.randomUUID().toString().replace("-", Pac4jConstants.EMPTY_STRING);
            builder.append(suffix);
        }
        return builder.substring(0, size);
    }

    /**
     * Copy a date.
     *
     * @param original original date
     * @return date copy
     */
    public static Date newDate(final Date original) {
        return original != null ? new Date(original.getTime()) : null;
    }

    /**
     * Convert a string into an URI.
     *
     * @param s the string
     * @return the URI
     */
    public static URI asURI(final String s) {
        try {
            return new URI(s);
        } catch (final URISyntaxException e) {
            throw new TechnicalException("Cannot make an URI from: " + s, e);
        }
    }

    /**
     * Taken from commons-lang3
     */

    private static final int INDEX_NOT_FOUND = -1;

    public static String substringBetween(final String str, final String open, final String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        val start = str.indexOf(open);
        if (start != INDEX_NOT_FOUND) {
            val end = str.indexOf(close, start + open.length());
            if (end != INDEX_NOT_FOUND) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    public static String substringAfter(final String str, final String separator) {
        if (isEmpty(str)) {
            return str;
        }
        if (separator == null) {
            return Pac4jConstants.EMPTY_STRING;
        }
        val pos = str.indexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return Pac4jConstants.EMPTY_STRING;
        }
        return str.substring(pos + separator.length());
    }

    public static String substringBefore(final String str, final String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.length() == 0) {
            return Pac4jConstants.EMPTY_STRING;
        }
        val pos = str.indexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    private static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private static final Map<String, Constructor> constructorsCache = new HashMap<>();

    /**
     * Get the constructor of the class.
     *
     * @param name the name of the class
     * @return the constructor
     * @throws ClassNotFoundException class not found
     * @throws NoSuchMethodException  method not found
     */
    public static Constructor getConstructor(final String name) throws ClassNotFoundException, NoSuchMethodException {
        var constructor = constructorsCache.get(name);
        if (constructor == null) {
            synchronized (constructorsCache) {
                constructor = constructorsCache.get(name);
                if (constructor == null) {
                    Class<?> clazz;
                    try {
                        clazz = Class.forName(name, true, CommonHelper.class.getClassLoader());
                    } catch (final ClassNotFoundException e) {
                        val tccl = Thread.currentThread().getContextClassLoader();
                        if (tccl == null) {
                            clazz = Class.forName(name);
                        } else {
                            clazz = Class.forName(name, true, tccl);
                        }
                    }

                    constructor = clazz.getDeclaredConstructor();
                    constructorsCache.put(name, constructor);
                }
            }
        }

        return constructor;
    }

    public static String ifBlank(final String value, final String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }
}
