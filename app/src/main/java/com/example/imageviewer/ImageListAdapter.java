package com.example.imageviewer;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder> {

    private final List<String> timestampList;
    private final List<Bitmap> thumbnailList;
    View.OnClickListener mListener;
    int mRowNumber;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;

        LinearLayout linearLayout;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.image_view);
            textView = view.findViewById(R.id.text_view);
            linearLayout = itemView.findViewById(R.id.recycler_view_list);
        }
    }

    ImageListAdapter(List<String> timestampList, List<Bitmap> thumbnailList) {
        this.timestampList = timestampList;
        this.thumbnailList = thumbnailList;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_image_list, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // Get element from your dataset at this position and replace the contexts
        // of the view with that element

        viewHolder.imageView.setImageBitmap(thumbnailList.get(position));
        viewHolder.textView.setText(timestampList.get(position));

        final int pos = position;
        viewHolder.linearLayout.setOnClickListener(view -> {
            mRowNumber = pos; // 行数を記録
            mListener.onClick(view);
        });
    }

    // Return the size of your dataset(invoked by the layout manager)
    @Override
    public int getItemCount() {
        return timestampList.size();
    }

    public void setOnItemClickListener(View.OnClickListener listener) {
        mListener = listener;
    }

    public int getLine(){
        return mRowNumber; //行数を取得
    }
}