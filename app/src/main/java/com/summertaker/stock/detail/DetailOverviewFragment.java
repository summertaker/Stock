package com.summertaker.stock.detail;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.summertaker.stock.R;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.BaseFragment;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.util.Util;

public class DetailOverviewFragment extends BaseFragment {

    private Callback mEventListener;

    private String mCode;
    private Item mItem;

    private ScrollView mScrollView;

    // Container Activity must implement this interface
    public interface Callback {
        void onDetailOverviewFragmentEvent(String event, String field, String value);
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

    public DetailOverviewFragment() {
    }

    public static DetailOverviewFragment newInstance(int position, String code) {
        DetailOverviewFragment fragment = new DetailOverviewFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putString("code", code);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.detail_overview_fragment, container, false);

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
                mEventListener.onDetailOverviewFragmentEvent(Config.PARAM_FINISH, null, null);
                return false;
            }
        });

        // 개요
        TextView tvDescription = rootView.findViewById(R.id.tvDescription);
        tvDescription.setText(Html.fromHtml(mItem.getOverview(), Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);

        return rootView;
    }

    private void startKakaoStock() {
        Util.startKakaoStockDeepLink(mContext, mCode);
    }

    public void goToTheTop() {
        //Toast.makeText(mContext, "onToolbarClick()", Toast.LENGTH_SHORT).show();
        mScrollView.scrollTo(0, 0);
    }
}
