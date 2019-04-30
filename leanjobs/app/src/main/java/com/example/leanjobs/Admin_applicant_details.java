package com.example.leanjobs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Admin_applicant_details extends AppCompatActivity {

    public int appid, jobid;
    public String appname,appemail,appphone,appstatus,jobtit,jobstat,resumeURL,salt,User_Id,NewStatus;
    TextView name,email,phone,status, jobtitle,jst,txtMessage;
    Button ViewResume, ChangeStatus, Accept, Reject;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.homescreen, menu);
        return true;
        //return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                Intent i = new Intent(getApplicationContext(),Admin_HomeScreen.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_applicant_details);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            appid = extras.getInt("appid");
            appname = extras.getString("appname");
            appemail = extras.getString("email");
            appphone = extras.getString("phone");
            appstatus = extras.getString("status");
            jobid = extras.getInt("jobid");
            jobtit = extras.getString("jobti");
            jobstat = extras.getString("jobsta");
            resumeURL = extras.getString("resumeURL");
        }
        ViewResume = findViewById(R.id.btnApplresume);
        ChangeStatus = findViewById(R.id.btnChangeStatus);
        Accept = findViewById(R.id.btnAccept);
        Reject = findViewById(R.id.BtnReject);
        name = (TextView) findViewById(R.id.applname);
        email = (TextView) findViewById(R.id.emailid);
        phone = (TextView) findViewById(R.id.phone);
        status = (TextView) findViewById(R.id.status);
        jobtitle = (TextView) findViewById(R.id.JobTitle);
        jst = (TextView) findViewById(R.id.AdminJobStatus);
        txtMessage = findViewById(R.id.txtMessage);
        salt = getSharedPreferences("AdminDataPreferences", Context.MODE_PRIVATE).getString("salt","");
        User_Id = getSharedPreferences("AdminDataPreferences", Context.MODE_PRIVATE).getString("user_id","");
        name.setText(appname);
        email.setText(appemail);
        phone.setText(appphone);
        status.setText(appstatus);
        jobtitle.setText(jobtit);
        jst.setText(jobstat);


        if(appstatus.equals("Applied")){
            NewStatus = "1";
            Accept.setVisibility(View.INVISIBLE);
            Reject.setVisibility(View.INVISIBLE);
        }
        else if(appstatus.equals("Shortlisted")){
            ChangeStatus.setVisibility(View.INVISIBLE);
            Accept.setVisibility(View.VISIBLE);
            Reject.setVisibility(View.VISIBLE);
        }
        else  if(appstatus.equals("Accepted")){
            txtMessage.setText("ACCEPTED");
            ChangeStatus.setVisibility(View.INVISIBLE);
            Accept.setVisibility(View.INVISIBLE);
            Reject.setVisibility(View.INVISIBLE);
            txtMessage.getResources().getColor(R.color.msgGreen);
        }
        else  if(appstatus.equals("Rejected")){
            txtMessage.setText("REJECTED");
            ChangeStatus.setVisibility(View.INVISIBLE);
            Accept.setVisibility(View.INVISIBLE);
            Reject.setVisibility(View.INVISIBLE);
            txtMessage.setTextColor(Color.RED);
        }

        Accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewStatus = "2";
                PostUserData();
                Intent myIntent = new Intent(getApplicationContext(), List_of_applicants.class);
                startActivityForResult(myIntent, 0);
            }
        });
        Reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewStatus = "3";
                PostUserData();
                Intent myIntent = new Intent(getApplicationContext(), List_of_applicants.class);
                startActivityForResult(myIntent, 0);
            }
        });


        ViewResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{ String URL=resumeURL;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
                    startActivity(browserIntent);}
                catch (Exception ex){
                    Toast.makeText(Admin_applicant_details.this, "No Resume Available", Toast.LENGTH_LONG).show();
                }
            }
        });

        ChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostUserData();
                Intent myIntent = new Intent(getApplicationContext(), List_of_applicants.class);
                startActivityForResult(myIntent, 0);
            }
        });

    }

    private void PostUserData() {
        String URLPost = "http://dhillonds.com/leanjobsweb/index.php/api/jobs/change_app_status";
        StringRequest stringRequest = new StringRequest(Request.Method.POST,URLPost, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject UserCredentials = new JSONObject(response);
                    String StatusFlag = UserCredentials.getString("status");
                    String Message = UserCredentials.getString("message");

                    if(StatusFlag == "true"){
                        JSONObject Data = UserCredentials.getJSONObject("data");
                        Toast.makeText(Admin_applicant_details.this,Message,Toast.LENGTH_SHORT).show();
                    }
                    else if(StatusFlag == "false"){
                        Toast.makeText(getApplication(),Message,Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception ex){
                    Toast.makeText(getApplication(),ex.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Admin_applicant_details.this,error+"",Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                params.put("user_id",String.valueOf(User_Id));
                params.put("salt",salt);
                params.put("app_id",String.valueOf(appid));
                params.put("new_status",NewStatus);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
