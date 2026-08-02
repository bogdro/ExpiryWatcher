package bogdrosoft.expirywatcher.util;

/**
 * Ordinal doubles as the {@code sortMode} bind parameter in {@code ProductDao.searchByName},
 * and as the persisted value in SharedPreferences, so the declaration order here matters.
 */
public enum SortOrder {
    EXPIRY_ASC,
    EXPIRY_DESC,
    NAME_ASC,
    NAME_DESC,
    TYPE_ASC,
    TYPE_DESC,
    CONTAINER_ASC,
    CONTAINER_DESC;

    public static SortOrder fromOrdinal(int ordinal) {
        SortOrder[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return EXPIRY_ASC;
        }
        return values[ordinal];
    }
}
