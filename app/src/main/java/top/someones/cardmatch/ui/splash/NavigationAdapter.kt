package top.someones.cardmatch.ui.splash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import top.someones.cardmatch.R

class NavigationAdapter(private val size: Int) :
    RecyclerView.Adapter<NavigationAdapter.ViewHolder>() {
    private var select = 0
    fun setSelect(select: Int) {
        this.select = select
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.navigation_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == select) {
            holder.view.alpha = 1f
        } else {
            holder.view.alpha = 0.5f
        }
    }

    override fun getItemCount(): Int {
        return size
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(
        view
    )
}