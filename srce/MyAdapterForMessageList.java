package amit.example.lilichatians;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
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

import amit.example.lilichatians.ui.messageusers.MessageUsersFragment;

public class MyAdapterForMessageList extends RecyclerView.Adapter<MyAdapterForMessageList.ViewHolderList> {

    private ArrayList<DataSnapshot> dataSnapshots;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String uid = mAuth.getUid();
    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users");
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    View v;
    public MyAdapterForMessageList(ArrayList<DataSnapshot> dataSnapshots)
    {
        this.dataSnapshots = dataSnapshots;
    }
    @NonNull
    @Override
    public ViewHolderList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_single,parent,false);
        ViewHolderList vh = new ViewHolderList(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolderList holder, int position) {
        holder.username = holder.itemView.findViewById(R.id.username);
        holder.lastMessage = holder.itemView.findViewById(R.id.lastMessage);
        holder.itemView.setTag(dataSnapshots.get(position));
        holder.userPhoto = holder.itemView.findViewById(R.id.userPhoto);
        holder.status = holder.itemView.findViewById(R.id.status);
        holder.status.setVisibility(View.GONE);
        holder.status_msg = dataSnapshots.get(position).child("lastMessage").child("status").getValue().toString();


        String attach;
            if(dataSnapshots.get(position).child("lastMessage").child("senderId").getValue().toString().equals(uid)){
                holder.current_uid = dataSnapshots.get(position).child("lastMessage").child("recieverId").getValue().toString();
                attach = " You : ";
            }else{
                if(holder.status_msg.equals("0"))
                {
                    holder.lastMessage.setBackground(ContextCompat.getDrawable(holder.lastMessage.getContext(),R.drawable.green_button));
                    holder.lastMessage.setTextColor(ContextCompat.getColor(holder.lastMessage.getContext(),R.color.white));
                }
                holder.current_uid = dataSnapshots.get(position).child("lastMessage").child("senderId").getValue().toString();
                attach = " -> ";
            }

            mRef.child(holder.current_uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Long status = (Long) dataSnapshot.child("status").getValue();
                    try{
                        if(status==1) {
                            holder.status.setVisibility(View.VISIBLE);
                            holder.username.setText(dataSnapshot.child("name").getValue().toString().trim());
                        }
                        else {
                            holder.username.setText(dataSnapshot.child("name").getValue().toString().trim());
                        }
                    }catch (NullPointerException e)
                    {

                    }
                    if (dataSnapshot.child("profilePic").exists()){
                        StorageReference storageReference = mStorageRef.child("ProfileImages").child(dataSnapshot.child("profilePic").getValue().toString());
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri.toString()).into(holder.userPhoto);
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

            holder.lastMessage.setText(attach + dataSnapshots.get(position).child("lastMessage").child("content").getValue().toString()+"....");


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment fragment = new MessageUsersFragment(holder.current_uid);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("MessageUsersFragment").commit();
            }
        });

    }


    @Override
    public int getItemCount() {
        return dataSnapshots.size();
    }

    public class ViewHolderList extends RecyclerView.ViewHolder {
        TextView username,lastMessage;
        String current_uid,status_msg;
        ImageView userPhoto;
        CardView status;


        public ViewHolderList(@NonNull View itemView) {
            super(itemView);
        }
    }
}
