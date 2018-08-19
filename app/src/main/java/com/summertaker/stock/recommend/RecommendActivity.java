package com.summertaker.stock.recommend;

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
import com.summertaker.stock.common.DataManager;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Site;
import com.summertaker.stock.util.SlidingTabLayout;

import java.util.ArrayList;

public class RecommendActivity extends BaseActivity implements RecommendFragment.Callback {

    private boolean mIsActionRefresh = false;

    private ArrayList<Site> mSites = new ArrayList<>();
    private SectionsPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    private int mProgress = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommend_activity);

        mContext = RecommendActivity.this;
        initBaseActivity(mContext);

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runFragment(Config.PARAM_GO_TO_THE_TOP);
            }
        });

        mListMode = false;

        showBaseProgress(0);
        loadItemPrice();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recommend, menu);
        mMenuItemList = menu.findItem(R.id.action_chart);
        //setMenuItemList();
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
            case R.id.action_chart:
                onActionListClick();
                return true;
            case R.id.action_finish:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadItemPrice() {
        setBaseProgressBar(mProgress++);
        mDataManager.setOnItemPriceLoaded(new DataManager.ItemPriceCallback() {
            @Override
            public void onParse(int count) {
                setBaseProgressBar(mProgress++);
            }

            @Override
            public void onLoad() {
                loadRecommendTop();
            }
        });
        mDataManager.loadItemPrice();
    }

    private void loadBaseTrade() {
        setBaseProgressBar(mProgress++);
        mDataManager.setOnBaseTradeLoaded(new DataManager.BaseTradeCallback() {
            @Override
            public void onParse(int count) {
                setBaseProgressBar(mProgress++);
            }

            @Override
            public void onLoad() {
                loadRecommendTop();
            }
        });
        mDataManager.loadBaseTrade();
    }

    private void loadRecommendTop() {
        setBaseProgressBar(mProgress++);
        mDataManager.setOnRecommendTopItemLoaded(new DataManager.RecommendTopItemCallback() {
            @Override
            public void onLoad() {
                init();
            }
        });
        mDataManager.loadRecommendTopItem(this);
    }

    private void init() {
        hideBaseProgress();

        mSites = BaseApplication.getInstance().getRecommendPagerItems();

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
                RecommendFragment fragment = (RecommendFragment) getSupportFragmentManager().findFragmentByTag(tag);
                if (fragment != null) {
                    onFragmentItemSizeChange(position, fragment.getItemSize());
                    mListMode = fragment.getListMode();
                    setMenuItemList();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onRecoFragmentEvent(String event) {
        if (event.equals(Config.PARAM_DATA_CHANGED)) {
            mIsActionRefresh = false;
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
            return RecommendFragment.newInstance(position);
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
        RecommendFragment fragment = (RecommendFragment) getSupportFragmentManager().findFragmentByTag(tag);

        if (fragment != null) {
            if (command.equals(Config.PARAM_GO_TO_THE_TOP)) {
                fragment.goToTheTop();
            } else if (command.equals(Config.PARAM_DO_REFRESH)) {
                fragment.refreshFragment(false);
            } else if (command.equals(Config.PARAM_DATA_CHANGED)) {
                fragment.notifyDataSetChanged();
            } else if (command.equals(Config.PARAM_TOGGLE_CHART)) {
                fragment.toggleChart();
            } else if (command.equals(Config.PARAM_TOGGLE_LIST)) {
                fragment.toggleList();
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
            RecommendFragment fragment = (RecommendFragment) getSupportFragmentManager().findFragmentByTag(tag);
            fragment.updateFragmentItem(item);
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
            RecommendFragment fragment = (RecommendFragment) getSupportFragmentManager().findFragmentByTag(tag);
            fragment.refreshFragment(mIsActionRefresh);
        }

        // 개별 프레그먼트 새로 고침
        //Fragment f = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + mViewPager.getCurrentItem());
        //((BaseFragment) f).refreshFragment();
    }

    /*
    private void startRefreshAnimation() {
        if (mMenuItemRefreshView != null) {
            mMenuItemRefreshView.startAnimation(mRotateAnimation);
        }
    }

    private void stopRefreshAnimation() {
        if (mMenuItemRefreshView != null) {
            mMenuItemRefreshView.clearAnimation();
        }
    }
    */

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
                    Item item = null;
                    for (Item bi : BaseApplication.getInstance().getItemPrices()) {
                        if (bi.getCode().equals(code)) {
                            bi.setTagIds(tagIds);
                            item = bi;
                            break;
                        }
                    }

                    if (item != null) {
                        updateFragmentItem(item);
                    }
                }
            }
        }
    }
}
