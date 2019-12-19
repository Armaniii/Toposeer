package com.ciyrus.arman.toposeernew;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

//import com.yarolegovich.discretescrollview.R;


import com.github.mikephil.charting.charts.LineChart;
import com.mapbox.mapboxsdk.plugins.annotation.Line;

import java.util.ArrayList;


public class MyCustomAdapter extends RecyclerView.Adapter<MyCustomAdapter.MyViewHolder>
{

    ArrayList<DataModel> listDataModel = new ArrayList<DataModel>();

    public MyCustomAdapter(ArrayList<DataModel> _listDataModel) {
        this.listDataModel = _listDataModel;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_linechart, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        DataModel objDataModel = listDataModel.get(position);

        //holder.maxHeight.setText(objDataModel.getHighestElevation());
        //holder.minHeight.setText(objDataModel.getLowestElevation().toString());
        //holder.imgRestaurant.setImageResource(objDataModel.);
        holder.chart.setData(objDataModel.getChart());
        holder.chart.invalidate();
    }

    @Override
    public int getItemCount() {
        return listDataModel.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView minHeight, maxHeight;
        LineChart chart;

       //ImageView imgRestaurant;

        public MyViewHolder(View view) {
            super(view);
            minHeight = (TextView) view.findViewById(R.id.minHeight);
            maxHeight = (TextView) view.findViewById(R.id.maxHeight);
            chart = view.findViewById(R.id.chart1);
            //imgRestaurant = (ImageView) view.findViewById(R.id.imgRestaurant);
        }
    }
}
