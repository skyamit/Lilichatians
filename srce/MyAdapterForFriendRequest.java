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

import amit.example.lilichatians.ui.friendProfile.FriendProfile;

public class
MyAdapterForFriendRequest extends RecyclerView.Adapter<MyAdapterForFriendRequest.ViewHolder> {

    ArrayList<DataSnapshot> userData;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String uid  = mAuth.getUid();
    private String combinedUid;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    View v;

    public MyAdapterForFriendRequest(ArrayList<DataSnapshot> userData)
    {
        this.userData = userData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_single_data,parent,false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.button = holder.itemView.findViewById(R.id.add_friend);
        holder.textView = holder.itemView.findViewById(R.id.name);
        holder.current_uid = userData.get(position).getKey();
        holder.button.setTag(userData);
        holder.friendPhoto = holder.itemView.findViewById(R.id.friendPhoto);

        final Long[] status = {(Long) userData.get(position).child("status").getValue()};
            holder.button.setText("Accept Request");

        holder.current_uid = userData.get(position).child("senderId").getValue().toString();

        if (holder.current_uid.compareTo(uid) > 0) {
            combinedUid = holder.current_uid + uid;
        } else {
            combinedUid = uid + holder.current_uid;
        }

        myRef.child("users").child(holder.current_uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    holder.textView.setText(dataSnapshot.child("name").getValue().toString());

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.current_uid.compareTo(uid) > 0) {
                    combinedUid = holder.current_uid + uid;
                } else {
                    combinedUid = uid + holder.current_uid;
                }
                System.out.println(combinedUid);

                myRef.child("friends").child(combinedUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            status[0] = (Long)dataSnapshot.child("status").getValue();
                            if(status[0]==0)
                            {
                                myRef.child("friends").child(combinedUid).child("status").setValue(1);
                                holder.button.setText("Accepted");
                                holder.button.setEnabled(false);

                            }
                            if(status[0]==1)
                            {
                                holder.button.setText("Accepted");
                                holder.button.setEnabled(false);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment fragment = new FriendProfile(holder.current_uid);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("FriendProfile").commit();
            }
        });

    }

    @Override
    public int getItemCount() {
        return userData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        Button button;
        String current_uid;
        ImageView friendPhoto;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
