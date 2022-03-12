package amit.example.lilichatians;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import amit.example.lilichatians.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AboutFragment extends Fragment {

    private String uuid;
    private View v;
    private TextView name,email,bio,dob,friend,active;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = firebaseDatabase.getReference();
    private int FriendFound1=0,FriendFound2=0,FriendFound=0;

    public AboutFragment(String uuid)
    {
        this.uuid = uuid;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        v =  inflater.inflate(R.layout.about_fragment, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        name = v.findViewById(R.id.about_name_content);
        email = v.findViewById(R.id.about_email_content);
        bio = v.findViewById(R.id.about_bio_content);
        dob = v.findViewById(R.id.about_dob_content);
        friend = v.findViewById(R.id.about_count_friends_content);
        active = v.findViewById(R.id.about_last_active_content);

        mRef.child("users").child(uuid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    Long time = System.currentTimeMillis();
                    if(dataSnapshot.child("name").exists())
                        name.setText(dataSnapshot.child("name").getValue().toString().trim());
                    if(dataSnapshot.child("email").exists())
                        email.setText(dataSnapshot.child("email").getValue().toString().trim());
                    if(dataSnapshot.child("bio").exists())
                        bio.setText(dataSnapshot.child("bio").getValue().toString().trim());
                    if(dataSnapshot.child("dob").exists())
                        dob.setText(dataSnapshot.child("dob").getValue().toString().trim());
                    if(dataSnapshot.child("timestamp").exists())
                        active.setText(calculate_time(time - (Long) dataSnapshot.child("timestamp").getValue()));
                    else
                        active.setText("Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        count_friends();

    }
    public void count_friends() {
        Query query = mRef.child("friends").orderByChild("senderId").equalTo(uuid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FriendFound1=0;
                if (dataSnapshot.getValue()!= null) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if (issue.child("status").getValue().toString().equals("1")) {
                            FriendFound1++;
                        }
                    }
                    friend.setText(FriendFound1+FriendFound2+" friends");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        Query query2 = mRef.child("friends").orderByChild("recieverId").equalTo(uuid);
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FriendFound2=0;
                if (dataSnapshot.getValue()!= null) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if (issue.child("status").getValue().toString().equals("1"))
                        {
                            FriendFound2++;
                        }
                    }
                    friend.setText(FriendFound1+FriendFound2+" friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
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
