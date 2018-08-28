package com.summertaker.stock.portfolio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.summertaker.stock.detail.DetailActivity;
import com.summertaker.stock.util.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PortfolioFragment extends BaseFragment {

    private Callback mEventListener;

    private int mPosition = -1;

    private ArrayList<Item> mItems = new ArrayList<>();
    private PortfolioAdapter mAdapter;
    private RecyclerView mRecyclerView;
    //private DividerItemDecoration mDividerItemDecoration;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean mIsFirstLoading = true;
    private boolean mChartMode = true;

    // Container Activity must implement this interface
    public interface Callback {
        void onPortfolioFragmentEvent(String event);

        void onFragmentItemSizeChanged(int position, int itemSize);
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

    public PortfolioFragment() {
    }

    public static PortfolioFragment newInstance(int position) {
        PortfolioFragment fragment = new PortfolioFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.portfolio_fragment, container, false);

        mContext = getContext();
        initBaseFragment(mContext, rootView);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mPosition = getArguments().getInt("position", 0);
        }

        mAdapter = new PortfolioAdapter(mContext, mPosition, mItems);
        mAdapter.setHasStableIds(true);

        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Item item = mItems.get(position);

                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra("code", item.getCode());
                startActivityForResult(intent, Config.ACTIVITY_REQUEST_CODE);
            }

            @Override
            public void onLongClick(View view, int position) {
                //mEventListener.onPortfolioFragmentEvent(Config.PARAM_FINISH);
                //Util.startKakaoStockDeepLink(mContext, mItems.get(position).getCode());
            }
        }));

        //mDividerItemDecoration = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);

        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mItems.size() == 0) {
                    mSwipeRefreshLayout.setRefreshing(false);
                } else {
                    renderData();
                }
            }
        });

        /*
        mDataManager.setOnItemTagSaved(new DataManager.ItemTagCallback() {
            @Override
            public void onItemTagSaved() {
                mEventListener.onPortfolioFragmentEvent(Config.PARAM_REFRESH_ALL_FRAGMENT);
                //mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        */

        showBaseProgress();
        parseData();

        return rootView;
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        //Log.e(">>", menuItem.getGroupId() + ", " + menuItem.getItemId());

        int position = menuItem.getOrder();
        if (position == mPosition) {
            //mSwipeRefreshLayout.setRefreshing(true);

            int itemId = menuItem.getItemId();
            Item item = mItems.get(itemId);

            String tagId = String.valueOf(menuItem.getGroupId());
            mDataManager.setItemTagIds(item, tagId);

            // 프래그먼트의 태그 아이디
            //String fragmentTagId = String.valueOf(BaseApplication.getInstance().getTags().get(mPosition).getId());
            //if (item.getTagIds().isEmpty() || !item.getTagIds().contains(fragmentTagId)) {
            //    mItems.remove(itemId);
            //    mAdapter.notifyItemRemoved(itemId);
            //} else {
            //    mAdapter.notifyItemChanged(itemId);
            //}

            //Toast.makeText(mContext, item.getCode() + " " + item.getTagIds(), Toast.LENGTH_SHORT).show();
            //Log.e(TAG, item.getCode() + " / " + item.getTagIds());

            //mDataManager.writePortfolios();

            mDataManager.setOnItemTagSaved(new DataManager.ItemTagCallback() {
                @Override
                public void onItemTagSaved() {
                    mEventListener.onPortfolioFragmentEvent(Config.PARAM_REFRESH_ALL_FRAGMENT);
                }
            });
            mDataManager.saveItemTag(item.getCode(), item.getTagIds());
        }

        return super.onContextItemSelected(menuItem);
    }

    private void parseData() {
        mItems.clear();

        // 프래그먼트의 태그 아이디
        String fragmentTagId = String.valueOf(BaseApplication.getInstance().getTags().get(mPosition).getId());

        //Log.e(TAG, "TagId: " + tagId);
        //Log.e(TAG, "BaseItems.size(): " + BaseApplication.getInstance().getItemPrices().size());

        // 태그 아이디 필터링
        for (Item bi : BaseApplication.getInstance().getItemPrices()) {
            for (Portfolio portfolio : BaseApplication.getInstance().getPortfolios()) {
                if (portfolio.getCode().equals(bi.getCode()) && portfolio.getTagIds().contains(fragmentTagId)) {
                    Item item = new Item();
                    item.setCode(bi.getCode());
                    item.setName(bi.getName());
                    item.setPrice(bi.getPrice());
                    item.setRof(bi.getRof());
                    item.setNor(bi.getNor());
                    item.setTagIds(portfolio.getTagIds());

                    mItems.add(item);
                }
            }
        }

        // 등락률 정렬
        Collections.sort(mItems, new Comparator<Item>() {
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

        long id = 1;
        for (Item item : mItems) {
            item.setId(id);
            id++;
        }

        renderData();
    }

    private void renderData() {
        // 차트
        //long millis = System.currentTimeMillis();
        for (Item item : mItems) {
            item.setChartMode(mChartMode);
            String chartUrl = mChartMode ? BaseApplication.getWeekCandleChartUrl(item.getCode()) :
                    BaseApplication.getDayChartUrl(item.getCode());
            item.setChartUrl(chartUrl);
        }

        //mRecyclerView.removeItemDecoration(mDividerItemDecoration);
        //if (!mChartMode) {
        //    mRecyclerView.addItemDecoration(mDividerItemDecoration); // Divider
        //}

        if (mIsFirstLoading) {
            hideBaseProgress();
            mIsFirstLoading = false;
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        mAdapter.notifyDataSetChanged();
        mEventListener.onFragmentItemSizeChanged(mPosition, mItems.size());
    }

    public void goToTheTop() {
        LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        if (manager != null) {
            manager.scrollToPositionWithOffset(0, 0);
        }
    }

    public void refreshFragment() {
        //mSwipeRefreshLayout.setRefreshing(true);
        parseData();
    }

    public void toggleChart() {
        mChartMode = !mChartMode;
        renderData();
    }

    public void updateFragmentItem(Item newItem) {
        parseData();
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    public int getItemSize() {
        return mItems.size();
    }

    public boolean getChartMode() {
        return mChartMode;
    }
}
