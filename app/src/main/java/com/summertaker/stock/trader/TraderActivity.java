package com.summertaker.stock.trader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.summertaker.stock.R;
import com.summertaker.stock.common.BaseActivity;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.DataManager;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Site;
import com.summertaker.stock.trade.TradeFragment;
import com.summertaker.stock.util.SlidingTabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TraderActivity extends BaseActivity implements TraderFragment.Callback {

    private ArrayList<Site> mSites = new ArrayList<>();
    private SectionsPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    private ArrayList<Item> mItems = new ArrayList<>();

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

        showBaseProgress(0);
        loadTraderList();
        //init();
    }

    private void loadTraderList() {
        mDataManager.setOnTraderListLoaded(new DataManager.TraderListCallback() {
            @Override
            public void onLoad(ArrayList<String> urls) {
                loadTraderItemList(urls);
            }
        });
        mDataManager.loadTraderList();
    }

    private void loadTraderItemList(ArrayList<String> urls) {
        hideBaseProgress();
        showBaseProgress(urls.size());

        mDataManager.setOnTraderItemListLoaded(new DataManager.TraderItemListCallback() {
            @Override
            public void onParse(int count) {
                setBaseProgressBar(count);
            }

            @Override
            public void onLoad() {
                init();
            }
        });
        mDataManager.loadTraderItemList(urls);
    }

    private void init() {
        hideBaseProgress();

        mSites = BaseApplication.getInstance().getTraderPagerItems();

        mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(mPagerAdapter);

        //-------------------------------------------------------------------------------------------------------
        // 뷰페이저 간 이동 시 프레그먼트 자동으로 새로고침 방지
        // https://stackoverflow.com/questions/28494637/android-how-to-stop-refreshing-fragments-on-tab-change
        //-------------------------------------------------------------------------------------------------------
        mViewPager.setOffscreenPageLimit(mSites.size());

        SlidingTabLayout slidingTabLayout = findViewById(R.id.slidingTabs);
        slidingTabLayout.setViewPager(mViewPager);
        slidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //Toast.makeText(mContext, "onPageSelected(): " + position, Toast.LENGTH_SHORT).show();
                String tag = "android:switcher:" + R.id.viewpager + ":" + position;
                TraderFragment fragment = (TraderFragment) getSupportFragmentManager().findFragmentByTag(tag);
                if (fragment != null) {
                    onFragmentItemSizeChange(position, fragment.getItemSize());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TraderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return mSites.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mSites.get(position).getTitle();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trader, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_finish:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void onToolbarClick() {

    }

    @Override
    protected void onSwipeRight() {

    }

    @Override
    protected void onSwipeLeft() {

    }

    @Override
    public void onTraderFragmentEvent(String event) {

    }

    @Override
    public void onFragmentItemSizeChange(int position, int itemSize) {

    }
}
