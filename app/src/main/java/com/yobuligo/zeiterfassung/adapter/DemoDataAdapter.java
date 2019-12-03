package com.yobuligo.zeiterfassung.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yobuligo.zeiterfassung.DemoData;
import com.yobuligo.zeiterfassung.R;

public class DemoDataAdapter extends RecyclerView.Adapter<DemoDataAdapter.ViewHolder> {

    private DemoData demoData;

    public DemoDataAdapter(DemoData demoData) {
        this.demoData = demoData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_demo_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(demoData.getNamebyIndex(position));
    }

    @Override
    public int getItemCount() {
        return demoData.getSize();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.ItemDemoData);
        }
    }
}
