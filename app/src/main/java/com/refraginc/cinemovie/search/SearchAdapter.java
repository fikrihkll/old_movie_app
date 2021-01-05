package com.refraginc.cinemovie.search;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.refraginc.cinemovie.Connector;
import com.refraginc.cinemovie.MainActivity;
import com.refraginc.cinemovie.R;
import com.refraginc.cinemovie.movie.DetailActivity;
import com.refraginc.cinemovie.tv_show.DetailActivityTvShow;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ItemViewHolder> {
    private Context context;
    private ArrayList<SearchData> data;
    private String img = "https://image.tmdb.org/t/p";

    public SearchAdapter(Context context, ArrayList<SearchData> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public SearchAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from( context ).inflate( R.layout.layout_find, viewGroup, false );
        return new ItemViewHolder( view );
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.ItemViewHolder vh, final int i) {
        Picasso.get().load( img + "/w500" + data.get( i ).getImgPath() ).fit().into( vh.sIv );

        vh.tvTitle.setText( data.get( i ).getTitle() );

        vh.tvDate.setText( data.get( i ).getDate() );
        vh.tvRating.setText( data.get( i ).getRating() );

        //On Click
        vh.sIv.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.get( i ).type.equals( "movie" )) showDetailMovie( i );
                else showDetailTv( i );
            }
        } );
        vh.tvTitle.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.get( i ).type.equals( "movie" )) showDetailMovie( i );
                else showDetailTv( i );
            }
        } );
        vh.tvDate.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.get( i ).type.equals( "movie" )) showDetailMovie( i );
                else showDetailTv( i );
            }
        } );
        vh.tvRating.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.get( i ).type.equals( "movie" )) showDetailMovie( i );
                else showDetailTv( i );
            }
        } );
        vh.relFind.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.get( i ).type.equals( "movie" )) showDetailMovie( i );
                else showDetailTv( i );
            }
        } );
    }

    private void showDetailMovie(Integer position) {
        Connector con = new Connector();

        con.setId( data.get( position ).id );

        Intent intent = new Intent( context, DetailActivity.class );
        intent.putExtra( MainActivity.PARCE_KEY, con );
        context.startActivity( intent );
    }

    private void showDetailTv(Integer position) {
        Connector con = new Connector();

        con.setId( data.get( position ).id );

        Intent intent = new Intent( context, DetailActivityTvShow.class );
        intent.putExtra( MainActivity.PARCE_KEY, con );
        context.startActivity( intent );
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView sIv;
        TextView tvTitle, tvRating, tvDate;
        RelativeLayout relFind;

        public ItemViewHolder(@NonNull View itemView) {
            super( itemView );
            sIv = itemView.findViewById( R.id.sIvPoster );
            tvTitle = itemView.findViewById( R.id.sTvTitle );
            tvRating = itemView.findViewById( R.id.sTvRating );
            tvDate = itemView.findViewById( R.id.sTvDate );
            relFind = itemView.findViewById( R.id.relFind );
        }
    }

}
