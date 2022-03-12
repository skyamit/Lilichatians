package amit.example.lilichatians;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import amit.example.lilichatians.ui.people.PeopleFragment;
import amit.example.lilichatians.ui.friendProfile.FriendProfile;
import amit.example.lilichatians.ui.home.HomeFragment;
import amit.example.lilichatians.ui.people.PeopleFragment;

import amit.example.lilichatians.R.id;

public class MyAdapterForFriend extends RecyclerView.Adapter<MyAdapterForFriend.ViewHolder> {
    ArrayList<DataSnapshot> userData;
    FirebaseAuth mAuth= FirebaseAuth.getInstance();
    String uid = mAuth.getUid();
    String combinedUid;
    public Long status_friend ;


    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

    public MyAdapterForFriend(PeopleFragment peopleFragment, ArrayList userData)
    {
        this.userData = userData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_single_data,parent,false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        //set id of the views
        holder.button = holder.itemView.findViewById(R.id.add_friend);
        holder.textView = holder.itemView.findViewById(id.name);
        holder.current_uid = userData.get(position).getKey();
        holder.button.setTag(userData);
        holder.friend_status = holder.itemView.findViewById(R.id.status_friend);
        holder.friend_status.setVisibility(View.GONE);

        Long status = (Long) userData.get(position).child("status").getValue();
        String current_name = userData.get(position).child("name").getValue().toString();
        holder.textView.setText(current_name);

        if(status>0)
        {
            holder.friend_status.setVisibility(View.VISIBLE);
        }
        mRef.child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (holder.current_uid.compareTo(uid) > 0) {
                    combinedUid = holder.current_uid + uid;
                } else {
                    combinedUid = uid + holder.current_uid;
                }
                if(dataSnapshot.child(combinedUid).child("status").exists()) {
                    status_friend = (Long) dataSnapshot.child(combinedUid).child("status").getValue();
                    if(status_friend==0) {
                        if(dataSnapshot.child(combinedUid).child("senderId").getValue().equals(uid))
                            holder.button.setText("Cancel Request");
                        else
                            holder.button.setText("Accept Request");
                    }
                    if(status_friend==1)
                    {
                        holder.button.setText("unfriend");
                    }
                }
                else{
                    holder.button.setText("Add Friend");
                }

                if(holder.current_uid.equals(uid)) {
                    holder.button.setText("View Profile");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //checking the status if exists.
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.current_uid.equals(uid)) {
                    AppCompatActivity activity = (AppCompatActivity) view.getContext();
                    Fragment fragment = new HomeFragment();
                    activity.getSupportFragmentManager().beginTransaction().replace(id.nav_host_fragment, fragment).addToBackStack("HomeFragment").commit();
                } else {
                    if (holder.current_uid.compareTo(uid) > 0) {
                        combinedUid = holder.current_uid + uid;
                    } else {
                        combinedUid = uid + holder.current_uid;
                    }
                    //getting value of friends status
                    mRef.child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child(combinedUid).child("status").exists()) {
                                status_friend = (Long) dataSnapshot.child(combinedUid).child("status").getValue();
                                if(status_friend==0) {
                                    if(dataSnapshot.child(combinedUid).child("senderId").getValue().equals(uid))
                                    {
                                        mRef.child("friends").child(combinedUid).removeValue();
                                        holder.button.setText("Add Friend");
                                    }
                                    else
                                    {
                                        mRef.child("friends").child(combinedUid).child("status").setValue(1);
                                        holder.button.setText("UnFriend");
                                    }
                                }
                                if(status_friend==1)
                                {
                                    mRef.child("friends").child(combinedUid).removeValue();
                                    holder.button.setText("Add Friend");
                                }
                            }
                            else{
                                mRef.child("friends").child(combinedUid).child("senderId").setValue(uid);
                                mRef.child("friends").child(combinedUid).child("recieverId").setValue(holder.current_uid);
                                mRef.child("friends").child(combinedUid).child("status").setValue(0);
                                holder.button.setText("Cancel Request");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment fragment = new FriendProfile(holder.current_uid);
                activity.getSupportFragmentManager().beginTransaction().replace(id.nav_host_fragment, fragment).commit();
            }
        });

    }

    //to count the number of the data
    @Override
    public int getItemCount() {
        return userData.size();
    }

    //class to hold the data of the recycler view
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        Button button;
        String current_uid;
        CardView friend_status;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


}