package amit.example.lilichatians;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyAdapterForViewImage extends RecyclerView.Adapter<MyAdapterForViewImage.ViewHolder> {

    private ArrayList<ArrayList<String>> dataSnapshots ;
    private String uuid;
    private int i =1;
     ArrayList<String> strings;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

    public MyAdapterForViewImage(ArrayList<ArrayList<String>> dataSnapshots,String uuid)
    {
        this.dataSnapshots = dataSnapshots;
        this.uuid = uuid;
    }

    @NonNull
    @Override
    public MyAdapterForViewImage.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_photo_row,parent,false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyAdapterForViewImage.ViewHolder holder, int positionN) {

        holder.image_one = holder.itemView.findViewById(R.id.image_one);
        holder.image_one.setVisibility(View.GONE);
        holder.image_two = holder.itemView.findViewById(R.id.image_two);
        holder.image_two.setVisibility(View.GONE);
        holder.image_three = holder.itemView.findViewById(R.id.image_three);
        holder.image_three.setVisibility(View.GONE);

        strings = dataSnapshots.get(positionN);

        for(int i=0;i<strings.size();i++)
        {
            if(i==0)
            {
                holder.image_one.setVisibility(View.VISIBLE);
                StorageReference storageReference = mStorageRef.child("PostImages").child(strings.get(i));
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri.toString()).into(holder.image_one);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(holder.image_one,e+"",Snackbar.LENGTH_LONG).show();
                    }
                });
            }
            if(i==1)
            {
                holder.image_two.setVisibility(View.VISIBLE);
                StorageReference storageReference = mStorageRef.child("PostImages").child(strings.get(i));
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri.toString()).into(holder.image_two);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(holder.image_two,e+"",Snackbar.LENGTH_LONG).show();
                    }
                });

            }
            if(i==2)
            {
                holder.image_three.setVisibility(View.VISIBLE);
                StorageReference storageReference = mStorageRef.child("PostImages").child(strings.get(i));
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri.toString()).into(holder.image_three);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(holder.image_three,e+"",Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        }

    }

    @Override
    public int getItemCount() {
        return dataSnapshots.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image_one,image_two,image_three;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
