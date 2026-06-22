package uk.co.frankz.hmcts.dts.spring.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServletOutputStreamWrapperTest {

    private ServletOutputStreamWrapper testSubject;

    private ByteArrayOutputStream buffer;

    @BeforeEach
    void setup() {
        buffer = new ByteArrayOutputStream();
        testSubject = new ServletOutputStreamWrapper(buffer);
    }

    @Test
    void writesNormalBytesToBuffer() {
        // given
        String given = "hello";

        // when
        testSubject.write(given.getBytes(UTF_8));

        // then
        assertEquals(given, buffer.toString(UTF_8));
        assertTrue(testSubject.isReady());
    }

    @Test
    void handlesEmptyByteArray() {
        // given

        // when
        testSubject.write(new byte[0]);

        // then
        assertEquals(0, buffer.size());
        assertTrue(testSubject.isReady());
    }

    @Test
    void handlesNullByteArrayGracefully() {
        // given
        testSubject = new ServletOutputStreamWrapper(buffer);

        // when
        testSubject.write(null);

        // then
        assertEquals(0, buffer.size());
        assertTrue(testSubject.isReady());
    }
}
