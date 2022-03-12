package amit.example.lilichatians.ui.share;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import amit.example.lilichatians.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import static java.lang.Math.random;

public class ShareFragment extends Fragment {

    private View v;
    private String u_pid;
    private String image_id;
    private Button share_now;
    private int type;
    private TextView share_content;
    private ImageView share_image;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String uid = mAuth.getUid();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef = firebaseStorage.getReference();
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Uri uri;
    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();

    public ShareFragment( String image_id,String u_pid,int type)
    {
        this.image_id = image_id;
        this.u_pid = u_pid;
        this.type = type;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
       v =  inflater.inflate(R.layout.share_fragment, container, false);
       return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        share_now = v.findViewById(R.id.share_now_button);
        share_image = v.findViewById(R.id.image_share);
        radioGroup = v.findViewById(R.id.radioGroup);
        share_content = v.findViewById(R.id.content_share);

        if(type == 1) {
            StorageReference storageReference = mStorageRef.child("PostImages").child(image_id);
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri.toString()).into(share_image);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Snackbar.make(v, e + "", Snackbar.LENGTH_LONG).show();
                }
            });
        }
        if(type == 2){
            StorageReference storageReference = mStorageRef.child("ProfileImages").child(image_id);
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri.toString()).into(share_image);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Snackbar.make(v, e + "", Snackbar.LENGTH_LONG).show();
                }
            });
        }
        if(type == 3){
            StorageReference storageReference = mStorageRef.child("CoverImages").child(image_id);
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri.toString()).into(share_image);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Snackbar.make(v, e + "", Snackbar.LENGTH_LONG).show();
                }
            });
        }
        if(type == 4){
            StorageReference storageReference = mStorageRef.child("Messages").child(image_id);
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri.toString()).into(share_image);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Snackbar.make(v, e + "", Snackbar.LENGTH_LONG).show();
                }
            });
        }


        share_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selected = radioGroup.getCheckedRadioButtonId();

                Bitmap bitmap = ((BitmapDrawable)share_image.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,50, baos);
                byte[] data = baos.toByteArray();
                final String newPush;
                StorageReference storageReference;
                UploadTask uploadTask;
                /*

                 */

                switch (selected)
                {
                    case R.id.timelineRadioButton:
                        Snackbar.make(v,"Uploading Post ...",Snackbar.LENGTH_INDEFINITE).show();

                        final String push = mRef.child("posts").child(uid).push().getKey();
                        newPush = random() + push;
                        storageReference = mStorageRef.child("PostImages/" + newPush);

                        uploadTask = storageReference.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                mRef.child("posts").child(uid).child(push).child("photo").setValue(newPush);
                                mRef.child("NewsFeed").child(push).setValue(uid);
                                long timeMillis = System.currentTimeMillis();
                                mRef.child("posts").child(uid).child(push).child("timestamp").setValue(timeMillis);
                                if (share_content.length() > 0) {
                                    mRef.child("posts").child(uid).child(push).child("content").setValue(share_content.getText().toString());
                                }
                                Snackbar.make(v, "This file has been Shared to your timeline...", Snackbar.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case R.id.profilePicRadioButton:

                        Snackbar.make(v,"Profile Photo Changing...",Snackbar.LENGTH_LONG).show();
                        newPush =random()+uid;
                        storageReference = mStorageRef.child("ProfileImages/"+newPush);

                        uploadTask = storageReference.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(v,"Failed to Changed Profile Picture.",Snackbar.LENGTH_LONG).show();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                mRef.child("users").child(uid).child("profilePic").setValue(newPush);
                                Snackbar.make(v,"Profile Photo Changed..",Snackbar.LENGTH_LONG).show();
                            }
                        });

                        break;
                    case R.id.coverPicRadioButton:
                        Snackbar.make(v,"Cover Photo Changing...",Snackbar.LENGTH_LONG).show();
                        newPush =random()+uid;
                        storageReference = mStorageRef.child("CoverImages/"+newPush);

                        uploadTask = storageReference.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(v,"Failed to Changed Cover Picture.",Snackbar.LENGTH_LONG).show();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                mRef.child("users").child(uid).child("coverPic").setValue(newPush);
                                Snackbar.make(v,"Cover Photo Changed..",Snackbar.LENGTH_LONG).show();
                            }
                        });
                        break;
                    default:

                }
            }
        });

    }

}
