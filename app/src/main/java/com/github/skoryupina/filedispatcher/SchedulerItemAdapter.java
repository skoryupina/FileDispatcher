package com.github.skoryupina.filedispatcher;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ekaterina on 28.10.2015.
 */
public class SchedulerItemAdapter extends ArrayAdapter<SchedulerItem> {

    private int layoutResource;
    private ListView listView;
    public ArrayList<SchedulerItem> schedulerItems;
    private Context context;
    private static final String LOG_TAG = "LOG";

    public static class SchedulerItemHolder {
        public LinearLayout mainView;
        public RelativeLayout deleteView;
        public RelativeLayout shareView;
        public TextView contact;
        public TextView file;
    }

    public SchedulerItemAdapter(Context context, int layoutResource, ArrayList<SchedulerItem> schedulerItems) {
        super(context, layoutResource, schedulerItems);
        this.schedulerItems = schedulerItems;
        this.layoutResource = layoutResource;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
  /*      // Get the data item for this position
        View workingView = null;
        View row=convertView;
        // Check if an existing view is being reused, otherwise inflate the view
        // view lookup cache stored in tag
        if (convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            workingView = inflater.inflate(layoutResource, parent, false);
        }
        else
        {
            Log.e(LOG_TAG, "---------цикличность---------- ");
            Log.e(LOG_TAG, "конверт: " + convertView.toString());

            workingView = convertView;
        }
        SchedulerItemHolder viewHolder = getSchedulerItemHolder(workingView,position);

        /*RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.mainView.getLayoutParams();
        params.rightMargin = 0;
        params.leftMargin = 0;
        viewHolder.mainView.setLayoutParams(params);
        SwipeDetector swipeDetector = new SwipeDetector(viewHolder, position);
        workingView.setOnTouchListener(swipeDetector);
        return workingView;*/


        // Get the data item for this position
        View workingView = null;
        View row = convertView;
        SchedulerItemHolder viewHolder;
        // Check if an existing view is being reused, otherwise inflate the view
        // view lookup cache stored in tag
        if (row == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            row = inflater.inflate(layoutResource, parent, false);
            viewHolder = getSchedulerItemHolder(row, position);
        } else {
            Log.e(LOG_TAG, "---------цикличность---------- ");
            Log.e(LOG_TAG, "конверт: " + convertView.toString());
            viewHolder = (SchedulerItemHolder) row.getTag();


            //workingView = convertView;
        }
        viewHolder.contact.setText(getItem(position).contact);
        viewHolder.file.setText(getItem(position).file);
        //SchedulerItemHolder viewHolder = getSchedulerItemHolder(workingView,position);

        /*RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.mainView.getLayoutParams();
        params.rightMargin = 0;
        params.leftMargin = 0;
        viewHolder.mainView.setLayoutParams(params);*/
        SwipeDetector swipeDetector = new SwipeDetector(viewHolder, position);
        row.setOnTouchListener(swipeDetector);
        return row;
    }

    private SchedulerItemHolder getSchedulerItemHolder(View workingView, int position) {
        Object tag = workingView.getTag();
        SchedulerItemHolder holder = null;

        if (tag == null || !(tag instanceof SchedulerItemHolder)) {
            holder = new SchedulerItemHolder();
            holder.mainView = (LinearLayout) workingView.findViewById(R.id.item_scheduler_mainview);
            holder.deleteView = (RelativeLayout) workingView.findViewById(R.id.item_scheduler_deleteview);
            holder.shareView = (RelativeLayout) workingView.findViewById(R.id.item_scheduler_shareview);

            SchedulerItem schedulerItem = getItem(position);

            holder.contact = (TextView) workingView.findViewById(R.id.tvContact);
            holder.file = (TextView) workingView.findViewById(R.id.tvFile);
            workingView.setTag(holder);

            // Populate the data into the template view using the data object
            holder.contact.setText(schedulerItem.contact);
            holder.file.setText(schedulerItem.file);
            Log.e(LOG_TAG, "---------отображение---------- ");
            Log.e(LOG_TAG, "В адаптере: позиция: " + position);
            Log.e(LOG_TAG, "В адаптере: айтем: " + schedulerItem.toString());
            Log.e(LOG_TAG, "В адаптере: айтем контакт: " + schedulerItem.contact);
            Log.e(LOG_TAG, "---------отображение---------- ");
        } else {
            holder = (SchedulerItemHolder) tag;
        }
        return holder;
    }


