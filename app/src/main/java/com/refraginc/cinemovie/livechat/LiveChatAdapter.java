package com.refraginc.cinemovie.livechat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.refraginc.cinemovie.R;

import java.util.ArrayList;

public class LiveChatAdapter extends RecyclerView.Adapter<LiveChatAdapter.CustomViewHolder> {

    Context context;
    ArrayList<ChatData> data;

    public LiveChatAdapter(Context context, ArrayList<ChatData> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from( context ).inflate( R.layout.layout_rc_chat, viewGroup, false );
        return new CustomViewHolder( view );
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder vh, int i) {
        if (data.get( i ).getMyId().equals( data.get( i ).getFrom() )) {
            vh.Ret.setVisibility( View.GONE );

            vh.tvMe.setText( context.getResources().getString( R.string.strMe ) );
            vh.tvMsgMe.setText( data.get( i ).getMessage() );
            vh.tvDateMe.setText( data.get( i ).getDate() );
        } else {
            vh.Me.setVisibility( View.GONE );

            vh.tvSendder.setText( data.get( i ).getFrom() );
            vh.tvMsgSendder.setText( data.get( i ).getMessage() );
            vh.tvDateSendder.setText( data.get( i ).getDate() );
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout Ret, Me;
        TextView tvSendder, tvMsgSendder, tvDateSendder, tvMe, tvMsgMe, tvDateMe;

        public CustomViewHolder(@NonNull View itemView) {
            super( itemView );

            Ret = itemView.findViewById( R.id.Ret );
            Me = itemView.findViewById( R.id.Me );
            tvSendder = itemView.findViewById( R.id.tvSendder );
            tvMsgSendder = itemView.findViewById( R.id.tvMsgSendder );
            tvDateSendder = itemView.findViewById( R.id.tvDateSendder );
            tvMe = itemView.findViewById( R.id.tvMe );
            tvMsgMe = itemView.findViewById( R.id.tvMsgMe );
            tvDateMe = itemView.findViewById( R.id.tvDateMe );
        }
    }
}
