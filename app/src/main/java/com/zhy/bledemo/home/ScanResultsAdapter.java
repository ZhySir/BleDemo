package com.zhy.bledemo.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.zhy.bledemo.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zhy on 2020/3/16.
 */
public class ScanResultsAdapter extends RecyclerView.Adapter<ScanResultsAdapter.ViewHolder> {

    private List<ScanResult> mList = new ArrayList<>();

    //============interface============
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v);
            }
        }
    };

    //排序器
    private static final Comparator<ScanResult> SORTING_COMPARATOR = (o1, o2) ->
            o1.getBleDevice().getMacAddress().compareTo(o2.getBleDevice().getMacAddress());

    void addScanResult(ScanResult scanResult) {
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).getBleDevice().equals(scanResult.getBleDevice())) {
                mList.set(i, scanResult);
                notifyItemChanged(i);
                return;
            }
        }
        mList.add(scanResult);
        Collections.sort(mList, SORTING_COMPARATOR);
        notifyDataSetChanged();
    }

    void clearScanResults() {
        mList.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scan_results, parent, false);
        itemView.setOnClickListener(onClickListener);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanResult scanResult = mList.get(position);
        RxBleDevice bleDevice = scanResult.getBleDevice();
        holder.tvLine1.setText(String.format(Locale.getDefault(), "%s（%s）", bleDevice.getMacAddress(), bleDevice.getName()));
        holder.tvLine2.setText(String.format(Locale.getDefault(), "RSSI：%d", scanResult.getRssi()));
    }

    ScanResult getItemAtPosition(int position) {
        return mList.get(position);
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvLine1)
        TextView tvLine1;
        @BindView(R.id.tvLine2)
        TextView tvLine2;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
