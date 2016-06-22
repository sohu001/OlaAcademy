package com.snail.olaxueyuan.ui.course;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.snail.olaxueyuan.R;
import com.snail.olaxueyuan.common.manager.Logger;
import com.snail.olaxueyuan.common.manager.TitleManager;
import com.snail.olaxueyuan.common.manager.ToastUtil;
import com.snail.olaxueyuan.protocol.manager.SEAuthManager;
import com.snail.olaxueyuan.protocol.manager.SEUserManager;
import com.snail.olaxueyuan.protocol.result.SystemCourseResult;
import com.snail.olaxueyuan.protocol.result.UserAlipayResult;
import com.snail.olaxueyuan.protocol.result.UserWXpayResult;
import com.snail.olaxueyuan.ui.activity.SuperActivity;
import com.snail.olaxueyuan.ui.course.pay.weixin.MD5;
import com.snail.olaxueyuan.ui.course.pay.weixin.WxPayUtile;
import com.snail.olaxueyuan.ui.course.pay.zhifubao.PayResult;

import java.text.DecimalFormat;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PaySystemVideoActivity extends SuperActivity {
    TitleManager titleManager;
    @Bind(R.id.left_return)
    TextView leftReturn;
    @Bind(R.id.right_response)
    ImageView rightResponse;
    @Bind(R.id.name)
    TextView name;
    @Bind(R.id.leanstage)
    TextView leanstage;
    @Bind(R.id.org)
    TextView org;
    @Bind(R.id.price)
    TextView priceTv;
    @Bind(R.id.alipay_icon)
    ImageView alipayIcon;
    @Bind(R.id.alipay_radio)
    ImageButton alipayRadio;
    @Bind(R.id.alipay_view)
    RelativeLayout alipayView;
    @Bind(R.id.wechat_icon)
    ImageView wechatIcon;
    @Bind(R.id.wechat_radio)
    ImageButton wechatRadio;
    @Bind(R.id.wechat_view)
    RelativeLayout wechatView;
    @Bind(R.id.buy_vip)
    Button buyVip;
    SystemCourseResult.ResultEntity resultEntity;
    private String courseId;
    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_CHECK_FLAG = 2;
    private static final int PAY_BY_ALIPAY = 101;//使用支付宝支付
    private static final int PAY_BY_WECHAT = 102;//使用微信支付
    private int payType = PAY_BY_ALIPAY;//最终支付方式,默认支付宝
    private String userId = "126";//测试的userId
    private String price;
    private String type = "3";// 1月度会员 2 年度会员 3 整套视频

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        courseId = getIntent().getStringExtra("courseId");
        resultEntity = (SystemCourseResult.ResultEntity) getIntent().getSerializableExtra("ResultEntity");
        setContentView(R.layout.activity_pay_system_video_view);
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        titleManager = new TitleManager(this, "支付订单", this, true);

    }

    @Override
    public void initData() {
        if (resultEntity != null) {
            name.setText(resultEntity.getName());
            leanstage.setText("有效期:" + resultEntity.getLeanstage() + "(共" + resultEntity.getVideonum() + "课时)");
            org.setText(resultEntity.getOrg());
            Logger.e("price==" + resultEntity.getPrice());
            priceTv.setText(String.valueOf(resultEntity.getPrice()));
        }
    }

    @OnClick({R.id.left_return, R.id.right_response, R.id.alipay_view, R.id.wechat_view, R.id.buy_vip})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.left_return:
                finish();
                break;
            case R.id.right_response:
                break;
            case R.id.alipay_view:
                payType = PAY_BY_ALIPAY;
                alipayRadio.setSelected(true);
                wechatRadio.setSelected(false);
                break;
            case R.id.wechat_view:
                payType = PAY_BY_WECHAT;
                alipayRadio.setSelected(false);
                wechatRadio.setSelected(true);
                break;
            case R.id.buy_vip:
                switch (payType) {
                    case PAY_BY_WECHAT:
                        payForWXRequest();
                        break;
                    case PAY_BY_ALIPAY:
                    default:
                        payForAlipay();
                        break;
                }
                break;
        }
    }

    public void payForWXRequest() {
        price = "0.01";
        userId = SEAuthManager.getInstance().getAccessUser().getId();
        double pricess = Double.parseDouble(price) * 100;
        final String prices = new DecimalFormat("###").format(pricess);
        SEUserManager.getInstance().getWXPayReq(userId, type, courseId, new Callback<UserWXpayResult>() {
            @Override
            public void success(UserWXpayResult wxPayModule, Response response) {
                if (wxPayModule != null && wxPayModule.getApicode() == 10000) {
                    final UserWXpayResult.ResultBean wxpayResult = wxPayModule.getResult();
                    if (wxpayResult != null) {
                        //调用微信支付
                        WxPayUtile.getInstance(PaySystemVideoActivity.this, prices,
                                "http://121.40.35.3/test", "测试商品", wxpayResult,
                                genOutTradNo()).doPay();
                    }
                } else {
                    ToastUtil.showToastShort(PaySystemVideoActivity.this, wxPayModule.getMessage());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                ToastUtil.showToastShort(PaySystemVideoActivity.this, R.string.data_request_fail);
            }
        });
    }

    private String genOutTradNo() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }

    public void payForAlipay() {
        userId = SEAuthManager.getInstance().getAccessUser().getId();
        SEUserManager.getInstance().getAliOrderInfo(userId, type, courseId, new Callback<UserAlipayResult>() {
            @Override
            public void success(UserAlipayResult userAlipayResult, Response response) {
                if (userAlipayResult != null && userAlipayResult.getApicode() == 10000) {
                    final UserAlipayResult.ResultBean orderInfoBean = userAlipayResult.getResult();
                    if (orderInfoBean != null) {
                        Runnable payRunnable = new Runnable() {

                            @Override
                            public void run() {
                                // 构造PayTask 对象
                                PayTask alipay = new PayTask(PaySystemVideoActivity.this);
                                String result = alipay.pay(orderInfoBean.getOrderInfo(), true);
//                                Logger.e("result==" + result);
                                Message msg = new Message();
                                msg.what = SDK_PAY_FLAG;
                                msg.obj = result;
                                mHandler.sendMessage(msg);
                            }
                        };
                        // 必须异步调用
                        Thread payThread = new Thread(payRunnable);
                        payThread.start();
                    }
                } else {
                    ToastUtil.showToastShort(PaySystemVideoActivity.this, userAlipayResult.getMessage());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                ToastUtil.showToastShort(PaySystemVideoActivity.this, R.string.data_request_fail);
            }
        });
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((String) msg.obj);
                    /**
                     * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                     * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                     * docType=1) 建议商户依赖异步通知
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息

                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        Toast.makeText(PaySystemVideoActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                    } else {
                        // 判断resultStatus 为非"9000"则代表可能支付失败
                        // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(PaySystemVideoActivity.this, "支付结果确认中", Toast.LENGTH_SHORT).show();

                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            Toast.makeText(PaySystemVideoActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                }
                case SDK_CHECK_FLAG: {
                    Toast.makeText(PaySystemVideoActivity.this, "检查结果为：" + msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                }
                case 100:
                    ToastUtil.showToastShort(PaySystemVideoActivity.this, R.string.alipay_not_install);
                    break;
                default:
                    break;
            }
        }

        ;
    };
}
