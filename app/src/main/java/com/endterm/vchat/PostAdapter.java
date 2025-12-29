package com.endterm.vchat;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;

    // Cache (bộ nhớ tạm) để lưu thông tin User
    private Map<String, User> userCache = new HashMap<>();

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        // [QUAN TRỌNG] Thêm Try-Catch để ngăn chặn crash nếu dữ liệu lỗi
        try {
            Post post = postList.get(position);

            // 1. Hiển thị nội dung
            if (post.getDescription() != null) {
                holder.description.setText(post.getDescription());
            } else {
                holder.description.setText("");
            }

            // 2. Hiển thị ảnh bài viết
            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                holder.postImage.setVisibility(View.VISIBLE);
                try {
                    Glide.with(context) // Có thể đổi thành holder.itemView.getContext() nếu context gốc lỗi
                            .load(post.getImageUrl())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(holder.postImage);
                } catch (Exception e) {
                    Log.e("PostAdapter", "Glide error: " + e.getMessage());
                }
            } else {
                holder.postImage.setVisibility(View.GONE);
            }

            // 3. Hiển thị thời gian
            if (post.getTimestamp() != null) {
                long time = post.getTimestamp().getTime();
                CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                holder.postTime.setText(timeAgo);
            } else {
                holder.postTime.setText("Just now");
            }

            // 4. Tải thông tin người đăng
            loadPublisherInfo(holder, post.getUserId());

            // 5. Quản lý Listener
            holder.clearListeners();
            holder.isLikedListener = isLiked(post.getPostId(), holder.likeButton);
            holder.nrLikesListener = nrLikes(holder.likesCounter, post.getPostId());

            // 6. Xử lý click Like
            holder.likeButton.setOnClickListener(v -> handleLikeClick(holder, post));

        } catch (Exception e) {
            Log.e("PostAdapter", "Error binding view: " + e.getMessage());
            // Ẩn item lỗi thay vì crash app
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    @Override
    public void onViewRecycled(@NonNull PostViewHolder holder) {
        super.onViewRecycled(holder);
        holder.clearListeners();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView userAvatar;
        public ImageView postImage, moreOptions, likeButton;
        public TextView username, postTime, description, likesCounter;
        
        ListenerRegistration isLikedListener, nrLikesListener;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.iv_user_avatar);
            username = itemView.findViewById(R.id.tv_username);
            postTime = itemView.findViewById(R.id.tv_post_time);
            description = itemView.findViewById(R.id.tv_post_description);
            postImage = itemView.findViewById(R.id.iv_post_image);
            moreOptions = itemView.findViewById(R.id.iv_more_options);
            likeButton = itemView.findViewById(R.id.like_button);
            likesCounter = itemView.findViewById(R.id.likes_counter);
        }

        void clearListeners() {
            if (isLikedListener != null) {
                isLikedListener.remove();
                isLikedListener = null;
            }
            if (nrLikesListener != null) {
                nrLikesListener.remove();
                nrLikesListener = null;
            }
        }
    }

    private void handleLikeClick(PostViewHolder holder, Post post) {
        if (firebaseUser == null) return;
        DocumentReference likeRef = db.collection("Likes").document(post.getPostId());
        
        // Kiểm tra tag null an toàn
        Object tag = holder.likeButton.getTag();
        if (tag != null && tag.equals("like")) {
            Map<String, Object> data = new HashMap<>();
            data.put("likedBy", FieldValue.arrayUnion(firebaseUser.getUid()));
            likeRef.set(data, SetOptions.merge());
            addNotification(post.getUserId(), post.getPostId());
        } else {
            likeRef.update("likedBy", FieldValue.arrayRemove(firebaseUser.getUid()));
        }
    }

    private void loadPublisherInfo(PostViewHolder holder, String userId) {
        if (userId == null) return;

        if (userCache.containsKey(userId)) {
            updateUserUI(holder, userCache.get(userId));
        } else {
            db.collection("Users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        userCache.put(userId, user);
                        updateUserUI(holder, user);
                    }
                }
            }).addOnFailureListener(e -> Log.e("PostAdapter", "Error load user: " + e.getMessage()));
        }
    }

    private void updateUserUI(PostViewHolder holder, User user) {
        try {
            holder.username.setText(user.getUsername());
            if (user.getImageurl() != null && !user.getImageurl().isEmpty()) {
                Glide.with(context) // Hoặc holder.itemView.getContext()
                        .load(user.getImageurl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(holder.userAvatar);
            } else {
                holder.userAvatar.setImageResource(R.drawable.ic_launcher_background);
            }
        } catch (Exception e) {
            Log.e("PostAdapter", "Error update UI: " + e.getMessage());
        }
    }

    private ListenerRegistration isLiked(String postId, ImageView imageView) {
        if (firebaseUser == null) return null;
        DocumentReference likeRef = db.collection("Likes").document(postId);
        return likeRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) return;
            try {
                if (snapshot != null && snapshot.exists()) {
                    List<String> likedBy = (List<String>) snapshot.get("likedBy");
                    if (likedBy != null && likedBy.contains(firebaseUser.getUid())) {
                        imageView.setImageResource(R.drawable.ic_liked);
                        imageView.setTag("liked");
                    } else {
                        imageView.setImageResource(R.drawable.ic_like);
                        imageView.setTag("like");
                    }
                } else {
                     imageView.setImageResource(R.drawable.ic_like);
                     imageView.setTag("like");
                }
            } catch (Exception ex) {
                Log.e("PostAdapter", "Error isLiked: " + ex.getMessage());
            }
        });
    }

    private ListenerRegistration nrLikes(TextView likes, String postId) {
        DocumentReference likeRef = db.collection("Likes").document(postId);
        return likeRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) return;
            try {
                 if (snapshot != null && snapshot.exists()) {
                    List<String> likedBy = (List<String>) snapshot.get("likedBy");
                    likes.setText((likedBy != null ? likedBy.size() : 0) + " likes");
                } else {
                    likes.setText("0 likes");
                }
            } catch (Exception ex) {
                Log.e("PostAdapter", "Error nrLikes: " + ex.getMessage());
            }
        });
    }

    private void addNotification(String userid, String postid) {
        if (firebaseUser == null || firebaseUser.getUid().equals(userid)) {
            return; 
        }
        // [FIX] Cập nhật constructor mới (thêm receiverId) để tránh lỗi nếu Notification.java đã đổi
        // Nhưng cần cẩn thận nếu Notification.java có constructor cũ hay không.
        // Tốt nhất dùng constructor cũ nếu chưa rõ, hoặc constructor mới nếu đã chắc chắn.
        // Ở bước trước tôi đã thêm constructor 5 tham số và GIỮ constructor 4 tham số.
        // Nên dùng cái nào cũng được. Dùng cái cũ để an toàn logic cũ.
        DocumentReference reference = db.collection("Notifications").document();
        Notification notification = new Notification(firebaseUser.getUid(), "liked your post", postid, true, userid);
        reference.set(notification);
    }
}
