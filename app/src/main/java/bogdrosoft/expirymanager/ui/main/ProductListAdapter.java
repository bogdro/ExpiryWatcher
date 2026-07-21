package bogdrosoft.expirymanager.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import bogdrosoft.expirymanager.R;
import bogdrosoft.expirymanager.data.entity.Product;
import bogdrosoft.expirymanager.util.Constants;

public class ProductListAdapter extends ListAdapter<Product, ProductListAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final OnProductClickListener listener;
    private final Map<String, Integer> typeLeadTimeOverrides = new HashMap<>();
    private int defaultLeadTimeDays = Constants.DEFAULT_LEAD_TIME_DAYS;

    public ProductListAdapter(OnProductClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    /**
     * Drives both the expiry-status text and the card background color, so they stay in sync
     * with the same effective lead time (a product type's own override, or the global default)
     * used by the expiry reminder notifications. Triggers a full rebind, since this changes how
     * every row is colored regardless of whether the underlying product list itself changed.
     */
    public void setLeadTimeSettings(Map<String, Integer> typeLeadTimeOverrides, int defaultLeadTimeDays) {
        this.typeLeadTimeOverrides.clear();
        this.typeLeadTimeOverrides.putAll(typeLeadTimeOverrides);
        this.defaultLeadTimeDays = defaultLeadTimeDays;
        notifyDataSetChanged();
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
                    && Objects.equals(oldItem.container, newItem.container)
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
        Product product = getItem(position);
        int leadTimeDays = typeLeadTimeOverrides.getOrDefault(product.type, defaultLeadTimeDays);
        holder.bind(product, listener, leadTimeDays);
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView card;
        private final TextView textName;
        private final TextView textDetails;
        private final TextView textExpiry;
        private final TextView textOpenDate;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            textName = itemView.findViewById(R.id.text_name);
            textDetails = itemView.findViewById(R.id.text_details);
            textExpiry = itemView.findViewById(R.id.text_expiry);
            textOpenDate = itemView.findViewById(R.id.text_open_date);
        }

        void bind(Product product, OnProductClickListener listener, int leadTimeDays) {
            textName.setText(product.name);
            if (product.container != null && !product.container.isEmpty()) {
                textDetails.setText(itemView.getContext().getString(R.string.item_details_with_container_format,
                        product.type, product.quantity, product.unit, product.container));
            } else {
                textDetails.setText(itemView.getContext().getString(
                        R.string.item_details_format, product.type, product.quantity, product.unit));
            }

            long daysLeft = product.expiryDate.toEpochDay() - LocalDate.now().toEpochDay();

            String status;
            int textColorRes;
            int cardColorRes;
            if (daysLeft < 0) {
                status = itemView.getContext().getString(R.string.status_expired);
                textColorRes = R.color.status_expired;
                cardColorRes = R.color.card_expired;
            } else if (daysLeft <= leadTimeDays) {
                status = daysLeft == 0
                        ? itemView.getContext().getString(R.string.status_expires_today)
                        : itemView.getContext().getString(R.string.status_days_left, (int) daysLeft);
                textColorRes = R.color.status_soon;
                cardColorRes = R.color.card_near_expiry;
            } else {
                status = itemView.getContext().getString(R.string.status_days_left, (int) daysLeft);
                textColorRes = R.color.status_ok;
                cardColorRes = R.color.card_fresh;
            }

            // Exhausted products (0 units left) are sorted to the bottom regardless of expiry
            // date; the card background reflects that grouping too, taking priority over the
            // expiry-status color, while the status text/color still reports the real expiry.
            if (product.quantity == 0) {
                cardColorRes = R.color.card_exhausted;
            }

            textExpiry.setText(itemView.getContext().getString(
                    R.string.item_expiry_format, product.expiryDate.format(DATE_FORMATTER), status));
            textExpiry.setTextColor(itemView.getContext().getColor(textColorRes));
            card.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), cardColorRes));

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
