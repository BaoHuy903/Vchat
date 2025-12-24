package com.endterm.vchat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;

    public ContactAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_contact, parent, false);
        return new ContactAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.username.setText(user.getUsername());

        if (user.getImageurl() != null && !user.getImageurl().isEmpty()) {
            Glide.with(mContext).load(user.getImageurl()).into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_launcher_background); 
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra("userid", user.getId());
            mContext.startActivity(intent);
        });

        // For now, we'll leave last message and timestamp as placeholders
        holder.lastMessage.setText("Tap to chat");
        holder.timestamp.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public CircleImageView profileImage;
        public TextView lastMessage;
        public TextView timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.contact_name);
            profileImage = itemView.findViewById(R.id.contact_profile_image);
            lastMessage = itemView.findViewById(R.id.contact_last_message);
            timestamp = itemView.findViewById(R.id.contact_timestamp);
        }
    }
}
