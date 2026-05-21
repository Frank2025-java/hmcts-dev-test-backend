package uk.co.frankz.hmcts.dts.spring.http;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.validation.constraints.NotNull;

import java.io.ByteArrayInputStream;

public class ServletInputStreamWrapper extends ServletInputStream {

    private final ByteArrayInputStream buffer;

    public ServletInputStreamWrapper(byte[] cachedBody) {
        this.buffer = new ByteArrayInputStream(cachedBody);
    }

    @Override
    public int read() {
        return buffer.read();
    }

    @Override
    public int read(byte[] b, int off, int len) {
        return buffer.read(b, off, len);
    }

    @Override
    public boolean isFinished() {
        return buffer.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true; // always ready for synchronous IO
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        // async IO not used — no-op
    }
}
