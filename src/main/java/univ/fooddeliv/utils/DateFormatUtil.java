package univ.fooddeliv.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateFormatUtil {
    private static final DateTimeFormatter DISPLAY =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private DateFormatUtil() {
    }

    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "—";
        }
        return dateTime.format(DISPLAY);
    }
}
