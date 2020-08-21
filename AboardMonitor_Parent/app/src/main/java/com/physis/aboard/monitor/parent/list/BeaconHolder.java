package com.physis.aboard.monitor.parent.list;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.physis.aboard.monitor.parent.R;

public class BeaconHolder extends RecyclerView.ViewHolder {

    TextView tvName, tvAddress;
    LinearLayout rcItem;

    public BeaconHolder(@NonNull View itemView) {
        super(itemView);
        tvName =itemView.findViewById(R.id.tv_name);
        tvAddress =itemView.findViewById(R.id.tv_address);

        rcItem = itemView.findViewById(R.id.rc_item);
    }
}
