package amit.example.lilichatians.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import amit.example.lilichatians.MyAdapterForNotification;

public class NotificationFragment extends Fragment {

    private View v;
    ArrayList<DataSnapshot> dataSnapshots = new ArrayList<>();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = firebaseDatabase.getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String uid = mAuth.getUid();
    private Button markAllAsRead ;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        v =  inflater.inflate(R.layout.notification_fragment, container, false);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final RecyclerView recyclerView = v.findViewById(R.id.notification_recyclerView);
        markAllAsRead = v.findViewById(R.id.set_all);

        mRef.child("Notification").child(uid).orderByChild("timestamp").limitToLast(20).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshots.clear();
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot data : dataSnapshot.getChildren())
                    {
                        dataSnapshots.add(data);
                    }
                    MyAdapterForNotification myAdapterForNotification = new MyAdapterForNotification(dataSnapshots);
                    recyclerView.setAdapter(myAdapterForNotification);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        markAllAsRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRef.child("Notification").child(uid).orderByChild("status").startAt(0).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        dataSnapshots.clear();
                        if(dataSnapshot.exists())
                        {
                            for(DataSnapshot data : dataSnapshot.getChildren())
                            {
                                data.child("status").getRef().setValue(1);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

    }

}
