package bogdrosoft.expirymanager.util;

/**
 * Ordinal doubles as the {@code statusFilter} bind parameter in
 * {@code ProductDao.searchByName} (-1 there means "no filter", since an enum can't hold that).
 */
public enum ProductStatusFilter {
    EXPIRED,
    EXPIRING_SOON,
    NOT_EXPIRING,
    EXHAUSTED
}
