package uk.co.frankz.hmcts.dts.spring.http;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;

import java.util.Optional;

/**
 * Builds an {@link IdemPotencyScopeKey} from an incoming HTTP request.
 *
 * <p>This component extracts the HTTP method, request URI and the optional
 * {@code Idempotency-Key} header from the {@link HttpServletRequest} and
 * constructs a canonical idempotency scope key. If any unexpected error
 * occurs during key creation (for example, invalid or blank fields),
 * the method returns {@link Optional#empty()} instead of propagating
 * the exception.
 *
 * <p>This builder is used by the idempotency filter to normalise incoming
 * requests into a consistent key format for downstream idempotency
 * storage and lookup.
 *
 * <p>When applicable, request attributes are used to construct the scope key:
 * <ul>
 *   <li>HTTP method via {@code request.getMethod()}</li>
 *   <li>Request path via {@code request.getRequestURI()}</li>
 *   <li>Idempotency key via {@code request.getHeader("Idempotency-Key")}</li>
 * </ul>
 */
@Component
public class IdemPotencyScopeKeyBuilder
    extends uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKeyBuilder<HttpServletRequest> {

    public IdemPotencyScopeKeyBuilder() {
        super(
            HttpServletRequest::getMethod,
            HttpServletRequest::getRequestURI,
            (request) -> request.getHeader("Idempotency-Key") // returns null when not present
        );
    }
}
