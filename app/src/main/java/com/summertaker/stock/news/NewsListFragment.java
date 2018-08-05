package com.summertaker.stock.news;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.summertaker.stock.R;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.BaseFragment;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.common.DataManager;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.News;
import com.summertaker.stock.util.RecyclerTouchListener;

import java.util.ArrayList;

// RecyclerView + SwipeRefresh + EndlessScroll
// https://github.com/esantiago1/LoadMore-RecyclerView/tree/master/app/src/main/java/com/esantiago/pagination

public class NewsListFragment extends BaseFragment implements NewsListAdapter.OnLoadMoreListener {

    private Callback mEventListener;

    private int mPosition = -1;
    private String mUrl;

    private ArrayList<News> mNewsList = new ArrayList<>();
    private NewsListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int mPage = 1;
    private int mPerPage = -1;
    private boolean mIsFirstLoading = true;
    private boolean mIsDataLoading = false;
    private boolean mIsDataRemains = true;

    // Container Activity must implement this interface
    public interface Callback {
        void onNewsListFragmentEvent(String event);

        void onFragmentItemSizeChanged(int position, int itemSize);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            Activity activity = (Activity) context;

            // This makes sure that the container activity has implemented
            // the callback interface. If not, it throws an exception
            try {
                mEventListener = (Callback) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement Listener.");
            }
        }
    }

    public NewsListFragment() {
    }

    public static NewsListFragment newInstance(int position) {
        NewsListFragment fragment = new NewsListFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.news_list_fragment, container, false);

        mContext = getContext();
        initBaseFragment(mContext, rootView);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mPosition = getArguments().getInt("position", 0);
        }

        mUrl = BaseApplication.getInstance().getNewsPagerItems().get(mPosition).getUrl();

        mAdapter = new NewsListAdapter(mContext, this, mNewsList);

        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL)); // Divider
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                News news = mNewsList.get(position);

                Intent intent = new Intent(mContext, NewsDetailActivity.class);
                intent.putExtra("title", news.getTitle());
                intent.putExtra("publishedText", news.getPublishedText());
                intent.putExtra("elapsed", news.getElapsed());
                intent.putExtra("url", news.getUrl());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {
                mEventListener.onNewsListFragmentEvent(Config.PARAM_FINISH);
            }
        }));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager llManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (llManager != null) {
                    if (dy > 0 && llManager.findLastCompletelyVisibleItemPosition() == (mAdapter.getItemCount() - 2)) {
                        mAdapter.showLoading();
                    }
                }
            }
        });

        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPage = 1;
                loadData();
            }
        });

        showBaseProgress();
        loadData();

        return rootView;
    }

    private void loadData() {
        if (mIsDataLoading) {
            return;
        }

        mIsDataLoading = true;

        String url = mUrl + mPage;
        //Log.e(TAG, "url: " + url);

        mDataManager.setOnNewsListLoaded(new DataManager.NewsListCallback() {
            @Override
            public void onLoad(ArrayList<News> list) {
                if (mPerPage == -1) {
                    mPerPage = list.size();
                }
                if (mPage == 1) {
                    mAdapter.addAll(list);
                    mSwipeRefreshLayout.setRefreshing(false);
                } else {
                    long id = mAdapter.getItemCount();
                    for (News news : list) {
                        news.setId(id);
                        id++;
                    }

                    mAdapter.dismissLoading();
                    mAdapter.addItemMore(list);

                    if (list.size() < mPerPage) {
                        mIsDataRemains = false;
                        mAdapter.setMore(false);
                    } else {
                        mAdapter.setMore(true);
                    }
                }

                renderData();
            }
        });
        mDataManager.loadNewsList(url);
    }

    private void renderData() {
        if (mIsFirstLoading) {
            hideBaseProgress();
            mIsFirstLoading = false;
        }

        mPage++;
        mEventListener.onFragmentItemSizeChanged(mPosition, mNewsList.size());
        mIsDataLoading = false;
    }

    @Override
    public void onLoadMore() {
        //Log.e(TAG, "mIsDataRemains: " + mIsDataRemains);
        if (mIsDataRemains) {
            loadData();
        }
    }

    public void goToTheTop() {
        LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        if (manager != null) {
            manager.scrollToPositionWithOffset(0, 0);
        }
    }

    public void refreshFragment() {

    }

    public void updateFragmentItem(Item newItem) {
        //parseData();
    }

    public void notifyDataSetChanged() {

    }

    public int getItemSize() {
        return mNewsList.size();
    }
}

