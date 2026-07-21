package bogdrosoft.expirymanager.ui.containers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import bogdrosoft.expirymanager.R;
import bogdrosoft.expirymanager.data.entity.Container;

public class ContainerAdapter extends ListAdapter<Container, ContainerAdapter.ContainerViewHolder> {

    public interface Listener {
        void onDeleteContainer(Container container);
    }

    private final Listener listener;

    public ContainerAdapter(Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Container> DIFF_CALLBACK = new DiffUtil.ItemCallback<Container>() {
        @Override
        public boolean areItemsTheSame(@NonNull Container oldItem, @NonNull Container newItem) {
            return oldItem.name.equals(newItem.name);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Container oldItem, @NonNull Container newItem) {
            return oldItem.name.equals(newItem.name);
        }
    };

    @NonNull
    @Override
    public ContainerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_container, parent, false);
        return new ContainerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContainerViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ContainerViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final View buttonDelete;

        ContainerViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_container_name);
            buttonDelete = itemView.findViewById(R.id.button_delete_container);
        }

        void bind(Container container, Listener listener) {
            textName.setText(container.name);
            buttonDelete.setOnClickListener(v -> listener.onDeleteContainer(container));
        }
    }
}
