package amit.example.lilichatians.ui.Message_List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import amit.example.lilichatians.MyAdapterForMessageList;

public class Message_List extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textView;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String uid = mAuth.getUid();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = firebaseDatabase.getReference();
    private ArrayList<DataSnapshot> userData = new ArrayList<>();
    private ArrayList<DataSnapshot> userData2 = new ArrayList<>();
    private ArrayList<DataSnapshot> userData3 = new ArrayList<>();


    public static Message_List newInstance() {
        return new Message_List();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.message_list_fragment, container, false);

        recyclerView = v.findViewById(R.id.message_list_fragment_recyclerView);
        progressBar = v.findViewById(R.id.progressBar_message_list);
        textView = v.findViewById(R.id.message_none);
        progressBar.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);

        //if change occurs
        mRef.child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                show_messages();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void show_messages()
    {
        userData.clear();
        progressBar.setVisibility(View.VISIBLE);

        Query query = mRef.child("friends").orderByChild("senderId").equalTo(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userData.clear();
                if (dataSnapshot.getValue()!= null) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if(issue.child("lastMessage").exists()) {
                            userData.add(issue);
                        }
                    }
                }
                if(userData.size()>0 || userData2.size()>0) {
                    userData3.clear();
                    userData3.addAll(userData);
                    userData3.addAll(userData2);
                    MyAdapterForMessageList myAdapterForMessageList = new MyAdapterForMessageList(userData3);
                    recyclerView.setAdapter(myAdapterForMessageList);
                }
                else
                {
                    textView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query query2 = mRef.child("friends").orderByChild("recieverId").equalTo(uid);
        query2.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userData2.clear();
                if (dataSnapshot.getValue()!= null) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if(issue.child("lastMessage").exists())
                            userData2.add(issue);

                        progressBar.setVisibility(View.GONE);
                    }
                }
                if(userData.size()>0 || userData2.size()>0) {
                    userData3.clear();
                    userData3.addAll(userData);
                    userData3.addAll(userData2);
                    MyAdapterForMessageList myAdapterForMessageList = new MyAdapterForMessageList(userData3);
                    recyclerView.setAdapter(myAdapterForMessageList);
                }
                else
                {
                    textView.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }
}
