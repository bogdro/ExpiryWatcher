/*
 * Copyright (C) 2026 Bogdan Drozdowski, bogdro (at) users . sourceforge . net
 * License: GNU General Public License, v3+
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package bogdrosoft.expirywatcher.data;

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
