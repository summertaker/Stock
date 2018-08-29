package com.summertaker.stock.fluctuation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.summertaker.stock.R;
import com.summertaker.stock.SearchActivity;
import com.summertaker.stock.common.BaseActivity;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Site;
import com.summertaker.stock.trade.TradeFragment;
import com.summertaker.stock.util.SlidingTabLayout;

import java.util.ArrayList;

public class FluctuationActivity extends BaseActivity implements FluctuationFragment.Callback {

    private ArrayList<Site> mSites = new ArrayList<>();
    private SectionsPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fluctuation_activity);

        mContext = FluctuationActivity.this;
        initBaseActivity(mContext);

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runFragment(Config.PARAM_GO_TO_THE_TOP);
            }
        });

        showBaseProgress(0);
        init();

        /*
        showBaseProgress(2);
        setBaseProgressBar(1);
        mDataManager.setOnItemPriceLoaded(new DataManager.ItemPriceCallback() {
            @Override
            public void onParse(int count) {
                setBaseProgressBar(count + 1);
            }

            @Override
            public void onLoad() {
                init();
            }
        });
        mDataManager.loadItemPrice();
        */
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fluctuation, menu);
        //mMenuItemChart = menu.findItem(R.id.action_chart);
        //setMenuItemChart();
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
            //case R.id.action_chart:
            //    onActionChartClick();
            //    return true;
            case R.id.action_finish:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        hideBaseProgress();

        mSites = BaseApplication.getInstance().getTradePagerItems();

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
                FluctuationFragment fragment = (FluctuationFragment) getSupportFragmentManager().findFragmentByTag(tag);
                if (fragment != null) {
                    onFragmentItemSizeChange(position, fragment.getItemSize());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onFluctuationFragmentEvent(String event) {
        if (event.equals(Config.PARAM_LOAD_STARTED)) {
            //startRefreshAnimation();
        } else if (event.equals(Config.PARAM_LOAD_FINISHED)) {
            //stopRefreshAnimation();
        } else if (event.equals(Config.PARAM_DATA_CHANGED)) {
            refreshAllFragment();
        } else if (event.equals(Config.PARAM_FINISH)) {
            finish();
        }
    }

    @Override
    public void onFragmentItemSizeChange(int position, int itemSize) {
        if (mViewPager != null && position == mViewPager.getCurrentItem()) {
            setActionBarTitleCount(itemSize);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TradeFragment.newInstance(position);
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

    public void runFragment(String command) {
        //--------------------------------------------------------------------------------------------
        // 프레그먼트에 이벤트 전달하기
        // https://stackoverflow.com/questions/34861257/how-can-i-set-a-tag-for-viewpager-fragments
        //--------------------------------------------------------------------------------------------
        String tag = "android:switcher:" + R.id.viewpager + ":" + mViewPager.getCurrentItem();
        FluctuationFragment fragment = (FluctuationFragment) getSupportFragmentManager().findFragmentByTag(tag);

        if (fragment != null) {
            if (command.equals(Config.PARAM_GO_TO_THE_TOP)) {
                fragment.goToTheTop();
            } else if (command.equals(Config.PARAM_DO_REFRESH)) {
                fragment.refreshFragment();
            } else if (command.equals(Config.PARAM_DATA_CHANGED)) {
                fragment.notifyDataSetChanged();
            } else if (command.equals(Config.PARAM_TOGGLE_CHART)) {
                fragment.toggleChart();
            }
        }
    }

    public void updateFragmentItem(Item item) {
        //--------------------------------------------------------------------------------------------
        // 프레그먼트에 이벤트 전달하기
        // https://stackoverflow.com/questions/34861257/how-can-i-set-a-tag-for-viewpager-fragments
        //--------------------------------------------------------------------------------------------
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            String tag = "android:switcher:" + R.id.viewpager + ":" + i;
            FluctuationFragment fragment = (FluctuationFragment) getSupportFragmentManager().findFragmentByTag(tag);
            if (fragment != null) {
                fragment.updateFragmentItem(item);
            }
        }
    }

    public void refreshAllFragment() {
        //--------------------------------------------------------------------------------------------
        // 프레그먼트에 이벤트 전달하기
        // https://stackoverflow.com/questions/34861257/how-can-i-set-a-tag-for-viewpager-fragments
        //--------------------------------------------------------------------------------------------

        // 모든 프레그먼트 새로 고침
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            String tag = "android:switcher:" + R.id.viewpager + ":" + i;
            FluctuationFragment fragment = (FluctuationFragment) getSupportFragmentManager().findFragmentByTag(tag);
            if (fragment != null) {
                fragment.refreshFragment();
            }
        }

        // 개별 프레그먼트 새로 고침
        //Fragment f = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + mViewPager.getCurrentItem());
        //((BaseFragment) f).refreshFragment();
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

                    // 변경된 포트폴리오 태그 정보 업데이트
                    Item newItem = null;
                    for (Item item : BaseApplication.getInstance().getItemPrices()) {
                        if (item.getCode().equals(code)) {
                            newItem = item;
                            newItem.setTagIds(tagIds);
                            break;
                        }
                    }

                    if (newItem != null) {
                        updateFragmentItem(newItem);
                    }
                }
            }
        }
    }
}
