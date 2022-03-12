package amit.example.lilichatians;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import amit.example.lilichatians.ui.ViewImage.Viewimage;

public class MessageAdapter extends  RecyclerView.Adapter<MessageAdapter.MessageHolder>  {

    private ArrayList<DataSnapshot> arrayData;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String uid = mAuth.getUid();
    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
    private String combinedUid;
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef = firebaseStorage.getReference();

    public MessageAdapter(ArrayList<DataSnapshot> dataSnapshot)
    {
     this.arrayData = dataSnapshot;
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single,parent,false);
        return new MessageHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageHolder holder, final int position) {
        holder.itemView.setTag(arrayData.get(position));

        holder.linearLayout = holder.itemView.findViewById(R.id.message_single_linearLayout);
        holder.msg = holder.itemView.findViewById(R.id.Message);
        holder.when_sent = holder.itemView.findViewById(R.id.when_sent);
        holder.message_linearLayout = holder.itemView.findViewById(R.id.message_linearLayout);
        LinearLayout.LayoutParams params,params1;
        params = (LinearLayout.LayoutParams) holder.message_linearLayout.getLayoutParams();
        params1 = (LinearLayout.LayoutParams) holder.when_sent.getLayoutParams();

        try {
            String diffTime="Doesn't defined";
            long timeMillis = System.currentTimeMillis();
            if(arrayData.get(position).child("timestamp").exists()) {
                long currentTime = (Long) arrayData.get(position).child("timestamp").getValue();
                diffTime = calculate_time(timeMillis - currentTime)+"";
            }

            holder.senderId = arrayData.get(position).child("senderId").getValue().toString();
            holder.recieverId = arrayData.get(position).child("recieverId").getValue().toString();
            holder.msgKey = arrayData.get(position).getKey().toString();

            if (holder.senderId.compareTo(holder.recieverId)>0) {
                combinedUid = holder.senderId + holder.recieverId;
            } else {
                combinedUid = holder.recieverId + holder.senderId;
            }

            if(arrayData.get(position).child("status").getValue().toString().equals("0")){ holder.status= "Not Seen"; }
            else{ holder.status = "Seen"; }

            if (uid.equals(holder.senderId)) {
                holder.linearLayout.setGravity(Gravity.END);
                params.gravity = Gravity.END;
                params.setMargins(80,0,0,0);
                holder.message_linearLayout.setLayoutParams(params);
                params1.gravity = Gravity.END;
                holder.when_sent.setLayoutParams(params1);

                if(arrayData.get(position).child("content").exists()) {
                    holder.msg.setText(arrayData.get(position).child("content").getValue().toString());
                    holder.msg.setBackground(ContextCompat.getDrawable(holder.msg.getContext(),R.drawable.green_button));
                    holder.msg.setTextColor(ContextCompat.getColor(holder.msg.getContext(),R.color.white));
                }
                else {
                    holder.msg.setText("You Sent an Image");
                    holder.msg.setBackground(ContextCompat.getDrawable(holder.msg.getContext(),R.drawable.border));
                    holder.msg.setTextColor(ContextCompat.getColor(holder.msg.getContext(),R.color.login_button));
                    holder.msg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AppCompatActivity activity = (AppCompatActivity) view.getContext();
                            Fragment fragment = new Viewimage(combinedUid+"/"+arrayData.get(position).child("images").getValue().toString(),4,holder.senderId);
                            activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("ToImage").commit();
                        }
                    });
                }

                holder.when_sent.setText(holder.status+" -- "+diffTime);
            }
            else{
                holder.linearLayout.setGravity(Gravity.START);
                params.gravity = Gravity.START;
                params.setMargins(0,0,80,0);
                holder.message_linearLayout.setLayoutParams(params);
                params1.gravity = Gravity.START;
                holder.when_sent.setLayoutParams(params1);
                mRef.child("message").child(combinedUid).child("messages").child(holder.msgKey).child("status").setValue(1);

                if(arrayData.get(position).child("content").exists()) {
                    holder.msg.setText(arrayData.get(position).child("content").getValue().toString());
                    holder.msg.setBackground(ContextCompat.getDrawable(holder.msg.getContext(),R.drawable.blue_button));
                    holder.msg.setTextColor(ContextCompat.getColor(holder.msg.getContext(),R.color.white));
                }
                else {
                    holder.msg.setText("You Recieved an Image");
                    holder.msg.setBackground(ContextCompat.getDrawable(holder.msg.getContext(),R.drawable.border));
                    holder.msg.setTextColor(ContextCompat.getColor(holder.msg.getContext(),R.color.signup_button));
                    holder.msg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AppCompatActivity activity = (AppCompatActivity) view.getContext();
                            Fragment fragment = new Viewimage(combinedUid+"/"+arrayData.get(position).child("images").getValue().toString(),4,holder.senderId);
                            activity.getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack("ToImage").commit();
                        }
                    });
                }

                holder.when_sent.setText(diffTime);
            }

            mRef.child("friends").child(combinedUid).child("lastMessage").child("recieverId").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        if (dataSnapshot.getValue().toString().equals(uid))
                            mRef.child("friends").child(combinedUid).child("lastMessage").child("status").setValue(1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        } catch (NullPointerException e)
        {

        }

    }

    public String calculate_time(long time)
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

    @Override
    public int getItemCount() {
        return arrayData.size();
    }

    public class MessageHolder extends RecyclerView.ViewHolder {

        TextView msg,when_sent;
        ImageView imageView_message;
        LinearLayout linearLayout,message_linearLayout;
        String senderId,recieverId,msgKey,status;
        public MessageHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


}
