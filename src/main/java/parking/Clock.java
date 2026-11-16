package parking;

import java.time.Duration;
import java.time.LocalDateTime;

public class Clock {
    private static final LocalDateTime START = LocalDateTime.of(2027, 1, 1, 8, 0, 0);
    private final long startedNanos = System.nanoTime();
    private Duration extra = Duration.ZERO;

    public LocalDateTime now() {
        long realNanos = System.nanoTime() - startedNanos;
        return START.plusNanos(realNanos * 60).plus(extra);
    }

    public void skip(Duration educational) {
        extra = extra.plus(educational);
    }
}
