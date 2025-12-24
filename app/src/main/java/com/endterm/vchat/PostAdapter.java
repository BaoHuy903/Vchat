package com.endterm.vchat;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        holder.description.setText(post.getDescription());

        // Load post image
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.postImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(post.getImageUrl()).into(holder.postImage);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }

        // Set post time
        if (post.getTimestamp() != null) {
            long time = post.getTimestamp().getTime();
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            holder.postTime.setText(timeAgo);
        } else {
            holder.postTime.setText("Just now");
        }

        // Load publisher info
        publisherInfo(holder.userAvatar, holder.username, post.getUserId());

        // Clear previous listeners before attaching new ones
        holder.clearListeners();

        // Handle like button state and clicks
        holder.isLikedListener = isLiked(post.getPostId(), holder.likeButton);
        holder.nrLikesListener = nrLikes(holder.likesCounter, post.getPostId());

        holder.likeButton.setOnClickListener(v -> {
            if (firebaseUser == null) return;
            DocumentReference likeRef = FirebaseFirestore.getInstance().collection("Likes").document(post.getPostId());
            if (holder.likeButton.getTag().equals("like")) {
                Map<String, Object> data = new HashMap<>();
                data.put("likedBy", FieldValue.arrayUnion(firebaseUser.getUid()));
                likeRef.set(data, SetOptions.merge());
                addNotification(post.getUserId(), post.getPostId());
            } else {
                likeRef.update("likedBy", FieldValue.arrayRemove(firebaseUser.getUid()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    @Override
    public void onViewRecycled(@NonNull PostViewHolder holder) {
        super.onViewRecycled(holder);
        // Clear listeners when view is recycled
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

    private void publisherInfo(final CircleImageView userAvatar, final TextView username, final String userId) {
        if (userId == null) return;
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    username.setText(user.getUsername());
                    if (user.getImageurl() != null && !user.getImageurl().isEmpty()) {
                        Glide.with(context).load(user.getImageurl()).into(userAvatar);
                    } else {
                        userAvatar.setImageResource(R.drawable.ic_launcher_background);
                    }
                }
            }
        });
    }

    private ListenerRegistration isLiked(String postId, ImageView imageView) {
        if (firebaseUser == null) return null;
        DocumentReference likeRef = FirebaseFirestore.getInstance().collection("Likes").document(postId);
        return likeRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) { return; }
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
        });
    }

    private ListenerRegistration nrLikes(TextView likes, String postId) {
        DocumentReference likeRef = FirebaseFirestore.getInstance().collection("Likes").document(postId);
        return likeRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) { return; }
             if (snapshot != null && snapshot.exists()) {
                List<String> likedBy = (List<String>) snapshot.get("likedBy");
                likes.setText((likedBy != null ? likedBy.size() : 0) + " likes");
            } else {
                likes.setText("0 likes");
            }
        });
    }

    private void addNotification(String userid, String postid) {
        if (firebaseUser == null || firebaseUser.getUid().equals(userid)) {
            return; // Don't notify for your own likes or if not logged in
        }
        DocumentReference reference = FirebaseFirestore.getInstance().collection("Notifications").document();
        Notification notification = new Notification(userid, "liked your post", postid, true);
        reference.set(notification);
    }
}
