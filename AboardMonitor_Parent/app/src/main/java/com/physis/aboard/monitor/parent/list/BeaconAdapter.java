package com.physis.aboard.monitor.parent.list;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.physis.aboard.monitor.parent.R;

import java.util.LinkedList;
import java.util.List;

public class BeaconAdapter extends RecyclerView.Adapter<BeaconHolder> {

    public interface OnSelectedDeviceListener{
        void onSelected(BluetoothDevice device);
    }

    private OnSelectedDeviceListener onSelectedDeviceListener;

    public void setOnSelectedDeviceListener(OnSelectedDeviceListener listener){
        onSelectedDeviceListener = listener;
    }

    private List<BluetoothDevice> devices = new LinkedList<>();

    @NonNull
    @Override
    public BeaconHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view_beacon, parent, false);
        return new BeaconHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BeaconHolder holder, int position) {
        final BluetoothDevice device = devices.get(position);

        holder.tvName.setText(device.getName());
        holder.tvAddress.setText(device.getAddress());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onSelectedDeviceListener != null)
                    onSelectedDeviceListener.onSelected(device);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void clearItem(){
        devices.clear();
        notifyDataSetChanged();
    }

    public void addItem(BluetoothDevice device){
        if(devices.contains(device) || device.getName() == null)
            return;
        devices.add(device);
        notifyDataSetChanged();
    }
}
