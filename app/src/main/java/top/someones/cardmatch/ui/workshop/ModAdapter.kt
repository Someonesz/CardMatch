package top.someones.cardmatch.ui.workshop

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import top.someones.cardmatch.databinding.ModInfoLayoutBinding
import top.someones.cardmatch.entity.WorkShopMod
import top.someones.cardmatch.util.NetUtil

class ModAdapter(
    private val mModList: List<WorkShopMod>,
    private val onClick: ((it: WorkShopMod) -> Unit)?
) : RecyclerView.Adapter<ModAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ModInfoLayoutBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mod = mModList[position]
        onClick?.let {
            holder.binding.root.setOnClickListener { _: View? -> it.invoke(mod) }
        }
        Glide.with(context)
            .load("${NetUtil.MAIN}mod/${mod.uuid}/img")
            .into(holder.binding.modCover)
        holder.binding.modName.text = mod.name
        holder.binding.modAuthor.text = "作者：${mod.author}"
        holder.binding.modVersion.text = "版本：${mod.version}"
    }

    override fun getItemCount(): Int {
        return mModList.size
    }

    class ViewHolder(val binding: ModInfoLayoutBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}