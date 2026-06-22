package uk.co.frankz.hmcts.dts.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

/**
 * Custom declaration matching Servlet doFilter method, with maintaining the exception annotation.
 *
 * @param <T> ServletRequest
 * @param <U> ServletResponse
 */
@FunctionalInterface
public interface ServletFilterMethod<T extends ServletRequest, U extends ServletResponse> {
    void accept(T t, U u) throws ServletException, IOException;
}
