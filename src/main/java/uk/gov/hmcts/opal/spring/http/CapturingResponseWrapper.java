package uk.gov.hmcts.opal.spring.http;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Captures all response bytes in memory.
 */
public class CapturingResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(16 * 1024);
    private final Charset defaultCharset = StandardCharsets.UTF_8;

    private ServletOutputStream teeOutputStream;
    private PrintWriter teeWriter;

    public CapturingResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        if (teeWriter != null) {
            throw new IllegalStateException("getWriter() already called");
        }
        if (teeOutputStream == null) {
            teeOutputStream = new ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {
                    /* no-op */
                }

                @Override
                public void write(int b) {
                    buffer.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) {
                    buffer.write(b, off, len);
                }

                @Override
                public void flush() {
                    /* swallow; we flush once at end */
                }

                @Override
                public void close() {
                    /* swallow; we'll close later */
                }
            };
        }
        return teeOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (teeOutputStream != null) {
            throw new IllegalStateException("getOutputStream() already called");
        }
        if (teeWriter == null) {
            Charset cs = getCharacterEncoding() != null
                ? Charset.forName(getCharacterEncoding())
                : defaultCharset;
            teeWriter = new PrintWriter(new OutputStreamWriter(buffer, cs));
        }
        return teeWriter;
    }

    /**
     * Returns the exact bytes written so far (flushes the writer first).
     */
    public byte[] getCaptured() {
        if (teeWriter != null) {
            teeWriter.flush();
        }
        return buffer.toByteArray();
    }
}