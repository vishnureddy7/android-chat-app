package androidchatapp.com.androidchatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

public class Chat extends AppCompatActivity {
    LinearLayout layout;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    Firebase reference1, reference2;
    SharedPreferences sharedpreferences;


    protected void onStart(){
        super.onStart();
        //shared preferences object
        sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        //retrieve existing shared preferences
        Log.d("place:","entering chat");
        String restoredText = sharedpreferences.getString("username", null);
        if (restoredText == null) {
            //activity shifting
            startActivity(new Intent(Chat.this, Login.class));
        }
        else{
            String username = sharedpreferences.getString("username", "none");
            String password = sharedpreferences.getString("password", "none");
            //set user details
            UserDetails.username = username;
            UserDetails.password = password;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        //retrieve existing shared preferences
        String restoredText = sharedpreferences.getString("username", null);
        if (restoredText == null) {
            //activity shifting
            startActivity(new Intent(Chat.this, Login.class));
        }
        else{
            String username = sharedpreferences.getString("username", "none");
            String password = sharedpreferences.getString("password", "none");
            //set user details
            UserDetails.username = username;
            UserDetails.password = password;
        }

        layout = (LinearLayout)findViewById(R.id.layout1);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        Firebase.setAndroidContext(this);
        reference1 = new Firebase("https://androidchatapp-3e8d5.firebaseio.com/messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference2 = new Firebase("https://androidchatapp-3e8d5.firebaseio.com/messages/" + UserDetails.chatWith + "_" + UserDetails.username);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("user", UserDetails.username);
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
                messageArea.setText("");
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String userName = map.get("user").toString();

                if(userName.equals(UserDetails.username)){
                    addMessageBox(message, 1);
                }
                else{
                    addMessageBox(message, 2);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


    public void addMessageBox(String message, int type){
        TextView textView = new TextView(Chat.this);
        textView.setText(message);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(17);
        if(type == 1) {
            textView.setBackgroundResource(R.drawable.rounded_corner1);
            lp.gravity = Gravity.RIGHT;
            lp.setMargins(200, 5, 10, 5);
        }
        else{
            textView.setBackgroundResource(R.drawable.rounded_corner2);
            lp.gravity = Gravity.LEFT;
            lp.setMargins(10, 5, 200, 5);
        }
        textView.setLayoutParams(lp);
        layout.addView(textView);
        //scrollView.scrollTo(0, scrollView.getBottom());
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}