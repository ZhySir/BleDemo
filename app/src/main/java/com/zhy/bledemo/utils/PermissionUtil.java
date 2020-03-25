package com.zhy.bledemo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.zhy.bledemo.R;

import java.util.List;

/**
 * Created by zhy on 2019/8/1.
 */
public class PermissionUtil {

    public static final int REQUEST_CODE_SETTING = 1;

    private static PermissionUtil mPermissionUtil;

    private PermissionUtil() {
    }

    public static PermissionUtil getInstance() {
        if (mPermissionUtil == null) {
            mPermissionUtil = new PermissionUtil();
        }
        return mPermissionUtil;
    }

    /**
     * Request permissions.
     */
    @SuppressLint("WrongConstant")
    public void requestPermission(final Context context, String... permissions) {
        AndPermission.with(context)
                .runtime()
                .permission(permissions)
                .rationale(new RuntimeRationale())
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
//                        toast(context, R.string.successfully);
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
//                        toast(context, R.string.failure);
                        if (AndPermission.hasAlwaysDeniedPermission(context, permissions)) {
                            showSettingDialog(context, permissions);
                        }
                    }
                })
                .start();
    }

    /**
     * Display setting dialog.
     */
    private void showSettingDialog(final Context context, final List<String> permissions) {
        List<String> permissionNames = Permission.transformText(context, permissions);
        String message = context.getString(R.string.message_permission_always_failed,
                TextUtils.join("\n", permissionNames));

        new AlertDialog.Builder(context).setCancelable(false)
                .setTitle(R.string.title_dialog)
                .setMessage(message)
                .setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setPermission(context);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    /**
     * Set permissions.
     */
    private void setPermission(Context context) {
        AndPermission.with(context).runtime().setting().start(REQUEST_CODE_SETTING);
    }

    protected void toast(Context context, @StringRes int message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
