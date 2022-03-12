package amit.example.lilichatians;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import amit.example.lilichatians.R;
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

public class MyAdapterForComments extends RecyclerView.Adapter<MyAdapterForComments.ViewHolder> {

    private ArrayList<DataSnapshot> dataSnapshots;
    FirebaseAuth mAuth= FirebaseAuth.getInstance();
    String uid = mAuth.getUid();
    String post_id;
    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef = firebaseStorage.getReference();

    MyAdapterForComments(ArrayList<DataSnapshot> dataSnapshot, String post_id)
    {
        this.dataSnapshots = dataSnapshot;
        this.post_id = post_id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_comment,parent,false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int positionn) {
        final int position = dataSnapshots.size()-positionn-1;
        holder.comment_body = holder.itemView.findViewById(R.id.comment_body);
        holder.comment_name = holder.itemView.findViewById(R.id.comment_name);
        holder.comment_profile_image = holder.itemView.findViewById(R.id.comment_profile_image);
        holder.comment_time = holder.itemView.findViewById(R.id.comment_time);
        holder.delete_comment = holder.itemView.findViewById(R.id.delete_comment);
        holder.delete_comment.setVisibility(View.GONE);

        Long time = System.currentTimeMillis();
        holder.comment_body.setText(dataSnapshots.get(position).child("content").getValue().toString());
        holder.comment_time.setText(calculate_time(time - (Long)dataSnapshots.get(position).child("timestamp").getValue()));

        final String comment_uid = dataSnapshots.get(position).child("from").getValue().toString();
        mRef.child("users").child(comment_uid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long status = (Long) dataSnapshot.child("status").getValue();
                if(status==1) {
                    holder.comment_name.setText(dataSnapshot.child("name").getValue().toString().trim());
                }
                else {
                    holder.comment_name.setText(dataSnapshot.child("name").getValue().toString().trim());
                }

                if (dataSnapshot.child("profilePic").exists()){
                    StorageReference storageReference = mStorageRef.child("ProfileImages").child(dataSnapshot.child("profilePic").getValue().toString());
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri.toString()).into(holder.comment_profile_image);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(holder.comment_body,e+"",Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.comment_body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uid.equals(comment_uid))
                    holder.delete_comment.setVisibility(View.VISIBLE);
            }
        });
        holder.delete_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uid.equals(comment_uid))
                    mRef.child("comments").child(post_id).child(dataSnapshots.get(position).getKey()).removeValue();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSnapshots.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView comment_name,comment_body,comment_time;
        ImageView comment_profile_image,delete_comment;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public String calculate_time(long time)
    {
        time = time/1000;
        if(time<60)
        {
            return time+" seconds ago ";
        }
        else
        {
            long min = time/60;
            if(min<60)
            {
                return min+" minutes ago ";
            }
            else
            {
                long hour = min/60;
                if(hour<24)
                {
                    return hour+" hours ago ";
                }
                else
                {
                    long day = hour/24;
                    if(day<31)
                    {
                        return day+" days ago ";
                    }
                    else
                    {
                        long month = day/31;
                        if(month<12)
                        {
                            return month+" months ago ";
                        }
                        else
                        {
                            long year = month/12;
                            return year+" years ago ";
                        }
                    }
                }
            }
        }
    }
}
