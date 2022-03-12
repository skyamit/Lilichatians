package amit.example.lilichatians;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class Lilichatians extends Application {
    public static final String Message = "This notification is for new Messages";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();

    }
    private void createNotificationChannels()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            NotificationChannel Message_channel = new NotificationChannel(
                    Message,
                    "Message",
                    NotificationManager.IMPORTANCE_HIGH
            );
            Message_channel.setDescription("This Notification is to let user know about new Message Arrived.");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(Message_channel);
        }

    }
}

