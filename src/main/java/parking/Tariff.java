package parking;

import java.time.Duration;
import java.time.LocalDateTime;

public class Tariff {
    static int pay(LocalDateTime from, LocalDateTime to) {
        return pay(Duration.between(from, to));
    }

    static int pay(Duration stay) {
        if (stay.compareTo(Duration.ofMinutes(20)) <= 0) {
            return 0;
        }
        if (stay.compareTo(Duration.ofHours(12)) <= 0) {
            return started(stay.getSeconds(), 3600) * 200;
        }
        return started(stay.getSeconds(), 86400) * 200;
    }

    private static int started(long seconds, long unit) {
        return (int) ((seconds + unit - 1) / unit);
    }
}
