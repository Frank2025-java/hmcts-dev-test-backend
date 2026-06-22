package uk.co.frankz.hmcts.dts.spring.http;

import jakarta.annotation.Nullable;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import java.io.ByteArrayOutputStream;

public class ServletOutputStreamWrapper extends ServletOutputStream {

    private final ByteArrayOutputStream buffer;

    public ServletOutputStreamWrapper(ByteArrayOutputStream buffer) {
        this.buffer = buffer;
    }

    @Override
    public boolean isReady() {
        return true; // always ready for synchronous IO
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        // async IO not used — no-op
    }

    @Override
    public void write(int b) {
        buffer.write(b);
    }

    @Override
    public void write(@Nullable byte[] b) {
        if (b != null) {
            write(b, 0, b.length);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) {
        buffer.write(b, off, len);
    }
}
