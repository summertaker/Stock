package com.summertaker.stock.trade;

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
import com.summertaker.stock.common.DataManager;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Site;
import com.summertaker.stock.detail.DetailActivity;
import com.summertaker.stock.util.RecyclerTouchListener;

import java.util.ArrayList;

public class TradeFragment extends BaseFragment {

    private Callback mEventListener;

    private int mPosition = -1;

    private Site mSite;

    //private int mUrlLoadingCount = 0;
    //private ArrayList<String> mTradeItemUrls = new ArrayList<>();

    private ArrayList<Item> mItems = new ArrayList<>();
    private TradeAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private DividerItemDecoration mDividerItemDecoration;

    private boolean mChartMode = true;

    // Container Activity must implement this interface
    public interface Callback {
        void onTradeFragmentEvent(String event);

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

    public TradeFragment() {
    }

    public static TradeFragment newInstance(int position) {
        TradeFragment fragment = new TradeFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.trade_fragment, container, false);

        mContext = getContext();
        initBaseFragment(mContext, rootView);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mPosition = bundle.getInt("position", 0);
        }

        mSite = BaseApplication.getInstance().getTradePagerItems().get(mPosition);

        mAdapter = new TradeAdapter(mContext, mItems);
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
                mEventListener.onTradeFragmentEvent(Config.PARAM_FINISH);
                //Util.startKakaoStockDeepLink(mContext, mItems.get(position).getCode());
                //mDataManager.updateMyItem(mItems.get(position).getCode(), Config.KEY_FAVORITES);
                //mEventListener.onFragmentEvent(Config.PARAM_DATA_CHANGED);
            }
        }));

        mDividerItemDecoration = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);

        showBaseProgress();

        mDataManager.setOnTradeLoaded(new DataManager.TradeCallback() {
            @Override
            public void onParse(int count) {

            }

            @Override
            public void onLoad(ArrayList<Item> items) {
                //Log.e(TAG, "items.size(): " + items.size());

                long id = 1;
                for (Item item : items) {
                    if (item.isForeigner() && item.isBuy()) {
                        if (mSite.getId().equals(Config.KEY_TRADE_FOREIGNER) && mSite.getGroupId().equals(Config.KEY_TRADE_BUY)) {
                            //-------------------
                            // 외국인 매수
                            //-------------------
                            item.setId(id);
                            mItems.add(item);
                            id++;
                        } else if (mSite.getId().equals(Config.KEY_ACC_TRADE_FOREIGNER) && mSite.getGroupId().equals(Config.KEY_TRADE_BUY)) {
                            //-------------------
                            // 외국인 누적 매수
                            //-------------------
                            item.setId(id);
                            mItems.add(item);
                            id++;
                        }
                    } else if (item.isForeigner() && item.isSell()) {
                        //-------------------
                        // 외국인 매도
                        //-------------------
                        if (mSite.getId().equals(Config.KEY_TRADE_FOREIGNER) && mSite.getGroupId().equals(Config.KEY_TRADE_SELL)) {
                            item.setId(id);
                            mItems.add(item);
                            id++;
                        }
                    } else if (item.isInstitution() && item.isBuy()) {
                        if (mSite.getId().equals(Config.KEY_TRADE_INSTITUTION) && mSite.getGroupId().equals(Config.KEY_TRADE_BUY)) {
                            //-------------------
                            // 기관 매수
                            //-------------------
                            item.setId(id);
                            mItems.add(item);
                            id++;
                        } else if (mSite.getId().equals(Config.KEY_ACC_TRADE_INSTITUTION) && mSite.getGroupId().equals(Config.KEY_TRADE_BUY)) {
                            //-------------------
                            // 기관 누적 매수
                            //-------------------
                            item.setId(id);
                            mItems.add(item);
                            id++;
                        }
                    } else if (item.isInstitution() && item.isSell()) {
                        //-------------------
                        // 기관 매도
                        //-------------------
                        if (mSite.getId().equals(Config.KEY_TRADE_INSTITUTION) && mSite.getGroupId().equals(Config.KEY_TRADE_SELL)) {
                            item.setId(id);
                            mItems.add(item);
                            id++;
                        }
                    } else if (item.isOverseas() && item.isBuy()) {
                        //-------------------
                        // 외국계 증권사 매수
                        //-------------------
                        if (mSite.getId().equals(Config.KEY_TRADE_OVERSEAS) && mSite.getGroupId().equals(Config.KEY_TRADE_BUY)) {
                            item.setId(id);
                            mItems.add(item);
                            id++;
                        }
                    } else if (item.isDomestic() && item.isBuy()) {
                        //-------------------
                        // 국내 증권사 매수
                        //-------------------
                        if (mSite.getId().equals(Config.KEY_TRADE_DOMESTIC) && mSite.getGroupId().equals(Config.KEY_TRADE_BUY)) {
                            item.setId(id);
                            mItems.add(item);
                            id++;
                        }
                    }
                }

                renderData();
            }
        });
        mDataManager.loadTrade(mSite.getId());

        return rootView;
    }

    private void renderData() {
        for (Item item : mItems) {
            item.setChartMode(mChartMode);
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
}
