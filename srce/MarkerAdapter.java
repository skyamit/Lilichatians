package amit.example.lilichatians;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MarkerAdapter implements GoogleMap.InfoWindowAdapter {

    private String muid;
    private Context context;
    private TextView textView;
    private Button button;
    private ImageView imageView;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = firebaseDatabase.getReference().child("users");
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef = firebaseStorage.getReference();
    private View v;

    public MarkerAdapter(Context context)
    {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.marker,null);

        textView = v.findViewById(R.id.name_marker);

        muid = marker.getTitle();
        textView.setText(muid);

        return v;
    }


}
