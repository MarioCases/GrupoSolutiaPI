package com.example.portalinternogruposolutia.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.portalinternogruposolutia.R;
import com.example.portalinternogruposolutia.model.Project;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    private List<Project> projects;
    private boolean readOnly;

    public ProjectAdapter(List<Project> projects, boolean readOnly) {
        this.projects = projects;
        this.readOnly = readOnly;
    }

    public void updateList(List<Project> newList, boolean readOnly) {
        this.projects = newList;
        this.readOnly = readOnly;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_project, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Project p = projects.get(pos);

        h.name.setText(p.getName());
        h.description.setText(p.getDescription());
        h.statusLabel.setText(p.getStatusLabel());
        h.department.setText(p.getDepartment());

        h.itemView.setAlpha(readOnly ? 0.6f : 1f);

        h.statusStrip.setBackgroundColor(p.getStatusColor());

        GradientDrawable badgeBg = (GradientDrawable) h.statusLabel.getBackground();
        badgeBg.setColor(p.getStatusBgRes());

        h.tagsContainer.removeAllViews();
        for (String tech : p.getTechnologies()) {
            TextView tag = (TextView) LayoutInflater.from(h.itemView.getContext())
                    .inflate(R.layout.item_tag, h.tagsContainer, false);
            tag.setText(tech);
            h.tagsContainer.addView(tag);
        }
    }

    @Override
    public int getItemCount() { return projects.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View statusStrip;
        TextView name, description, statusLabel, department;
        LinearLayout tagsContainer;

        ViewHolder(View v) {
            super(v);
            statusStrip = v.findViewById(R.id.statusStrip);
            name = v.findViewById(R.id.projectName);
            description = v.findViewById(R.id.projectDescription);
            statusLabel = v.findViewById(R.id.projectStatusLabel);
            department = v.findViewById(R.id.projectDepartment);
            tagsContainer = v.findViewById(R.id.tagsContainer);
        }
    }
}
