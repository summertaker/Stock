package com.summertaker.stock.trade;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.summertaker.stock.R;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.data.Item;

import java.util.ArrayList;

public class TradeAdapter extends RecyclerView.Adapter<TradeAdapter.ItemViewHolder> {

    private Context mContext;
    private ArrayList<Item> mItems;

    public TradeAdapter(Context context, ArrayList<Item> items) {
        this.mContext = context;
        this.mItems = items;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView tvId;
        public TextView tvName;
        public LinearLayout loTag;
        public ImageView ivChart;

        private ItemViewHolder(View view) {
            super(view);

            tvId = view.findViewById(R.id.tvId);
            tvName = view.findViewById(R.id.tvName);
            loTag = view.findViewById(R.id.loTag);
            ivChart = view.findViewById(R.id.ivChart);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.trade_row, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = mItems.get(position);

        // 번호
        String id = item.getId() + ".";
        holder.tvId.setText(id);

        // 종목이름
        String name = item.getName();
        if (item.getNor() > 0) { // 추천수
            name = name + " (" + item.getNor() + ")";
        }
        holder.tvName.setText(name);

        // 태그
        if (!item.isChartMode() || item.getTagIds() == null || item.getTagIds().isEmpty()) {
            holder.loTag.setVisibility(View.GONE);
        } else {
            holder.loTag.setVisibility(View.VISIBLE);
            BaseApplication.getInstance().renderTag(mContext, item, holder.loTag);
        }

        // 차트
        String chartUrl = BaseApplication.getDayCandleChartUrl(item.getCode());
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
