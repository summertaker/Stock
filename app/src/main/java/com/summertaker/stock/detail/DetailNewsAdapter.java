package com.summertaker.stock.detail;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.summertaker.stock.R;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.News;

import java.util.ArrayList;

public class DetailNewsAdapter extends RecyclerView.Adapter<DetailNewsAdapter.ItemViewHolder> {

    private Context mContext;
    private Resources mResources;
    private ArrayList<News> mNewsList;
    private Item mItem;

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView tvId;
        public TextView tvTitle;
        public TextView tvElapsed;
        public TextView tvPublished;
        //public TextView tvSummary;

        public ItemViewHolder(View view) {
            super(view);

            tvId = view.findViewById(R.id.tvId);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvElapsed = view.findViewById(R.id.tvElapsed);
            tvPublished = view.findViewById(R.id.tvPublished);
            //tvSummary = view.findViewById(R.id.tvSummary);
        }
    }

    public DetailNewsAdapter(Context context, ArrayList<News> newsList, Item item) {
        this.mContext = context;
        this.mResources = context.getResources();
        this.mNewsList = newsList;
        this.mItem = item;
    }

    @Override
    public DetailNewsAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_news_row, parent, false);
        return new DetailNewsAdapter.ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DetailNewsAdapter.ItemViewHolder holder, int position) {
        News news = mNewsList.get(position);

        // 제목
        String title = news.getTitle();
        title = news.getId() + ". " + title;
        if (mItem == null) {
            holder.tvTitle.setText(title);
        } else {
            title = title.replace(mItem.getName(), String.format(Config.NEWS_ITEM_NAME_HIGHLIGHT_FORMAT, mItem.getName()));
            holder.tvTitle.setText(Html.fromHtml(title), TextView.BufferType.SPANNABLE);
        }

        // 경과일, 발행일 설정
        String elapsed;
        String publishedText = news.getPublishedText(); // 0000-00-00 00:00:00
        if (publishedText == null || publishedText.isEmpty()) {
            elapsed = "";
            publishedText = "";
        } else {
            publishedText = publishedText.substring(5, 16); // 년도, 초 잘라내기
            publishedText = publishedText.replace("-", ".");

            // 경과일
            if (news.getElapsed() == 0) {
                elapsed = mResources.getString(R.string.today);
                publishedText = publishedText.substring(6, 11); // 시간만 표시
            } else if (news.getElapsed() == 1) {
                elapsed = mResources.getString(R.string.yesterday);
            } else {
                elapsed = String.format(mResources.getString(R.string.s_days_ago), String.valueOf(news.getElapsed()));
            }
        }

        // 경과일
        holder.tvElapsed.setText(elapsed);

        // 날짜
        holder.tvPublished.setText(publishedText);

        // 내용
        //String summary = item_news.getSummary();
        //if (mItemName == null || mItemName.isEmpty()) {
        //    holder.tvSummary.setText(summary);
        //} else {
        //    summary = summary.replace(mItemName, "<font color='#1565C0'>" + mItemName + "</font>");
        //    holder.tvSummary.setText(Html.fromHtml(summary), TextView.BufferType.SPANNABLE);
        //}
    }

    @Override
    public int getItemCount() {
        return mNewsList.size();
    }

    @Override
    public long getItemId(int position) {
        return mNewsList.get(position).getId();
    }
}
