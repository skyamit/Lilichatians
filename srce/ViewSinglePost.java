package amit.example.lilichatians;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import amit.example.lilichatians.ui.ViewImage.Viewimage;
import amit.example.lilichatians.ui.friendProfile.FriendProfile;
import amit.example.lilichatians.ui.share.ShareFragment;

public class ViewSinglePost extends Fragment {

    private String post_id,u_pid;
    private ImageView single_post_profile;
    private EditText comment_body;
    private TextView single_post_content,single_post_name,single_post_count_like,single_post_time_post,single_post_count_comment;
    private Button single_post_like,single_post_comment,single_post_share,submit_comment_button;
    View v;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String uid = mAuth.getUid();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mRef = firebaseDatabase.getReference();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef = firebaseStorage.getReference();
    private ImageView single_post_image;
    private RecyclerView comment_recyclerView;

    ViewSinglePost(String pid,String u_pid)
    {
        this.post_id = pid;
        this.u_pid = u_pid;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        v =  inflater.inflate(R.layout.view_single_post_fragment, container, false);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        single_post_comment = v.findViewById(R.id.single_post_comment);
        single_post_like = v.findViewById(R.id.single_post_like);
        single_post_content = v.findViewById(R.id.single_post_content);
        single_post_share = v.findViewById(R.id.single_post_share);
        single_post_name = v.findViewById(R.id.single_post_name);
        single_post_count_like = v.findViewById(R.id.single_post_count_like);
        single_post_time_post = v.findViewById(R.id.single_post_time_post);
        single_post_profile = v.findViewById(R.id.single_post_profile);
        single_post_image = v.findViewById(R.id.single_post_image);
        single_post_image.setVisibility(View.GONE);
        single_post_content.setVisibility(View.GONE);
        submit_comment_button = v.findViewById(R.id.submit_comment_button);
        comment_body = v.findViewById(R.id.comment_body);
        comment_recyclerView = v.findViewById(R.id.comments_recyclerView);
        single_post_count_comment = v.findViewById(R.id.single_post_count_comment);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(comment_recyclerView.getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        comment_recyclerView.setLayoutManager(linearLayoutManager);

        mRef.child("users").child(u_pid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.child("name").exists())
                    {
                        single_post_name.setText(dataSnapshot.child("name").getValue().toString());
                    }
                    if(dataSnapshot.child("profilePic").exists())
                    {
                        StorageReference storageReference = mStorageRef.child("ProfileImages").child(dataSnapshot.child("profilePic").getValue().toString());
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri.toString()).into(single_post_profile);
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

        single_post_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment fragment = new FriendProfile(u_pid);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("FriendProfile").commit();
            }
        });

        mRef.child("posts").child(u_pid).child(post_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("content").exists())
                {
                    single_post_content.setVisibility(View.VISIBLE);
                    single_post_content.setText(dataSnapshot.child("content").getValue().toString());
                }

                if(dataSnapshot.child("photo").exists())
                {
                    StorageReference storageReference = mStorageRef.child("PostImages").child(dataSnapshot.child("photo").getValue().toString());
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri.toString()).into(single_post_image);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(v,e+"",Snackbar.LENGTH_LONG).show();
                        }
                    });
                    single_post_image.setVisibility(View.VISIBLE);

                single_post_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AppCompatActivity activity = (AppCompatActivity) view.getContext();
                        Fragment fragment = new Viewimage(dataSnapshot.child("photo").getValue().toString(),1,u_pid);
                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("ToImage").commit();
                    }
                });
                single_post_share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AppCompatActivity activity = (AppCompatActivity) view.getContext();
                        Fragment fragment = new ShareFragment(dataSnapshot.child("photo").getValue().toString(),"",1);
                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("ToImage").commit();

                    }
                });
                }

                if(dataSnapshot.child("timestamp").exists())
                {
                    long current_time = System.currentTimeMillis();
                    String time = calculate_time(current_time - (Long)dataSnapshot.child("timestamp").getValue() );
                    single_post_time_post.setText(time);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(v,"Unable to find the data...",Snackbar.LENGTH_LONG);
            }


        });

        mRef.child("likes").child(post_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()>0)
                    single_post_count_like.setText(dataSnapshot.getChildrenCount()+" people liked this..");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        single_post_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRef.child("likes").child(post_id).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            mRef.child("likes").child(post_id).child(uid).removeValue();
                        }else{
                            mRef.child("likes").child(post_id).child(uid).setValue("1 ");
                            if(!u_pid.equals(uid)) {
                                mRef.child("Notification").child(u_pid).child(post_id+uid).child("status").setValue("0");
                                mRef.child("Notification").child(u_pid).child(post_id+uid).child("type").setValue("1"); //1 for like
                                mRef.child("Notification").child(u_pid).child(post_id+uid).child("post_id").setValue(post_id);
                                mRef.child("Notification").child(u_pid).child(post_id+uid).child("from").setValue(uid);
                                long timeMillis = System.currentTimeMillis();
                                mRef.child("Notification").child(u_pid).child(post_id+uid).child("timestamp").setValue(timeMillis);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        mRef.child("likes").child(post_id).child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    single_post_like.setText("Liked");
                    single_post_like.setBackgroundResource(R.drawable.liked_blue);
                    single_post_like.setTextColor(Color.parseColor("#3F51B5"));
                }
                else {
                    single_post_like.setText("Like");
                    single_post_like.setBackgroundResource(R.drawable.blue_button);
                    single_post_like.setTextColor(Color.parseColor("#ffffff"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        single_post_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                single_post_comment.setVisibility(View.VISIBLE);
            }
        });

        submit_comment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = comment_body.getText().toString();
                if(comment.length()>0) {
                    long timeMillis = System.currentTimeMillis();
                    String some_key = mRef.child("comments").child(post_id).push().getKey();
                    mRef.child("comments").child(post_id).child(some_key).child("timestamp").setValue(timeMillis);
                    mRef.child("comments").child(post_id).child(some_key).child("content").setValue(comment);
                    mRef.child("comments").child(post_id).child(some_key).child("from").setValue(uid);
                    comment_body.setText("");
                    Toast.makeText(v.getContext(),"Commented", Toast.LENGTH_LONG).show();
                    if(!u_pid.equals(uid)) {
                        mRef.child("Notification").child(u_pid).child(some_key).child("status").setValue("0");
                        mRef.child("Notification").child(u_pid).child(some_key).child("type").setValue("2"); //2 for comment
                        mRef.child("Notification").child(u_pid).child(some_key).child("post_id").setValue(post_id);
                        mRef.child("Notification").child(u_pid).child(some_key).child("from").setValue(uid);
                        mRef.child("Notification").child(u_pid).child(some_key).child("timestamp").setValue(timeMillis);
                    }
                }
            }
        });

        mRef.child("comments").child(post_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()>0) {
                    single_post_count_comment.setText(dataSnapshot.getChildrenCount()+" Comment");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mRef.child("comments").child(post_id).limitToLast(10).addValueEventListener(new ValueEventListener() {
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
                    MyAdapterForComments myAdapterForComments = new MyAdapterForComments(arrayList,post_id);
                    comment_recyclerView.setAdapter(myAdapterForComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



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
