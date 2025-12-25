package com.endterm.vchat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context mContext;
    private List<Notification> mNotification;

    public NotificationAdapter(Context mContext, List<Notification> mNotification) {
        this.mContext = mContext;
        this.mNotification = mNotification;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // [FIX] Thêm try-catch để ngăn chặn mọi lỗi crash tiềm ẩn trong item
        try {
            final Notification notification = mNotification.get(position);

            holder.comment.setText(notification.getText());

            getUserInfo(holder.image_profile, holder.username, notification.getUserid());

            if (notification.isIspost()) {
                holder.post_image.setVisibility(View.VISIBLE);
                holder.post_image.setImageResource(android.R.color.transparent);
                getPostImage(holder.post_image, notification.getPostid());
            } else {
                holder.post_image.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e("NotificationAdapter", "Error binding view: " + e.getMessage());
            // Ẩn view lỗi hoặc hiển thị view trống thay vì crash app
            holder.itemView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mNotification.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView image_profile;
        public ImageView post_image;
        public TextView username, comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
        }
    }

    private void getUserInfo(final CircleImageView imageView, final TextView username, String publisherid) {
        if (publisherid == null || publisherid.isEmpty()) return;
        try {
            DocumentReference reference = FirebaseFirestore.getInstance().collection("Users").document(publisherid);
            reference.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null && imageView != null && imageView.getContext() != null) {
                        try {
                            Glide.with(imageView.getContext())
                                 .load(user.getImageurl())
                                 .placeholder(R.drawable.ic_launcher_background)
                                 .into(imageView);
                            username.setText(user.getUsername());
                        } catch (Exception e) { Log.e("Adapter", "Glide error"); }
                    }
                }
            }).addOnFailureListener(e -> Log.e("Adapter", "Firestore error: " + e.getMessage()));
        } catch (Exception e) { Log.e("Adapter", "Error getting user info"); }
    }

    private void getPostImage(final ImageView imageView, String postid) {
        if (postid == null || postid.isEmpty()) return;
        try {
            DocumentReference reference = FirebaseFirestore.getInstance().collection("posts").document(postid);
            reference.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Post post = documentSnapshot.toObject(Post.class);
                    if (post != null && imageView != null && imageView.getContext() != null) {
                        try {
                            Glide.with(imageView.getContext())
                                .load(post.getImageUrl())
                                .placeholder(android.R.color.darker_gray) // [FIX] Đổi thành màu có sẵn của hệ thống
                                .into(imageView);
                        } catch (Exception e) { Log.e("Adapter", "Glide error"); }
                    }
                }
            });
        } catch (Exception e) { Log.e("Adapter", "Error getting post image"); }
    }
}
