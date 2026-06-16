package uk.co.frankz.hmcts.dts.model;

import java.util.Optional;
import java.util.function.Function;

/**
 * Utility class that creates a IdemPotencyScopeKey from a http request.
 * But it is agnostic of whether it is
 * a jakarta servlet or a aws gatewway event that is passed in.
 *
 * @param <T> where T is the type that has methods for
 *            getting the httpMethod, path and Header. This abstracts
 *            from whether we handle a servlet http or AWS Gateway http request..
 */
public class IdemPotencyScopeKeyBuilder<T> {

    private final Function<T, String> httpMethodGetter;

    private final Function<T, String> httpPathGetter;

    private final Function<T, String> httpHeaderGetter;

    public IdemPotencyScopeKeyBuilder(
        Function<T, String> httpMethodGetter,
        Function<T, String> httpPathGetter,
        Function<T, String> httpHeaderGetter) {

        this.httpMethodGetter = httpMethodGetter;
        this.httpPathGetter = httpPathGetter;
        this.httpHeaderGetter = httpHeaderGetter;
    }

    /**
     * Attempts to construct an {@link IdemPotencyScopeKey} from the given
     * {@link jakarta.servlet.http.HttpServletRequest}, but only for HTTP methods that represent
     * mutating operations.
     *
     * <p>Idempotency is only applicable to unsafe or state‑changing HTTP methods
     * (POST, PUT, PATCH, DELETE). For all other methods, this method returns
     * {@link Optional#empty()} immediately without attempting to build a key.
     *
     * <p>If key construction succeeds, the resulting {@link IdemPotencyScopeKey}
     * is wrapped in {@link Optional#of(Object)}. If any exception occurs during
     * extraction or construction (for example, invalid or missing fields),
     * this method returns {@link Optional#empty()} instead of propagating the
     * exception.
     *
     * @param request the incoming HTTP servlet request
     * @return an {@link Optional} containing the constructed scope key for mutating requests.
     *     The return will be {@code Optional.empty()} if the request is non‑mutating or key creation fails
     */
    public Optional<IdemPotencyScopeKey> build(T request) {

        try {
            // Only apply to mutating methods
            if (!isMutating(request)) {
                return Optional.empty();
            }

            var key = new IdemPotencyScopeKey(
                httpMethodGetter.apply(request),
                httpPathGetter.apply(request),
                httpHeaderGetter.apply(request) // returns null when not present
            );

            return Optional.of(key);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Determines whether the given HTTP request represents a mutating operation.
     *
     * <p>Mutating HTTP methods are those that can create, modify or delete server-side
     * state and therefore require idempotency protection. According to HTTP
     * semantics, the methods {@code POST}, {@code PUT}, {@code PATCH} and
     * {@code DELETE} are considered unsafe or state-changing operations. All other
     * methods (such as {@code GET}, {@code HEAD}, {@code OPTIONS}) are treated as
     * non-mutating and do not participate in idempotency handling.
     *
     * @param request the incoming Jakarta or AWS http request
     * @return {@code true} if the request method is one of POST, PUT, PATCH or DELETE; {@code false} otherwise
     */
    public boolean isMutating(T request) {
        String method = httpMethodGetter.apply(request);
        method = method == null ? "" : method.toUpperCase();
        return switch (method) {
            case "POST", "PUT", "PATCH", "DELETE" -> true;
            default -> false;
        };
    }
}
