package com.example.xu.myapplication.moduleMy.fragment.presenter;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.example.xu.myapplication.Common;
import com.example.xu.myapplication.R;
import com.example.xu.myapplication.base.BasePresenter;
import com.example.xu.myapplication.httpRequest.MyOkHttp;
import com.example.xu.myapplication.httpRequest.response.GsonResponseHandler;
import com.example.xu.myapplication.httpRequest.response.JsonResponseHandler;
import com.example.xu.myapplication.moduleMy.fragment.activity.orders.MyOrdersActivity;
import com.example.xu.myapplication.moduleMy.fragment.bean.OrdersBean;
import com.example.xu.myapplication.moduleMy.fragment.view.CircleImageView;
import com.example.xu.myapplication.moduleMy.fragment.viewInterface.IMy;
import com.example.xu.myapplication.util.Logger;
import com.example.xu.myapplication.util.SPUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import q.rorbin.badgeview.QBadgeView;

/**
 * Created by 逝 on 2017/09/18.
 */

public class MyPresenter extends BasePresenter {
    private IMy view;
    private SPUtil util;

    public MyPresenter(IMy view) {
        this.view = view;
        util = new SPUtil(this.view.getCon());
    }

    /**
     * 跳转到 MyOrdersActivity
     *
     * @param value 对应MyOrdersActivity中第几个fragment
     */
    public void toMyOrdersActivity(int value) {
        Intent intent = new Intent(view.getCon(), MyOrdersActivity.class);
        intent.putExtra("order", value);
        view.getAct().startActivity(intent);
    }

    /**
     * 跳转Activity
     */
    public void toActivity(Class<?> cls0, Class<?> cls1) {
        if (cls1 == null) {
            view.getAct().startActivity(new Intent(view.getCon(), cls0));
            return;
        }

        if (TextUtils.equals(util.getString(SPUtil.IS_USER, ""), "")) {
            view.getAct().startActivity(new Intent(view.getCon(), cls0));
        } else {
            view.getAct().startActivity(new Intent(view.getCon(), cls1));
        }
    }

    /**
     * 显示BadgeView 标记
     *
     * @param tv
     * @param badgeNumber
     */
    private void showBadgeView(TextView tv, int badgeNumber) {
        if (badgeNumber == 0) {
            return;
        }
        QBadgeView badge = new QBadgeView(view.getCon());
        badge.bindTarget(tv);
        badge.setBadgeGravity(Gravity.END | Gravity.TOP);
        badge.setBadgeTextColor(view.getCon().getResources().getColor(R.color.color_White));
        badge.setBadgeBackgroundColor(view.getCon().getResources().getColor(R.color.colorPrimary));
        badge.setBadgeNumber(badgeNumber);
        badge.setBadgePadding(1, false);
    }

    /**
     * 根据用户手机号 获取用户所有信息
     * @param refreshMy
     * @param ivMyHead  头像控件
     * @param tvMyUserName 昵称控件
     * @param tv1 需要显示的右上角角标的控件
     * @param tv2 ...
     * @param tv3 ...
     */
    public void getUser(final SwipeRefreshLayout refreshMy, final CircleImageView ivMyHead, final TextView tvMyUserName,
                        final TextView tv1, final TextView tv2, final TextView tv3) {
        showBadgeView(tv1, 0);
        showBadgeView(tv2, 0);
        showBadgeView(tv3, 0);

        String phone = util.getString(SPUtil.IS_USER, "");
        if (TextUtils.equals(util.getString(SPUtil.IS_USER, ""), "")) {
            ivMyHead.setImageDrawable(view.getCon().getResources().getDrawable(R
                    .mipmap.iv_head));
            tvMyUserName.setText(view.getCon().getResources().getString(R.string.login_register));
            if (refreshMy.isRefreshing()){
                refreshMy.setRefreshing(false);
            }
            return;
        }
        //刷新
//        if (!refreshMy.isRefreshing()){
//            refreshMy.setRefreshing(true);
//        }

        JSONObject jo = new JSONObject();
        try {
            jo.put("phoneNumber", phone);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MyOkHttp.newInstance().postJson(Common.URL_GET_USER, jo, new JsonResponseHandler() {
            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                try {
                    String nickName = response.getString("nickName");
                    String headImage = response.getString("headImage");
                    String orders = response.getString("orders");
                    //昵称
                    if (TextUtils.equals(nickName, "null")) {
                        tvMyUserName.setVisibility(View.GONE);
                    } else {
                        tvMyUserName.setVisibility(View.VISIBLE);
                        tvMyUserName.setText(nickName);
                    }

                    //头像
                    if (TextUtils.equals(headImage, "null")) {
                        ivMyHead.setImageDrawable(view.getCon().getResources().getDrawable(R
                                .mipmap.iv_head));
                    } else {

                    }
                    //订单
                    JSONArray array = new JSONArray(orders);
                    JSONObject jo;
                    int daishou = 0;
                    int pingjia = 0;
                    int tuikuan = 0;
                    for (int i = 0; i < array.length(); i++) {
                        jo = array.getJSONObject(i);
                        if (jo.getInt("orderState") == 1) {
                            daishou++;
                        } else if (jo.getInt("orderState") == 2) {
                            tuikuan++;
                        } else if (jo.getInt("orderState") == 0 && jo.getInt("reviewState") == 1) {
                            pingjia++;
                        }
                        showBadgeView(tv1, daishou);
                        showBadgeView(tv2, pingjia);
                        showBadgeView(tv3, tuikuan);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (refreshMy.isRefreshing()){
                    refreshMy.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(int statusCode, String error_msg) {

            }
        });
    }
}
