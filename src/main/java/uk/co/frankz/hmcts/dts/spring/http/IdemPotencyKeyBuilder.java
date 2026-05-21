package uk.co.frankz.hmcts.dts.spring.http;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import uk.co.frankz.hmcts.dts.model.IdemPotencyScopeKey;
import uk.co.frankz.hmcts.dts.model.exception.IdemPotencyException;

import java.util.Optional;

@Component
public class IdemPotencyKeyBuilder {
    public Optional<IdemPotencyScopeKey> build(HttpServletRequest request) {
        try {
            return Optional.of(IdemPotencyScopeKey.scopeKey(request));
        } catch (IdemPotencyException e) {
            return Optional.empty();
        }
    }
}
