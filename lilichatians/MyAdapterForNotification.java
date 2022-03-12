package amit.example.lilichatians;

import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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

public class MyAdapterForNotification extends RecyclerView.Adapter<MyAdapterForNotification.ViewHolderList>{

    private ArrayList<DataSnapshot> dataSnapshots;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String uid = mAuth.getUid();
    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    View v;

    public MyAdapterForNotification(ArrayList<DataSnapshot> dataSnapshots)
    {
        this.dataSnapshots = dataSnapshots;
    }

    @NonNull
    @Override
    public ViewHolderList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_single_list,parent,false);
        return new ViewHolderList(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolderList holder, int tempPosition) {
        final int position = dataSnapshots.size() - tempPosition - 1;
        final String com = dataSnapshots.get(position).getKey();
        holder.notification_message = holder.itemView.findViewById(R.id.notification_message);
        holder.notification_profile = holder.itemView.findViewById(R.id.notification_profile);
        holder.constraint_notification = holder.itemView.findViewById(R.id.constraint_notification);
        holder.time_notification = holder.itemView.findViewById(R.id.time_notification);

        assert com != null;
        String hisId = dataSnapshots.get(position).child("from").getValue().toString();

        Long time = System.currentTimeMillis();

        if((dataSnapshots.get(position).child("timestamp").exists()))
        {
            holder.time_notification.setText(calculate_time(time - (Long)dataSnapshots.get(position).child("timestamp").getValue()));
        }

        mRef.child("users").child(hisId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshots.get(position).child("type").getValue().toString().equals("1"))
                {
                    holder.notification_message.setText(dataSnapshot.child("name").getValue().toString() + " liked your photos.");
                }

                if(dataSnapshots.get(position).child("type").getValue().toString().trim().equals("2"))
                {
                    holder.notification_message.setText(dataSnapshot.child("name").getValue().toString() + " has commented on your photos.");
                }

                if(dataSnapshot.child("profilePic").exists())
                {
                    StorageReference storageReference = mStorageRef.child("ProfileImages").child(dataSnapshot.child("profilePic").getValue().toString());
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri.toString()).into(holder.notification_profile);
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

        if(dataSnapshots.get(position).child("status").getValue().toString().equals("0"))
        {
            holder.constraint_notification.setBackgroundResource(R.drawable.liked_blue);
            holder.notification_message.setTextColor(Color.parseColor("#03A9F4"));
        }



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRef.child("Notification").child(uid).child(com).child("status").setValue(1);
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment fragment = new ViewSinglePost(dataSnapshots.get(position).child("post_id").getValue().toString(),uid);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("ViewSinglePost").commit();
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataSnapshots.size();
    }

    public class ViewHolderList extends RecyclerView.ViewHolder {
        TextView notification_message,time_notification;
        String name,post;
        ConstraintLayout constraint_notification;
        ImageView notification_profile;


        public ViewHolderList(@NonNull View itemView) {
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
