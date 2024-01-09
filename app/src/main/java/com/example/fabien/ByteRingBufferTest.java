

package com.example.fabien;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

public class ByteRingBufferTest {
    @Test
    public void testByteRingBufferConstructor() {
        ByteRingBuffer buffer = new ByteRingBuffer(5);
        assertNotNull(buffer);
        assertEquals(0, buffer.bytesToRead());
    }
    @Test
    public void testPutAndGetBytes() throws BufferOverflowException, BufferUnderflowException {
        ByteRingBuffer buffer = new ByteRingBuffer(5);

        buffer.put((byte) 1);
        assertEquals(1, buffer.bytesToRead());

        byte[] byteArray = {2, 3, 4};
        buffer.put(byteArray);
        assertEquals(4, buffer.bytesToRead());

        assertEquals(1, buffer.get());
        byte[] allBytes = buffer.getAll();
        assertArrayEquals(new byte[]{2, 3, 4}, allBytes);
        assertEquals(0, buffer.bytesToRead());
    }
    @Test(expected = BufferOverflowException.class)
    public void testBufferOverflow() throws BufferOverflowException {
        ByteRingBuffer buffer = new ByteRingBuffer(2);
        buffer.put((byte) 1);
        buffer.put((byte) 2);
        // The next put should throw BufferOverflowException
        buffer.put((byte) 3);
    }

    @Test(expected = BufferUnderflowException.class)
    public void testBufferUnderflow() throws BufferUnderflowException {
        ByteRingBuffer buffer = new ByteRingBuffer(2);
        // The next get should throw BufferUnderflowException
        buffer.get();
    }

    @Test
    public void testPartiallyFilledBuffer() throws BufferOverflowException, BufferUnderflowException {
        ByteRingBuffer buffer = new ByteRingBuffer(3);
        buffer.put((byte) 1);
        buffer.put((byte) 2);
        assertEquals(2, buffer.bytesToRead());
        assertEquals(1, buffer.get());
        assertEquals(1, buffer.bytesToRead());
    }
    @Test
    public void testPutSingleByte() throws BufferOverflowException {
        ByteRingBuffer buffer = new ByteRingBuffer(3);
        buffer.put((byte) 1);
        assertEquals(1, buffer.bytesToRead());
        assertEquals(1, buffer.get());
        assertEquals(0, buffer.bytesToRead());
    }
    @Test
    public void testPutArrayWithWrapAround() throws BufferOverflowException, BufferUnderflowException {
        ByteRingBuffer buffer = new ByteRingBuffer(3);

        byte[] byteArray = {1, 2, 3};
        buffer.put(byteArray);
        assertEquals(3, buffer.bytesToRead());

        byte[] allBytes = buffer.getAll();
        assertArrayEquals(new byte[]{1, 2, 3}, allBytes);
        assertEquals(0, buffer.bytesToRead());

        byte[] newArray = {4, 5, 6};
        buffer.put(newArray);
        assertEquals(3, buffer.bytesToRead());

        byte[] allBytesAfterWrapAround = buffer.getAll();
        assertArrayEquals(new byte[]{4, 5, 6}, allBytesAfterWrapAround);
        assertEquals(0, buffer.bytesToRead());
    }

}
