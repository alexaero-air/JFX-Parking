package parking;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Journal {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final DateTimeFormatter FILE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final PrintWriter out;
    private LocalDate day;
    private int dayIn;
    private int dayOut;
    private int dayMoney;

    Journal(LocalDateTime start) {
        this.day = start.toLocalDate();
        try {
            Files.createDirectories(Path.of("journals"));
            Path file = Path.of("journals/parking-" + LocalDateTime.now().format(FILE) + ".log");
            out = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        write(TIME.format(start) + ". Журнал запуска открыт. Парковка пуста.");
    }

    String enter(String plate, int spot, LocalDateTime time) {
        dayIn++;
        return write(TIME.format(time) + ". Автомобиль " + plate + " заехал на место №" + spot + ".");
    }

    String leave(String plate, LocalDateTime in, LocalDateTime outTime, int money) {
        dayOut++;
        dayMoney += money;
        return write(TIME.format(outTime)
                + ". Автомобиль " + plate
                + " выехал. Въезд: " + TIME.format(in)
                + ". Оплачено: " + money + " ₽.");
    }

    String syncDays(LocalDateTime now) {
        StringBuilder added = new StringBuilder();
        while (day.isBefore(now.toLocalDate())) {
            added.append(write(TIME.format(day.atTime(23, 59, 59))
                            + ". Итог дня: въехало " + dayIn
                            + ", выехало " + dayOut
                            + ". Выручка: " + dayMoney + " ₽."))
                    .append('\n');
            day = day.plusDays(1);
            dayIn = 0;
            dayOut = 0;
            dayMoney = 0;
            added.append(write(DAY.format(day) + ". Начало записей нового дня.")).append('\n');
        }
        return added.toString().strip();
    }

    String close(LocalDateTime now) {
        String line = write(TIME.format(now)
                + ". Промежуточный итог дня: въехало " + dayIn
                + ", выехало " + dayOut
                + ". Выручка: " + dayMoney + " ₽.");
        out.close();
        return line;
    }

    int todayMoney() {
        return dayMoney;
    }

    private String write(String line) {
        out.println(line);
        out.flush();
        return line;
    }
}
