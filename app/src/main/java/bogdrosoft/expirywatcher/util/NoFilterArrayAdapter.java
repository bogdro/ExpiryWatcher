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
