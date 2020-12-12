package top.someones.cardmatch.ui.game;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import top.someones.cardmatch.R;
import top.someones.cardmatch.databinding.GameRankItemLayoutBinding;
import top.someones.cardmatch.databinding.GameRankLayoutBinding;
import top.someones.cardmatch.entity.RankItemData;

public class GameRankDialog extends Dialog {
    private static final String HOSTS = "http://Someones.top:12450/score/";

    private final String mGameUUID;
    private final SQLiteOpenHelper mDatabaseHelper;
    private final OkHttpClient mHttpClient;
    private final Point mSize;

    private final Handler mHandler = new Handler();
    private boolean isLocal = true;
    private final List<RankItemData> localListData = new ArrayList<>();
    private final List<RankItemData> worldListData = new ArrayList<>();
    private final RankListAdapter localListAdapter;
    private final RankListAdapter worldListAdapter;

    private final GameRankLayoutBinding mViewBinding;

    public GameRankDialog(@NonNull Context context, String gameUUID, String nikeName, SQLiteOpenHelper databaseHelper, OkHttpClient mHttpClient, Point mSize) {
        super(context);
        mViewBinding = GameRankLayoutBinding.inflate(LayoutInflater.from(context));
        setContentView(mViewBinding.getRoot());

        mViewBinding.localList.setLayoutManager(new LinearLayoutManager(context));
        mViewBinding.worldList.setLayoutManager(new LinearLayoutManager(context));
        localListAdapter = new RankListAdapter(localListData, nikeName);
        worldListAdapter = new RankListAdapter(worldListData, nikeName);
        mViewBinding.localList.setAdapter(localListAdapter);
        mViewBinding.worldList.setAdapter(worldListAdapter);
        int checkedColor = context.getColor(R.color.rank_title_checked);
        int uncheckedColor = context.getColor(R.color.rank_title_unchecked);
        mViewBinding.actionShowLocalList.setOnClickListener(v -> {
            if (isLocal)
                return;
            isLocal = true;
            mViewBinding.localList.setVisibility(View.VISIBLE);
            mViewBinding.worldList.setVisibility(View.INVISIBLE);
            mViewBinding.timeOrName.setText("时间");
            mViewBinding.actionShowLocalList.setTextColor(checkedColor);
            mViewBinding.actionShowWorldList.setTextColor(uncheckedColor);
        });
        mViewBinding.actionShowWorldList.setOnClickListener(v -> {
            if (isLocal) {
                isLocal = false;
                mViewBinding.localList.setVisibility(View.INVISIBLE);
                mViewBinding.worldList.setVisibility(View.VISIBLE);
                mViewBinding.timeOrName.setText("昵称");
                mViewBinding.actionShowLocalList.setTextColor(uncheckedColor);
                mViewBinding.actionShowWorldList.setTextColor(checkedColor);
            }
        });

        this.mGameUUID = gameUUID;
        this.mDatabaseHelper = databaseHelper;
        this.mHttpClient = mHttpClient;
        this.mSize = mSize;
        initData();
    }

    private void initData() {
        new Thread(() -> {
            Log.d("排名", "加载本地数据");
            try (SQLiteDatabase db = mDatabaseHelper.getWritableDatabase()) {
                try (Cursor cursor = db.rawQuery("SELECT TIME,SCORE FROM GameHistory WHERE UUID = ? ORDER BY SCORE DESC", new String[]{mGameUUID})) {
                    while (cursor.moveToNext()) {
                        localListData.add(new RankItemData(cursor.getString(0), cursor.getInt(1)));
                    }
                }
            }
            mHandler.post(localListAdapter::notifyDataSetChanged);
        }).start();
        new Thread(() -> {
            Log.d("排名", "加载世界数据");
            Call call = mHttpClient.newCall(new Request.Builder().get().url(HOSTS + mGameUUID).build());
            try (Response response = call.execute()) {
                JSONArray array = new JSONArray(response.body().string());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject json = array.getJSONObject(i);
                    worldListData.add(new RankItemData(json.getString("nickName"), json.getInt("score")));
                }
            } catch (IOException e) {
                Log.d("排名", "加载世界数据失败！网络连接错误。" + e.getMessage());
                mHandler.post(() -> Toast.makeText(getContext(), "加载世界数据失败！网络连接错误", Toast.LENGTH_SHORT).show());
            } catch (JSONException e) {
                Log.d("排名", "加载世界数据失败！JSON解析错误。" + e.getMessage());
                mHandler.post(() -> Toast.makeText(getContext(), "加载世界数据失败！JSON解析错误", Toast.LENGTH_SHORT).show());
            }
            mHandler.post(worldListAdapter::notifyDataSetChanged);
        }).start();
    }

    @Override
    public void show() {
        super.show();
        Log.d("排名", "x：" + mSize.x);
        Log.d("排名", "y：" + mSize.y);
        getWindow().setLayout(mSize.x, mSize.y);
//        getWindow().setLayout(900, 1500);
        getWindow().setBackgroundDrawableResource(R.drawable.fillet_bg);
    }

    private static class RankListAdapter extends RecyclerView.Adapter<RankListAdapter.ViewHolder> {
        private final List<RankItemData> listData;
        private final String mNikeName;

        public RankListAdapter(List<RankItemData> listData, String mNikeName) {
            this.listData = listData;
            this.mNikeName = mNikeName;
        }

        @NonNull
        @Override
        public RankListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RankListAdapter.ViewHolder(GameRankItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RankListAdapter.ViewHolder holder, int position) {
            RankItemData data = listData.get(position);
            if (position < 3)
                holder.binding.index.setText("");
            switch (position) {
                case 0:
                    holder.binding.index.setBackgroundResource(R.drawable.champion);
                    break;
                case 1:
                    holder.binding.index.setBackgroundResource(R.drawable.runner_up);
                    break;
                case 2:
                    holder.binding.index.setBackgroundResource(R.drawable.third_place);
                    break;
                default:
                    holder.binding.index.setText(String.valueOf(position + 1));
                    holder.binding.index.setBackground(null);
            }
            holder.binding.timeOrName.setText(data.getTimeOrName());
            if (data.getTimeOrName().equals(mNikeName)) {
                holder.binding.timeOrName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            } else {
                holder.binding.timeOrName.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }
            holder.binding.score.setText(String.valueOf(data.getScore()));
        }

        @Override
        public int getItemCount() {
            return listData.size();
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {

            final GameRankItemLayoutBinding binding;

            public ViewHolder(GameRankItemLayoutBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

}
