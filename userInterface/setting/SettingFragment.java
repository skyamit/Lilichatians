package amit.example.lilichatians.ui.setting;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import amit.example.lilichatians.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;
import static java.lang.Math.random;

public class SettingFragment extends Fragment {

    private EditText change_name_text,change_bio_text,new_password,current_password,Reason_to_deactivate;
    private CheckBox show_me,hide_me;
    private TextView location_message;
    private Button change_location_button,change_name_button,change_bio_button,change_password_button,deactivate_account_button,selectImageCover,Change_cover,Change_profile,selectImageProfile;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String uid = mAuth.getUid();
    FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = firebaseDatabase.getReference();
    private int PICK_IMAGE_REQUEST1 = 71;
    private int PICK_IMAGE_REQUEST2 = 72;
    private Uri filePath1 = null;
    private String UriPath1 = null;
    private Uri filePath2 = null;
    private String UriPath2 = null;
    private StorageReference mStorageRef;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View v =  inflater.inflate(R.layout.setting_fragment, container, false);

        change_bio_button = v.findViewById(R.id.change_bio_button);
        change_name_button = v.findViewById(R.id.change_name_button);
        change_password_button = v.findViewById(R.id.change_password_button);
        deactivate_account_button = v.findViewById(R.id.deactivate_account_button);
        change_name_text = v.findViewById(R.id.change_name_text);
        change_bio_text = v.findViewById(R.id.change_bio_text);
        new_password = v.findViewById(R.id.new_password);
        current_password = v.findViewById(R.id.current_password);
        Reason_to_deactivate = v.findViewById(R.id.Reason_to_deactivate);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        selectImageCover = v.findViewById(R.id.selectImageCover);
        selectImageProfile = v.findViewById(R.id.selectImageProfile);
        Change_cover = v.findViewById(R.id.Change_cover);
        Change_profile = v.findViewById(R.id.Change_profile);
        change_location_button = v.findViewById(R.id.Change_location_button);
        show_me = v.findViewById(R.id.show_location);
        hide_me = v.findViewById(R.id.hide_location);
        location_message = v.findViewById(R.id.message_location);

        change_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!show_me.isChecked() && hide_me.isChecked())
                {
                    mRef.child("users").child(uid).child("location").setValue(0);
                    mRef.child("mapLocation").child(uid).removeValue();
                }
                else if(show_me.isChecked() && !hide_me.isChecked())
                {
                    mRef.child("users").child(uid).child("location").setValue(1);
                }
                else
                {
                    Toast.makeText(getContext(), "Need to check one item at time..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("location").exists())
                {
                    if(dataSnapshot.child("location").getValue().toString().equals("1")){
                        location_message.setText("You're currently visble to the world");
                        location_message.setTextColor(ContextCompat.getColor(location_message.getContext(),R.color.login_button));
                    }
                    else{
                        location_message.setText("You're currently hiddden from the world");
                        location_message.setTextColor(ContextCompat.getColor(location_message.getContext(),R.color.gray));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        change_bio_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(change_bio_text.getText().toString().trim().length()>0)
                {
                    mRef.child("users").child(uid).child("bio").setValue(change_bio_text.getText().toString().trim());
                    Snackbar.make(v,"Bio Changed Successfully...",Snackbar.LENGTH_LONG).show();
                    change_bio_text.setText("");
                }
                else
                {
                    Snackbar.make(v,"Error Occurred...",Snackbar.LENGTH_LONG).show();
                }
            }
        });

        change_name_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(change_name_text.getText().toString().trim().length()>0)
                {
                    mRef.child("users").child(uid).child("name").setValue(change_name_text.getText().toString().trim());
                    Snackbar.make(v,"Name Changed Successfully...",Snackbar.LENGTH_LONG).show();
                    change_name_text.setText("");
                }
                else
                {
                    Snackbar.make(v,"Error Occurred...",Snackbar.LENGTH_LONG).show();
                }
            }
        });

        change_password_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String current_passwordd = current_password.getText().toString().trim();
                if(new_password.getText().toString().trim().length()>5 && current_passwordd.length()>0)
                {
                    String email = user.getEmail();
                    AuthCredential authCredential = EmailAuthProvider.getCredential(email,current_passwordd);
                    user.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                user.updatePassword(new_password.getText().toString());
                                new_password.setText("");
                                current_password.setText("");
                                Snackbar.make(v,"Password Changed Successfully.",Snackbar.LENGTH_LONG).show();
                            }
                            else
                            {
                                Snackbar.make(v,"Wrong Password.",Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else
                {
                    Snackbar.make(v,"Minimum Password Length should be 5.",Snackbar.LENGTH_LONG).show();
                }
            }
        });

        deactivate_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Reason_to_deactivate.getText().toString().trim().length()>0)
                {
                    Snackbar.make(v,"We will fix your issue. Your Account will be Deactivated After 10days.",Snackbar.LENGTH_LONG).show();
                }
            }
        });

        selectImageCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Cover.."),PICK_IMAGE_REQUEST1);
            }
        });

        selectImageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Profile.."),PICK_IMAGE_REQUEST2);
            }
        });



        Change_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(v,"Profile Updating...",Snackbar.LENGTH_INDEFINITE).show();
                Bitmap bitmap = null;
                if(UriPath2.length()>0)
                {
                    final String newPush =random()+uid;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), filePath2);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,20,baos);
                        byte[] data = baos.toByteArray();
                        //set Reference
                        StorageReference storageReference = mStorageRef.child("ProfileImages/"+newPush);
                        UploadTask uploadTask = storageReference.putBytes(data);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        mRef.child("users").child(uid).child("profilePic").setValue(newPush);
                                        Snackbar.make(v,"Profile Photo Changed..",Snackbar.LENGTH_LONG).show();
                                        selectImageProfile.setText("Select Picture");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Snackbar.make(v,"Failed to Changed Profile Picture.",Snackbar.LENGTH_LONG).show();
                                    }
                                });
                        }catch (Exception e)
                        {
                            System.out.println(e);
                        }
                }
            }
        });

        Change_cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(v,"Cover Updating...",Snackbar.LENGTH_INDEFINITE).show();
                Bitmap bitmap = null;
                if(UriPath1.length()>0)
                {
                    final String newPush =random()+uid;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), filePath1);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
                        byte[] data = baos.toByteArray();
                        //set Reference
                        StorageReference storageReference = mStorageRef.child("CoverImages/"+newPush);
                        UploadTask uploadTask = storageReference.putBytes(data);

                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        mRef.child("users").child(uid).child("coverPic").setValue(newPush);
                                        Snackbar.make(v,"Cover Photo Changed..",Snackbar.LENGTH_LONG).show();
                                        selectImageProfile.setText("Select Picture");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Snackbar.make(v,"Failed to Changed Cover Picture.",Snackbar.LENGTH_LONG).show();
                                    }
                                });
                    }catch (Exception e)
                    {
                        System.out.println(e);
                    }
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST1 && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            filePath1 = data.getData();
            UriPath1 = filePath1.getPath();
            selectImageCover.setText(UriPath1.substring(UriPath1.indexOf("/")+1));
        }
        if(requestCode == PICK_IMAGE_REQUEST2 && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            filePath2 = data.getData();
            UriPath2 = filePath2.getPath();
            selectImageProfile.setText(UriPath2.substring(UriPath2.indexOf("/")+1));
        }
    }

}
