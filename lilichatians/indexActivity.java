package amit.example.lilichatians;

import android.Manifest;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import amit.example.lilichatians.ui.Message_List.Message_List;
import amit.example.lilichatians.ui.ViewProfile.ViewProfileFragment;
import amit.example.lilichatians.ui.friendrequest.FriendRequest;
import amit.example.lilichatians.ui.home.HomeFragment;
import amit.example.lilichatians.ui.maps.maps_fragment;
import amit.example.lilichatians.ui.notification.NotificationFragment;
import amit.example.lilichatians.ui.people.PeopleFragment;
import amit.example.lilichatians.ui.setting.SettingFragment;

import static amit.example.lilichatians.Lilichatians.Message;

public class indexActivity extends AppCompatActivity {
    private FirebaseUser user;
    String uid;
    NavigationView navigationView;
    DrawerLayout drawer;
    ImageView nav_header_profile_image;
    TextView name, email;
    private AppBarConfiguration mAppBarConfiguration;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    StorageReference myStorageRef = FirebaseStorage.getInstance().getReference();
    private int FriendFound1 = 0, FriendFound2 = 0, countNoti = 0;
    NavController navController;
    MovableFloatingActionButton floatingActionButton;
    Fragment fragment;
    MediaPlayer mediaPlayer;
    LocationManager locationManager;
    NotificationManagerCompat notificationManagerCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        floatingActionButton = findViewById(R.id.fab);
        mediaPlayer = MediaPlayer.create(this, R.raw.msgalert);
        notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

