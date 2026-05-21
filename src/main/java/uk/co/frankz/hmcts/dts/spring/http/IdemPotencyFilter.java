package uk.co.frankz.hmcts.dts.spring.http;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.co.frankz.hmcts.dts.model.IdemPotencyRecord;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyException;
import uk.co.frankz.hmcts.dts.service.IdemPotencyStore;

import java.io.IOException;
import java.util.Optional;

@Component
public class IdemPotencyFilter extends OncePerRequestFilter {

    private final IdemPotencyStore store;

    private final WrapperResponseFilter idemPotencyInnerFilter;

    private final IdemPotencyKeyBuilder keyBuilder;

    public IdemPotencyFilter(
        IdemPotencyStore store,
        IdemPotencyKeyBuilder keyBuilder,
        WrapperResponseFilter innerFilter
    ) {
        this.store = store;
        this.keyBuilder = keyBuilder;
        this.idemPotencyInnerFilter = innerFilter;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain chain
    ) throws ServletException, IOException {

        // Only apply to mutating methods
        if (!isMutating(request)) {
            chain.doFilter(request, response);
            return;
        }

        Optional<IdemPotencyScopeKey> key = keyBuilder.build(request);
        if (key.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // Check if completed record exists
            Optional<IdemPotencyRecord> idemPotencyRecord = store.findById(key.get());
            if (idemPotencyRecord.isPresent()) {
                idemPotencyInnerFilter.doFilterUsingIdemPotency(
                    idemPotencyRecord.get(),
                    request,
                    response
                );
            } else {
                IdemPotencyRecord record = idemPotencyInnerFilter.doFilterExtractIdemPotency(
                    key.get(),
                    request,
                    response,
                    chain::doFilter
                );

                // Create record
                store.saveSafe(record);
            }
        } catch (IdemPotencyException e) {
            e.getIssue().sendError(response);
        }
    }

    private boolean isMutating(HttpServletRequest req) {
        return switch (req.getMethod()) {
            case "POST", "PUT", "PATCH", "DELETE" -> true;
            default -> false;
        };
    }

}


