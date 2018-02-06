package com.wellth.View.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wellth.R;

import java.util.List;


public class IssueAdapter extends ArrayAdapter<String> {
    private final Context mContext;
    private List<String> dataSet;

    public IssueAdapter(List<String> data, Context context) {
        super(context, R.layout.list_adapter_issue_cell, data);
        this.dataSet = data;
        this.mContext=context;
    }
    @Override
    public String getItem(int position){
        return dataSet.get(position);
    }

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View itemView = convertView;
        LayoutInflater inflater;
        AppInfoHolder holder = null;
        String dataModel = getItem(position);
        if(itemView == null) {
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = (View)inflater.inflate(R.layout.list_adapter_issue_cell, parent, false);

            holder = new AppInfoHolder();
            holder.img_check = (ImageView) itemView.findViewById(R.id.img_check);
            holder.txt_title = (TextView) itemView.findViewById(R.id.txt_title);
            itemView.setTag(holder);
        } else {
            holder = (AppInfoHolder)itemView.getTag();
        }
        holder.img_check.setImageResource(R.drawable.ic_check_mark);
        holder.txt_title.setText(dataModel);

        return itemView;
    }

    static class AppInfoHolder
    {
        TextView txt_title;
        ImageView img_check;
    }
}
