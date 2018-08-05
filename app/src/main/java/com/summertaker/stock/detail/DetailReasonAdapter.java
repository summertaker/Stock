package com.summertaker.stock.detail;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.summertaker.stock.R;
import com.summertaker.stock.data.Reason;

import java.util.ArrayList;

public class DetailReasonAdapter extends RecyclerView.Adapter<DetailReasonAdapter.ItemViewHolder> {

    private Context mContext;
    private ArrayList<Reason> mReasons;

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView tvBroker;
        public TextView tvPortfolio;
        public TextView tvPublished;
        public TextView tvContent;

        public ItemViewHolder(View view) {
            super(view);

            tvBroker = view.findViewById(R.id.tvBroker);
            tvPortfolio = view.findViewById(R.id.tvPortfolio);
            tvPublished = view.findViewById(R.id.tvPublished);
            tvContent = view.findViewById(R.id.tvContent);
        }
    }

    public DetailReasonAdapter(Context context, ArrayList<Reason> reasons) {
        this.mContext = context;
        this.mReasons = reasons;
    }

    @Override
    public DetailReasonAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_reason_row, parent, false);
        return new DetailReasonAdapter.ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DetailReasonAdapter.ItemViewHolder holder, int position) {
        Reason reason = mReasons.get(position);

        // 증권사
        String broker = reason.getBroker();
        //Log.e(">>", "broker: " + broker);
        holder.tvBroker.setText(broker);

        // 포트폴리오
        String portfolio = reason.getPortfolio();
        holder.tvPortfolio.setText(portfolio);

        // 날짜
        String published = reason.getPublished();
        holder.tvPublished.setText(published);

        // 내용
        String content = reason.getContent();
        holder.tvContent.setText(content);
    }

    @Override
    public int getItemCount() {
        return mReasons.size();
    }

    @Override
    public long getItemId(int position) {
        return mReasons.get(position).getId();
    }
}

