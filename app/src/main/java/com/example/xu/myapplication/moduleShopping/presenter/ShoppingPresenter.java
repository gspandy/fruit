package com.example.xu.myapplication.moduleShopping.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.xu.myapplication.Common;
import com.example.xu.myapplication.R;
import com.example.xu.myapplication.base.BasePresenter;
import com.example.xu.myapplication.httpRequest.MyOkHttp;
import com.example.xu.myapplication.httpRequest.response.JsonResponseHandler;
import com.example.xu.myapplication.httpRequest.response.RawResponseHandler;
import com.example.xu.myapplication.moduleShopping.adapter.ShoppingCarAdapter;
import com.example.xu.myapplication.moduleShopping.bean.FruitBean;
import com.example.xu.myapplication.moduleShopping.viewInterface.IShopping;
import com.example.xu.myapplication.util.Logger;
import com.example.xu.myapplication.util.SPUtil;
import com.example.xu.myapplication.util.ToastUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 逝 on 2017/09/18.
 */

public class ShoppingPresenter extends BasePresenter {
    private IShopping view;

    private SPUtil util;

    public ShoppingPresenter(IShopping view) {
        this.view = view;
        util = new SPUtil(this.view.getCon());
    }

    /**
     * 跳转Activity
     */
    public void toActivity(Class<?> cls, TextView tv) {
        List<FruitBean> list = new ArrayList<FruitBean>();
        int size = 0;
        for (int i = 0; i < view.getListSize(); i++) {
            if (view.getListItem(i).isChecked()) {
                list.add(view.getListItem(i));
                size++;
            }
        }
        if (size == 0) {
            ToastUtils.showToast(view.getCon(), "请选择商品哦！");
            return;
        }
        Intent intent = new Intent(view.getCon(), cls);
        Bundle bundle = new Bundle();
        bundle.putSerializable("par_orders", (Serializable) list);
        bundle.putString("money", tv.getText().toString().trim());
        intent.putExtras(bundle);
        view.getAct().startActivity(intent);
    }

