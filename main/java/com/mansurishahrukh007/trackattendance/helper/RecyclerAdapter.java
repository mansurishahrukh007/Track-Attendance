package com.mansurishahrukh007.trackattendance.helper;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import info.androidhive.loginandregistration.R;

/**
 * Created by Shahrukh Mansuri on 4/12/2016.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private List<Attendance> attendances;
    private Activity activity;

    public RecyclerAdapter(List<Attendance> attendances, Activity activity) {
        this.attendances = attendances;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.item_recycler, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.date.setText(attendances.get(position).getDate());
        holder.attendance.setText(attendances.get(position).getAttendance());
        holder.addedAt.setText(attendances.get(position).getAddedAt());
        holder.updatedAt.setText(attendances.get(position).getUpdatedAt());

    }

    @Override
    public int getItemCount() {
        return (null != attendances ? attendances.size() : 0);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private TextView date;
        private TextView attendance;
        private TextView addedAt;
        private TextView updatedAt;
        private View container;

        public ViewHolder(View view) {
            super(view);
            date = (TextView) view.findViewById(R.id.tv_date);
            attendance = (TextView) view.findViewById(R.id.tv_attendance);
            addedAt = (TextView) view.findViewById(R.id.tv_added_at);
            updatedAt = (TextView) view.findViewById(R.id.tv_updated_at);
            container = view.findViewById(R.id.card_view);
        }
    }
}
