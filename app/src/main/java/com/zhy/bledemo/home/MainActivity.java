package com.zhy.bledemo.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.internal.util.UUIDUtil;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.zhy.bledemo.common.Constants;
import com.zhy.bledemo.common.MainApplication;
import com.zhy.bledemo.R;
import com.zhy.bledemo.utils.PermissionUtil;
import com.zhy.bledemo.utils.ScanExceptionHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btnScan)
    Button btnScan;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private RxBleClient rxBleClient;
    private Disposable scanDisposable;
    private boolean hasClickedScan;
    private ScanResultsAdapter scanResultsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        rxBleClient = MainApplication.rxBleClient;
        initData();
    }

    @SuppressLint({"WrongConstant"})
    private void initData() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        scanResultsAdapter = new ScanResultsAdapter();
        recyclerView.setAdapter(scanResultsAdapter);
        scanResultsAdapter.setOnItemClickListener((view) -> {
            int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
            ScanResult scanResult = scanResultsAdapter.getItemAtPosition(childAdapterPosition);
            String macAddress = scanResult.getBleDevice().getMacAddress();
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_MAC_ADDRESS, macAddress);
            startActivity(intent);
        });
    }

    @OnClick({R.id.btnScan})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnScan:
                if (isScanning()) {
                    scanDisposable.dispose();
                } else {
                    if (rxBleClient.isScanRuntimePermissionGranted()) {
                        scanBleDevices();
                    } else {
                        hasClickedScan = true;
                        PermissionUtil.getInstance().requestPermission(this, Permission.Group.LOCATION);
                    }
                }
                updateUIState();
                break;
        }
    }

    //扫描设备
    private void scanBleDevices() {
        scanDisposable = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder()
//                        .setServiceUuid(Constants.SERVICE_UUID)
                        .build()
        )
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::dispose)
                .subscribe(scanResultsAdapter::addScanResult, this::onScanFailure);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isScanning()) {
            scanDisposable.dispose();
        }
    }

    private void onScanFailure(Throwable throwable) {
        if (throwable instanceof BleScanException) {
            ScanExceptionHandler.handleException(this, (BleScanException) throwable);
        }
    }

    private void dispose() {
        scanDisposable = null;
        scanResultsAdapter.clearScanResults();
        updateUIState();
    }

    private boolean isScanning() {
        return scanDisposable != null;
    }

    private void updateUIState() {
        btnScan.setText(isScanning() ? "停止扫描" : "开始扫描");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PermissionUtil.REQUEST_CODE_SETTING:
//                Toast.makeText(this, R.string.message_setting_comeback, Toast.LENGTH_SHORT).show();
                if (AndPermission.hasPermissions(this, Permission.Group.LOCATION)) {
                    hasClickedScan = false;
                    scanBleDevices();
                } else {
                    hasClickedScan = true;
                }
                break;
        }
    }

}
