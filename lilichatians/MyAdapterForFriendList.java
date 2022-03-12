package amit.example.lilichatians;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import amit.example.lilichatians.ui.people.PeopleFragment;
import amit.example.lilichatians.ui.friendProfile.FriendProfile;
import amit.example.lilichatians.ui.messageusers.MessageUsersFragment;
import amit.example.lilichatians.ui.people.PeopleFragment;


public class MyAdapterForFriendList extends RecyclerView.Adapter<MyAdapterForFriendList.ViewHolderList> {

    ArrayList<DataSnapshot> userData;
    FirebaseAuth mAuth= FirebaseAuth.getInstance();
    String uid = mAuth.getUid();
    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users");
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    View v;

    public MyAdapterForFriendList(PeopleFragment peopleFragment, ArrayList userData)
    {
        this.userData = userData;
    }

    @NonNull
    @Override
    public ViewHolderList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_single_data,parent,false);
        return new ViewHolderList(v);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolderList holder, int position) {

        holder.button = holder.itemView.findViewById(R.id.add_friend);
        holder.textView = holder.itemView.findViewById(R.id.name);
        holder.friendPhoto = holder.itemView.findViewById(R.id.friendPhoto);
        holder.status_friend = holder.itemView.findViewById(R.id.status_friend);
        holder.status_friend.setVisibility(View.GONE);

        holder.button.setTag(userData);
        if (userData.get(position).child("senderId").getValue().toString().equals(uid)) {
            holder.current_uid = userData.get(position).child("recieverId").getValue().toString();
        } else {
            holder.current_uid = userData.get(position).child("senderId").getValue().toString();
        }

        mRef.child(holder.current_uid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long status = (Long) dataSnapshot.child("status").getValue();
                if(status==1) {
                    holder.status_friend.setVisibility(View.VISIBLE);
                    holder.textView.setText(dataSnapshot.child("name").getValue().toString().trim());
                }
                else {
                    holder.textView.setText(dataSnapshot.child("name").getValue().toString().trim());
                }

                if (dataSnapshot.child("profilePic").exists()){
                    StorageReference storageReference = mStorageRef.child("ProfileImages").child(dataSnapshot.child("profilePic").getValue().toString());
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri.toString()).into(holder.friendPhoto);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(v,e+"",Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.button.setText("Message");
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment fragment = new MessageUsersFragment(holder.current_uid);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("ToMessage").commit();
            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment fragment = new FriendProfile(holder.current_uid);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("ToFriendProfile").commit();
            }
        });

    }


    @Override
    public int getItemCount() {
        return userData.size();
    }


    public static class ViewHolderList extends RecyclerView.ViewHolder {
        TextView textView;
        Button button;
        String current_uid;
        ImageView friendPhoto;
        CardView status_friend;
        public ViewHolderList(@NonNull View itemView) {
            super(itemView);
        }
    }

}
