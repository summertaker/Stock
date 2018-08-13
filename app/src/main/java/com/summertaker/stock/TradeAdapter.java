package com.summertaker.stock;

import android.content.Context;
import android.content.res.Resources;
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
import com.summertaker.stock.common.Config;
import com.summertaker.stock.data.Item;

import java.util.ArrayList;

public class TradeAdapter extends RecyclerView.Adapter<TradeAdapter.ItemViewHolder> {

    private Context mContext;
    private Resources mResources;
    private ArrayList<Item> mItems;

    public TradeAdapter(Context context, ArrayList<Item> items) {
        this.mContext = context;
        this.mResources = context.getResources();
        this.mItems = items;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView tvId;
        public TextView tvName;
        public LinearLayout loPrice;
        public LinearLayout loPriceL;
        public TextView tvPrice;
        public TextView tvPriceL;
        public TextView tvFlucIcon;
        public TextView tvFlucIconL;
        public TextView tvRof;
        public TextView tvRofL;
        public LinearLayout loPot;
        //public LinearLayout loPotL;
        public TextView tvPot;
        public TextView tvPotL;
        public TextView tvVot;
        public TextView tvVotL;
        public LinearLayout loTag;
        public ImageView ivChart;

        private ItemViewHolder(View view) {
            super(view);

            tvId = view.findViewById(R.id.tvId);
            tvName = view.findViewById(R.id.tvName);
            loPrice = view.findViewById(R.id.loPrice);
            loPriceL = view.findViewById(R.id.loPriceL);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvPriceL = view.findViewById(R.id.tvPriceL);
            tvFlucIcon = view.findViewById(R.id.tvFlucIcon);
            tvFlucIconL = view.findViewById(R.id.tvFlucIconL);
            tvRof = view.findViewById(R.id.tvRof);
            tvRofL = view.findViewById(R.id.tvRofL);
            loPot = view.findViewById(R.id.loPot);
            //loPotL = view.findViewById(R.id.loPotL);
            tvPot = view.findViewById(R.id.tvPot);
            tvPotL = view.findViewById(R.id.tvPotL);
            tvVot = view.findViewById(R.id.tvVot);
            tvVotL = view.findViewById(R.id.tvVotL);
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

        // 가격
        BaseApplication.getInstance().renderPrice(item, holder.tvPrice, holder.tvPriceL);

        // 등락률
        BaseApplication.getInstance().renderRof(item, holder.tvFlucIcon, holder.tvFlucIconL, holder.tvRof, holder.tvRofL);

        // 태그
        if (!item.isChartMode() || item.getTagIds() == null || item.getTagIds().isEmpty()) {
            holder.loTag.setVisibility(View.GONE);
        } else {
            holder.loTag.setVisibility(View.VISIBLE);
            BaseApplication.getInstance().renderTag(mContext, item, holder.loTag);
        }

        // 거래량
        String vot = Config.NUMBER_FORMAT.format(item.getVot());
        vot = vot + "주";
        holder.tvVot.setText(vot);
        holder.tvVotL.setText(vot);

        // 거래금액
        String pot = " - ";
        if (item.getPot() > 0) {
            pot = Config.NUMBER_FORMAT.format(item.getPot()) + "백만원";
        }
        holder.tvPot.setText(pot);
        holder.tvPotL.setText(pot);

        // 표시 토글
        if (item.isChartMode()) {
            // 차트 모드
            holder.loPrice.setVisibility(View.VISIBLE);
            holder.loPriceL.setVisibility(View.GONE);
            holder.loPot.setVisibility(View.VISIBLE);
            //holder.loPotL.setVisibility(View.GONE);
            holder.ivChart.setVisibility(View.VISIBLE);

            // 차트
            String chartUrl = BaseApplication.getDayCandleChartUrl(item.getCode());
            Glide.with(mContext).load(chartUrl).apply(new RequestOptions()).into(holder.ivChart);
        } else {
            // 리스트 모드
            holder.loPrice.setVisibility(View.GONE);
            holder.loPriceL.setVisibility(View.VISIBLE);
            holder.loPot.setVisibility(View.GONE);
            //holder.loPotL.setVisibility(View.VISIBLE);
            holder.ivChart.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).getId();
    }

    /*
    public void refresh(ArrayList<Item> newList) {
        ItemDiffCallback callback = new ItemDiffCallback(mItems, newList);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback);

        this.mItems.clear();
        this.mItems.addAll(newList);

        //diffResult.dispatchUpdatesTo(this);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                diffResult.dispatchUpdatesTo(FlucAdapter.this);
            }
        });
    }
    */
}
