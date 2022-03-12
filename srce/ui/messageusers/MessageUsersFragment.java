package amit.example.lilichatians.ui.messageusers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.fragment.app.Fragment;
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

import java.util.ArrayList;

import amit.example.lilichatians.MessageAdapter;

import static android.app.Activity.RESULT_OK;

public class MessageUsersFragment extends Fragment {

    private static String current_uid;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users");
    private DatabaseReference mRefMessage = FirebaseDatabase.getInstance().getReference("message");
    private DatabaseReference mRefFriends = FirebaseDatabase.getInstance().getReference("friends");
    private String uid = mAuth.getUid();
    private TextView FriendMessageName;
    private EditText message_content;
    private Button send_message;
    private RecyclerView Message_recycler;
    private ImageView sendImage;
    private ArrayList<DataSnapshot> arrayList = new ArrayList<>();
    private TextView typing;
    private ImageView message_user_image;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private int PICK_IMAGE_REQUEST = 71;
    private Uri filePath = null;
    private String UriPath = null;

    public MessageUsersFragment( String current_uidd) {
        current_uid = current_uidd;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View v =  inflater.inflate(R.layout.message_users_fragment, container, false);

        Message_recycler = v.findViewById(R.id.Message_recycler);
        FriendMessageName = v.findViewById(R.id.Friend_Name);
        send_message = v.findViewById(R.id.send_message);
        message_content = v.findViewById(R.id.actual_message);
        message_user_image = v.findViewById(R.id.message_user_image);
        typing = v.findViewById(R.id.typing);
        typing.setVisibility(View.GONE);
        sendImage = v.findViewById(R.id.sendImage);

        final String key = getKey(current_uid,uid);
        final Long time = System.currentTimeMillis();

        //getting the name of the friends and status
        mRef.child(current_uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("status").getValue().toString().equals("1"))
                    FriendMessageName.setText(dataSnapshot.child("name").getValue().toString().trim()+ " -- Online");
                else {
                    if(dataSnapshot.child("timestamp").exists())
                        FriendMessageName.setText(dataSnapshot.child("name").getValue().toString().trim() + " -- " + calculate_time(time - (Long) dataSnapshot.child("timestamp").getValue()));
                    else
                        FriendMessageName.setText(dataSnapshot.child("name").getValue().toString().trim()+ " -- Offline");
                }

                if (dataSnapshot.child("profilePic").exists()){
                    StorageReference storageReference = mStorageRef.child("ProfileImages").child(dataSnapshot.child("profilePic").getValue().toString());
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri.toString()).into(message_user_image);
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

        //getting message
        mRefMessage.child(key).child("messages").orderByKey().limitToLast(50).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    arrayList.clear();
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        arrayList.add(issue);
                    }
                    MessageAdapter messageAdapter = new MessageAdapter(arrayList);
                    Message_recycler.setAdapter(messageAdapter);
                }
                else{
                    arrayList.clear();
                    MessageAdapter messageAdapter = new MessageAdapter(arrayList);
                    Message_recycler.setAdapter(messageAdapter);
                }
                Message_recycler.scrollToPosition(arrayList.size()-1);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //typing or not
        mRefMessage.child(key).child(current_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    typing.setVisibility(View.VISIBLE);
                    if (dataSnapshot.getValue().toString().equals("")) {
                        typing.setText(dataSnapshot.getValue().toString().trim());
                    } else {
                        typing.setText(dataSnapshot.getValue().toString().trim());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //uploading message to server
        send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = message_content.getText().toString().trim();
                if(!message.equals("")) {
                    long timeMillis = System.currentTimeMillis();
                    String message_key = mRefMessage.child("message").child(key).child("messages").push().getKey();
                    mRefMessage.child(key).child("messages").child(message_key).child("content").setValue(message);
                    mRefMessage.child(key).child("messages").child(message_key).child("recieverId").setValue(current_uid);
                    mRefMessage.child(key).child("messages").child(message_key).child("senderId").setValue(uid);
                    mRefMessage.child(key).child("messages").child(message_key).child("status").setValue(0);
                    mRefMessage.child(key).child("messages").child(message_key).child("timestamp").setValue(timeMillis);
                    mRefMessage.child(key).child(uid).setValue("");

                    mRefFriends.child(key).child("lastMessage").child("senderId").setValue(uid);
                    mRefFriends.child(key).child("lastMessage").child("recieverId").setValue(current_uid);
                    mRefFriends.child(key).child("lastMessage").child("content").setValue(message);
                    mRefFriends.child(key).child("lastMessage").child("status").setValue(0);

                    message_content.setText("");
                }
            }
        });

        final Handler h = new Handler();
        h.postDelayed(new Runnable(){
            @Override
            public void run() {
                mRefMessage.child(key).child(uid).setValue("");
                h.postDelayed(this,1000);
            }
        },1000);

        message_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                mRefMessage.child(key).child(uid).setValue(" Typing.. ");
            }
        });

        select_photo();

        return v;
    }

    //Image MEssage
    public void select_photo()
    {
        sendImage.setOnClickListener(new View.OnClickListener() {
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
            final String key = getKey(current_uid,uid);
            if(UriPath!=null)
            {
                Snackbar.make(sendImage,"Sending Images...",Snackbar.LENGTH_LONG).show();
                if(UriPath.length()>0) {
                    final String message_key = mRefMessage.child("message").child(key).child("messages").push().getKey();

                    //set Reference
                    StorageReference storageReference = mStorageRef.child("Messages/"+key+"/"+message_key);

                    storageReference.putFile(filePath)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    long timeMillis = System.currentTimeMillis();
                                    mRefMessage.child(key).child("messages").child(message_key).child("images").setValue(message_key);
                                    mRefMessage.child(key).child("messages").child(message_key).child("recieverId").setValue(current_uid);
                                    mRefMessage.child(key).child("messages").child(message_key).child("senderId").setValue(uid);
                                    mRefMessage.child(key).child("messages").child(message_key).child("status").setValue(0);
                                    mRefMessage.child(key).child("messages").child(message_key).child("timestamp").setValue(timeMillis);

                                    mRefFriends.child(key).child("lastMessage").child("senderId").setValue(uid);
                                    mRefFriends.child(key).child("lastMessage").child("recieverId").setValue(current_uid);
                                    mRefFriends.child(key).child("lastMessage").child("content").setValue("Last Message is Image..");
                                    mRefFriends.child(key).child("lastMessage").child("status").setValue(0);
                                    Snackbar.make(sendImage, "Image Uploaded.", Snackbar.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(sendImage.getContext(), "Unable to send Images!! Please try again..", Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }

        }
    }


    public String getKey(String key1,String key2)
    {
        String combine_id;
        if(key1.compareTo(key2)>0)
        {
            combine_id = key1+key2;
        }
        else
        {
            combine_id = key2+key1;
        }
        return combine_id;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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


}
