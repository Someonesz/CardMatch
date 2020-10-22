package top.someones.cardmatch.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import top.someones.cardmatch.R;
import top.someones.cardmatch.entity.Mod;;

public class ModAdapter extends RecyclerView.Adapter<ModAdapter.ViewHolder> {

    private static final String AUTHOR = "作者：";
    private static final String VERSION = "版本：";

    private final Mod[] mModList;
    private final ModOnClickListener mModOnClickListener;

    public ModAdapter(Mod[] modList) {
        this.mModList = modList;
        this.mModOnClickListener = null;
    }

    public ModAdapter(Mod[] modList, ModOnClickListener modOnClickListener) {
        this.mModList = modList;
        this.mModOnClickListener = modOnClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mod_info_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mod mod = mModList[position];
        if (mModOnClickListener != null) {
            holder.view.setOnClickListener(l -> {
                mModOnClickListener.onClick(mod);
            });
        }
        holder.modImage.setImageBitmap(mod.getImage());
        holder.modName.setText(mod.getName());
        holder.modAuthor.setText(AUTHOR + mod.getAuthor());
        holder.modVersion.setText(VERSION + mod.getVersion());
    }

    @Override
    public int getItemCount() {
        return mModList.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView modImage;
        TextView modName, modAuthor, modVersion;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            modImage = view.findViewById(R.id.modImage);
            modName = view.findViewById(R.id.modName);
            modAuthor = view.findViewById(R.id.modAuthor);
            modVersion = view.findViewById(R.id.modVersion);
        }
    }

    public interface ModOnClickListener {
        void onClick(Mod mod);
    }
}