        //permission for location...........................
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
                }, 30);
                return;
            }
        }
        //permission ends here...............................


        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_map,
                R.id.nav_view_profile, R.id.nav_home, R.id.nav_message, R.id.nav_people, R.id.nav_friend_request, R.id.nav_setting, R.id.nav_share)
                .setDrawerLayout(drawer)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        //body start
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = mAuth.getUid();
        try {
            FriendRequestNotification();
            MessageNotification();
            notificationCount();
        }catch (Exception e)
        {
            System.out.println(e);
        }

        //setting user active
        status(1);

        //floating button action
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment = new Message_List();
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("MessageList").commit();

            }
        });

        detectChange();

        setProfile();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.index, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Toast.makeText(indexActivity.this, "Setting Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.logout:
                logout();
                break;
            default:
                drawer.openDrawer(GravityCompat.START);
        }
        return true;
    }



    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void logout() {
        status(0);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception e){
            System.out.println(e);
        }
        startActivity(new Intent(indexActivity.this,LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        status(1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        status(0);
    }

    public void status(int status)
    {
        Long time = System.currentTimeMillis();
        myRef.child("users").child(uid).child("timestamp").setValue(time);
        myRef.child("users").child(uid).child("status").setValue(status);
    }

    //Friend Request Notification
    public void FriendRequestNotification()
    {
        Query checkRequestCount = myRef.child("friends").orderByChild("recieverId").startAt(uid).endAt(uid + "\uf8ff");
        checkRequestCount.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count=0;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if(issue.child("status").getValue().toString().equals("0"))
                        {
                            count++;
                        }
                    }
                    if(count>0)
                        navigationView.getMenu().findItem(R.id.nav_friend_request).setTitle("New Friend Request : "+count);
                    else
                        navigationView.getMenu().findItem(R.id.nav_friend_request).setTitle("Friend Request");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //Check New Message
    public void MessageNotification()
    {
        Query query = myRef.child("friends").orderByChild("senderId").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FriendFound1=0;
                if (dataSnapshot.getValue()!= null) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if(issue.child("lastMessage").exists()) {
                            if (issue.child("lastMessage").child("status").getValue().toString().equals("0") &&
                                    issue.child("lastMessage").child("recieverId").getValue().toString().equals(uid))
                                    FriendFound1++;
                        }
                    }
                    if(FriendFound1+FriendFound2 >0) {
                        sendOnChannelMessage();
                        mediaPlayer.start();
                        navigationView.getMenu().findItem(R.id.nav_message).setTitle("New Messages : " + (FriendFound1 + FriendFound2));
                        floatingActionButton.setImageResource(R.drawable.notification);
                    }
                    else {
                        navigationView.getMenu().findItem(R.id.nav_message).setTitle("Messages");
                        floatingActionButton.setImageResource(R.drawable.notification_image);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        Query query2 = myRef.child("friends").orderByChild("recieverId").equalTo(uid);
        query2.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FriendFound2=0;
                if (dataSnapshot.getValue()!= null) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if(issue.child("lastMessage").exists()) {
                            if (issue.child("lastMessage").child("status").getValue().toString().equals("0") &&
                                    issue.child("lastMessage").child("recieverId").getValue().toString().equals(uid))
                                FriendFound2++;
                        }
                    }
                }
                if(FriendFound1+FriendFound2 >0) {
                    sendOnChannelMessage();
                    navigationView.getMenu().findItem(R.id.nav_message).setTitle("New Messages : " + (FriendFound1 + FriendFound2));
                    floatingActionButton.setImageResource(R.drawable.notification);
                }
                else {
                    navigationView.getMenu().findItem(R.id.nav_message).setTitle("Messages");
                    floatingActionButton.setImageResource(R.drawable.notification_image);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void notificationCount()
    {
        myRef.child("Notification").child(uid).limitToLast(20).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                countNoti = 0;
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot temp : dataSnapshot.getChildren())
                    {
                        if(temp.child("status").getValue().toString().equals("0"))
                        {
                            countNoti++;
                        }
                    }
                    if(countNoti>0)
                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("New Notification : "+countNoti);
                    else
                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("Notifications");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setProfile()
    {
        View navHeaderView = navigationView.inflateHeaderView(R.layout.nav_header_index);
        name = navHeaderView.findViewById(R.id.name);
        email = navHeaderView.findViewById(R.id.email);
        nav_header_profile_image = navHeaderView.findViewById(R.id.nav_header_profile_image);

        myRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child("name").exists())
                    name.setText(dataSnapshot.child("name").getValue().toString());

                if(dataSnapshot.child("email").exists())
                    email.setText(dataSnapshot.child("email").getValue().toString());

                if (dataSnapshot.child("profilePic").exists()){
                    StorageReference storageReference = myStorageRef.child("ProfileImages").child(dataSnapshot.child("profilePic").getValue().toString());
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri.toString()).into(nav_header_profile_image);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(navigationView,e+"",Snackbar.LENGTH_LONG).show();
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void detectChange()
    {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                drawer.closeDrawers();
                int id = menuItem.getItemId();
                switch (id)
                {
                    case R.id.nav_map:
                        fragment = new maps_fragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("Maps").commit();
                        break;
                    case R.id.nav_people:
                        fragment = new PeopleFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("People").commit();
                        break;
                    case R.id.nav_message:
                        fragment = new Message_List();
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("MessageList").commit();
                        break;
                    case R.id.nav_view_profile:
                        fragment = new ViewProfileFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("ViewProfile").commit();
                        break;
                    case R.id.nav_home:
                        fragment = new HomeFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("Home").commit();
                        break;
                    case R.id.nav_notification:
                        fragment = new NotificationFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("Notification").commit();
                        break;
                    case R.id.nav_friend_request:
                        fragment = new FriendRequest();
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("FriendRequest").commit();
                        break;
                    case R.id.nav_setting:
                        fragment = new SettingFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("SettingFragment").commit();
                        break;
                    case R.id.nav_share:
                        ShareIt();
                        break;
                    default:
                        fragment = new ViewProfileFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("ViewProfile").commit();
                }
                return true;
            }
        });
    }

    private void ShareIt() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Download Lilichatians : https://play.google.com/store/apps/details?id=amit.example.lilichatians \nHey, I'm enjoying using Lilichatians.\nLilichatians is a new Social Media Application, In which you can post photos, Make Friends, Chat with Friends, Set Cool Looking Bio and Profile Pictures and All the basic features of Social Media.";
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT,"Download Lilichatians : https://play.google.com/store/apps/details?id=amit.example.lilichatians \n");
        sharingIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        startActivity(Intent.createChooser(sharingIntent,"Share via"));
    }

    public void sendOnChannelMessage()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            new Notification.Builder(getApplicationContext(),Message)
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle("New Message Arrived")
                    .setContentText("Check Your Message Layout on lilichatians")
                    .build();

        }
        else{
            new Notification.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.icon)
                    .setContentTitle("New Message Arrived")
                    .setContentText("Check Your Message Layout on lilichatians")
                    .build();

        }
    }

}
