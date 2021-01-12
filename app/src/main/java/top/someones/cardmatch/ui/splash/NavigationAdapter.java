package top.someones.cardmatch.ui.splash;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import top.someones.cardmatch.R;

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.ViewHolder> {

    private final int size;
    private int select = 0;

    public NavigationAdapter(int size) {
        this.size = size;
    }

    public void setSelect(int select) {
        this.select = select;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == select) {
            holder.view.setAlpha(1);
        } else {
            holder.view.setAlpha(0.5f);
        }
    }

    @Override
    public int getItemCount() {
        return size;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
        }
    }
}
