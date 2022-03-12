package amit.example.lilichatians.ui.ViewProfile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import amit.example.lilichatians.AboutFragment;
import amit.example.lilichatians.MyAdapterForViewProfilePost;
import amit.example.lilichatians.ui.ViewImage.Viewimage;
import amit.example.lilichatians.ui.ViewPhotos.ViewPhotos;
import amit.example.lilichatians.ui.people.PeopleFragment;

import static android.app.Activity.RESULT_OK;
import static java.lang.Math.random;

public class ViewProfileFragment extends Fragment {

    private ImageView profilePhoto;
    private ImageView profileCover;
    private TextView ProfileName;
    private TextView ProfileBio;
    private Button select_photo;
    private Button upload_post;
    private EditText post_data;
    private ImageView collection_images;
    private Button about;
    private Button friends;
    private RecyclerView ViewProfile_recycler_view;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String uid = mAuth.getUid();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = firebaseDatabase.getReference();
    private View v;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private int PICK_IMAGE_REQUEST = 71;
    private Uri filePath = null;
    private String UriPath = null;
    private ArrayList<DataSnapshot> dataSnapshots = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        v =  inflater.inflate(R.layout.view_profile_fragment, container, false);

        profileCover = v.findViewById(R.id.coverProfile);
        profilePhoto = v.findViewById(R.id.profileImage);
        ProfileName = v.findViewById(R.id.profileName);
        ProfileBio = v.findViewById(R.id.profileBio);
        select_photo = v.findViewById(R.id.select_photo);
        upload_post = v.findViewById(R.id.upload_post);
        post_data = v.findViewById(R.id.post_data);
        ViewProfile_recycler_view = v.findViewById(R.id.ViewProfile_post_RecyclerView);
        collection_images = v.findViewById(R.id.collection_images);
        about = v.findViewById(R.id.about);
        friends = v.findViewById(R.id.friends);

        mRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                Fragment fragment = new Viewimage(dataSnapshot.child("coverPic").getValue().toString(),3,uid);
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
                                Fragment fragment = new Viewimage(dataSnapshot.child("profilePic").getValue().toString(),2,uid);
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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(v.getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        ViewProfile_recycler_view.setLayoutManager(linearLayoutManager);
        
        select_photo();
        upload_post();
        get_post();
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private void select_photo()
    {
        select_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select photo.."),PICK_IMAGE_REQUEST);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            filePath = data.getData();
            UriPath = filePath.getPath();
            select_photo.setText(UriPath.substring(UriPath.indexOf("/")+1));
        }
    }

    private void upload_post() {
        upload_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String push;
                Bitmap bitmap = null;
                if(UriPath!=null)
                {
                    Snackbar.make(v,"Uploading post.",Snackbar.LENGTH_LONG).show();
                    if(UriPath.length()>0) {
                        push = mRef.child("posts").child(uid).push().getKey();
                        final String newPush = random() + push;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), filePath);
                        }catch (Exception ignored)
                        {

                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
                        byte[] data = baos.toByteArray();
                        //set Reference
                        StorageReference storageReference = mStorageRef.child("PostImages/" + newPush);
                        UploadTask uploadTask = storageReference.putBytes(data);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        mRef.child("NewsFeed").child(push).setValue(uid);

                                        mRef.child("posts").child(uid).child(push).child("photo").setValue(newPush);
                                        long timeMillis = System.currentTimeMillis();
                                        mRef.child("posts").child(uid).child(push).child("timestamp").setValue(timeMillis);
                                        if (post_data.length() > 0) {
                                            mRef.child("posts").child(uid).child(push).child("content").setValue(post_data.getText().toString());
                                            post_data.setText("");
                                        }
                                        Snackbar.make(v, "Post Uploaded.", Snackbar.LENGTH_LONG).show();
                                        select_photo.setText("Select Photo");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Snackbar.make(v, "Post Failed to Upload.", Snackbar.LENGTH_LONG).show();
                                    }
                                });
                    }
                }
                else
                {
                    if (post_data.length()>0) {
                        Snackbar.make(v,"Uploading post.",Snackbar.LENGTH_INDEFINITE).show();

                        push = mRef.child("posts").child(uid).push().getKey();
                        Snackbar.make(v,"Post Uploaded.",Snackbar.LENGTH_LONG).show();
                        mRef.child("posts").child(uid).child(push).child("content").setValue(post_data.getText().toString());
                        long timeMillis = System.currentTimeMillis();
                        mRef.child("NewsFeed").child(push).setValue(uid);
                        mRef.child("posts").child(uid).child(push).child("timestamp").setValue(timeMillis);
                        post_data.setText("");
                    }
                }
            }
        });

        mRef.child("posts").child(uid).orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
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
                                    Picasso.get().load(uri.toString()).into(collection_images);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(v,e+"",Snackbar.LENGTH_LONG).show();
                                }
                            });
                            collection_images.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    AppCompatActivity activity = (AppCompatActivity) view.getContext();
                                    Fragment fragment = new ViewPhotos(uid);
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

        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment fragment = new PeopleFragment();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("Friends").commit();
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment fragment = new AboutFragment(uid);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("About").commit();

            }
        });

    }

    private void get_post()
    {
        mRef.child("posts").child(uid).orderByKey().limitToLast(20).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshots.clear();
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        dataSnapshots.add(issue);
                    }
                    MyAdapterForViewProfilePost myAdapterForViewProfilePost = new MyAdapterForViewProfilePost(dataSnapshots);
                    ViewProfile_recycler_view.setAdapter(myAdapterForViewProfilePost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(v,"Database Error Occured!! Please Check Internet Connection..",Snackbar.LENGTH_LONG).show();
            }
        });
    }

}
