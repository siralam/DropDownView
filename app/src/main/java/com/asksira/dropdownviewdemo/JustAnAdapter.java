package com.asksira.dropdownviewdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class JustAnAdapter extends RecyclerView.Adapter<JustAnAdapter.JustAViewHolder> {

    Context context;
    List<String> stringList;

    public JustAnAdapter(Context context, List<String> stringList) {
        this.context = context;
        this.stringList = stringList;
    }

    @Override
    public JustAViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new JustAViewHolder(LayoutInflater.from(context).inflate(R.layout.item_string, parent, false));
    }

    public void setStringList (List<String> list) {
        stringList = list;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(JustAViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return stringList.size();
    }

    public class JustAViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public JustAViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.item_string);
        }

        public void bind(int position) {
            textView.setText(stringList.get(position));
        }
    }
}
