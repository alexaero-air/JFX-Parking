package parking;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Car {
    private static final Random RND = new Random();
    private static final Set<String> USED = new HashSet<>();
    private static final String ABC = "ABCDEFGHJKLMNPRSTUVWXYZ";
    private static final String[] COLORS = {
            "#E76F51", "#F4A261", "#E9C46A", "#2A9D8F", "#4CC9F0",
            "#90BE6D", "#C77DFF", "#F8F9FA", "#118AB2", "#EF476F"
    };

    final String plate;
    final String color;

    Car() {
        plate = nextPlate();
        color = COLORS[RND.nextInt(COLORS.length)];
    }

    private static String nextPlate() {
        String plate;
        do {
            plate = switch (RND.nextInt(4)) {
                case 0 -> (RND.nextInt(9) + 1) + letters(3) + digits(3);
                case 1 -> letters(1) + digits(3) + letters(2) + digits(2);
                case 2 -> letters(3) + "-" + digits(4);
                default -> letters(2) + " " + digits(3) + " " + letters(2);
            };
        } while (!USED.add(plate));
        return plate;
    }

    private static String letters(int n) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) {
            s.append(ABC.charAt(RND.nextInt(ABC.length())));
        }
        return s.toString();
    }

    private static String digits(int n) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) {
            s.append(RND.nextInt(10));
        }
        return s.toString();
    }
}
