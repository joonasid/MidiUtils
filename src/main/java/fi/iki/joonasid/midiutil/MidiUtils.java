package fi.iki.joonasid.midiutil;

/**
 * @author <a href="mailto:joonas.id@iki.fi">Joonas Id</a>
 */
public class MidiUtils {

    private static final char ZERO = '0';
    private static final char ONE = '1';

    public static String getHexByte(int value) {
        String hex = Integer.toHexString(value);
        if (hex.length() > 2) {
            throw new IllegalArgumentException("Decimal value " + value + " too large for 8-bit data");
        }
        return lpad(hex, 2, ZERO);
    }

    /**
     * Insert specified values into a binary value pattern, yielding a 8-bit hex value
     *
     * @param pattern the binary pattern, with a different mask char used for each inserted value
     * @param values  the values to insert (first value for first masked area, second value for second, etc)
     * @return the result of the values applied to the mask, as 8-bit (2-character) hex value
     */
    public static String getHexByte(String pattern, int... values) {
        String binary = getData(pattern, values);
        String hex = Integer.toString(Integer.parseInt(binary, 2), 16).toUpperCase();
        return lpad(hex, 2, ZERO);
    }

    /**
     * Insert specified values into a binary value pattern, yielding a binary value.
     *
     * @param pattern the binary pattern, with a different mask char used for each inserted value
     * @param values  the values to insert (first value for first masked area, second value for second, etc)
     * @return the result of the values applied to the mask
     */
    public static String getData(String pattern, int... values) {

        StringBuilder result = new StringBuilder(pattern.length());

        // go through the pattern, converting masked values to actual values on-the-go
        char[] chars = pattern.toCharArray();
        int currentMaskIndex = -1;
        StringBuilder currentMask = new StringBuilder(pattern.length());
        char prev = 0;
        boolean insideMask = false;

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c != prev && insideMask) {
                // finish previous mask before continuing with the next
                result.append(getBinaryValue(pattern, currentMask, values[currentMaskIndex]));

                // clean up residuals from the newly finished mask
                if (i < chars.length - 1) {
                    currentMask.delete(0, currentMask.length());
                }
            }

            if (c == ONE || c == ZERO) {
                result.append(c); // insert literal value as-is
                insideMask = false;

            } else {
                insideMask = true;
                if (c == prev) {
                    // current mask continues
                    currentMask.append(c);

                } else {
                    // new mask!

                    currentMaskIndex++;
                    if (currentMaskIndex > values.length) {
                        throw new IllegalArgumentException("No value specified for mask '" + c + "' starting at position "
                                + (i + 1) + " in pattern '" + pattern + "'");
                    }

                    currentMask.append(c);
                }
            }

            prev = c;
        }

        if (insideMask) {
            // the pattern ended with a mask, which yet needs to be finished
            result.append(getBinaryValue(pattern, currentMask, values[currentMaskIndex]));
        }

        int maskCount = currentMaskIndex + 1;
        if (maskCount != values.length) {
            throw new IllegalArgumentException("Too many values specified for pattern " + pattern + ": " + values.length);
        }

        return result.toString();
    }

    private static String getBinaryValue(String pattern, CharSequence mask, int decimalValue) {
        String binaryValue = Integer.toString(decimalValue, 2);
        if (binaryValue.length() > mask.length()) {
            int maxValue = (int) Math.pow(2, mask.length()) - 1;
            int startIndex = pattern.indexOf(mask.toString());
            throw new IllegalArgumentException("Invalid value for mask '" + mask + "' starting at position "
                    + (startIndex + 1) + " in pattern '" + pattern + "'; value (" + decimalValue
                    + "), value too large, max value for pattern: " + maxValue);
        }

        // actual value may be shorter than the masked part, pad with trailing zeroes to required length
        binaryValue = lpad(binaryValue, mask.length(), ZERO);

        // TODO debug
        // System.out.println("Converted value " + decimalValue + " for mask '" + mask + "' --> '" + binaryValue + "'");
        // end TODO
        return binaryValue;
    }

    private static String coalesce(String s) {
        return s != null ? "'" + s + "'" : s;
    }

    private static String lpad(String s, int length, char pad) {
        if (s == null || s.length() >= length) {
            return s;

        }

        StringBuilder result = new StringBuilder(length);
        int padSize = length - s.length();
        while (padSize > 0) {
            result.append(pad);
            padSize--;
        }
        result.append(s);
        return result.toString();
    }

    public static void main(String... args) {
        for (int i = 0; i < 15; i++) {
            String binary = lpad(Integer.toString(i, 2), 4, ZERO);
            assertEquals(getData(binary), binary); // no conversion
        }

        assertEquals("00", getData("0x", 0));
        assertEquals("01", getData("0x", 1));

        assertEquals("00", getData("xx", 0));
        assertEquals("01", getData("xx", 1));
        assertEquals("10", getData("xx", 2));
        assertEquals("11", getData("xx", 3));

        assertEquals("1100", getData("1x0y", 1, 0));
        assertEquals("11110000", getData("aaaabbbb", 15, 0));

        assertFails(IllegalArgumentException.class, "x", 2);
        assertFails(IllegalArgumentException.class, "0", 2);
    }

    private static void assertEquals(String expected, String actual) {
        if (expected == null && actual != null
                || expected != null && !expected.equals(actual)) {
            throw new AssertionError("Expected: " + coalesce(expected) + ", actual: " + coalesce(actual));
        }

        System.out.println("Passed: " + coalesce(actual));
    }

    static void assertFails(Class<? extends Throwable> expectedThrowable, String pattern, int... values) {
        try {
            getData(pattern, values);
            StringBuilder sb = new StringBuilder();
            sb.append("getData() with pattern ").append(coalesce(pattern)).append(" and values {");
            for (int i = 0; i < values.length; i++) {
                sb.append(" ").append(values[i]);
                if (i < values.length - 1) {
                    sb.append(",");
                }
            }
            sb.append(" } did not raise an exception");
            throw new AssertionError(sb.toString());

        } catch (Exception e) {
            if (!expectedThrowable.isAssignableFrom(e.getClass())) {
                throw new AssertionError("Wrong exception, expected: " + expectedThrowable.getName() + "; actual: " + e);
            }
        }
    }
}
