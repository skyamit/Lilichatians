package amit.example.lilichatians.ui.friendrequest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import amit.example.lilichatians.MyAdapterForFriendRequest;

public class FriendRequest extends Fragment {

    private FriendRequestViewModel mViewModel;
    private RecyclerView friend_request_recyclerView ;
    private ProgressBar progressBar_Request;
    private TextView count_request;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
    private ArrayList<DataSnapshot> userData = new ArrayList<>();

    public static FriendRequest newInstance() {
        return new FriendRequest();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.friend_request_fragment, container, false);

        progressBar_Request = v.findViewById(R.id.progressBar_Request);
        progressBar_Request.setVisibility(View.VISIBLE);
        friend_request_recyclerView = v.findViewById(R.id.friend_request_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        friend_request_recyclerView.setLayoutManager(linearLayoutManager);
        count_request = v.findViewById(R.id.count_request);
        count_request.setVisibility(View.GONE);

        String uid = mAuth.getUid();
        Query query = myRef.child("friends").orderByChild("recieverId").startAt(uid).endAt(uid + "\uf8ff");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count=0;
                if (dataSnapshot.exists()) {
                    userData.clear();
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if(issue.child("status").getValue().toString().equals("0"))
                        {
                            userData.add(issue);
                            count++;
                        }
                    }
                    if(count==0)
                    {
                        count_request.setVisibility(View.VISIBLE);
                    }
                    progressBar_Request.setVisibility(View.GONE);
                    MyAdapterForFriendRequest myAdapterForFriendRequest = new MyAdapterForFriendRequest(userData);
                    friend_request_recyclerView.setAdapter(myAdapterForFriendRequest);
                }
                else{
                    progressBar_Request.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

}
