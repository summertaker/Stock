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
import com.summertaker.stock.detail.DetailActivity;
import com.summertaker.stock.util.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RiseActivity extends BaseActivity {

    private String mSection;

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
        setContentView(R.layout.rise_activity);

        Intent intent = getIntent();
        mSection = intent.getStringExtra("section");

        mContext = RiseActivity.this;
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
                //mSwipeRefreshLayout.setRefreshing(false);
                //loadData();
                renderData();
            }
        });

        /*
        mDataManager.setOnItemTagSaved(new DataManager.ItemTagCallback() {
            @Override
            public void onItemTagSaved() {
                //Toast.makeText(mContext, "Saved!", Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        */

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                //ViewCompat.animate(mFab).rotation(360f).withLayer().setDuration(3000L).setInterpolator(new OvershootInterpolator()).start();
                onFabRefreshClick();
            }
        });

        showBaseProgress(2);
        loadData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
                onActionDeleteClick();
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

    private void loadData() {
        if (mIsDataLoading) {
            Toast.makeText(mContext, getString(R.string.loading), Toast.LENGTH_SHORT).show();
            return;
        }

        mIsDataLoading = true;
        if (mIsFirstLoading) {
            setBaseProgressBar(1);
        }
        mDataManager.setOnFlucLoaded(new DataManager.FlucCallback() {
            @Override
            public void onParse(int count) {
                if (mIsFirstLoading) {
                    setBaseProgressBar(count + 1);
                }
            }

            @Override
            public void onLoad(ArrayList<Item> items) {
                setData(items);
            }
        });
        mDataManager.loadFluc(Config.KEY_FLUC_RISE);
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
            item.setChart(mChartMode);

            String chartUrl = mChartMode ? BaseApplication.getChartUrl(item.getCode(), millis) :
                    BaseApplication.getDayChartUrl(item.getCode(), millis);
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

    private void onToolbarClick() {
        LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        if (manager != null) {
            manager.scrollToPositionWithOffset(0, 0);
        }
    }

    protected void onActionTagClick() {
        mTagMode = !mTagMode;
        String tagMode = mTagMode ? Config.PREFERENCE_TAG_MODE_ON : Config.PREFERENCE_TAG_MODE_OFF;
        mDataManager.writePreferences(Config.PREFERENCE_TAG_MODE, tagMode);

        setmMenuItemTag();

        if (mTagMode) {
            // 선택 목록만 표시하기
            ArrayList<Item> items = new ArrayList<>();
            long id = 1;
            for (Item item : mItems) {
                if (item.getTagIds() != null && !item.getTagIds().isEmpty()) {
                    item.setId(id);
                    items.add(item);
                    id++;
                }
            }
            mItems.clear();
            mItems.addAll(items);
        } else {
            // 전체 목록 표시하기
            mItems.clear();
            mItems.addAll(mItemsBackup);
        }

        mAdapter.notifyDataSetChanged();
        setActionBarTitleCount(mItems.size());
    }

    protected void onActionChartClick() {
        mChartMode = !mChartMode;
        setMenuItemChart();
        renderData();
    }

    private void onActionDeleteClick() {
        if (mTagMode) {
            for (Item item : mItems) {
                item.setTagIds("");
            }
            mItems.clear();
            mAdapter.notifyDataSetChanged();

            for (Item item : mItemsBackup) {
                item.setTagIds("");
            }
            setActionBarTitleCount(mItems.size());
        } else {
            for (int i = 0; i < mItems.size(); i++) {
                mItems.get(i).setTagIds("");
                mAdapter.notifyItemChanged(i);
            }
        }

        BaseApplication.getInstance().getPortfolios().clear();

        //mSwipeRefreshLayout.setRefreshing(true);
        //mDataManager.saveItemTag("clear", "");
        mDataManager.writePortfolios();
    }

    private void onFabRefreshClick() {
        //mSwipeRefreshLayout.setRefreshing(true);
        //loadData();
        renderData();
    }

    @Override
    protected void onSwipeRight() {

    }

    @Override
    protected void onSwipeLeft() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String param = data.getStringExtra(Config.KEY_PARAM);
            if (param != null) {
                //Toast.makeText(mContext, param, Toast.LENGTH_SHORT).show();

                if (param.equals(Config.PARAM_DATA_CHANGED)) {
                    // 종목 상세 화면에서 내용이 변경되어 돌아 온 경우
                    String code = data.getStringExtra("code");
                    String tagIds = data.getStringExtra("tagIds");
                    //Toast.makeText(mContext, code + ": " + tagIds, Toast.LENGTH_LONG).show();

                    // 변경된 태그 정보 업데이트
                    for (int i = 0; i < mItems.size(); i++) {
                        if (mItems.get(i).getCode().equals(code)) {
                            mItems.get(i).setTagIds(tagIds);

                            if ((tagIds == null || tagIds.isEmpty()) && mTagMode) {
                                mItems.remove(i);
                                mAdapter.notifyItemRemoved(i);
                            } else {
                                mAdapter.notifyItemChanged(i);
                            }
                            break;
                        }
                    }

                    setActionBarTitleCount(mItems.size());
                }
            }
        }
    }
}
