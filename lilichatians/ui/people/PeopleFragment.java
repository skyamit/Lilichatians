package amit.example.lilichatians.ui.people;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import amit.example.lilichatians.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import amit.example.lilichatians.MyAdapterForFriend;
import amit.example.lilichatians.MyAdapterForFriendList;

public class PeopleFragment extends Fragment {
    private ArrayList<DataSnapshot> userData = new ArrayList<>();
    private PeopleViewModel peopleViewModel;
    private Button search_button;
    private Button search_email;
    private EditText search_text;
    private TextView message;
    private int FriendFound =0;
    private RecyclerView recyclerView;
    private ProgressBar progressBar3;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        peopleViewModel =
                ViewModelProviders.of(this).get(PeopleViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_people, container, false);

        search_button = root.findViewById(R.id.search_button);
        search_email = root.findViewById(R.id.search_email);
        search_text = root.findViewById(R.id.search_text);
        progressBar3 = root.findViewById(R.id.progressBar3);
        progressBar3.setAlpha(0);
        message = root.findViewById(R.id.message);
        message.setAlpha(0);

        //Working with recycler View
        recyclerView = root.findViewById(R.id.friend_list_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        get_friends();
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_name();
            }
        });

        search_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_email();
            }
        });

        peopleViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        return root;
    }
    private void search_name()
    {
        message.setAlpha(0);
        String search_string = search_text.getText().toString().trim();
        if(search_string.length()>0)
        {
            progressBar3.setAlpha(1);
            Query query = myRef.child("users").orderByChild("name").startAt(search_string).endAt(search_string+"\uf8ff");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    message.setAlpha(0);
                    if (dataSnapshot.exists()) {
                        userData.clear();
                        for (DataSnapshot issue : dataSnapshot.getChildren()) {
                            userData.add(issue);
                        }
                        MyAdapterForFriend myAdapterForFriend = new MyAdapterForFriend(PeopleFragment.this, userData);
                        recyclerView.setAdapter(myAdapterForFriend);
                    }
                    else{
                        userData.clear();
                        MyAdapterForFriend myAdapterForFriend = new MyAdapterForFriend(PeopleFragment.this,userData);
                        recyclerView.setAdapter(myAdapterForFriend);
                        message.setAlpha(1);
                        message.setText("No Data Found");
                    }
                    progressBar3.setVisibility(View.GONE);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    progressBar3.setVisibility(View.GONE);
                    message.setAlpha(1);
                    message.setText("Error Occurred");
                }
            });
         }
    }

    public  void search_email()
    {
        message.setAlpha(0);
        String search_string = search_text.getText().toString().trim();
        if(search_string.length()>0)
        {
            progressBar3.setAlpha(1);
            Query query = myRef.child("users").orderByChild("email").startAt(search_string).endAt(search_string+"\uf8ff");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    message.setAlpha(0);
                    if (dataSnapshot.exists()) {
                        userData.clear();
                        for (DataSnapshot issue : dataSnapshot.getChildren()) {
                            userData.add(issue);
                        }
                        MyAdapterForFriend myAdapterForFriend = new MyAdapterForFriend(PeopleFragment.this, userData);
                        recyclerView.setAdapter(myAdapterForFriend);
                    }
                    else{
                        userData.clear();
                        MyAdapterForFriend myAdapterForFriend = new MyAdapterForFriend(PeopleFragment.this,userData);
                        recyclerView.setAdapter(myAdapterForFriend);
                        message.setAlpha(1);
                        message.setText("No Data Found");
                    }
                    progressBar3.setVisibility(View.GONE);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    progressBar3.setVisibility(View.GONE);
                    message.setAlpha(1);
                    message.setText("Error Occurred");
                }
            });
        }
    }


    public void get_friends() {
        userData.clear();
        progressBar3.setAlpha(1);

        String uid = mAuth.getUid();

        Query query = myRef.child("friends").orderByChild("senderId").equalTo(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!= null) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if (issue.child("status").getValue().toString().equals("1")) {
                            FriendFound++;
                            userData.add(issue);
                        }
                    }
                    MyAdapterForFriendList myAdapterForFriendList = new MyAdapterForFriendList(PeopleFragment.this, userData);
                    recyclerView.setAdapter(myAdapterForFriendList);
                }
                else
                {
                    FriendFound =0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        Query query2 = myRef.child("friends").orderByChild("recieverId").equalTo(uid);
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!= null) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if (issue.child("status").getValue().toString().equals("1"))
                        {
                            userData.add(issue);
                        }
                    }
                    progressBar3.setVisibility(View.GONE);
                    MyAdapterForFriendList myAdapterForFriendList = new MyAdapterForFriendList(PeopleFragment.this, userData);
                    recyclerView.setAdapter(myAdapterForFriendList);
                }
                else {
                    progressBar3.setVisibility(View.GONE);
                    if(FriendFound==0)
                    {
                        message.setAlpha(1);
                        message.setText("No Friend Found");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

}