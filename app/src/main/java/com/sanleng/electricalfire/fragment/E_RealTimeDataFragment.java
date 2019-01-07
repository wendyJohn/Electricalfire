package com.sanleng.electricalfire.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.loopj.android.http.RequestParams;
import com.sanleng.electricalfire.R;
import com.sanleng.electricalfire.adapter.E_RealTimeDataAdapter;
import com.sanleng.electricalfire.bean.ERealTimeDataBean;
import com.sanleng.electricalfire.myview.MarqueeViews;
import com.sanleng.electricalfire.net.NetCallBack;
import com.sanleng.electricalfire.net.RequestUtils;
import com.sanleng.electricalfire.net.URLs;
import com.sanleng.electricalfire.util.PreferenceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 智能电气火灾实时数据
 *
 * @author Qiaoshi
 */
public class E_RealTimeDataFragment extends BaseFragment implements OnClickListener {
    private ListView e_realtimedatalslistview;
    private E_RealTimeDataAdapter e_realtimedataAdapter;//(有数据版)
    private View view;
    private List<ERealTimeDataBean> list;
    private Receivers receivers;
    private static final String BROADCAST_PERMISSION_DISC = "com.permissions.MY_BROADCAST";
    private static final String BROADCAST_ACTION_DISC = "com.permissions.my_broadcast";
    private ImageView query_im;
    private boolean state = true;

    private LinearLayout yaout;
    private ImageView imageView_item;
    private MarqueeViews marqueeviews;

    private List<Map<String, Object>> lists;
    private List<Map<String, Object>> allList;

    private int pageNo = 1;// 设置pageNo的初始化值为1，即默认获取的是第一页的数据。
    private int allpage;
    private boolean is_divPage;// 是否进行分页操作
    private boolean finish = true;// 是否加载完成;
    private List<String> info;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.e_realtimedatafragment, null);
        initview();
        return view;
    }

    //初始化
    private void initview() {
        query_im = view.findViewById(R.id.query_im);
        query_im.setOnClickListener(this);
        yaout = view.findViewById(R.id.yaout);
        imageView_item = view.findViewById(R.id.imageView_item);

        receivers = new Receivers();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION_DISC); // 只有持有相同的action的接受者才能接收此广
        getActivity().registerReceiver(receivers, intentFilter, BROADCAST_PERMISSION_DISC, null);
        marqueeviews = (MarqueeViews) view.findViewById(R.id.marqueeviews);
        list = new ArrayList<>();
        ERealTimeDataBean beana = new ERealTimeDataBean();
        beana.setAddress("南京工程学院A栋一层配电箱");
        beana.setTemperature("当前温度：20℃");
        beana.setTemperaturelimit("限值：0～100℃");
        beana.setResidualcurrent("剩余电流：20MA");
        beana.setCurrentlimit("限值：30～100MA");
        beana.setState("有报警");
        beana.setNumber("10");
        list.add(beana);

        ERealTimeDataBean beanb = new ERealTimeDataBean();
        beanb.setAddress("南京工程学院A栋二层配电箱");
        beanb.setTemperature("当前温度：20℃");
        beanb.setTemperaturelimit("限值：0～100℃");
        beanb.setResidualcurrent("剩余电流：20MA");
        beanb.setCurrentlimit("限值：30～100MA");
        beanb.setState("无报警");
        beanb.setNumber("0");
        list.add(beanb);

        ERealTimeDataBean beanc = new ERealTimeDataBean();
        beanc.setAddress("南京工程学院A栋三层配电箱");
        beanc.setTemperature("当前温度：20℃");
        beanc.setTemperaturelimit("限值：0～100℃");
        beanc.setResidualcurrent("剩余电流：20MA");
        beanc.setCurrentlimit("限值：30～100MA");
        beanc.setState("有报警");
        beanc.setNumber("10");
        list.add(beanc);

        ERealTimeDataBean beand = new ERealTimeDataBean();
        beand.setAddress("南京工程学院A栋四层配电箱");
        beand.setTemperature("当前温度：20℃");
        beand.setTemperaturelimit("限值：0～100℃");
        beand.setResidualcurrent("剩余电流：20MA");
        beand.setCurrentlimit("限值：30～100MA");
        beand.setState("无报警");
        beand.setNumber("0");
        list.add(beand);

        ERealTimeDataBean beane = new ERealTimeDataBean();
        beane.setAddress("南京工程学院A栋五层配电箱");
        beane.setTemperature("当前温度：20℃");
        beane.setTemperaturelimit("限值：0～100℃");
        beane.setResidualcurrent("剩余电流：20MA");
        beane.setCurrentlimit("限值：30～100MA");
        beane.setState("有报警");
        beane.setNumber("10");
        list.add(beane);

        e_realtimedatalslistview = view.findViewById(R.id.realtimedatalslistview);
        e_realtimedataAdapter = new E_RealTimeDataAdapter(getActivity(), list, mHandler);
        e_realtimedatalslistview.setAdapter(e_realtimedataAdapter);
        e_realtimedatalslistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(getActivity(), AlarmRecordActivity.class);
