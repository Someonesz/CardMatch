package top.someones.cardmatch.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import top.someones.cardmatch.R;
import top.someones.cardmatch.entity.Mod;;

public class ModAdapter extends RecyclerView.Adapter<ModAdapter.ViewHolder> {

    private final List<Mod> mModList;

    public ModAdapter(List<Mod> mModList) {
        this.mModList = mModList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mod_info_layout, parent, false);
        ModAdapter.ViewHolder holder = new ModAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mod mod = mModList.get(position);
        holder.modImage.setImageBitmap(mod.getImage());
        holder.modName.setText(mod.getName());
        holder.modAuthor.setText("作者:" + mod.getAuthor());
        holder.modVersion.setText("版本:" + mod.getVersion());
    }

    @Override
    public int getItemCount() {
        return mModList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView modImage;
        TextView modName, modAuthor, modVersion;

        public ViewHolder(View view) {
            super(view);
            modImage = view.findViewById(R.id.modImage);
            modName = view.findViewById(R.id.modName);
            modAuthor = view.findViewById(R.id.modAuthor);
            modVersion = view.findViewById(R.id.modVersion);
        }

    }
}
