package amit.example.lilichatians;

import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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

import amit.example.lilichatians.ui.share.ShareFragment;

public class MyAdapterForHomePost extends RecyclerView.Adapter<MyAdapterForHomePost.ViewHolder> {
    private ArrayList<DataSnapshot> dataSnapshots;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mRef = firebaseDatabase.getReference();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String uid = mAuth.getUid();
    View v;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("PostImages");
    private StorageReference mStorageRe = FirebaseStorage.getInstance().getReference();

    public MyAdapterForHomePost(ArrayList<DataSnapshot> dataSnapshots)
    {
        this.dataSnapshots = dataSnapshots;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_home_single,parent,false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int positionn) {
        final int position = dataSnapshots.size() - positionn -1;
        holder.post_content_toshow = holder.itemView.findViewById(R.id.post_content_toshow);
        holder.post_username = holder.itemView.findViewById(R.id.post_username);
        holder.post_content_toshow.setVisibility(View.GONE);
        holder.like = holder.itemView.findViewById(R.id.like);
        holder.comment = holder.itemView.findViewById(R.id.comment);
        holder.delete = holder.itemView.findViewById(R.id.delete);
        holder.share = holder.itemView.findViewById(R.id.share);
        holder.comment_body = holder.itemView.findViewById(R.id.comment_body);
        holder.comment_body.setVisibility(View.GONE);
        holder.submit_comment_button = holder.itemView.findViewById(R.id.submit_comment_button);
        holder.submit_comment_button.setVisibility(View.GONE);
        holder.post_image = holder.itemView.findViewById(R.id.post_image);
        holder.post_image.setVisibility(View.GONE);
        holder.delete.setVisibility(View.GONE);
        holder.itemView.setTag(dataSnapshots.get(position));
        holder.count_like = holder.itemView.findViewById(R.id.count_like);
        holder.timepost = holder.itemView.findViewById(R.id.time_post);
        holder.userImage = holder.itemView.findViewById(R.id.userImage);
        holder.last_comment = holder.itemView.findViewById(R.id.last_comment);
        holder.count_comment = holder.itemView.findViewById(R.id.count_comment);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(holder.last_comment.getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        holder.last_comment.setLayoutManager(linearLayoutManager);

        final String value = dataSnapshots.get(position).getValue().toString().trim();
        final String key = dataSnapshots.get(position).getKey().toString();

        String combine_id;
        if(value.compareTo(uid)>0)
            combine_id = value+uid;
        else
            combine_id = uid+value;

        //for my post.
        if(value.equals(uid))
        {
            mRef.child("posts").child(value).child(key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot data) {
                    if(data.child("content").exists()) {
                        holder.post_content_toshow.setVisibility(View.VISIBLE);
                        holder.post_content_toshow.setText(data.child("content").getValue().toString().trim());
                    }
                    if(data.child("timestamp").exists())
                    {
                        long timeMillis = System.currentTimeMillis();
                        long currentTime = (Long) data.child("timestamp").getValue();
                        String diffTime = calculate_time(timeMillis - currentTime) + "";
                        holder.timepost.setText(diffTime);
                    }
                    if(data.child("photo").exists())
                    {
                        StorageReference storageReference = mStorageRef.child(data.child("photo").getValue().toString());
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri.toString()).into(holder.post_image);
                                holder.post_image.setVisibility(View.VISIBLE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(v,e+"",Snackbar.LENGTH_LONG).show();
                            }
                        });
                        holder.post_image.setVisibility(View.VISIBLE);
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        //check if its friend post..
        mRef.child("friends").child(combine_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    mRef.child("posts").child(value).child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot data) {
                            if(data.child("content").exists()) {
                                holder.post_content_toshow.setVisibility(View.VISIBLE);
                                holder.post_content_toshow.setText(data.child("content").getValue().toString().trim());
                            }

                            if(data.child("photo").exists())
                            {
                                StorageReference storageReference = mStorageRef.child(data.child("photo").getValue().toString());
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Picasso.get().load(uri.toString()).into(holder.post_image);
                                        holder.post_image.setVisibility(View.VISIBLE);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(v,e+"",Snackbar.LENGTH_LONG).show();
                                    }
                                });
                                holder.post_image.setVisibility(View.VISIBLE);
                                holder.share.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AppCompatActivity activity = (AppCompatActivity) view.getContext();
                                        Fragment fragment = new ShareFragment(data.child("photo").getValue().toString(),"",1);
                                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("ToImage").commit();

                                    }
                                });

                            }
                            if(data.child("timestamp").exists())
                            {
                                long timeMillis = System.currentTimeMillis();
                                long currentTime = (Long) data.child("timestamp").getValue();
                                String diffTime = calculate_time(timeMillis - currentTime) + "";
                                holder.timepost.setText(diffTime);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRef.child("likes").child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    holder.count_like.setText(dataSnapshot.getChildrenCount()+" people liked this..");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRef.child("users").child(value).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.post_username.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRef.child("users").child(value).child("profilePic").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    StorageReference storageReference = mStorageRe.child("ProfileImages").child(dataSnapshot.getValue().toString());
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri.toString()).into(holder.userImage);
                            holder.post_image.setVisibility(View.VISIBLE);
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

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.comment_body.setVisibility(View.VISIBLE);
                holder.submit_comment_button.setVisibility(View.VISIBLE);
            }
        });

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRef.child("likes").child(key).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long timeMillis = System.currentTimeMillis();
                        if(dataSnapshot.exists())
                        {
                            mRef.child("likes").child(key).child(uid).removeValue();
                        }else{
                            mRef.child("likes").child(key).child(uid).setValue("1 ");
                            if(!value.equals(uid)) {
                                mRef.child("Notification").child(value).child(key+uid).child("status").setValue("0");
                                mRef.child("Notification").child(value).child(key+uid).child("type").setValue("1"); //1 for like
                                mRef.child("Notification").child(value).child(key+uid).child("post_id").setValue(key);
                                mRef.child("Notification").child(value).child(key+uid).child("from").setValue(uid);
                                mRef.child("Notification").child(value).child(key+uid).child("timestamp").setValue(timeMillis);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        mRef.child("likes").child(key).child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    holder.like.setText("Liked");
                    holder.like.setBackgroundResource(R.drawable.liked_blue);
                    holder.like.setTextColor(Color.parseColor("#3F51B5"));
                }
                else {
                    holder.like.setText("Like");
                    holder.like.setBackgroundResource(R.drawable.blue_button);
                    holder.like.setTextColor(Color.parseColor("#ffffff"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Upload comment to fireBase
        holder.submit_comment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = holder.comment_body.getText().toString();
                if(comment.length()>0) {
                    long timeMillis = System.currentTimeMillis();
                    String some_key = mRef.child("comments").child(key).push().getKey();
                    mRef.child("comments").child(key).child(some_key).child("timestamp").setValue(timeMillis);
                    mRef.child("comments").child(key).child(some_key).child("content").setValue(comment);
                    mRef.child("comments").child(key).child(some_key).child("from").setValue(uid);
                    holder.comment_body.setText("");
                    Toast.makeText(v.getContext(),"Commented", Toast.LENGTH_LONG).show();
                    if(!value.equals(uid)) {
                        mRef.child("Notification").child(value).child(some_key).child("status").setValue("0");
                        mRef.child("Notification").child(value).child(some_key).child("type").setValue("2"); //2 for comment
                        mRef.child("Notification").child(value).child(some_key).child("post_id").setValue(key);
                        mRef.child("Notification").child(value).child(some_key).child("from").setValue(uid);
                        mRef.child("Notification").child(value).child(some_key).child("timestamp").setValue(timeMillis);
                    }
                }
            }
        });
        mRef.child("comments").child(key).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<DataSnapshot> arrayList = new ArrayList<>();
                arrayList.clear();
                for(DataSnapshot temp : dataSnapshot.getChildren())
                {
                    if(temp.hasChild("from")) {
                        arrayList.add(temp);
                    }
                }
                if(dataSnapshot.getChildrenCount()>0) {
                    MyAdapterForComments myAdapterForComments = new MyAdapterForComments(arrayList,key);
                    holder.last_comment.setAdapter(myAdapterForComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //count comment
        mRef.child("comments").child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()>0) {
                    holder.count_comment.setText(dataSnapshot.getChildrenCount()+" comments");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //VIewPost
        if(uid.equals(value)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppCompatActivity activity = (AppCompatActivity) view.getContext();
                    Fragment fragment = new ViewSinglePost(key,uid);
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("ViewPost").commit();
                }
            });
        }
        else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppCompatActivity activity = (AppCompatActivity) view.getContext();
                    Fragment fragment = new ViewSinglePost(key,value);
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("ViewPost").commit();
                }
            });
        }

    }


    private String calculate_time(long time)
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



    @Override
    public int getItemCount() {
        return dataSnapshots.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView timepost;
        TextView post_username,post_content_toshow,count_like,count_comment;
        RecyclerView last_comment;
        ImageView post_image,delete,userImage;
        Button like,comment,share,submit_comment_button;
        EditText comment_body;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
