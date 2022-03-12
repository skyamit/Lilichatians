package amit.example.lilichatians.ui.home;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import amit.example.lilichatians.R;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import amit.example.lilichatians.MyAdapterForHomePost;

import static android.app.Activity.RESULT_OK;
import static java.lang.Math.random;

public class HomeFragment extends Fragment{
    private TextView completeProfile;
    private EditText setName;
    private Button submitName;
    private static TextView setDob;
    private Button submitDob;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button submitGender;
    private TextView selectGender;
    private ProgressBar progressBar;
    private Button upload_post,select_photo;
    private EditText post_data;
    private ArrayList<DataSnapshot> dataSnapshots = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar progress_bar_forUploadPost;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("users");
    private DatabaseReference mRef = database.getReference();
    private String uid = mAuth.getUid();
    private int PICK_IMAGE_REQUEST = 71;
    private Uri filePath = null;
    private String UriPath = null;
    private NestedScrollView scrollViewHome;

    //to upload photo
    private View root;
    private StorageReference mStorageRef;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_home, container, false);

        progressBar = root.findViewById(R.id.profile_completed);
        setName = root.findViewById(R.id.setName);
        submitName = root.findViewById(R.id.submitName);
        setName.setVisibility(View.GONE);
        submitName.setVisibility(View.GONE);
        setDob = root.findViewById(R.id.setDob);
        submitDob = root.findViewById(R.id.submitDob);
        setDob.setVisibility(View.GONE);
        submitDob.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        radioGroup = root.findViewById(R.id.gender);
        radioGroup.setVisibility(View.GONE);
        submitGender = root.findViewById(R.id.submit_gender);
        submitGender.setVisibility(View.GONE);
        selectGender = root.findViewById(R.id.select_gender);
        selectGender.setVisibility(View.GONE);
        completeProfile = root.findViewById(R.id.completeProfile);
        completeProfile.setVisibility(View.GONE);
        scrollViewHome = root.findViewById(R.id.scrollViewHome);
        scrollViewHome.setVisibility(View.GONE);
        recyclerView = root.findViewById(R.id.home_post_RecyclerView);

        post_data = root.findViewById(R.id.post_data);
        upload_post = root.findViewById(R.id.upload_post);
        select_photo = root.findViewById(R.id.select_photo);
        progress_bar_forUploadPost = root.findViewById(R.id.progress_bar_forUploadPost);
        progress_bar_forUploadPost.setVisibility(View.VISIBLE);



        mStorageRef = FirebaseStorage.getInstance().getReference();

        //call if user already exists.
        myRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child("name").exists()) {
                    progress_bar_forUploadPost.setVisibility(View.GONE);
                    completeProfile.setVisibility(View.VISIBLE);
                    setName_function();
                } else {
                    if (!dataSnapshot.child("dob").exists()) {
                        setDateOfBirth();
                    } else {
                        if (!dataSnapshot.child("sex").exists()) {
                            setSex();
                        } else {
                            scrollViewHome.setVisibility(View.VISIBLE);
                            progress_bar_forUploadPost.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(root, "Database Error Occured!! Please Check Internet Connection..", Snackbar.LENGTH_LONG).show();
            }
        });

        //submit the name on button clicked
        submitName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = setName.getText().toString().trim();
                if (name.length() > 1) {
                    myRef.child(uid).child("name").setValue(name);
                }
            }
        });

        //submit the dob on button clicked
        submitDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dob = setDob.getText().toString().trim();
                if (dob.length() > 1) {
                    myRef.child(uid).child("dob").setValue(dob);
                }
            }
        });

        //calls date picker on setDob click
        setDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new SelectDateFragment();
                newFragment.show(getFragmentManager(), "DatePicker");
            }
        });

        //submit the gender on button click
        submitGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selected = radioGroup.getCheckedRadioButtonId();
                radioButton = root.findViewById(selected);

                if (radioButton.getText().toString().trim().length() > 1) {
                    myRef.child(uid).child("sex").setValue(radioButton.getText().toString().trim());

                    completeProfile.setVisibility(View.GONE);
                    radioGroup.setVisibility(View.GONE);
                    submitGender.setVisibility(View.GONE);
                    selectGender.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    scrollViewHome.setVisibility(View.VISIBLE);
                }
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(root.getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerView.setLayoutManager(linearLayoutManager);

        select_photo();

        //helps to upload posts.
        upload_post();
        get_post();

        return root;
    }

    public void setName_function() {
        scrollViewHome.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        setDob.setVisibility(View.GONE);
        submitDob.setVisibility(View.GONE);
        setName.setVisibility(View.VISIBLE);
        submitName.setVisibility(View.VISIBLE);
        radioGroup.setVisibility(View.GONE);
        submitGender.setVisibility(View.GONE);
        selectGender.setVisibility(View.GONE);
    }

    public void setDateOfBirth() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(33);
        setName.setVisibility(View.GONE);
        submitName.setVisibility(View.GONE);
        setDob.setVisibility(View.VISIBLE);
        submitDob.setVisibility(View.VISIBLE);
        radioGroup.setVisibility(View.GONE);
        submitGender.setVisibility(View.GONE);
        selectGender.setVisibility(View.GONE);
    }

    public void setSex() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(66);
        setName.setVisibility(View.GONE);
        submitName.setVisibility(View.GONE);
        setDob.setVisibility(View.GONE);
        submitDob.setVisibility(View.GONE);
        radioGroup.setVisibility(View.VISIBLE);
        submitGender.setVisibility(View.VISIBLE);
        selectGender.setVisibility(View.VISIBLE);
    }

    //function to call datepicker
    public static class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, yy, mm, dd);
        }

        public void onDateSet(DatePicker view, int yy, int mm, int dd) {
            populateSetDate(yy, mm + 1, dd);
        }

        public void populateSetDate(int year, int month, int day) {
            setDob.setText(month + "/" + day + "/" + year);
        }

    }

    public void upload_post() {
        upload_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String push;
                Bitmap bitmap = null;
                if(UriPath!=null)
                {
                    Snackbar.make(root,"Uploading post.",Snackbar.LENGTH_INDEFINITE).show();

                    if(UriPath.length()>0) {
                        push = mRef.child("posts").child(uid).push().getKey();
                        final String newPush = random() + push;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), filePath);
                        }catch (Exception e)
                        {

                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,30,baos);
                        byte[] data = baos.toByteArray();
                        //set Reference
                        StorageReference storageReference = mStorageRef.child("PostImages/" + newPush);
                        UploadTask uploadTask = storageReference.putBytes(data);
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        mRef.child("posts").child(uid).child(push).child("photo").setValue(newPush);
                                        mRef.child("NewsFeed").child(push).setValue(uid);
                                        long timeMillis = System.currentTimeMillis();
                                        mRef.child("posts").child(uid).child(push).child("timestamp").setValue(timeMillis);
                                        if (post_data.length() > 0) {
                                            mRef.child("posts").child(uid).child(push).child("content").setValue(post_data.getText().toString());
                                            post_data.setText("");
                                        }
                                        Snackbar.make(root, "Post Uploaded.", Snackbar.LENGTH_LONG).show();
                                        select_photo.setText("Select Photo");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Snackbar.make(root, "Post Failed to Upload.", Snackbar.LENGTH_LONG).show();
                                    }
                                });
                    }
                }
                else
                {
                    if (post_data.length()>0) {
                        Snackbar.make(root,"Uploading post.",Snackbar.LENGTH_LONG).show();

                        push = mRef.child("posts").child(uid).push().getKey();
                        Snackbar.make(root,"Post Uploaded.",Snackbar.LENGTH_LONG).show();
                        mRef.child("posts").child(uid).child(push).child("content").setValue(post_data.getText().toString());

                        mRef.child("NewsFeed").child(push).setValue(uid);

                        long timeMillis = System.currentTimeMillis();
                        mRef.child("posts").child(uid).child(push).child("timestamp").setValue(timeMillis);
                        post_data.setText("");
                    }
                }
            }
        });
    }

    public void select_photo()
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


    public void get_post()
    {
        progress_bar_forUploadPost.setVisibility(View.VISIBLE);

        mRef.child("NewsFeed").orderByKey().limitToLast(20).addListenerForSingleValueEvent(new ValueEventListener() {

            int count =0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshots.clear();
                    for (final DataSnapshot issue : dataSnapshot.getChildren()) {
                        dataSnapshots.add(issue);
                    }
                    MyAdapterForHomePost myAdapterForHomePost = new MyAdapterForHomePost(dataSnapshots);
                    recyclerView.setAdapter(myAdapterForHomePost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(root,databaseError.toString(),Snackbar.LENGTH_LONG).show();
            }
        });
    }

}