package androidchatapp.com.androidchatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;

import org.json.JSONException;
import org.json.JSONObject;

public class Register extends AppCompatActivity {
    EditText username, password, cpassword;
    Button registerButton;
    String user, pass, cpass;
    TextView login;
    SharedPreferences sharedpreferences;

    @Override
    protected void onStart(){
        super.onStart();
        //shared preferences object
        sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        //retrieve existing shared preferences
        Log.d("place:","entering register");
        String restoredText = sharedpreferences.getString("username", null);
        if (restoredText != null) {
            String username = sharedpreferences.getString("username", "none");
            String password = sharedpreferences.getString("password", "none");
            //set user details
            UserDetails.username = username;
            UserDetails.password = password;

            //activity shifting
            startActivity(new Intent(Register.this, Users.class));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sharedpreferences = getSharedPreferences(Login.MyPREFERENCES, Context.MODE_PRIVATE);
        //retrieve existing shared preferences
        String restoredText = sharedpreferences.getString("username", null);
        if (restoredText != null) {
            String username = sharedpreferences.getString("username", "none");
            String password = sharedpreferences.getString("password", "none");
            //set user details
            UserDetails.username = username;
            UserDetails.password = password;

            //activity shifting
            startActivity(new Intent(Register.this, Users.class));
        }

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        cpassword = (EditText)findViewById(R.id.cpassword);
        registerButton = (Button)findViewById(R.id.registerButton);
        login = (TextView)findViewById(R.id.login);

        Firebase.setAndroidContext(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = username.getText().toString();
                pass = password.getText().toString();
                cpass = cpassword.getText().toString();
                if(user.equals("")){
                    username.setError("can't be blank");
                }
                else if(pass.equals("")){
                    password.setError("can't be blank");
                }
                else if(!user.matches("[A-Za-z0-9]+")){
                    username.setError("only alphabet or number allowed");
                }
                else if(user.length()<8){
                    username.setError("atleast 8 characters long");
                }
                else if(pass.length()<8){
                    password.setError("atleast 8 characters long");
                }
                else if(!pass.equals(cpass)){
                    cpassword.setError("password does not match");
                }
                else {
                    final ProgressDialog pd = new ProgressDialog(Register.this);
                    pd.setMessage("Loading...");
                    pd.show();

                    String url = "https://androidchatapp-3e8d5.firebaseio.com/users.json";

                    StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
                        @Override
                        public void onResponse(String s) {
                            Firebase reference = new Firebase("https://androidchatapp-3e8d5.firebaseio.com/users");

                            if(s.equals("null")) {
                                reference.child(user).child("password").setValue(pass);
                                Toast.makeText(Register.this, "registration successful", Toast.LENGTH_LONG).show();
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("username", user);
                                editor.putString("password", pass);
                                editor.commit();

                                //set user details
                                UserDetails.username = user;
                                UserDetails.password = pass;

                                //activity shifting
                                startActivity(new Intent(Register.this, Users.class));
                            }
                            else {
                                try {
                                    JSONObject obj = new JSONObject(s);

                                    if (!obj.has(user)) {
                                        reference.child(user).child("password").setValue(pass);
                                        Toast.makeText(Register.this, "registration successful", Toast.LENGTH_LONG).show();
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putString("username", user);
                                        editor.putString("password", pass);
                                        editor.commit();

                                        //set user details
                                        UserDetails.username = user;
                                        UserDetails.password = pass;

                                        //activity shifting
                                        startActivity(new Intent(Register.this, Users.class));
                                    } else {
                                        Toast.makeText(Register.this, "username already exists", Toast.LENGTH_LONG).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            pd.dismiss();
                        }

                    },new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            System.out.println("" + volleyError );
                            pd.dismiss();
                        }
                    });

                    RequestQueue rQueue = Volley.newRequestQueue(Register.this);
                    rQueue.add(request);
                }
            }
        });
    }
}