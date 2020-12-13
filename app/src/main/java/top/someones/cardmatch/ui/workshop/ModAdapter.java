package top.someones.cardmatch.ui.workshop;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import top.someones.cardmatch.databinding.ModInfoLayoutBinding;
import top.someones.cardmatch.entity.Mod;;

public class ModAdapter extends RecyclerView.Adapter<ModAdapter.ViewHolder> {

    private final List<Mod> mModList;
    private final ModOnClickListener mOnClickListener;

    public ModAdapter(List<Mod> modList, ModOnClickListener modOnClickListener) {
        this.mModList = modList;
        this.mOnClickListener = modOnClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ModInfoLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mod mod = mModList.get(position);
        if (mOnClickListener != null) {
            holder.binding.getRoot().setOnClickListener(l -> {
                mOnClickListener.onClick(mod);
            });
        }
        holder.binding.modCover.setImageBitmap(mod.getCover());
        holder.binding.modName.setText(mod.getName());
        holder.binding.modAuthor.setText("作者：".concat(mod.getAuthor()));
        holder.binding.modVersion.setText("版本：".concat(String.valueOf(mod.getVersion())));
    }

    @Override
    public int getItemCount() {
        return mModList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final ModInfoLayoutBinding binding;

        public ViewHolder(ModInfoLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface ModOnClickListener {
        void onClick(Mod mod);
    }
}


