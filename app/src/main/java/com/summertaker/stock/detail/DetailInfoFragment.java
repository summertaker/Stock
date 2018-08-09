package com.summertaker.stock.detail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.summertaker.stock.R;
import com.summertaker.stock.WebActivity;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.BaseFragment;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Portfolio;
import com.summertaker.stock.data.Tag;
import com.summertaker.stock.util.Util;

public class DetailInfoFragment extends BaseFragment {

    private Callback mEventListener;

    //private int mPosition = -1;
    private String mCode;
    private Item mItem;

    private boolean mIsLoading = false;

    private ScrollView mScrollView;

    private LinearLayout.LayoutParams mParams;
    private LinearLayout.LayoutParams mParamsNoMargin;

    private LinearLayout mLoTag;

    // Container Activity must implement this interface
    public interface Callback {
        void onDetailInfoFragmentEvent(String event, String field, String value);
        //void onFragmentItemSizeChanged(int position, int itemSize);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            Activity activity = (Activity) context;

            // This makes sure that the container activity has implemented
            // the callback interface. If not, it throws an exception
            try {
                mEventListener = (Callback) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement Listener.");
            }
        }
    }

    public DetailInfoFragment() {
    }

    public static DetailInfoFragment newInstance(int position, String code) {
        DetailInfoFragment fragment = new DetailInfoFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putString("code", code);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.detail_info_fragment, container, false);

        mContext = getContext();
        initBaseFragment(mContext, rootView);

        Bundle bundle = getArguments();
        if (bundle != null) {
            //mPosition = bundle.getInt("position", 0);
            mCode = bundle.getString("code");
        }

        mItem = BaseApplication.getInstance().getItem();

        mScrollView = rootView.findViewById(R.id.svContent);

        // 컨텐츠 래퍼
        LinearLayout loContent = rootView.findViewById(R.id.loContent);
        loContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mEventListener.onDetailInfoFragmentEvent(Config.PARAM_FINISH, null, null);
                return false;
            }
        });

        // 현재가
        TextView tvPrice = rootView.findViewById(R.id.tvPrice);
        BaseApplication.getInstance().renderPrice(mItem, tvPrice, null);

        // 등락률
        TextView tvFlucIcon = rootView.findViewById(R.id.tvFlucIcon);
        TextView tvRof = rootView.findViewById(R.id.tvRof);
        BaseApplication.getInstance().renderRof(mItem, tvFlucIcon, null, tvRof, null);

        // 전일비
        TextView tvPof = rootView.findViewById(R.id.tvPof);
        BaseApplication.getInstance().renderPof(mItem, tvPof, "(%s원)", null, "");

        // 태그
        float density = mContext.getResources().getDisplayMetrics().density;
        int margin = (int) (5 * density);
        mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mParams.setMargins(margin, 0, 0, 0);
        mParamsNoMargin = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mLoTag = rootView.findViewById(R.id.loTag);
        renderTag();

        setSiteLink(rootView);

        // 추천 사유
        TextView tvReason = rootView.findViewById(R.id.tvReason);
        if (mItem.getReason() == null || mItem.getReason().isEmpty()) {
            tvReason.setVisibility(View.GONE);
        } else {
            tvReason.setText(mItem.getReason());
        }

        // 일 차트
        //final String dayChart = "https://ssl.pstatic.net/imgfinance/chart/item/area/day/" + mCode + ".png?sidcode=" + System.currentTimeMillis();
        final String dayChart = "https://fn-chart.dunamu.com/images/kr/stock/d/A" + mCode + ".png?" + System.currentTimeMillis();
        ImageView ivDayChart = rootView.findViewById(R.id.ivDayChart);
        setChart(ivDayChart, dayChart);

        // 주 차트
        final String weekChart = "https://ssl.pstatic.net/imgfinance/chart/item/area/week/" + mCode + ".png?sidcode=" + System.currentTimeMillis();
        ImageView ivWeekChart = rootView.findViewById(R.id.ivWeekChart);
        setChart(ivWeekChart, weekChart);

        // 일봉 차트
        //final String dayCandleChart = "https://ssl.pstatic.net/imgfinance/chart/mobile/candle/day/" + mCode + "_end.png"; // 네이버
        final String dayCandleChart = "https://fn-chart.dunamu.com/images/kr/candle/d/A" + mCode + ".png"; // 다음
        ImageView ivD1Chart = rootView.findViewById(R.id.ivDayCandleChart);
        setChart(ivD1Chart, dayCandleChart);

        // 주봉 차트
        //final String weekCandleChart = "https://ssl.pstatic.net/imgfinance/chart/mobile/candle/week/" + mCode + "_end.png"; // 네이버
        final String weekCandleChart = "https://fn-chart.dunamu.com/images/kr/candle/w/A" + mCode + ".png"; // 다음
        ImageView ivW1Chart = rootView.findViewById(R.id.ivWeekCandleChart);
        setChart(ivW1Chart, weekCandleChart);

        // 월봉 차트
        final String monthCandleChart = "https://fn-chart.dunamu.com/images/kr/candle/m/A" + mCode + ".png"; // 다음
        ImageView ivYear3Chart = rootView.findViewById(R.id.ivMonthCandleChart);
        setChart(ivYear3Chart, monthCandleChart);

        return rootView;
    }

    private void setSiteLink(View rootView) {
        // 네이버 검색
        TextView tvNaverSearch = rootView.findViewById(R.id.tvNaverSearch);
        tvNaverSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.format(Config.URL_NAVER_SEARCH, mItem.getName());
                Intent intent = new Intent(mContext, WebActivity.class);
                intent.putExtra("title", mItem.getName());
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });

        // 네이버 금융
        TextView tvNaverFinance = rootView.findViewById(R.id.tvNaverFinance);
        tvNaverFinance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.format(Config.URL_NAVER_FINANCE, mItem.getCode());
                Intent intent = new Intent(mContext, WebActivity.class);
                intent.putExtra("title", mItem.getName());
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });

        // 다음 검색
        TextView tvDaumSearch = rootView.findViewById(R.id.tvDaumSearch);
        tvDaumSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.format(Config.URL_DAUM_SEARCH, mItem.getName());
                Intent intent = new Intent(mContext, WebActivity.class);
                intent.putExtra("title", mItem.getName());
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });

        // 다음 금융
        TextView tvDaumFinance = rootView.findViewById(R.id.tvDaumFinance);
        tvDaumFinance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.format(Config.URL_DAUM_FINANCE, mItem.getCode());
                Intent intent = new Intent(mContext, WebActivity.class);
                intent.putExtra("title", mItem.getName());
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });
    }

    private void setChart(ImageView imageView, String url) {
        Glide.with(mContext).load(url).apply(new RequestOptions()).into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startKakaoStock();
            }
        });
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mEventListener.onDetailInfoFragmentEvent(Config.PARAM_FINISH, null, null);
                return false;
            }
        });
    }

    private void renderTag() {
        if (BaseApplication.getInstance().getTags().size() == 0) {
            mLoTag.setVisibility(View.GONE);
        } else {
            mLoTag.removeAllViews();

            for (Portfolio pf : BaseApplication.getInstance().getPortfolios()) {
                if (pf.getCode().equals(mCode)) {
                    mItem.setTagIds(pf.getTagIds());
                }
            }

            int i = 0;
            for (Tag tag : BaseApplication.getInstance().getTags()) {
                TextView tv = new TextView(mContext);
                if (i == 0) {
                    tv.setLayoutParams(mParamsNoMargin);
                } else {
                    tv.setLayoutParams(mParams);
                }
                tv.setText(tag.getName());
                tv.setBackground(ContextCompat.getDrawable(mContext, R.drawable.tag_background_xs));
                GradientDrawable drawable = (GradientDrawable) tv.getBackground();

                if (mItem.getTagIds() != null && mItem.getTagIds().contains(String.valueOf(tag.getId()))) {
                    tv.setTextColor(Color.parseColor(Config.TAG_FGC_ON));
                    drawable.setColor(Color.parseColor(Config.TAG_BGC_ON));
                    /*
                    // 글자색
                    if (tag.getFgc() != null && tag.getFgc().length() == 7) {
                        tv.setTextColor(Color.parseColor(tag.getFgc()));
                    }

                    // 배경색
                    if (tag.getBgc() != null && tag.getBgc().length() == 7) {
                        // https://stackoverflow.com/questions/18391830/how-to-programmatically-round-corners-and-set-random-background-colors
                        drawable.setColor(Color.parseColor(tag.getBgc()));
                    }
                    */
                } else {
                    tv.setTextColor(Color.parseColor(Config.TAG_FGC_OFF));
                    drawable.setColor(Color.parseColor(Config.TAG_BGC_OFF));
                }

                tv.setTag(tag.getId());
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String tagId = view.getTag().toString();
                        onTagClick(tagId);
                    }
                });

                mLoTag.addView(tv);
                i++;
            }
        }
    }

    private void onTagClick(String tagId) {
        mDataManager.setItemTagIds(mItem, tagId);

        /*
        if (mItem.getTagIds().contains(tagId)) {
            ArrayList<Portfolio> portfolios = new ArrayList<>();
            for (Portfolio portfolio : BaseApplication.getInstance().getPortfolios()) {
                if (portfolio.getCode().equals(mCode)) {
                    continue;
                }
                portfolios.add(portfolio);
            }
            BaseApplication.getInstance().getPortfolios().clear();
            BaseApplication.getInstance().getPortfolios().addAll(portfolios);
        } else {
            Portfolio portfolio = new Portfolio();
            portfolio.setCode(mCode);
            portfolio.setTagIds(tagId);
            BaseApplication.getInstance().getPortfolios().add(portfolio);
        }

        mDataManager.writePortfolios();
        */

        mEventListener.onDetailInfoFragmentEvent(Config.PARAM_DATA_CHANGED, "tagIds", mItem.getTagIds());
        renderTag();
    }

    /*
    private void onTagClick(final String tagId) {
        if (mIsLoading) {
            Toast.makeText(mContext, getString(R.string.data_processing_is_not_finished), Toast.LENGTH_LONG).show();
            return;
        }

        mIsLoading = true;

        //mMenuItemRefreshView.setVisibility(View.VISIBLE);
        //mMenuItemRefreshView.startAnimation(mRotateAnimation);

        StringRequest strReq = new StringRequest(Request.Method.POST, Config.URL_PORTFOLIO_UPDATE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // 포트폴리오 전체 목록
                ArrayList<Portfolio> portfolios = new ArrayList<>();
                String newTagIds = null;
                try {
                    JSONObject json = new JSONObject(response);
                    JSONArray array = json.getJSONArray("portfolios");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);

                        String code = Util.getString(obj, "code");
                        String tagIds = Util.getString(obj, "tag_ids");

                        Portfolio pf = new Portfolio();
                        pf.setCode(code);
                        pf.setTagIds(tagIds);
                        portfolios.add(pf);

                        if (code.equals(mCode)) {
                            newTagIds = tagIds;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //BaseApplication.getInstance().setPortfolios(portfolios);
                BaseApplication.getInstance().getPortfolios().clear();
                BaseApplication.getInstance().getPortfolios().addAll(portfolios);

                mItem.setTagIds(newTagIds);

                renderTag();
                mIsLoading = false;

                mEventListener.onDetailInfoFragmentEvent(Config.PARAM_DATA_CHANGED, "tagIds", newTagIds);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "ERROR: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag_id", tagId);
                params.put("code", mCode);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }
    */

    private void startKakaoStock() {
        Util.startKakaoStockDeepLink(mContext, mCode);
    }

    public void goToTheTop() {
        //Toast.makeText(mContext, "onToolbarClick()", Toast.LENGTH_SHORT).show();
        mScrollView.scrollTo(0, 0);
    }
}
