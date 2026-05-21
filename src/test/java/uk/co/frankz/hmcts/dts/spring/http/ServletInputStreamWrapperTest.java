package uk.co.frankz.hmcts.dts.spring.http;

import jakarta.servlet.ReadListener;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServletInputStreamWrapperTest {

    @Test
    void readSingleBytesCorrectly() throws IOException {
        byte[] data = "abc".getBytes(StandardCharsets.UTF_8);
        try (ServletInputStreamWrapper wrapper = new ServletInputStreamWrapper(data)) {

            assertEquals('a', wrapper.read());
            assertEquals('b', wrapper.read());
            assertEquals('c', wrapper.read());
            assertEquals(-1, wrapper.read()); // end of stream
        }
    }

    @Test
    void readIntoBufferCorrectly() throws IOException {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        byte[] buffer;
        int read;
        try (ServletInputStreamWrapper wrapper = new ServletInputStreamWrapper(data)) {

            buffer = new byte[5];
            read = wrapper.read(buffer, 0, 5);
        }

        assertEquals(5, read);
        assertArrayEquals(data, buffer);
    }

    @Test
    void isFinishedReflectsBufferState() throws IOException {
        byte[] data = "x".getBytes(StandardCharsets.UTF_8);
        try (ServletInputStreamWrapper wrapper = new ServletInputStreamWrapper(data)) {

            assertFalse(wrapper.isFinished());
            assertEquals('x', wrapper.read());
            assertTrue(wrapper.isFinished());
            assertEquals(-1, wrapper.read());
            assertTrue(wrapper.isFinished());
        }
    }

    @Test
    void isReadyAlwaysTrue() throws IOException {
        try (ServletInputStreamWrapper wrapper = new ServletInputStreamWrapper(new byte[]{1, 2, 3})) {
            assertTrue(wrapper.isReady());
        }
    }

    @Test
    void setReadListenerDoesNotThrow() throws IOException {
        try (ServletInputStreamWrapper wrapper = new ServletInputStreamWrapper(new byte[]{1, 2, 3})) {
            assertDoesNotThrow(() -> wrapper.setReadListener(new DummyReadListener()));
        }
    }

    @Test
    void emptyBodyBehavesCorrectly() throws IOException {
        try (ServletInputStreamWrapper wrapper = new ServletInputStreamWrapper(new byte[0])) {

            assertTrue(wrapper.isFinished());
            assertEquals(-1, wrapper.read());
        }
    }

    // simple no-op listener for testing
    private static class DummyReadListener implements ReadListener {
        @Override
        public void onDataAvailable() {
        }

        @Override
        public void onAllDataRead() {
        }

        @Override
        public void onError(Throwable t) {
        }
    }
}
