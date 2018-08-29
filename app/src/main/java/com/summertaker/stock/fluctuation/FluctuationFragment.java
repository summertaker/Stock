package com.summertaker.stock.fluctuation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.summertaker.stock.R;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.BaseFragment;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.common.DataManager;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Portfolio;
import com.summertaker.stock.data.Site;
import com.summertaker.stock.detail.DetailActivity;
import com.summertaker.stock.util.RecyclerTouchListener;

import java.util.ArrayList;

public class FluctuationFragment extends BaseFragment {

    private Callback mEventListener;

    private int mPosition = -1;

    private Site mSite;

    //private int mUrlLoadingCount = 0;
    //private ArrayList<String> mTradeItemUrls = new ArrayList<>();

    private ArrayList<Item> mItems = new ArrayList<>();
    private FluctuationAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private DividerItemDecoration mDividerItemDecoration;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean mChartMode = true;

    // Container Activity must implement this interface
    public interface Callback {
        void onFluctuationFragmentEvent(String event);

        void onFragmentItemSizeChange(int position, int itemSize);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            Activity activity = (Activity) context;

            try {
                mEventListener = (Callback) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement Listener.");
            }
        }
    }

    public FluctuationFragment() {
    }

    public static FluctuationFragment newInstance(int position) {
        FluctuationFragment fragment = new FluctuationFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fluctuation_fragment, container, false);

        mContext = getContext();
        initBaseFragment(mContext, rootView);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mPosition = bundle.getInt("position", 0);
        }

        mSite = BaseApplication.getInstance().getFluctuationPagerItems().get(mPosition);

        mAdapter = new FluctuationAdapter(mContext, mPosition, mItems);
        mAdapter.setHasStableIds(true);

        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Item item = mItems.get(position);
                //Toast.makeText(mContext, item.getName(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra("code", item.getCode());
                intent.putExtra("nor", item.getNor());
                startActivityForResult(intent, Config.ACTIVITY_REQUEST_CODE);
            }

            @Override
            public void onLongClick(View view, int position) {
                //mEventListener.onFluctuationFragmentEvent(Config.PARAM_FINISH);

                //Util.startKakaoStockDeepLink(mContext, mItems.get(position).getCode());
                //mDataManager.updateMyItem(mItems.get(position).getCode(), Config.KEY_FAVORITES);
                //mEventListener.onFragmentEvent(Config.PARAM_DATA_CHANGED);
            }
        }));

        mDividerItemDecoration = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);

        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        showBaseProgress();

        mDataManager.setOnFlucLoaded(new DataManager.FluctuationCallback() {
            @Override
            public void onParse(int count) {

            }

            @Override
            public void onLoad(ArrayList<Item> items) {
                //Log.e(TAG, "items.size(): " + items.size());
                mItems.clear();

                float low = BaseApplication.getInstance().getFloatSetting(Config.SETTING_LOWEST_ROF); // 최저 등락률
                float high = BaseApplication.getInstance().getFloatSetting(Config.SETTING_HIGHEST_ROF); // 최고 등락률

                long id = 1;
                for (Item item : items) {
                    if (mSite.getId().equals(Config.KEY_FLUCTUATION_RISE) || mSite.getId().equals(Config.KEY_FLUCTUATION_JUMP)) {
                        if (low > 0 && item.getRof() < low) { // 최저 등락률
                            continue;
                        }
                        if (high > 0 && item.getRof() > high) { // 최고 등락률
                            continue;
                        }
                    }

                    for (Item ti : BaseApplication.getInstance().getRecommendTopItems()) { // 추천수
                        if (item.getCode().equals(ti.getCode())) {
                            item.setNor(ti.getNor());
                        }
                    }

                    for (Portfolio portfolio : BaseApplication.getInstance().getPortfolios()) { // 태그
                        if (portfolio.getCode().equals(item.getCode())) {
                            item.setTagIds(portfolio.getTagIds());
                        }
                    }

                    item.setId(id);
                    mItems.add(item);
                    id++;
                }
                renderData();
            }
        });
        mDataManager.loadFluctuation(mSite.getId());

        return rootView;
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        //Log.e(">>", menuItem.getGroupId() + ", " + menuItem.getItemId());

        int position = menuItem.getOrder();
        if (position == mPosition) {
            int itemId = menuItem.getItemId();
            Item item = mItems.get(itemId);

            String tagId = String.valueOf(menuItem.getGroupId());
            mDataManager.setItemTagIds(item, tagId);

            //if (item.getTagIds().isEmpty()) {
            //    mItems.remove(itemId);
            //    mAdapter.notifyItemRemoved(itemId);
            //} else {
            mAdapter.notifyItemChanged(itemId);
            //}

            mSwipeRefreshLayout.setRefreshing(true);
            mDataManager.setOnItemTagSaved(new DataManager.ItemTagCallback() {
                @Override
                public void onItemTagSaved() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
            mDataManager.saveItemTag(item.getCode(), item.getTagIds());
        }

        return super.onContextItemSelected(menuItem);
    }

    private void renderData() {
        for (Item item : mItems) {
            item.setChartMode(mChartMode);
            String chartUrl = mChartMode ? BaseApplication.getWeekChartUrl(item.getCode()) : BaseApplication.getDayChartUrl(item.getCode());
            item.setChartUrl(chartUrl);
        }

        mRecyclerView.removeItemDecoration(mDividerItemDecoration);
        if (!mChartMode) {
            mRecyclerView.addItemDecoration(mDividerItemDecoration); // Divider
        }

        hideBaseProgress();
        mAdapter.notifyDataSetChanged();

        // Activity 제목에 갯수 출력하기
        mEventListener.onFragmentItemSizeChange(mPosition, mItems.size());
        //mEventListener.onFragmentEvent(Config.PARAM_LOAD_FINISHED);
    }

    public void goToTheTop() {
        LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        if (manager != null) {
            manager.scrollToPositionWithOffset(0, 0);
        }
    }

    public void refreshFragment() {
        renderData();
    }

    public void updateFragmentItem(Item newItem) {
        for (Item item : mItems) {
            if (item.getCode().equals(newItem.getCode())) {
                item.setTagIds(newItem.getTagIds());
                break;
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    public void toggleChart() {
        mChartMode = !mChartMode;
        renderData();
    }

    public int getItemSize() {
        return mItems.size();
    }

    public boolean getChartMode() {
        return mChartMode;
    }
}
