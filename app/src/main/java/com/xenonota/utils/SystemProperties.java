package com.xenonota.utils;

/**
 * Created by pvyparts on 1/25/16.
 */
public class SystemProperties {

    private static Class<?> CLASS;

    static {
        try {
            CLASS = Class.forName("android.os.SystemProperties");
        } catch (ClassNotFoundException e) {
        }
    }

    /** Get the value for the given key. */
    public static String get(String key) {
        try {
            return (String) CLASS.getMethod("get", String.class).invoke(null, key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the value for the given key.
     *
     * @return if the key isn't found, return def if it isn't null, or an empty string otherwise
     */
    public static String get(String key, String def) {
        try {
            return (String) CLASS.getMethod("get", String.class, String.class).invoke(null, key,
                    def);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * Get the value for the given key, and return as an integer.
     *
     * @param key
     *            the key to lookup
     * @param def
     *            a default value to return
     * @return the key parsed as an integer, or def if the key isn't found or cannot be parsed
     */
    public static int getInt(String key, int def) {
        try {
            return (Integer) CLASS.getMethod("getInt", String.class, int.class).invoke(null, key,
                    def);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * Get the value for the given key, and return as a long.
     *
     * @param key
     *            the key to lookup
     * @param def
     *            a default value to return
     * @return the key parsed as a long, or def if the key isn't found or cannot be parsed
     */
    public static long getLong(String key, long def) {
        try {
            return (Long) CLASS.getMethod("getLong", String.class, long.class).invoke(null, key,
                    def);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * Get the value for the given key, returned as a boolean. Values 'n', 'no', '0', 'false' or
     * 'off' are considered false. Values 'y', 'yes', '1', 'true' or 'on' are considered true. (case
     * sensitive). If the key does not exist, or has any other value, then the default result is
     * returned.
     *
     * @param key
     *            the key to lookup
     * @param def
     *            a default value to return
     * @return the key parsed as a boolean, or def if the key isn't found or is not able to be
     *         parsed as a boolean.
     */
    public static boolean getBoolean(String key, boolean def) {
        try {
            return (Boolean) CLASS.getMethod("getBoolean", String.class, boolean.class).invoke(
                    null, key, def);
        } catch (Exception e) {
            return def;
        }
    }

    private SystemProperties() {

    }
}
