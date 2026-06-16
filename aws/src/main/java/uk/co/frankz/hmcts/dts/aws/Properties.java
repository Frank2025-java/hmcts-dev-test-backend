package uk.co.frankz.hmcts.dts.aws;

import java.time.Duration;

public interface Properties {

    String TABLE = "Task";
    String TABLE_IDEMPOTENCY = "IdemPotency";

    Duration TTL_IDEMPOTENCY = Duration.ofHours(1);
}
