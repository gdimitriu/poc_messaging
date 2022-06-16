/**
 * fixed length algorithm
 */
package mqtt;

import java.util.Iterator;
import java.util.Vector;

public class VariableLengthEncoding {
    public static void main(String[] args) {
        int value = 321;
        // Encode it
        Vector<Integer> digits = encodeValue(value);
        System.out.println(value + " encodes to "  + digits);
        // Derive original from encoded digits
        int value1 = decodeValue(digits);
        System.out.println("Original value was " + value1);
    }

    public static Vector encodeValue(int value) {
        Vector<Integer> digits = new Vector();
        do {
            int digit = value % 128;
            value = value / 128;

            // if there are more digits to encode
            // then set the top bit of this digit
            if ( value > 0 ) {
                digit = digit | 0x80;
            }
            digits.add(digit);
        }
        while ( value > 0 );
        return digits;
    }

    public static int decodeValue(Vector<Integer> digits) {
        int value = 0;
        int multiplier = 1;
        Iterator<Integer> iter = digits.iterator();
        while ( iter.hasNext() ) {
            int digit = iter.next();
            value += (digit & 127) * multiplier;
            multiplier *= 128;
        }
        return value;
    }
}