    @Override
    public int getCount() {
        return schedulerItems.size();
    }

    @Override
    public SchedulerItem getItem(int position) {
        return schedulerItems.get(position);
    }


    public void setListView(ListView view) {
        listView = view;
    }

    // swipe detector class here
    public class SwipeDetector implements View.OnTouchListener {

        private static final int MIN_DISTANCE = 300;
        private static final int MIN_LOCK_DISTANCE = 30; // disallow motion intercept
        private boolean motionInterceptDisallowed = false;
        private float downX, upX;
        private SchedulerItemHolder holder;
        private int position;

        public SwipeDetector(SchedulerItemHolder holder, int position) {
            this.holder = holder;
            this.position = position;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            String logTag = "LOG";
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    Log.d(logTag, "ACTION_DOWN");
                    downX = event.getX();
                    Log.d(logTag, "ACTION_DOWN_X: " + position);
                    return true; // allow other events like Click to be processed
                }

                case MotionEvent.ACTION_MOVE: {
                    Log.d(logTag, "ACTION_MOVE");
                    upX = event.getX();
                    float deltaX = downX - upX;
                    Log.d(logTag, "ACTION_MOVE: " + position);

                    if (Math.abs(deltaX) > MIN_LOCK_DISTANCE && listView != null && !motionInterceptDisallowed) {
                        listView.requestDisallowInterceptTouchEvent(true);
                        motionInterceptDisallowed = true;
                    }

                    if (deltaX > 0) {
                        holder.deleteView.setVisibility(View.GONE);
                    } else {
                        // if first swiped left and then swiped right
                        holder.deleteView.setVisibility(View.VISIBLE);
                    }

                    swipe(-(int) deltaX);
                    return true;
                }

                case MotionEvent.ACTION_UP: {
                    Log.d(logTag, "ACTION_UP");
                    upX = event.getX();
                    float deltaX = upX - downX;
                    if (deltaX > MIN_DISTANCE) {
                        // left or right
                        swipeRemove();
                    } else if (Math.abs(deltaX) > MIN_DISTANCE) {
                        ((MainActivity) context).sendSMSFileName(getItem(position));
                        swipe(0);
                    } else {
                        swipe(0);
                    }

                    if (listView != null) {
                        listView.requestDisallowInterceptTouchEvent(false);
                        motionInterceptDisallowed = false;
                    }

                    holder.deleteView.setVisibility(View.VISIBLE);
                    return true;
                }

                case MotionEvent.ACTION_CANCEL: {
                    Log.d(logTag, "ACTION_CANCEL");
                    holder.deleteView.setVisibility(View.VISIBLE);
                    swipe(0);
                    return false;
                }
            }

            return true;
        }

        private void swipe(int distance) {
            View animationView = holder.mainView;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) animationView.getLayoutParams();
            params.rightMargin = -distance;
            params.leftMargin = distance;
            animationView.setLayoutParams(params);
        }

        private void swipeRemove() {
            Log.e(LOG_TAG, "Удаление позиции: " + position);
            Log.e(LOG_TAG, "Удаление эл-та: " + getItem(position).contact);
            Log.e(LOG_TAG, "размер адаптера: " + schedulerItems.size());
            /*((MainActivity) context).schedulerItems.*/remove(getItem(position));

            Log.e(LOG_TAG, "После удаления: " + position);
            Log.e(LOG_TAG, "размер адаптера: " + schedulerItems.size());
            ((MainActivity) context).updateFileEntries(true);
            ((MainActivity) context).updateListView();
            notifyDataSetChanged();
        }
    }


}