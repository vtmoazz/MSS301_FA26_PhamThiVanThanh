package fu.se192641.post.common;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public final class DateUtils {

    public static final String ZONE = "Asia/Ho_Chi_Minh";
    public static final String PATTERN = "dd/MM/yyyy";

    private DateUtils() {
    }

    /** Hibernate có thể trả java.sql.Date (không hỗ trợ toInstant()), nên đổi qua epoch millis. */
    public static LocalDate toLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.of(ZONE)).toLocalDate();
    }

    /** Số ngày của chiến dịch = endDate - startDate (endDate luôn sau startDate). */
    public static long daysBetween(Date start, Date end) {
        return ChronoUnit.DAYS.between(toLocalDate(start), toLocalDate(end));
    }
}
