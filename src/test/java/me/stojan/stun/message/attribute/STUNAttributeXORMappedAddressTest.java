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

package me.stojan.stun.message.attribute;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by vuk on 08/11/16.
 */
public class STUNAttributeXORMappedAddressTest {

    @Test(expected = UnsupportedOperationException.class)
    public void noInstance() {
        new STUNAttributeXORMappedAddress();
    }

    @Test
    public void correctRFCType() {
        assertEquals(0x0020, STUNAttributeXORMappedAddress.TYPE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void lessThanIPv4Address() throws Exception {
        STUNAttributeXORMappedAddress.value(new byte[20], new byte[3], -1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void lessThanIPv6Address() throws Exception {
        STUNAttributeXORMappedAddress.value(new byte[20], new byte[8], -1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void overThanIPv6Address() throws Exception {
        STUNAttributeXORMappedAddress.value(new byte[20], new byte[17], -1);
    }

    @Test
    public void ipv4XORWithMagicCookieAndTransactionId() throws Exception {
        final byte[] header = new byte[20];

        for (int i = 4; i < header.length; i++) {
            header[i] = (byte) i;
        }

        final byte[] addr = new byte[] { (byte) 192, (byte) 168, 3, (byte) 234 };

        final int port = 0b1010_1010_1010_1010;

        final byte[] attribute = STUNAttributeXORMappedAddress.value(header, addr, port);

        // 0 padding
        assertEquals(0, attribute[0]);

        // IPv4
        assertEquals(STUNAttributeXORMappedAddress.ADDRESS_IPV4, attribute[1]);

        // port
        assertEquals((byte) ((port >> 8) & 255), (byte) (attribute[2] ^ header[4]));
        assertEquals((byte) (port & 255), (byte) (attribute[3] ^ header[5]));

        // address
        for (int i = 0; i < addr.length; i++) {
            assertEquals(addr[i], attribute[4 + i] ^ header[4 + i]);
        }
    }

    @Test
    public void ipv6XORWithMagicCookieAndTransactionId() throws Exception {
        final byte[] header = new byte[20];

        for (int i = 4; i < header.length; i++) {
            header[i] = (byte) i;
        }

        final byte[] addr = new byte[] { 0x20, 0x01, 0x0d, (byte) 0xb8, (byte) 0x85, (byte) 0xa3, 0x08, (byte) 0xd3, 0x13, 0x19, (byte) 0x8a, 0x2e, 0x03, 0x70, 0x73, 0x48 };

        final int port = 0b1010_1010_1010_1010;

        final byte[] attribute = STUNAttributeXORMappedAddress.value(header, addr, port);

        // 0 padding
        assertEquals(0, attribute[0]);

        // IPv6
        assertEquals(STUNAttributeMappedAddress.ADDRESS_IPV6, attribute[1]);

        // port
        assertEquals((byte) (port >> 8), attribute[2] ^ header[4]);
        assertEquals((byte) (port & 255), attribute[3] ^ header[5]);

        // address
        for (int i = 0; i < addr.length; i++) {
            assertEquals(addr[i], attribute[4 + i] ^ header[4 + i]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkHeaderNull() throws Exception {
        STUNAttributeXORMappedAddress.checkHeader(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkHeaderLessThan20BytesLong() throws Exception {
        STUNAttributeXORMappedAddress.checkHeader(new byte[19]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkHeaderMoreThan20BytesLong() throws Exception {
        STUNAttributeXORMappedAddress.checkHeader(new byte[21]);
    }

    @Test
    public void checkHeader20BytesLong() throws Exception {
        STUNAttributeXORMappedAddress.checkHeader(new byte[20]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkHeaderDoesNotStartWiht00Bits() throws Exception {
        final byte[] bytes = new byte[20];

        bytes[0] = (byte) 0b1100_0000;

        STUNAttributeXORMappedAddress.checkHeader(bytes);
    }

    @Test
    public void extractPort() throws Exception {
        final int port = 0b1010_1010_1010_1010;
        final byte[] header = new byte[20];

        final byte[] attribute = STUNAttributeXORMappedAddress.value(header, new byte[4], port);

        assertEquals(port, STUNAttributeXORMappedAddress.port(header, attribute));
    }

    @Test
    public void extractIPV4Address() throws Exception {
        final byte[] header = new byte[20];

        for (int i = 4; i < 8; i++) {
            header[i] = (byte) i;
        }

        final byte[] address = new byte[] { (byte) 192, (byte) 168, (byte) 3, (byte) 254 };

        final byte[] attribute = STUNAttributeXORMappedAddress.value(header, address, 0);

        assertTrue(Arrays.equals(address, STUNAttributeXORMappedAddress.address(header, attribute)));
    }

    @Test
    public void extractIPV6Address() throws Exception {
        final byte[] header = new byte[20];

        for (int i = 4; i < (4 + 16); i++) {
            header[i] = (byte) i;
        }

        final byte[] address = new byte[] { 0x20, 0x01, 0x0d, (byte) 0xb8, (byte) 0x85, (byte) 0xa3, 0x08, (byte) 0xd3, 0x13, 0x19, (byte) 0x8a, 0x2e, 0x03, 0x70, 0x73, 0x48 };

        final byte[] attribute = STUNAttributeXORMappedAddress.value(header, address, 0);

        assertTrue(Arrays.equals(address, STUNAttributeXORMappedAddress.address(header, attribute)));
    }

    @Test(expected = InvalidSTUNAttributeException.class)
    public void wrongAddressType() throws Exception {
        final byte[] header = new byte[20];
        final byte[] address = new byte[] { (byte) 192, (byte) 168, (byte) 3, (byte) 254 };

        final byte[] attribute = STUNAttributeXORMappedAddress.value(header, address, 0);

        attribute[1] = (byte) 192;

        STUNAttributeXORMappedAddress.address(header, attribute);
    }

    @Test(expected = InvalidSTUNAttributeException.class)
    public void wrongAddressLength() throws Exception {
        final byte[] header = new byte[20];
        final byte[] address = new byte[] { (byte) 192, (byte) 168, (byte) 3, (byte) 254 };

        final byte[] attribute = STUNAttributeXORMappedAddress.value(header, address, 0);

        final byte[] wrongAttribute = new byte[attribute.length + 4];

        System.arraycopy(attribute, 0, wrongAttribute, 0, attribute.length);

        STUNAttributeXORMappedAddress.address(header, wrongAttribute);
    }
}
