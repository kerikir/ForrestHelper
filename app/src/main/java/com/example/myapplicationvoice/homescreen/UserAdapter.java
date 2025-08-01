package com.example.myapplicationvoice.homescreen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationvoice.R;
import com.example.myapplicationvoice.userinformation.NetworkUser;
import com.example.myapplicationvoice.userinformation.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>
{
    private List<NetworkUser> users;
    private OnUserActionListener actionListener;


    public interface OnUserActionListener
    {
        void onMessageClick(NetworkUser user);
        void onCallClick(NetworkUser user);
    }


    public UserAdapter(List<NetworkUser> users, OnUserActionListener listener)
    {
        this.users = users;
        this.actionListener = listener;
    }


    public void updateUsers(List<NetworkUser> newUsers)
    {
        this.users = newUsers;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position)
    {
        NetworkUser user = users.get(position);
        holder.bind(user);
    }


    @Override
    public int getItemCount() {
        return users.size();
    }


    class UserViewHolder extends RecyclerView.ViewHolder
    {
        private TextView nameTextView;
        private TextView ipAddressTextView;
        private ImageButton messageButton;
        private ImageButton callButton;
        private CardView cardViewMessage;


        public UserViewHolder(@NonNull View itemView)
        {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.user_name);
            ipAddressTextView = itemView.findViewById(R.id.user_ip);
            messageButton = itemView.findViewById(R.id.btn_message);
            callButton = itemView.findViewById(R.id.btn_call);

            cardViewMessage = itemView.findViewById(R.id.btn_message_background);
        }


        public void bind(NetworkUser user)
        {
            nameTextView.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
            ipAddressTextView.setText(user.getIpAddress());

            // Установка обработчиков для кнопок
            messageButton.setOnClickListener(v ->
            {
                if (actionListener != null)
                {
                    actionListener.onMessageClick(user);
                }
            });

            callButton.setOnClickListener(v ->
            {
                if (actionListener != null)
                {
                    actionListener.onCallClick(user);
                }
            });
        }


        public void setMessageCardColor(int colorRes) {
            cardViewMessage.setCardBackgroundColor(
                    itemView.getContext().getResources().getColor(colorRes, null)
            );
        }
    }
}