    /*
    获取购物车列表
     */
    public void addList() {
        if (!view.isRefresh()) {
            view.setRefresh(true);
        }

        String phone = util.getString(SPUtil.IS_USER, "");
        if (TextUtils.equals(util.getString(SPUtil.IS_USER, ""), "")) {
            view.clearList();
            if (view.isRefresh()) {
//                view.setAdapterData(view.getList());
                view.setTvShoppingCartText("购物车(0)");
                view.setRefresh(false);
            }
            return;
        }

        JSONObject jo = new JSONObject();
        try {
            jo.put("phoneNumber", phone);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MyOkHttp.newInstance().postJson(Common.URL_GET_USER, jo, new JsonResponseHandler() {
            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                view.clearList();
                try {
                    JSONArray array = new JSONArray(response.getString("shoppingCars"));
                    if (array.length() == 0) {
                        view.setCbSelect(false);
                        view.setTvShoppingMoneyText("￥0.00");
                        view.setTvShoppingCartText("购物车(0)");
                        view.setRefresh(false);
                        return;
                    }
                    FruitBean bean = null;
                    JSONObject object;
                    JSONObject json;
                    Gson gson = new Gson();
                    for (int i = 0; i < array.length(); i++) {
                        object = array.getJSONObject(i);
                        int count = object.getInt("goodsCount");
                        int id = object.getInt("id");
                        String goods = object.getString("goods");
                        json = new JSONObject(goods);
                        int goodsId = json.getInt("id");
                        String goodsName = json.getString("goodsName");
                        double goodsPrice = json.getDouble("goodsPrice");
                        String goodsImage = json.getString("goodsImage");

                        FruitBean.GoodsBean goodsBean = gson.fromJson(goods, FruitBean.GoodsBean.class);

                        bean = new FruitBean(id, goodsName, goodsId, goodsPrice, count,
                                goodsImage, false, goodsBean);
                        view.listAddItem(bean);
                    }
                    if (!view.isEmptyList())
                        view.setAdapterData(view.getList());
                    Logger.e("购物车", view.getListSize() + "");
                    view.setTvShoppingCartText("购物车(" + array.length() + ")");
                    if (view.isRefresh()) {
                        view.setRefresh(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, String error_msg) {

            }
        });
    }

    /**
     * 设置购物车编辑功能
     *
     * @param cbEditor    编辑按键
     * @param cbSelectAll 全选按键
     * @param isChecked   是否被选中
     * @param linear1     第一个LinearLayout布局
     * @param linear2     第二个LinearLayout布局
     */
    public void cbEditorChanged(CheckBox cbEditor, CheckBox cbSelectAll, boolean isChecked,
                                LinearLayout linear1, LinearLayout linear2) {
        if (isChecked) {
            if (cbSelectAll.isChecked()) {
                cbSelectAll.setChecked(false);
            }
            cbEditor.setText(R.string.finish);
            linear1.setVisibility(View.GONE);
            linear2.setVisibility(View.VISIBLE);
        } else {
            if (cbSelectAll.isChecked()) {
                cbSelectAll.setChecked(false);
            }
            cbEditor.setText(R.string.editor);
            linear1.setVisibility(View.VISIBLE);
            linear2.setVisibility(View.GONE);
        }
    }

    /**
     * 设置全选按钮
     *
     * @param isChecked 是否被选中
     */
    public void cbSelectAllChanged(boolean isChecked) {
        if (isChecked) {
            for (int i = 0; i < view.getListSize(); i++) {
                view.getListItem(i).setChecked(true);
            }
        } else {
            if (view.getA() == 1) {
                for (int i = 0; i < view.getListSize(); i++) {
                    view.getListItem(i).setChecked(false);
                }
            }
        }
    }

    /**
     * 计算购物车列表中商品价格总和
     *
     * @param tvShopingMoney 显示总价的TextView控件
     * @param cbSelectAll    全选控件
     */
    public void UpdataSum(TextView tvShopingMoney, CheckBox cbSelectAll) {
        double sum = 0;
        int size = 0;
        BigDecimal bd;
        for (int i = 0; i < view.getListSize(); i++) {
            if (view.getListItem(i).isChecked()) {
                size++;
                /**
                 * 解决 double 进行运算时，经常出现精度丢失的问题
                 */
                bd = new BigDecimal(Double.toString(sum));
                sum = bd.add(new BigDecimal(Double.toString(view.getListItem(i).getPrice()))
                        .multiply(new BigDecimal(Double.toString(view.getListItem(i).getNumber()))))
                        .doubleValue();
            }
        }
        tvShopingMoney.setText("￥" + sum);
        if (size == view.getListSize()) {
            cbSelectAll.setChecked(true);
            view.setA(1);
        } else {
            view.setA(0);
            cbSelectAll.setChecked(false);
        }
    }

    /**
     * 获取选中商品数量
     *
     * @return 返回选中的数量
     */
    public int getGoodsNum() {
        int size = 0;
        for (int i = 0; i < view.getListSize(); i++) {
            if (view.getListItem(i).isChecked()) {
                size++;
            }
        }
        return size;
    }

    /**
     * 删除商品操作
     */
    public void deleteGoods() {
        JSONObject jo = new JSONObject();
        JSONArray array = new JSONArray();
        for (int i = 0; i < view.getListSize(); i++) {
            if (view.getListItem(i).isChecked()) {
                array.put(view.getListItem(i).getId());
            }
        }
        try {
            jo.put("number", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MyOkHttp.newInstance().postJson(Common.URL_SHOPPING_CAR_DELETE, jo, new
                RawResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, String response) {
                        addList();
                    }

                    @Override
                    public void onFailure(int statusCode, String error_msg) {
                        ToastUtils.showToast(view.getCon(), "删除失败,请重试");
                    }
                });

    }
}
