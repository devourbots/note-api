package fun.lula.flomo.util;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeUtil {

    public static String getNowString() {
        return DateTime.now().toString("yyyy-MM-dd HH:mm");
    }

    public static String getNowString2() {
        return DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
    }

    public static Integer getDaysUntilNow(String dateString) {
        DateTime now = DateTime.now();
        DateTimeFormatter pattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

        DateTime joinDate = DateTime.parse(dateString, pattern);

        return Days.daysBetween(joinDate, now).getDays() + 1;
    }

}