//                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume() {
        allList = new ArrayList<Map<String, Object>>();
        pageNo = 1;
        AddPolice(1);
        super.onResume();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            final Bundle data = message.getData();
            switch (message.what) {
                // 拍照确认
                case 66660:
                    int selIndex = data.getInt("selIndex");

                    break;
                //待处理
                case 66661:
                    int selIndexs = data.getInt("selIndex");

                    break;
                //历史轨迹
                case 66662:
                    int selIndex_p = data.getInt("selIndex");

                    break;
                // 打电话快捷键
                case 66663:
                    int selIndexp = data.getInt("selIndex");



                    break;
                default:
                    break;
            }
        }
    };


    // 收到报警广播处理，刷新界面
    public class Receivers extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BROADCAST_ACTION_DISC)) {
                System.out.println("收到； 1111111111111111");
                //刷新数据
                allList = new ArrayList<Map<String, Object>>();
                pageNo = 1;
                AddPolice(1);
            }

        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.query_im:
                if (state) {
                    yaout.setVisibility(View.VISIBLE);
                    imageView_item.setVisibility(View.GONE);
                    state = false;
                } else {
                    yaout.setVisibility(View.GONE);
                    imageView_item.setVisibility(View.VISIBLE);
                    state = true;
                }
                break;
            default:
                break;
        }
    }

    //获取报警信息
    private void AddPolice(int page) {
        info = new ArrayList<>();
        RequestParams params = new RequestParams();
        params.put("event_no", "142");
        params.put("pageNum", page + "");
        params.put("pageSize", "10");
        params.put("unit_code", PreferenceUtils.getString(getActivity(), "unitcode"));
        params.put("username", PreferenceUtils.getString(getActivity(), "MobileFig_username"));
        params.put("platformkey", "app_firecontrol_owner");

        RequestUtils.ClientPost(URLs.Police_URL, params, new NetCallBack() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onMySuccess(String result) {
                if (result == null || result.length() == 0) {
                    return;
                }
                System.out.println("报警数据请求成功" + result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String msg = jsonObject.getString("msg");
                    if (msg.equals("获取成功")) {
                        String data = jsonObject.getString("data");
                        JSONObject objects = new JSONObject(data);
                        String listsize = objects.getString("total");
                        String list = objects.getString("list");
                        JSONArray array = new JSONArray(list);
                        JSONObject object;
                        for (int i = 0; i < array.length(); i++) {
                            object = (JSONObject) array.get(i);
                            String unit_name = object.getString("unit_name");
                            String build_name = object.getString("build_name");
                            String position = object.getString("position");
                            String str = unit_name + build_name + position;
                            info.add(str);
                        }
                        // 在代码里设置自己的动画
                        marqueeviews.startWithList(info);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void onMyFailure(Throwable arg0) {

            }
        });
    }

    private void update(String ids) {
        RequestParams params = new RequestParams();
        params.put("ids", ids);
        params.put("state", "0");
        params.put("username", PreferenceUtils.getString(getActivity(), "MobileFig_username"));
        params.put("platformkey", "app_firecontrol_owner");
        RequestUtils.ClientPost(URLs.ElectricalFire_URL, params, new NetCallBack() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onMySuccess(String result) {
                if (result == null || result.length() == 0) {
                    return;
                }
            }

            @Override
            public void onMyFailure(Throwable arg0) {

            }
        });

    }

}
