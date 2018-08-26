package com.summertaker.stock.news;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.summertaker.stock.R;
import com.summertaker.stock.data.News;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BreakingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private Resources mResources;
    private ArrayList<News> mItems;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROGRESS = 0;

    private DateFormat mDateFormat;
    private Date mToday;

    private OnLoadMoreListener onLoadMoreListener;

    private boolean isMoreLoading = true;

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView tvId;
        public TextView tvTitle;
        public TextView tvElapsed;
        public TextView tvPublished;

        public ItemViewHolder(View view) {
            super(view);

            tvId = view.findViewById(R.id.tvId);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvElapsed = view.findViewById(R.id.tvElapsed);
            tvPublished = view.findViewById(R.id.tvPublished);
        }
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar pBar;

        public ProgressViewHolder(View v) {
            super(v);
            pBar = v.findViewById(R.id.pBar);
        }
    }

    public BreakingListAdapter(Context context, OnLoadMoreListener onLoadMoreListener, ArrayList<News> newsList) {
        this.mContext = context;
        this.mResources = context.getResources();
        this.onLoadMoreListener = onLoadMoreListener;
        this.mItems = newsList;

        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        mToday = cal.getTime();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_list_row, parent, false);
        //return new BreakingListAdapter.ItemViewHolder(itemView);

        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.breaking_list_row, parent, false);
            return new ItemViewHolder(itemView);

        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more, parent, false);
            return new ProgressViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            News news = mItems.get(position);

            // 제목
            String title = news.getTitle();
            title = news.getId() + ". " + title;
            ((ItemViewHolder) holder).tvTitle.setText(Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);

            int elapsed = 0;
            Date published = null;
            String publishedText = news.getPublishedText();
            try {
                published = mDateFormat.parse(publishedText);
                long diff = mToday.getTime() - published.getTime();
                elapsed = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                //Log.e(TAG, "elapsed: " + days);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            publishedText = publishedText.replace("-", ".");

            // 경과일
            String elapsedText;
            if (elapsed == 0) {
                elapsedText = mResources.getString(R.string.today);
                publishedText = publishedText.substring(11, 16); // 시간만 표시
            } else if (elapsed == 1) {
                elapsedText = mResources.getString(R.string.yesterday);
                publishedText = publishedText.substring(11, 16); // 시간만 표시
            } else {
                elapsedText = String.format(mResources.getString(R.string.s_days_ago), String.valueOf(news.getElapsed()));
                publishedText = publishedText.substring(5, 16);
            }

            // 경과일
            ((ItemViewHolder) holder).tvElapsed.setText(elapsedText);

            // 발행일
            ((ItemViewHolder) holder).tvPublished.setText(publishedText);
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

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    public void showLoading() {
        if (isMoreLoading && mItems != null && onLoadMoreListener != null) {
            isMoreLoading = false;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mItems.add(null);
                    notifyItemInserted(mItems.size() - 1);
                    onLoadMoreListener.onLoadMore();
                }
            });
        }
    }

    public void setMore(boolean isMore) {
        this.isMoreLoading = isMore;
    }

    public void dismissLoading() {
        if (mItems != null && mItems.size() > 0) {
            mItems.remove(mItems.size() - 1);
            notifyItemRemoved(mItems.size());
        }
    }

    public void addAll(ArrayList<News> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public void addItemMore(ArrayList<News> items) {
        int sizeInit = mItems.size();
        mItems.addAll(items);
        notifyItemRangeChanged(sizeInit, mItems.size());
    }
}
