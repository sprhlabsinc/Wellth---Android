package com.wellth.View.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.wellth.R;

import java.util.List;


public class ProfileHealthIssueAdapter extends ArrayAdapter<String> {
    private final Context mContext;
    private List<String> dataSet;

    public ProfileHealthIssueAdapter(List<String> data, Context context) {
        super(context, R.layout.list_adapter_profile_issue_cell, data);
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
            itemView = (View)inflater.inflate(R.layout.list_adapter_profile_issue_cell, parent, false);

            holder = new AppInfoHolder();
            holder.img_check = (ImageView) itemView.findViewById(R.id.img_check);
            holder.txt_title = (TextView) itemView.findViewById(R.id.txt_title);
            holder.swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipeLayout);
            holder.delete_but = (Button) itemView.findViewById(R.id.delete_but);
            holder.edit_but = (Button) itemView.findViewById(R.id.edit_but);
            itemView.setTag(holder);
        } else {
            holder = (AppInfoHolder)itemView.getTag();
        }
        holder.img_check.setImageResource(R.drawable.ic_check_mark);
        holder.txt_title.setText(dataModel);

        holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.swipeLayout.findViewWithTag("Bottom4"));

        holder.swipeLayout.findViewById(R.id.delete_but).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0);
            }
        });
        holder.swipeLayout.findViewById(R.id.edit_but).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ListView) parent).performItemClick(v, position, 0);
            }
        });

        return itemView;
    }

    static class AppInfoHolder
    {
        TextView txt_title;
        ImageView img_check;
        SwipeLayout swipeLayout;
        Button delete_but;
        Button edit_but;

    }
}
