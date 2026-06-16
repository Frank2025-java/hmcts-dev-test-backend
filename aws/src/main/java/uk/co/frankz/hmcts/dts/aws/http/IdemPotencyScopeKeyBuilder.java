package uk.co.frankz.hmcts.dts.aws.http;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;

import java.util.Optional;

/**
 * Utility class for constructing an {@link IdemPotencyScopeKey} from an
 * incoming API Gateway v2 HTTP event.
 *
 * <p>This builder extracts the HTTP method, request path and the optional
 * {@code Idempotency-Key} header from the {@link APIGatewayV2HTTPEvent}
 * and converts them into a canonical idempotency scope key suitable for
 * downstream idempotency storage and lookup.
 *
 * <p>API Gateway v2 normalises all header names to lowercase, therefore the
 * idempotency key is retrieved using the header name {@code "idempotency-key"}.
 *
 * <p>If key construction succeeds, the resulting scope key is wrapped in
 * {@link Optional#of(Object)}. If any exception occurs during extraction
 * or construction (for example, missing request context fields), this
 * method returns {@link Optional#empty()} instead of propagating the error.
 */
public class IdemPotencyScopeKeyBuilder
    extends uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKeyBuilder<APIGatewayV2HTTPEvent> {

    public IdemPotencyScopeKeyBuilder() {
        super(
            (event) -> event.getRequestContext().getHttp().getMethod(),
            (event) -> event.getRequestContext().getHttp().getPath(),
            (event) -> event.getHeaders().get("idempotency-key")
        );
    }

}
