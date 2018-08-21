package com.summertaker.stock.trader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Portfolio;
import com.summertaker.stock.data.Site;
import com.summertaker.stock.detail.DetailActivity;
import com.summertaker.stock.util.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TraderFragment extends BaseFragment {

    private Callback mEventListener;

    private int mPosition = -1;

    private Site mSite;

    //private int mUrlLoadingCount = 0;
    //private ArrayList<String> mTradeItemUrls = new ArrayList<>();

    private ArrayList<Item> mItems = new ArrayList<>();
    private TraderAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private DividerItemDecoration mDividerItemDecoration;

    private boolean mChartMode = true;

    // Container Activity must implement this interface
    public interface Callback {
        void onTraderFragmentEvent(String event);

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

    public TraderFragment() {
    }

    public static TraderFragment newInstance(int position) {
        TraderFragment fragment = new TraderFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.trader_fragment, container, false);

        mContext = getContext();
        initBaseFragment(mContext, rootView);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mPosition = bundle.getInt("position", 0);
        }

        mSite = BaseApplication.getInstance().getTraderPagerItems().get(mPosition);

        mAdapter = new TraderAdapter(mContext, mItems);
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
                startActivityForResult(intent, Config.ACTIVITY_REQUEST_CODE);
            }

            @Override
            public void onLongClick(View view, int position) {
                mEventListener.onTraderFragmentEvent(Config.PARAM_FINISH);
                //Util.startKakaoStockDeepLink(mContext, mItems.get(position).getCode());
                //mDataManager.updateMyItem(mItems.get(position).getCode(), Config.KEY_FAVORITES);
                //mEventListener.onFragmentEvent(Config.PARAM_DATA_CHANGED);
            }
        }));

        //mDividerItemDecoration = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);

        showBaseProgress();
        parseData();

        return rootView;
    }

    private void parseData() {
        mItems.clear();


        int maxCount = 5;
        for (Item item : BaseApplication.getInstance().getTraderItems()) {
            //Log.e(TAG, item.getName() + " (" + item.getCount() + ") " + item.isBuy() + " / " + item.isSell());

            if (item.isBuy() && mSite.getId().equals(Config.KEY_TRADER_BUY)) {
                if (item.getBuyCount() >= maxCount) {
                    mItems.add(item);
                }
            } else if (item.isSell() && mSite.getId().equals(Config.KEY_TRADER_SELL)) {
                if (item.getSellCount() >= maxCount) {
                    mItems.add(item);
                }
            }
        }

        // 정렬
        if (mSite.getId().equals(Config.KEY_TRADER_BUY)) {
            Collections.sort(mItems, new Comparator<Item>() {
                @Override
                public int compare(Item a, Item b) {
                    if (a.getBuyCount() < b.getBuyCount()) {
                        return 1;
                    } else if (a.getBuyCount() > b.getBuyCount()) {
                        return -1;
                    }
                    return 0;
                }
            });
        } else if (mSite.getId().equals(Config.KEY_TRADER_SELL)) {
            Collections.sort(mItems, new Comparator<Item>() {
                @Override
                public int compare(Item a, Item b) {
                    if (a.getSellCount() < b.getSellCount()) {
                        return 1;
                    } else if (a.getSellCount() > b.getSellCount()) {
                        return -1;
                    }
                    return 0;
                }
            });
        }

        long id = 1;
        for (Item item : mItems) {
            for (Item ti : BaseApplication.getInstance().getRecommendTopItems()) {
                if (ti.getCode().equals(item.getCode())) {
                    item.setNor(ti.getNor());
                    break;
                }

            }
            for (Portfolio portfolio : BaseApplication.getInstance().getPortfolios()) {
                if (portfolio.getCode().equals(item.getCode())) {
                    item.setTagIds(portfolio.getTagIds());
                }
            }
            item.setId(id);
            id++;
        }

        renderData();
    }

    private void renderData() {
        //for (Item item : mItems) {
        //    item.setChartMode(mChartMode);
        //}

        //mRecyclerView.removeItemDecoration(mDividerItemDecoration);
        //if (!mChartMode) {
        //    mRecyclerView.addItemDecoration(mDividerItemDecoration); // Divider
        //}

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
}
