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

package bogdrosoft.expirywatcher.ui.types;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import bogdrosoft.expirywatcher.R;
import bogdrosoft.expirywatcher.data.entity.ProductType;
import bogdrosoft.expirywatcher.util.SharedPrefsHelper;

public class ProductTypeAdapter extends ListAdapter<ProductType, ProductTypeAdapter.TypeViewHolder> {

    public interface Listener {
        void onEditType(ProductType type);

        void onDeleteType(ProductType type);
    }

    private final Listener listener;

    public ProductTypeAdapter(Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ProductType> DIFF_CALLBACK = new DiffUtil.ItemCallback<ProductType>() {
        @Override
        public boolean areItemsTheSame(@NonNull ProductType oldItem, @NonNull ProductType newItem) {
            return oldItem.name.equals(newItem.name);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProductType oldItem, @NonNull ProductType newItem) {
            return Objects.equals(oldItem.leadTimeDays, newItem.leadTimeDays);
        }
    };

    @NonNull
    @Override
    public TypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_type, parent, false);
        return new TypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TypeViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class TypeViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textLeadTime;
        private final View buttonEdit;
        private final View buttonDelete;

        TypeViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_type_name);
            textLeadTime = itemView.findViewById(R.id.text_type_lead_time);
            buttonEdit = itemView.findViewById(R.id.button_edit_type);
            buttonDelete = itemView.findViewById(R.id.button_delete_type);
        }

        void bind(ProductType type, Listener listener) {
            textName.setText(type.name);

            String summary = type.leadTimeDays != null
                    ? itemView.getContext().getString(R.string.type_lead_time_summary_custom, type.leadTimeDays)
                    : itemView.getContext().getString(R.string.type_lead_time_summary_default,
                            SharedPrefsHelper.getLeadTimeDays(itemView.getContext()));
            textLeadTime.setText(summary);

            itemView.setOnClickListener(v -> listener.onEditType(type));
            buttonEdit.setOnClickListener(v -> listener.onEditType(type));
            buttonDelete.setOnClickListener(v -> listener.onDeleteType(type));
        }
    }
}
