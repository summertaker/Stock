package com.summertaker.stock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.summertaker.stock.common.BaseActivity;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.common.DataManager;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Portfolio;
import com.summertaker.stock.data.Trader;
import com.summertaker.stock.detail.DetailActivity;
import com.summertaker.stock.util.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TraderActivity extends BaseActivity {

    private ArrayList<Item> mItems = new ArrayList<>();
    private ArrayList<Item> mItemsBackup = new ArrayList<>();
    private RiseAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton mFab;

    private boolean mIsFirstLoading = true;
    private boolean mIsDataLoading = false;

    private int mBuyPricePerItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trader_activity);

        mContext = TraderActivity.this;
        initBaseActivity(mContext);

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onToolbarClick();
            }
        });

        mTagMode = mDataManager.readPreferences(Config.PREFERENCE_TAG_MODE).equals(Config.PREFERENCE_TAG_MODE_ON);

        String buyPricePerItem = BaseApplication.getInstance().getStringSetting(Config.SETTING_BUY_PRICE_PER_ITEM);
        if (buyPricePerItem != null && !buyPricePerItem.isEmpty()) {
            buyPricePerItem = buyPricePerItem.replaceAll(",", "");
            mBuyPricePerItem = Integer.valueOf(buyPricePerItem);
        }

        mAdapter = new RiseAdapter(mContext, mItems);
        mAdapter.setHasStableIds(true);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Item item = mItems.get(position);
                //Util.startKakaoStockDeepLink(mContext, item.getCode());
                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra("code", item.getCode());
                startActivityForResult(intent, Config.ACTIVITY_REQUEST_CODE);
            }

            @Override
            public void onLongClick(View view, int position) {
                //finish();
                //Item item = mItems.get(position);
                //Util.startKakaoStockDeepLink(mContext, item.getCode());
            }
        }));

        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        showBaseProgress(2);
        loadData();
    }

    private void loadData() {
        if (mIsDataLoading) {
            Toast.makeText(mContext, getString(R.string.loading), Toast.LENGTH_SHORT).show();
            return;
        }

        mIsDataLoading = true;
        if (mIsFirstLoading) {
            setBaseProgressBar(1);
        }
        mDataManager.setOnTraderListLoaded(new DataManager.TraderListCallback() {
            @Override
            public void onLoad(ArrayList<String> urls) {
                //setData(urls);
            }
        });
        mDataManager.loadTraderList();
    }

    private void setData(ArrayList<Item> items) {
        mItemsBackup.clear();

        float low = BaseApplication.getInstance().getFloatSetting(Config.SETTING_LOWEST_ROF); // 최저 등락률
        float high = BaseApplication.getInstance().getFloatSetting(Config.SETTING_HIGHEST_ROF); // 최고 등락률

        for (Item item : items) {
            if (item.getPer() == 0) {
                continue;
            }
            if (low > 0 && item.getRof() < low) { // 최저 등락률
                continue;
            }
            if (high > 0 && item.getRof() > high) { // 최고 등락률
                continue;
            }

            int buyVolume = 0;
            if (item.getPrice() > 0 && mBuyPricePerItem > 0) {
                buyVolume = mBuyPricePerItem / item.getPrice();
            }
            item.setBuyVolume(buyVolume);

            for (Portfolio portfolio : BaseApplication.getInstance().getPortfolios()) {
                if (portfolio.getCode().equals(item.getCode())) {
                    item.setTagIds(portfolio.getTagIds());
                }
            }

            mItemsBackup.add(item);
        }

        // 등락률 정렬
        Collections.sort(mItemsBackup, new Comparator<Item>() {
            @Override
            public int compare(Item a, Item b) {
                if (a.getRof() < b.getRof()) {
                    return 1;
                } else if (a.getRof() > b.getRof()) {
                    return -1;
                }
                return 0;
            }
        });

        mItems.clear();
        long id = 1;
        for (Item item : mItemsBackup) {
            if (mTagMode) {
                if (item.getTagIds() != null && !item.getTagIds().isEmpty()) {
                    item.setId(id);
                    mItems.add(item);
                    id++;
                }
            } else {
                item.setId(id);
                mItems.add(item);
                id++;
            }
        }

        renderData();
    }

    @SuppressLint("RestrictedApi")
    private void renderData() {
        long millis = System.currentTimeMillis();
        for (Item item : mItems) {
            item.setChartMode(mChartMode);

            String chartUrl = mChartMode ? BaseApplication.getChartUrl(item.getCode()) : BaseApplication.getDayChartUrl(item.getCode());
            item.setChartUrl(chartUrl);
            item.setChartUrl(chartUrl);
        }

        mAdapter.notifyDataSetChanged();
        setActionBarTitleCount(mItems.size());

        if (mIsFirstLoading) {
            hideBaseProgress();
            mFab.setVisibility(View.VISIBLE);
            mIsFirstLoading = false;
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        mIsDataLoading = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rise, menu);
        mMenuItemTag = menu.findItem(R.id.action_tag);
        setmMenuItemTag();
        mMenuItemChart = menu.findItem(R.id.action_chart);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            //case R.id.action_search:
            //    Intent search = new Intent(this, SearchActivity.class);
            //    startActivity(search);
            //    return true;
            case R.id.action_tag:
                onActionTagClick();
                return true;
            //case R.id.action_chart:
            //    onActionChartClick();
            //    return true;
            case R.id.action_clear:
                return true;
            case R.id.action_refresh:
                mSwipeRefreshLayout.setRefreshing(true);
                loadData();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        //Log.e(">>", menuItem.getGroupId() + ", " + menuItem.getItemId());

        int itemId = menuItem.getItemId();
        Item item = mItems.get(itemId);

        String tagId = String.valueOf(menuItem.getGroupId());
        mDataManager.setItemTagIds(item, tagId);

        if (mTagMode && item.getTagIds().isEmpty()) {
            mItems.remove(itemId);
            mAdapter.notifyItemRemoved(itemId);
        } else {
            mAdapter.notifyItemChanged(itemId);
        }

        //mSwipeRefreshLayout.setRefreshing(true);
        //mDataManager.saveItemTag(item.getCode(), item.getTagIds());

        return super.onContextItemSelected(menuItem);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void onToolbarClick() {
        LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        if (manager != null) {
            manager.scrollToPositionWithOffset(0, 0);
        }
    }

    @Override
    protected void onSwipeRight() {

    }

    @Override
    protected void onSwipeLeft() {

    }
}
