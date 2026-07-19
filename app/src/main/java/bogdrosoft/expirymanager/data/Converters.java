package bogdrosoft.expirymanager.data;

import androidx.room.TypeConverter;

import java.time.LocalDate;

public class Converters {

    @TypeConverter
    public static LocalDate fromEpochDay(Long epochDay) {
        return epochDay == null ? null : LocalDate.ofEpochDay(epochDay);
    }

    @TypeConverter
    public static Long toEpochDay(LocalDate date) {
        return date == null ? null : date.toEpochDay();
    }
}
