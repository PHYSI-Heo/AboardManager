package com.physis.aboard.monitor.manager.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.physis.aboard.monitor.manager.R;
import com.physis.aboard.monitor.manager.data.ClientInfo;

import java.util.LinkedList;
import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientHolder> {

    public interface OnClientSelectedListener{
        void onClientPosition(int position);
    }

    public void setOnSelectedIndexListener(OnClientSelectedListener listener){
        onClientSelectedListener = listener;
    }

    private OnClientSelectedListener onClientSelectedListener;
    private List<ClientInfo> clients = new LinkedList<>();

    @NonNull
    @Override
    public ClientHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view_client_state, parent, false);
        return new ClientHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientHolder holder, final int position) {
        ClientInfo info = clients.get(position);

        String time = info.getTime();
        String state = info.getState();
        int stateColor;

        holder.tvName.setText(info.getName());
        holder.tvTime.setText(time.equals("null") ? "None" : time);
        if(state.equals("0")){
            state = "None";
            stateColor = R.color.colorStateDisable;
        }else if(state.equals("1")){
            state = "승차";
            stateColor = R.color.colorStateGetOn;
        }else{
            state = "하차";
            stateColor = R.color.colorStateGetOff;
        }
        holder.tvState.setText(state);
        holder.tvState.setBackgroundResource(stateColor);

        holder.ivBtnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onClientSelectedListener != null)
                    onClientSelectedListener.onClientPosition(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    public void setItems(List<ClientInfo> clients){
        this.clients = clients;
        notifyDataSetChanged();
    }
}
