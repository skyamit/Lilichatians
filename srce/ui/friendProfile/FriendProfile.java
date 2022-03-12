package amit.example.lilichatians.ui.friendProfile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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

import amit.example.lilichatians.AboutFragment;
import amit.example.lilichatians.MyAdapterForFriendProfilePost;
import amit.example.lilichatians.ui.ViewImage.Viewimage;
import amit.example.lilichatians.ui.ViewPhotos.ViewPhotos;

public class FriendProfile extends Fragment {

    private ImageView profilePhoto;
    private ImageView profileCover;
    private TextView ProfileName;
    private TextView ProfileBio;
    private ImageView collection_photo_friend;
    private Button about_friend,friend_friend;
    private RecyclerView friendViewProfile_recycler_view;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String uid = mAuth.getUid();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = firebaseDatabase.getReference();
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private ArrayList<DataSnapshot> dataSnapshots = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private View v;
    private String fuid;

    public FriendProfile(String uid)
    {
        this.fuid = uid;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.friend_profile_fragment, container, false);

            profileCover = v.findViewById(R.id.friendcoverProfile);
            profilePhoto = v.findViewById(R.id.friendprofileImage);
            ProfileName = v.findViewById(R.id.friendprofileName);
            ProfileBio = v.findViewById(R.id.friendprofileBio);
            collection_photo_friend = v.findViewById(R.id.collection_images_friends);
            about_friend = v.findViewById(R.id.about_friends);
            friend_friend = v.findViewById(R.id.friends_friends);

            friendViewProfile_recycler_view = v.findViewById(R.id.friendViewProfile_post_RecyclerView);

            mRef.child("users").child(fuid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {

                        if (dataSnapshot.child("name").exists())
                            ProfileName.setText(dataSnapshot.child("name").getValue().toString());

                        if (dataSnapshot.child("bio").exists())
                            ProfileBio.setText(dataSnapshot.child("bio").getValue().toString());

                        if (dataSnapshot.child("coverPic").exists()) {
                            StorageReference storageReference = mStorageRef.child("CoverImages").child(dataSnapshot.child("coverPic").getValue().toString());
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.get().load(uri.toString()).into(profileCover);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(v,e+"",Snackbar.LENGTH_LONG).show();
                                }
                            });
                            profileCover.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    AppCompatActivity activity = (AppCompatActivity) view.getContext();
                                    Fragment fragment = new Viewimage(dataSnapshot.child("coverPic").getValue().toString(),3,fuid);
                                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("ToImage").commit();
                                }
                            });
                        }

                        if (dataSnapshot.child("profilePic").exists()){
                            StorageReference storageReference = mStorageRef.child("ProfileImages").child(dataSnapshot.child("profilePic").getValue().toString());
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.get().load(uri.toString()).into(profilePhoto);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(v,e+"",Snackbar.LENGTH_LONG).show();
                                }
                            });
                            profilePhoto.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    AppCompatActivity activity = (AppCompatActivity) view.getContext();
                                    Fragment fragment = new Viewimage(dataSnapshot.child("profilePic").getValue().toString(),2,fuid);
                                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("ToImage").commit();
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            linearLayoutManager = new LinearLayoutManager(v.getContext()){
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };

            friendViewProfile_recycler_view.setLayoutManager(linearLayoutManager);

            mRef.child("posts").child(fuid).orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot issue : dataSnapshot.getChildren()) {
                            if(issue.child("photo").exists())
                            {
                                StorageReference storageReference = mStorageRef.child("PostImages").child(issue.child("photo").getValue().toString());
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Picasso.get().load(uri.toString()).into(collection_photo_friend);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(v,e+"",Snackbar.LENGTH_LONG).show();
                                    }
                                });
                                collection_photo_friend.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AppCompatActivity activity = (AppCompatActivity) view.getContext();
                                        Fragment fragment = new ViewPhotos(fuid);
                                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("Friends").commit();
                                    }
                                });
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Snackbar.make(v,"Database Error Occured!! Please Check Internet Connection..",Snackbar.LENGTH_LONG).show();
                }
            });

        friend_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"Sorry!! Not Allowed",Toast.LENGTH_LONG).show();
            }
        });
        about_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment fragment = new AboutFragment(fuid);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("AboutFriend").commit();

            }
        });



        get_post();

        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);



    }

    private void get_post()
    {
        mRef.child("posts").child(fuid).orderByKey().limitToLast(20).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshots.clear();
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        dataSnapshots.add(issue);
                    }
                    try {
                        MyAdapterForFriendProfilePost myAdapterForFriendProfilePost = new MyAdapterForFriendProfilePost(fuid, dataSnapshots);
                        friendViewProfile_recycler_view.setAdapter(myAdapterForFriendProfilePost);
                    }catch (Exception e ){

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(v,"Database Error Occured!! Please Check Internet Connection..",Snackbar.LENGTH_LONG).show();
            }
        });
    }

}
