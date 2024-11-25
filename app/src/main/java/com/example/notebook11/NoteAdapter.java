package com.example.notebook11;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
//适配器，用于将item显示到ListItem/RecycleItem
public class NoteAdapter extends BaseAdapter implements Filterable {
//因为继承的是BaseAdapter所以只能用ListView RecycleView要用专门的
    private Context mContext;
    private List<Note> backList;//备份原始数据
    private List<Note> noteList;//会改变的
    private MyFilter myFilter;
    private LayoutInflater inflater;

    public NoteAdapter(Context mContext,List<Note> noteList){
        this.mContext = mContext;
        this.noteList = noteList;
        backList = noteList;
        this.inflater = LayoutInflater.from(mContext);
    }
    //类中类
    class MyFilter extends Filter{
        //在此定义过滤规则

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults res = new FilterResults();
            List<Note> list;
            if(TextUtils.isEmpty(charSequence)){//过滤关键字为空，显示所有数据
                list = backList;
            }else {//查找符合对象
                list = new ArrayList<>();
                for(Note note :backList){
                    if(note.getContent().contains(charSequence)||note.getTitle().contains(charSequence)){
                        list.add(note);
                    }
                }
            }
            res.values = list;
            res.count = list.size();
            return res; //返回得到的集合
        }
        //告诉适配器更新界面
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            if(filterResults.values instanceof List) {
                noteList = (List<Note>) filterResults.values;
            }
            if(filterResults.count>0){
                notifyDataSetChanged();
            }else{
                notifyDataSetInvalidated();
            }
        }
    }

    static class ViewHolder{ //通过将 ViewHolder 声明为静态类，它可以独立存在，不持有外部类的引用，从而减少内存泄漏的风险。
        TextView tv_content;
        TextView tv_time;
        TextView tv_title;
        long _id;
        int tag;

    }

    @Override
    public int getCount() {
        return noteList.size();
    }

    @Override
    public Object getItem(int i) {
        return noteList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return noteList.get(i).getId();//大草 虽然好像和直接返回i是一样的
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        

        //正确的加载方式是当convertView不为空的时候直接重新使用convertView从而减少了很多不必要的View的创建，然后加载数据
        ViewHolder holder = new ViewHolder();
        if( convertView == null) {
            convertView = View.inflate(mContext, R.layout.note_layout, null);//渲染 layout传到v中 跟下面一样的
            //convertView = inflater.inflate(R.layout.note_layout,parent,false);

//        TextView tv_content = (TextView) convertView.findViewById(R.id.tv_content);//完了他有title 我不该质疑的 凭什么我想到了人家怎么可能没想到！！
//        TextView tv_time = (TextView) convertView.findViewById(R.id.tv_time);
//        TextView tv_title = (TextView)convertView.findViewById(R.id.tv_title);
            holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
            holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            convertView.setTag(holder); // 将 ViewHolder 附加到 convertView 上
        }else{
            holder = (ViewHolder) convertView.getTag(); // 从 convertView 中获取 ViewHolder
        }

        //his title ,THIS IS MY TITLE
        Note item = noteList.get(position);
        Log.d("tag",noteList.toString());
        holder._id = item.getId();
        holder.tv_content.setText(item.getContent());
        holder.tv_time.setText(item.getTime());
        holder.tv_title.setText(item.getTitle());
        holder.tag = item.getTag();

        //save note id to tag
        //convertView.setTag(item.getId());
        return convertView;
    }


    @Override
    public Filter getFilter() {
        if(myFilter == null){
            myFilter = new MyFilter();
        }
        return myFilter;
    }
}

