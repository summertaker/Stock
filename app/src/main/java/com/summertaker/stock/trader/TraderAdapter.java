package com.summertaker.stock.trader;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.summertaker.stock.R;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Tag;

import java.util.ArrayList;

public class TraderAdapter extends RecyclerView.Adapter<TraderAdapter.ItemViewHolder> {

    private Context mContext;
    private Resources mResources;
    private ArrayList<Item> mItems;

    public TraderAdapter(Context context, ArrayList<Item> items) {
        this.mContext = context;
        this.mResources = context.getResources();
        this.mItems = items;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView tvId;
        public TextView tvName;
        public TextView tvRof;
        public LinearLayout loTag;
        public ImageView ivChart;

        private ItemViewHolder(View view) {
            super(view);

            tvId = view.findViewById(R.id.tvId);
            tvName = view.findViewById(R.id.tvName);
            tvRof = view.findViewById(R.id.tvRof);
            loTag = view.findViewById(R.id.loTag);
            ivChart = view.findViewById(R.id.ivChart);

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            //contextMenu.setHeaderTitle(R.string.tag);
            for (Tag tag : BaseApplication.getInstance().getTags()) {
                contextMenu.add((int) tag.getId(), this.getAdapterPosition(), Menu.NONE, tag.getName());
            }
        }
    }

    @NonNull
    @Override
    public TraderAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.trader_row, parent, false);
        return new TraderAdapter.ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TraderAdapter.ItemViewHolder holder, int position) {
        Item item = mItems.get(position);

        // 번호
        String id = item.getId() + ".";
        holder.tvId.setText(id);

        // 종목 이름
        String name = item.getName();

        if (item.isBuy() && item.getBuyCount() > 0) { // 거래원 매수 중복수
            name = name + " (" + item.getBuyCount() + ")";
        } else if (item.isSell() && item.getSellCount() > 0) { // 거래원 매도 중복수
            name = name + " (" + item.getSellCount() + ")";
        }

        if (item.getNor() > 0) { // 추천수
            name = name + " +" + item.getNor();
        }
        holder.tvName.setText(name);

        // 등락률
        BaseApplication.getInstance().renderRof(item, null, null, holder.tvRof, null);

        // 태그
        if (item.getTagIds() == null || item.getTagIds().isEmpty()) {
            holder.loTag.setVisibility(View.GONE);
        } else {
            holder.loTag.setVisibility(View.VISIBLE);
            BaseApplication.getInstance().renderTag(mContext, item, holder.loTag);
        }

        // 차트
        String chartUrl = BaseApplication.getDayChartUrl(item.getCode());
        Glide.with(mContext).load(chartUrl).apply(new RequestOptions()).into(holder.ivChart);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).getId();
    }
}
