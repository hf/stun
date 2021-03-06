/*
 * Copyright (c) 2016 Stojan Dimitrovski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.stojan.stun.message;

/**
 * Defines a STUN Type-Length-Value.
 */
public final class STUNTypeLengthValue {

    /**
     * Create a value from the provided type and value.
     * @param type the type
     * @param value the value
     * @return the TLV bytes, never null
     */
    public static byte[] value(int type, byte[] value) {
        final int paddingLength;

        if (0 == value.length % 4) {
            paddingLength = value.length;
        } else {
            paddingLength = value.length + (4 - (value.length % 4));
        }

        final byte[] raw = new byte[4 + paddingLength];

        raw[0] = (byte) ((type >> 8) & 255);
        raw[1] = (byte) (type & 255);
        raw[2] = (byte) ((value.length >> 8) & 255);
        raw[3] = (byte) (value.length & 255);

        System.arraycopy(value, 0, raw, 4, value.length);

        return raw;
    }

    STUNTypeLengthValue() {
        throw new UnsupportedOperationException();
    }
}
