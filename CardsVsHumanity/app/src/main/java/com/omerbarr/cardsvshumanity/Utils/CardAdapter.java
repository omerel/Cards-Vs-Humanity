package com.omerbarr.cardsvshumanity.Utils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.omerbarr.cardsvshumanity.R;
import java.util.ArrayList;

/**
 * Created by omer on 05/04/2017.
 */

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    public ArrayList<String> mArrayList;


    public CardAdapter(ArrayList<String> arrayList) {
        mArrayList = arrayList;
    }

    @Override
    public CardAdapter.CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(CardAdapter.CardViewHolder viewHolder, int i) {}

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder{
        public TextView cardContent;
        public int parentPlayerId;

        public CardViewHolder(View view) {
            super(view);
            cardContent = (TextView)view.findViewById(R.id.card_content);
        }
    }

}
