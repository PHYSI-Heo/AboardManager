package com.physis.aboard.monitor.manager.list;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.physis.aboard.monitor.manager.R;

public class ClientHolder extends RecyclerView.ViewHolder {

    TextView tvName, tvTime, tvState;
    ImageView ivBtnDetail;

    public ClientHolder(@NonNull View itemView) {
        super(itemView);

        tvName = itemView.findViewById(R.id.tv_name);
        tvTime = itemView.findViewById(R.id.tv_aboard_time);
        tvState = itemView.findViewById(R.id.tv_aboard_state);
        ivBtnDetail = itemView.findViewById(R.id.iv_btn_detail);

    }
}
