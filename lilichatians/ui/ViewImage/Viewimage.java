package amit.example.lilichatians.ui.ViewImage;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import amit.example.lilichatians.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import amit.example.lilichatians.ui.share.ShareFragment;

public class Viewimage extends Fragment {

    private String image_id;
    private int type;
    private View v;
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef = firebaseStorage.getReference();
    private ImageView ViewImage;
    private ImageView shareImage;
    private String u_pid;

    public Viewimage(String image_id,int type,String u_pid)
    {
        this.image_id = image_id;
        /*
        1 for postImage
        2 for ProfileImage
        3 for CoverImage
        4 for MessageImages
        */
        this.type = type;
        this.u_pid = u_pid;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.viewimage_fragment, container, false);

        return  v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewImage = v.findViewById(R.id.ViewImage);
        shareImage = v.findViewById(R.id.share_image);

        if(type == 1) {
            StorageReference storageReference = mStorageRef.child("PostImages").child(image_id);
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri.toString()).into(ViewImage);
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
                    Picasso.get().load(uri.toString()).into(ViewImage);
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
                    Picasso.get().load(uri.toString()).into(ViewImage);
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
                    Picasso.get().load(uri.toString()).into(ViewImage);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Snackbar.make(v, e + "", Snackbar.LENGTH_LONG).show();
                }
            });
        }

        shareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                Fragment fragment = new ShareFragment(image_id,u_pid,type );
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("shareImage").commit();
            }
        });
    }

}
