package bogdrosoft.expirywatcher.util;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

/**
 * An ArrayAdapter for AutoCompleteTextView dropdowns with a fixed set of choices. ArrayAdapter's
 * default filter narrows its item list to entries matching the field's current text; since
 * selecting an item sets the field's text to that item's label, the dropdown would then only
 * ever show that one entry the next time it's opened. This overrides the filter to always
 * report the full, unfiltered list regardless of the field's text.
 */
public class NoFilterArrayAdapter<T> extends ArrayAdapter<T> {

    public NoFilterArrayAdapter(Context context, int resource, T[] objects) {
        super(context, resource, objects);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                results.count = getCount();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                // Intentionally empty: the item list is fixed and must not change on filtering.
            }
        };
    }
}
