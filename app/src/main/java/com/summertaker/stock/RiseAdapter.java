package com.summertaker.stock;

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
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Tag;

import java.util.ArrayList;

public class RiseAdapter extends RecyclerView.Adapter<RiseAdapter.ItemViewHolder> {

    private Context mContext;
    private Resources mResources;
    private ArrayList<Item> mItems;

    public RiseAdapter(Context context, ArrayList<Item> items) {
        this.mContext = context;
        this.mResources = context.getResources();
        this.mItems = items;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView tvId;
        public TextView tvName;
        public TextView tvBuyVolume;
        public TextView tvPrice;
        public TextView tvPof;
        public TextView tvRof;
        public TextView tvVot;
        public LinearLayout loTag;
        public ImageView ivChart;

        private ItemViewHolder(View view) {
            super(view);

            tvId = view.findViewById(R.id.tvId);
            tvName = view.findViewById(R.id.tvName);
            tvBuyVolume = view.findViewById(R.id.tvBuyVolume);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvPof = view.findViewById(R.id.tvPof);
            tvRof = view.findViewById(R.id.tvRof);
            tvVot = view.findViewById(R.id.tvVot);
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
    public RiseAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rise_row, parent, false);
        return new RiseAdapter.ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RiseAdapter.ItemViewHolder holder, int position) {
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

        // 매수 가능 수량(주)
        String buyVolume = Config.NUMBER_FORMAT.format(item.getBuyVolume());
        buyVolume = " (" + String.format(mResources.getString(R.string.format_stock), buyVolume) + ")";
        holder.tvBuyVolume.setText(buyVolume);

        // 가격
        BaseApplication.getInstance().renderPrice(item, holder.tvPrice, null);

        // 등락률
        BaseApplication.getInstance().renderRof(item, null, null, holder.tvRof, null);

        // 전일비
        BaseApplication.getInstance().renderPof(item, holder.tvPof, mResources.getString(R.string.format_money), null, mResources.getString(R.string.format_money));

        // 거래량
        String vot = Config.NUMBER_FORMAT.format(item.getVot());
        vot = String.format(mResources.getString(R.string.format_stock), vot);
        holder.tvVot.setText(vot);

        // 태그
        if (item.getTagIds() == null || item.getTagIds().isEmpty()) {
            holder.loTag.setVisibility(View.GONE);
        } else {
            holder.loTag.setVisibility(View.VISIBLE);
            BaseApplication.getInstance().renderTag(mContext, item, holder.loTag);
        }

        // 차트
        Glide.with(mContext).load(item.getChartUrl()).apply(new RequestOptions()).into(holder.ivChart);
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
