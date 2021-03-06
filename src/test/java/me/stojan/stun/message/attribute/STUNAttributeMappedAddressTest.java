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
 * Created by vuk on 06/11/16.
 */
public class STUNAttributeMappedAddressTest {

    @Test(expected = UnsupportedOperationException.class)
    public void noInstances() {
        new STUNAttributeMappedAddress();
    }

    @Test
    public void correctRFCType() {
        assertEquals(0x0001, STUNAttributeMappedAddress.TYPE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void lessThanIPv4Address() throws Exception {
        STUNAttributeMappedAddress.value(new byte[3], -1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void lessThanIPv6Address() throws Exception {
        STUNAttributeMappedAddress.value(new byte[8], -1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void overThanIPv6Address() throws Exception {
        STUNAttributeMappedAddress.value(new byte[17], -1);
    }

    @Test
    public void ipv4Address() throws Exception {
        final byte[] addr = new byte[] { (byte) 192, (byte) 168, (byte) 1, (byte) 123 };

        final byte[] attribute = STUNAttributeMappedAddress.value(addr, -1);

        assertEquals(8, attribute.length);

        // zero-padding
        assertEquals((byte) 0, attribute[0]);

        // IPv4 address type
        assertEquals(STUNAttributeXORMappedAddress.ADDRESS_IPV4, attribute[1]);

        // port
        assertEquals((byte) 255, attribute[2]);
        assertEquals((byte) 255, attribute[3]);

        // ipv4 address
        assertEquals((byte) 192, attribute[4]);
        assertEquals((byte) 168, attribute[5]);
        assertEquals((byte) 1, attribute[6]);
        assertEquals((byte) 123, attribute[7]);
    }

    @Test
    public void ipv6Address() throws Exception {
        final byte[] addr = new byte[] { 0x20, 0x01, 0x0d, (byte) 0xb8, (byte) 0x85, (byte) 0xa3, 0x08, (byte) 0xd3, 0x13, 0x19, (byte) 0x8a, 0x2e, 0x03, 0x70, 0x73, 0x48 };

        final byte[] attribute = STUNAttributeMappedAddress.value(addr, 123);

        assertEquals(20, attribute.length);

        // 0 padding
        assertEquals((byte) 0, attribute[0]);

        // ipv6 address
        assertEquals(STUNAttributeXORMappedAddress.ADDRESS_IPV6, attribute[1]);

        // port
        assertEquals((byte) 0, attribute[2]);
        assertEquals((byte) 123, attribute[3]);

        for (int i = 0; i < addr.length; i++) {
            assertEquals(addr[i], attribute[4 + i]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullAttribute() throws Exception {
        STUNAttributeMappedAddress.checkAttribute(null);
    }

    @Test(expected = InvalidSTUNAttributeException.class)
    public void shortAttribute() throws Exception {
        STUNAttributeMappedAddress.checkAttribute(new byte[0]);
    }

    @Test(expected = InvalidSTUNAttributeException.class)
    public void nonZeroFirstByte() throws Exception {
        STUNAttributeMappedAddress.checkAttribute(new byte[] { 1, 0, 0, 0 });
    }

    @Test
    public void extractPort() throws Exception {
        final int port = 0b1010_1010_1010_1010;

        final byte[] attribute = STUNAttributeMappedAddress.value(new byte[] { (byte) 192, (byte) 168, 0, 1 }, port);

        assertEquals(port, STUNAttributeMappedAddress.port(attribute));
    }

    @Test
    public void extractIPV4Address() throws Exception {
        final byte[] address = new byte[] { (byte) 192, (byte) 168, 7, (byte) 254};

        final byte[] attribute = STUNAttributeMappedAddress.value(address, 0);

        assertTrue(Arrays.equals(address, STUNAttributeMappedAddress.address(attribute)));
    }

    @Test
    public void extractIPV6Address() throws Exception {
        final byte[] address = new byte[] { 0x20, 0x01, 0x0d, (byte) 0xb8, (byte) 0x85, (byte) 0xa3, 0x08, (byte) 0xd3, 0x13, 0x19, (byte) 0x8a, 0x2e, 0x03, 0x70, 0x73, 0x48 };

        final byte[] attribute = STUNAttributeMappedAddress.value(address, 0);

        assertTrue(Arrays.equals(address, STUNAttributeMappedAddress.address(attribute)));
    }

    @Test(expected = InvalidSTUNAttributeException.class)
    public void wrongAddressType() throws Exception {
        final byte[] address = new byte[] { (byte) 192, (byte) 168, (byte) 3, (byte) 254 };

        final byte[] attribute = STUNAttributeMappedAddress.value(address, 0);

        attribute[1] = (byte) 192;

        STUNAttributeMappedAddress.address(attribute);
    }

    @Test(expected = InvalidSTUNAttributeException.class)
    public void wrongAddressLength() throws Exception {
        final byte[] address = new byte[] { (byte) 192, (byte) 168, (byte) 3, (byte) 254 };

        final byte[] attribute = STUNAttributeMappedAddress.value(address, 0);

        final byte[] wrongAttribute = new byte[attribute.length + 4];

        System.arraycopy(attribute, 0, wrongAttribute, 0, attribute.length);

        STUNAttributeMappedAddress.address(wrongAttribute);
    }

    @Test(expected = InvalidSTUNAttributeException.class)
    public void lengthNotMultipleOf4() throws Exception {
        STUNAttributeMappedAddress.checkAttribute(new byte[15]);
    }
}
