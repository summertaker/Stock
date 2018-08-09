package com.summertaker.stock.news;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.summertaker.stock.R;
import com.summertaker.stock.SearchActivity;
import com.summertaker.stock.common.BaseActivity;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.common.DataManager;
import com.summertaker.stock.data.News;
import com.summertaker.stock.util.RecyclerTouchListener;

import java.util.ArrayList;

public class BreakingListActivity extends BaseActivity implements BreakingListAdapter.OnLoadMoreListener {

    private ArrayList<News> mNewsList = new ArrayList<>();
    private BreakingListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int mPage = 1;
    private int mPerPage = -1;
    private boolean mIsFirstLoading = true;
    private boolean mIsDataLoading = false;
    private boolean mIsDataRemains = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.breaking_list_activity);

        mContext = BreakingListActivity.this;
        initBaseActivity(mContext);

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToolbarClick();
            }
        });

        mAdapter = new BreakingListAdapter(mContext, this, mNewsList);

        mRecyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL)); // Divider
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                News news = mNewsList.get(position);
                Intent intent = new Intent(mContext, NewsDetailActivity.class);
                intent.putExtra("url", news.getUrl());
                intent.putExtra("published", news.getPublished());
                //startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {
                finish();
            }
        }));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager llManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (dy > 0 && llManager.findLastCompletelyVisibleItemPosition() == (mAdapter.getItemCount() - 2)) {
                    mAdapter.showLoading();
                }
            }
        });

        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPage = 1;
                loadData();
            }
        });

        showBaseProgress(0);
        loadData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.breaking, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                Intent search = new Intent(this, SearchActivity.class);
                startActivity(search);
                return true;
            //case R.id.action_settings:
            //    Intent wordCategory = new Intent(this, WordCategoryActivity.class);
            //    startActivity(wordCategory);
            //    return true;
            case R.id.action_finish:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    private void loadBaseItem() {
        mDataManager.setOnBaseItemLoaded(new DataManager.BaseItemCallback() {
            @Override
            public void onParse(int count) {

            }

            @Override
            public void onLoad() {
                loadData();
            }
        });
        mDataManager.loadBaseItem();
    }
    */

    private void loadData() {
        if (mIsDataLoading) {
            return;
        }

        mIsDataLoading = true;
        mDataManager.setOnBreakingLoaded(new DataManager.BreakingCallback() {
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

        String url = Config.URL_BREAKING_LIST + mPage;
        mDataManager.loadBreaking(url);
    }

    private void renderData() {
        if (mIsFirstLoading) {
            hideBaseProgress();
            mIsFirstLoading = false;
        }

        mPage++;
        mIsDataLoading = false;
    }

    @Override
    public void onLoadMore() {
        //Log.e(TAG, "mIsDataRemains: " + mIsDataRemains);
        if (mIsDataRemains) {
            loadData();
        }
    }

    public void onToolbarClick() {
        LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        manager.scrollToPositionWithOffset(0, 0);
    }

    public int getItemSize() {
        return mNewsList.size();
    }

    @Override
    protected void onSwipeRight() {

    }

    @Override
    protected void onSwipeLeft() {

    }
}
