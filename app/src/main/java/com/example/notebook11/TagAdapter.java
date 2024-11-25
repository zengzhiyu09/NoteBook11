package com.example.notebook11;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagAdapter extends BaseAdapter {
    //侧边栏的展示 lv
    private final Context context;
    private final List<String> tagList;
    private final List<Integer> numList;


    public TagAdapter(Context context, List<String> tagList, List<Integer> numList) {
        this.context = context;
        this.tagList = tagList;
        this.numList = numList;

    }


    @Override
    public int getCount() {
        return tagList.size();
    }

    @Override
    public Object getItem(int i) {
        return tagList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    static class ViewHolder{ //通过将 ViewHolder 声明为静态类，它可以独立存在，不持有外部类的引用，从而减少内存泄漏的风险。
        TextView num_tag;
        TextView text_tag;

    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        Log.d("tag", "getView: " + numList.size());

        ViewHolder holder = new ViewHolder();
        if(view == null) {
            view = View.inflate(context, R.layout.tag_layout, null);
            holder.num_tag = view.findViewById(R.id.num_tag);
            holder.text_tag = view.findViewById(R.id.text_tag);
            view.setTag(holder);

        }else {
            holder = (ViewHolder) view.getTag();
        }

        holder.num_tag.setText(numList.get(i).toString());
        holder.text_tag.setText(tagList.get(i));


        return view;
    }
}
