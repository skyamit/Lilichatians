package amit.example.lilichatians.ui.maps;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import amit.example.lilichatians.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import amit.example.lilichatians.MarkerAdapter;

public class maps_fragment extends Fragment implements LocationListener{

    private View v;
    private MapView mMapView;
    private GoogleMap googleMap;
    private FirebaseDatabase firebaseDatabase  = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = firebaseDatabase.getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String uid = mAuth.getUid();
    private TextView allow_location;
    private Location location;
    private double latitude,longitude;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.maps_fragment_fragment, container, false);

        mMapView = v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        allow_location = v.findViewById(R.id.allow_location);
        allow_location.setVisibility(View.GONE);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                // For showing a move to my location button
                googleMap.setMyLocationEnabled(true);
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                try {
                    LocationManager locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
                    Criteria criteria = new Criteria();
                    String bestProvider = locationManager.getBestProvider(criteria,true);

                    location = locationManager.getLastKnownLocation(bestProvider);

                    if(location!=null)
                    {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        LatLng latLng = new LatLng(latitude,longitude);

                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16.0f));

                        MarkerAdapter markerAdapter = new MarkerAdapter(getContext());
                        googleMap.setInfoWindowAdapter(markerAdapter);

                        add_me(latitude,longitude);

                    }
                }catch(SecurityException e)
                {

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
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void add_me(final double latitude, final double longitude)
    {
        mRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("location").exists()) {
                    if (dataSnapshot.child("location").getValue().toString().equals("1")) {
                        mRef.child("mapLocation").child(uid).child("lat").setValue(latitude);
                        mRef.child("mapLocation").child(uid).child("lng").setValue(longitude);
                        getUserLocation(latitude,longitude);
                    } else {
                        allow_location.setVisibility(View.VISIBLE);
                    }
                }else{
                    allow_location.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserLocation(double latitude,double longitude)
    {
        final DecimalFormat decimalFormat4 = new DecimalFormat("#");
        decimalFormat4.setRoundingMode(RoundingMode.DOWN);

        String lat = decimalFormat4.format(latitude).trim();
        final String lng = decimalFormat4.format(longitude).trim();

        mRef.child("mapLocation").orderByChild("lat").startAt(Double.parseDouble(lat)).limitToLast(20).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                googleMap.clear();
                if(dataSnapshot.exists())
                {
                    for(final DataSnapshot temp : dataSnapshot.getChildren())
                    {
                        if(!temp.getKey().equals(uid)) {

                            mRef.child("users").child(temp.getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull final DataSnapshot user) {
                                    if(user.child("name").exists())
                                    {
                                        double temp_longD = (double) temp.child("lng").getValue();
                                        String temp_long = decimalFormat4.format(temp_longD);
                                        if (Double.parseDouble(temp_long) == Double.parseDouble(lng)) {
                                            if (user.child("profilePic").exists()) {
                                                StorageReference storageReference = mStorageRef.child("ProfileImages").child(user.child("profilePic").getValue().toString());
                                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        Picasso.get().load(uri.toString()).into(new Target() {
                                                            @Override
                                                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                                                Bitmap temp_bitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth()/5,bitmap.getHeight()/5,true);
                                                                temp_bitmap = getCroppedBitmap(temp_bitmap);
                                                                final MarkerOptions markerOptions = new MarkerOptions()
                                                                        .position(new LatLng((double) temp.child("lat").getValue(), (double) temp.child("lng").getValue()))
                                                                        .title(user.child("email").getValue().toString())
                                                                        .icon(BitmapDescriptorFactory.fromBitmap(temp_bitmap));
                                                                googleMap.addMarker(markerOptions);
                                                            }

                                                            @Override
                                                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                                            }

                                                            @Override
                                                            public void onPrepareLoad(Drawable placeHolderDrawable) {

                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Snackbar.make(mMapView, e + "", Snackbar.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        add_me(latitude,longitude);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public Bitmap getCroppedBitmap(Bitmap bitmap)
    {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0,0,0,0);
        paint.setColor(color);

        canvas.drawCircle(bitmap.getWidth()/2,bitmap.getHeight()/2,bitmap.getWidth()/2,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap,rect,rect,paint);
        return output;
    }
}
