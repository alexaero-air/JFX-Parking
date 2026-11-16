package parking;

import java.time.LocalDateTime;

public class Parking {
    public static final int SPOTS = 20;

    final Car[] cars = new Car[SPOTS];
    final LocalDateTime[] entered = new LocalDateTime[SPOTS];

    boolean isFree(int number) {
        return cars[number - 1] == null;
    }

    boolean park(Car car, int number, LocalDateTime time) {
        if (number < 1 || number > SPOTS || !isFree(number)) {
            return false;
        }
        cars[number - 1] = car;
        entered[number - 1] = time;
        return true;
    }

    Car leave(int number) {
        Car car = cars[number - 1];
        cars[number - 1] = null;
        entered[number - 1] = null;
        return car;
    }

    int freeCount() {
        int n = 0;
        for (Car car : cars) {
            if (car == null) {
                n++;
            }
        }
        return n;
    }

    int busyCount() {
        return SPOTS - freeCount();
    }
}
