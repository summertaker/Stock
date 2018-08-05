package com.summertaker.stock.portfolio;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
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
import com.summertaker.stock.data.Tag;

import java.util.ArrayList;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ItemViewHolder> {

    private Context mContext;
    //private Resources mResources;
    private int mPosition;

    private ArrayList<Item> mItems;

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView tvId;
        public TextView tvName;
        public TextView tvPrice;
        public TextView tvRof;
        public LinearLayout loTag;
        public ImageView ivChart;

        public ItemViewHolder(View view) {
            super(view);

            tvId = view.findViewById(R.id.tvId);
            tvName = view.findViewById(R.id.tvName);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvRof = view.findViewById(R.id.tvRof);
            loTag = view.findViewById(R.id.loTag);
            ivChart = view.findViewById(R.id.ivChart);

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            //contextMenu.setHeaderTitle(R.string.tag);
            for (Tag tag : BaseApplication.getInstance().getTags()) {
                contextMenu.add((int) tag.getId(), this.getAdapterPosition(), mPosition, tag.getName());
            }
        }
    }

    public PortfolioAdapter(Context context, int position, ArrayList<Item> mItems) {
        this.mContext = context;
        //this.mResources = context.getResources();
        this.mPosition = position;
        this.mItems = mItems;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.portfolio_row, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
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

        // 가격
        BaseApplication.getInstance().renderPrice(item, holder.tvPrice, null);

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
