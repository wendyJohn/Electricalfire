package com.sanleng.electricalfire.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jaeger.library.StatusBarUtil;
import com.sanleng.electricalfire.MyApplication;
import com.sanleng.electricalfire.R;
import com.sanleng.electricalfire.adapter.RealTimeDataAdapter;
import com.sanleng.electricalfire.bean.ERealTimeDataBean;
import com.sanleng.electricalfire.model.RealTimeDataRequest;
import com.sanleng.electricalfire.myview.MarqueeViews;
import com.sanleng.electricalfire.util.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 智能电气火灾实时数据
 *
 * @author Qiaoshi
 */
public class RealTimeDataActivity extends AppCompatActivity implements OnClickListener {
    @BindView(R.id.query_im)
    ImageView queryIm;
    @BindView(R.id.imageView_item)
    ImageView imageViewItem;
    @BindView(R.id.marqueeviews)
    MarqueeViews marqueeviews;
    @BindView(R.id.architecture)
    TextView architecture;
    @BindView(R.id.droponea)
    ImageView droponea;
    @BindView(R.id.spinner_architecture)
    RelativeLayout spinnerArchitecture;
    @BindView(R.id.floor)
    TextView floor;
    @BindView(R.id.droponeb)
    ImageView droponeb;
    @BindView(R.id.spinner_floor)
    RelativeLayout spinnerFloor;
    @BindView(R.id.box)
    TextView box;
    @BindView(R.id.droponec)
    ImageView droponec;
    @BindView(R.id.spinner_box)
    RelativeLayout spinnerBox;
    @BindView(R.id.equipment)
    TextView equipment;
    @BindView(R.id.droponed)
    ImageView droponed;
    @BindView(R.id.spinner_equipment)
    RelativeLayout spinnerEquipment;
    @BindView(R.id.yaout)
    LinearLayout yaout;
    @BindView(R.id.nodata)
    TextView nodata;
    private RealTimeDataAdapter realtimedataAdapter;//(有数据版)
    private Receivers receivers;
    private static final String BROADCAST_PERMISSION_DISC = "com.permissions.MY_BROADCAST";
    private static final String BROADCAST_ACTION_DISC = "com.permissions.my_broadcast";
    private ImageView query_im;
    private boolean state = true;
    private List<ERealTimeDataBean> allList;
    private int pageNo = 1;// 设置pageNo的初始化值为1，即默认获取的是第一页的数据。
    private int allpage;
    private boolean is_divPage;// 是否进行分页操作
    private boolean finish = true;// 是否加载完成;
    private List<String> info;
    ListView realtimedatalslistview;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.realtimedatafragment);
        ButterKnife.bind(this);
        StatusBarUtil.setColor(RealTimeDataActivity.this, R.color.translucency);
        EventBus.getDefault().register(this);
        initView();
        new RealTimeDataRequest(this).getRealTimeData(pageNo + "");
    }

    //初始化
    private void initView() {
        allList = new ArrayList<>();
        receivers = new Receivers();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION_DISC); // 只有持有相同的action的接受者才能接收此广
        registerReceiver(receivers, intentFilter, BROADCAST_PERMISSION_DISC, null);
        realtimedataAdapter = new RealTimeDataAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // 加载数据
    private void addData(int size, List<ERealTimeDataBean> list) {
        int length = 10;
        if (size % length == 0) {
            allpage = size / length;
        } else {
            allpage = size / length + 1;
        }
        allList.addAll(list);
        realtimedatalslistview=findViewById(R.id.realtimedatalslistview);
        realtimedataAdapter.bindData(RealTimeDataActivity.this, allList);
        if (pageNo == 1) {
            // 没有数据就提示暂无数据。
            realtimedatalslistview.setEmptyView(findViewById(R.id.nodata));
            realtimedatalslistview.setAdapter(realtimedataAdapter);
        }
        realtimedataAdapter.notifyDataSetChanged();
        pageNo++;
        finish = true;
        realtimedatalslistview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                /**
                 * 当分页操作is_divPage为true时、滑动停止时、且pageNo<=allpage（ 这里因为服务端有allpage页数据）时，加载更多数据。
                 */
                if (is_divPage && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && pageNo <= allpage
                        && finish) {
                    finish = false;
                    new RealTimeDataRequest(RealTimeDataActivity.this).getRealTimeData(pageNo + "");
                } else if (pageNo > allpage && finish) {
                    finish = false;
                    // 如果pageNo>allpage则表示，服务端没有更多的数据可供加载了。
                }
            }

            // 当：第一个可见的item（firstVisibleItem）+可见的item的个数（visibleItemCount）=
            // 所有的item总数的时候， is_divPage变为TRUE，这个时候才会加载数据。
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                is_divPage = (firstVisibleItem + visibleItemCount == totalItemCount);
            }
        });
        realtimedatalslistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
    }

    // 声明一个订阅方法，用于接收事件
    @Subscribe
    public void onEvent(MessageEvent messageEvent) {
        switch (messageEvent.getTAG()) {
            case MyApplication.MESSREALTIMEDATA:
                addData( messageEvent.getSize(),messageEvent.getList());
                break;
            case MyApplication.MESSREALTIMEDATAA:
                // 拍照确认
                int selIndex = messageEvent.getPosition();
                String buildids = messageEvent.getBuildids();
                String floorids = messageEvent.getFloorids();
                String electricalDetectorInfos = messageEvent.getElectricalDetectorInfos();
                Intent intent_Patrol = new Intent(RealTimeDataActivity.this, PatrolActivity.class);
                intent_Patrol.putExtra("buildids", buildids);
                intent_Patrol.putExtra("floorids", floorids);
                intent_Patrol.putExtra("electricalDetectorInfos", electricalDetectorInfos);
                startActivity(intent_Patrol);
                break;
            case MyApplication.MESSREALTIMEDATAB:
                //待处理
                int selIndexs = messageEvent.getPosition();
                String deviceIds = allList.get(selIndexs).getId();
                Intent intents = new Intent(RealTimeDataActivity.this, PendingActivity.class);
                intents.putExtra("deviceid", deviceIds);
                startActivity(intents);
                break;
            case MyApplication.MESSREALTIMEDATAC:
                //历史轨迹
                int selIndex_p = messageEvent.getPosition();
                String deviceId = allList.get(selIndex_p).getId();
                Intent intent = new Intent(RealTimeDataActivity.this, TimePumpingActivity.class);
                intent.putExtra("deviceid", deviceId);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.query_im:
                if (state) {
                    yaout.setVisibility(View.VISIBLE);
                    imageViewItem.setVisibility(View.GONE);
                    state = false;
                } else {
                    yaout.setVisibility(View.GONE);
                    imageViewItem.setVisibility(View.VISIBLE);
                    state = true;
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this); // 解绑
        super.onDestroy();
    }

    // 收到报警广播处理，刷新界面
    public class Receivers extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BROADCAST_ACTION_DISC)) {
                System.out.println("收到； 1111111111111111");
                //刷新数据
                new RealTimeDataRequest(RealTimeDataActivity.this).getRealTimeData("1");
            }

        }
    }



}
