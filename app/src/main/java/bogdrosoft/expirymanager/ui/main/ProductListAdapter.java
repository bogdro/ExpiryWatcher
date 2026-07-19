package bogdrosoft.expirymanager.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import bogdrosoft.expirymanager.R;
import bogdrosoft.expirymanager.data.entity.Product;

public class ProductListAdapter extends ListAdapter<Product, ProductListAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final OnProductClickListener listener;

    public ProductListAdapter(OnProductClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK = new DiffUtil.ItemCallback<Product>() {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.name.equals(newItem.name)
                    && oldItem.type.equals(newItem.type)
                    && oldItem.quantity == newItem.quantity
                    && oldItem.unit.equals(newItem.unit)
                    && oldItem.expiryDate.equals(newItem.expiryDate)
                    && Objects.equals(oldItem.openDate, newItem.openDate)
                    && Objects.equals(oldItem.barcode, newItem.barcode);
        }
    };

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textDetails;
        private final TextView textExpiry;
        private final TextView textOpenDate;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_name);
            textDetails = itemView.findViewById(R.id.text_details);
            textExpiry = itemView.findViewById(R.id.text_expiry);
            textOpenDate = itemView.findViewById(R.id.text_open_date);
        }

        void bind(Product product, OnProductClickListener listener) {
            textName.setText(product.name);
            textDetails.setText(itemView.getContext().getString(
                    R.string.item_details_format, product.type, product.quantity, product.unit));

            long daysLeft = product.expiryDate.toEpochDay() - LocalDate.now().toEpochDay();

            String status;
            int colorRes;
            if (daysLeft < 0) {
                status = itemView.getContext().getString(R.string.status_expired);
                colorRes = R.color.status_expired;
            } else if (daysLeft == 0) {
                status = itemView.getContext().getString(R.string.status_expires_today);
                colorRes = R.color.status_soon;
            } else {
                status = itemView.getContext().getString(R.string.status_days_left, (int) daysLeft);
                colorRes = daysLeft <= 3 ? R.color.status_soon : R.color.status_ok;
            }

            textExpiry.setText(itemView.getContext().getString(
                    R.string.item_expiry_format, product.expiryDate.format(DATE_FORMATTER), status));
            textExpiry.setTextColor(itemView.getContext().getColor(colorRes));

            if (product.openDate != null) {
                textOpenDate.setText(itemView.getContext().getString(
                        R.string.item_open_date_format, product.openDate.format(DATE_FORMATTER)));
                textOpenDate.setVisibility(View.VISIBLE);
            } else {
                textOpenDate.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onProductClick(product));
        }
    }
}
