package com.zhy.bledemo.home;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.zhy.bledemo.R;
import com.zhy.bledemo.common.MainApplication;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_MAC_ADDRESS = "extra_mac_address";

    @BindView(R.id.btnConnect)
    Button btnConnect;
    @BindView(R.id.tvContent)
    TextView tvContent;

    private Disposable connectionDisposable;
    private RxBleDevice rxBleDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        String macAddressStr = getIntent().getStringExtra(EXTRA_MAC_ADDRESS);
        setTitle(String.format(Locale.getDefault(), "MAC：%s", macAddressStr));
        rxBleDevice = MainApplication.rxBleClient.getBleDevice(macAddressStr);
    }

    @OnClick({R.id.btnConnect})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
                if (isConnected()) {
                    triggerDisconnect();
                } else {
                    connectionDisposable = rxBleDevice
                            .establishConnection(false)
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally(this::dispose)
                            .subscribe(this::onConnectionReceived, this::onConnectionFailure);
                }
                break;
        }
    }

    private void onConnectionReceived(RxBleConnection connection) {
        tvContent.setText("连接成功");
        updateUIState();
    }

    private void onConnectionFailure(Throwable throwable) {
        tvContent.setText(String.format(Locale.getDefault(), "连接失败：%s", throwable));
        updateUIState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        triggerDisconnect();
    }

    private boolean isConnected() {
        return rxBleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private void triggerDisconnect() {
        if (connectionDisposable != null) {
            connectionDisposable.dispose();
        }
    }

    private void dispose() {
        connectionDisposable = null;
        updateUIState();
    }

    private void updateUIState() {
        boolean connected = isConnected();
        btnConnect.setText(connected ? "断开连接" : "开始连接");
    }


}
