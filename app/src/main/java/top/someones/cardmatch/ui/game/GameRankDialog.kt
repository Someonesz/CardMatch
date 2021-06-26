package top.someones.cardmatch.ui.game

import android.app.Dialog
import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Point
import android.graphics.Typeface
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import top.someones.cardmatch.R
import top.someones.cardmatch.databinding.GameRankItemLayoutBinding
import top.someones.cardmatch.databinding.GameRankLayoutBinding
import top.someones.cardmatch.entity.GameScore
import top.someones.cardmatch.entity.RankItemData
import top.someones.cardmatch.entity.UserData
import top.someones.cardmatch.service.GameService
import java.util.*

class GameRankDialog(
    context: Context,
    private val gameUUID: String,
    private val databaseHelper: SQLiteOpenHelper,
    private val mSize: Point
) : Dialog(context) {
    private val mHandler = Handler()
    private var isLocal = true
    private val localListData: MutableList<RankItemData> = ArrayList()
    private val worldListData: MutableList<GameScore> = ArrayList()
    private val localListAdapter: RankListAdapter
    private val worldListAdapter: WorldListAdapter
    private val mViewBinding: GameRankLayoutBinding =
        GameRankLayoutBinding.inflate(LayoutInflater.from(context))

    private fun initData() {
        Thread {
            databaseHelper.writableDatabase.use { db ->
                db.rawQuery(
                    "SELECT TIME,SCORE FROM GameHistory WHERE UUID = ? ORDER BY SCORE DESC",
                    arrayOf(gameUUID)
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        localListData.add(RankItemData(cursor.getString(0), cursor.getInt(1)))
                    }
                }
            }
            mHandler.post { localListAdapter.notifyDataSetChanged() }
        }.start()
        GameService.getScore(context, gameUUID) {
            mViewBinding.actionShowWorldList.setTextColor(context.getColor(R.color.rank_title_unchecked))
            mViewBinding.actionShowWorldList.isEnabled = true
            worldListData.addAll(it)
            mHandler.post { worldListAdapter.notifyDataSetChanged() }
        }
    }

    override fun show() {
        super.show()
        window!!.setLayout(mSize.x, mSize.y)
        window!!.setBackgroundDrawableResource(R.drawable.fillet_bg)
    }

    private class RankListAdapter(
        private val listData: List<RankItemData>
    ) : RecyclerView.Adapter<RankListAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                GameRankItemLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = listData[position]
            if (position < 3) holder.binding.index.text = ""
            when (position) {
                0 -> holder.binding.index.setBackgroundResource(R.drawable.champion)
                1 -> holder.binding.index.setBackgroundResource(R.drawable.runner_up)
                2 -> holder.binding.index.setBackgroundResource(R.drawable.third_place)
                else -> {
                    holder.binding.index.text = (position + 1).toString()
                    holder.binding.index.background = null
                }
            }
            holder.binding.timeOrName.text = data.timeOrName
            if (data.timeOrName == UserData.username!!) {
                holder.binding.timeOrName.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            } else {
                holder.binding.timeOrName.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            }
            holder.binding.score.text = data.score.toString()
        }

        override fun getItemCount(): Int {
            return listData.size
        }

        private class ViewHolder(val binding: GameRankItemLayoutBinding) : RecyclerView.ViewHolder(
            binding.root
        )
    }

    init {
        setContentView(mViewBinding.root)
        mViewBinding.localList.layoutManager = LinearLayoutManager(context)
        mViewBinding.worldList.layoutManager = LinearLayoutManager(context)
        localListAdapter = RankListAdapter(localListData)
        worldListAdapter = WorldListAdapter(worldListData)
        mViewBinding.localList.adapter = localListAdapter
        mViewBinding.worldList.adapter = worldListAdapter
        val checkedColor = context.getColor(R.color.rank_title_checked)
        val uncheckedColor = context.getColor(R.color.rank_title_unchecked)
        mViewBinding.actionShowLocalList.setOnClickListener {
            if (isLocal) return@setOnClickListener
            isLocal = true
            mViewBinding.localList.visibility = View.VISIBLE
            mViewBinding.worldList.visibility = View.INVISIBLE
            mViewBinding.timeOrName.text = "时间"
            mViewBinding.actionShowLocalList.setTextColor(checkedColor)
            mViewBinding.actionShowWorldList.setTextColor(uncheckedColor)
        }
        mViewBinding.actionShowWorldList.setOnClickListener {
            if (isLocal) {
                isLocal = false
                mViewBinding.localList.visibility = View.INVISIBLE
                mViewBinding.worldList.visibility = View.VISIBLE
                mViewBinding.timeOrName.text = "昵称"
                mViewBinding.actionShowLocalList.setTextColor(uncheckedColor)
                mViewBinding.actionShowWorldList.setTextColor(checkedColor)
            }
        }
        initData()
    }
}

private class WorldListAdapter(
    private val listData: MutableList<GameScore>
) : RecyclerView.Adapter<WorldListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            GameRankItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listData[position]
        if (position < 3) holder.binding.index.text = ""
        when (position) {
            0 -> holder.binding.index.setBackgroundResource(R.drawable.champion)
            1 -> holder.binding.index.setBackgroundResource(R.drawable.runner_up)
            2 -> holder.binding.index.setBackgroundResource(R.drawable.third_place)
            else -> {
                holder.binding.index.text = (position + 1).toString()
                holder.binding.index.background = null
            }
        }
        holder.binding.timeOrName.text = data.username
        if (data.username == UserData.username!!) {
            holder.binding.timeOrName.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        } else {
            holder.binding.timeOrName.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        }
        holder.binding.score.text = data.score.toString()
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    private class ViewHolder(val binding: GameRankItemLayoutBinding) : RecyclerView.ViewHolder(
        binding.root
    )
}