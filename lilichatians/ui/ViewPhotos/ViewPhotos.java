package amit.example.lilichatians.ui.ViewPhotos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import amit.example.lilichatians.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import amit.example.lilichatians.MyAdapterForViewImage;

public class ViewPhotos extends Fragment {
    private View v;
    private RecyclerView view_photos_recyclerView;
    private String uuid;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = firebaseDatabase.getReference();
    private ArrayList<ArrayList<String>> dataSnapshots = new ArrayList<>();

    public ViewPhotos(String uuid)
    {
        this.uuid = uuid;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        v =  inflater.inflate(R.layout.view_photos_fragment, container, false);

        view_photos_recyclerView = v.findViewById(R.id.view_photos_recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(v.getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        view_photos_recyclerView.setLayoutManager(linearLayoutManager);

        mRef.child("posts").child(uuid).orderByKey().limitToLast(20).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshots.clear();
                if (dataSnapshot.exists()) {

                    ArrayList<String> strings = new ArrayList<>();

                    for (final DataSnapshot issue : dataSnapshot.getChildren()) {
                        if(issue.child("photo").exists())
                            strings.add(issue.child("photo").getValue().toString());
                    }
                    int count =strings.size();

                    for(int i=0;i<=(strings.size()/3);i++)
                    {
                        ArrayList<String> data = new ArrayList<>();

                        for(int y=0;y<3;y++)
                        {
                            if(0<count) {
                                data.add(strings.get(count-1));
                                count--;
                            }
                        }
                        dataSnapshots.add(i,data);
                    }
                    MyAdapterForViewImage myAdapterForViewImage = new MyAdapterForViewImage(dataSnapshots,uuid);
                    view_photos_recyclerView.setAdapter(myAdapterForViewImage);
                }
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
}
