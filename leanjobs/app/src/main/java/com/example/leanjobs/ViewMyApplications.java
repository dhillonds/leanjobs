package com.example.leanjobs;

import android.app.Application;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ViewMyApplications extends ListActivity implements AsyncResponseMyApplications {
    public int page = 0;
    MyApplicationsAdapter adapter;
    String UserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list_of_jobs);
        Intent intent = getIntent();
        UserId = intent.getStringExtra("User_User_id");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            Intent intent = new Intent(ViewMyApplications.this, User_HomeScreen.class);
            startActivity(intent);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void processFinishMyApplications(final ArrayList<Appln> apps) {

        adapter = new MyApplicationsAdapter(this, apps);
        setListAdapter(adapter);

        if (apps.size() > 0) {
            ListView listView = getListView();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                    Appln selectedApp  = apps.get(position);
                    Intent intent = new Intent(ViewMyApplications.this, MyApplicationDetails.class);
                    intent.putExtra("jobid",selectedApp.getJobIDAppl());
                    intent.putExtra("appid",selectedApp.getApplicationID());
                    intent.putExtra("jobtitle",selectedApp.getJobTitle());
                    intent.putExtra("jobdesc",selectedApp.getRoleDescription());
                    intent.putExtra("jobreq",selectedApp.getJobRequirements());
                    intent.putExtra("wages",selectedApp.getWages());
                    intent.putExtra("jobstat",selectedApp.getJobStatus());
                    intent.putExtra("appstatus",selectedApp.getApplicationStatus());
                    startActivity(intent);
                }
            });
        }
    }

    public void onResume(){
        super.onResume();
        MyApplicat asyncTask =new MyApplicat();
        asyncTask.delegate = this;
        asyncTask.execute(UserId);
    }
}

class MyApplicat extends AsyncTask<String, Void, String> {

    public AsyncResponseMyApplications delegate = null;

    protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
        InputStream in = entity.getContent();
        StringBuffer out = new StringBuffer();
        int n = 1;
        while (n > 0) {
            byte[] b = new byte[4096];
            n = in.read(b);
            if (n > 0) out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    @Override
    protected String doInBackground(String... userid) {
        String usid = userid[0];

        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        String url = "http://dhillonds.com/leanjobsweb/index.php/api/jobs/user_applications?user_id="+usid;
        HttpGet httpGet = new HttpGet(url);
        String text = null;
        try {
            HttpResponse response = httpClient.execute(httpGet, localContext);
            HttpEntity entity = response.getEntity();
            text = getASCIIContentFromEntity(entity);
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
        return text;
    }

    protected void onPostExecute(String results) {
        if (results != null) {
            ArrayList<Appln> myapplications = new ArrayList<Appln>();

            try {
                JSONObject jobj=new JSONObject(results);
                String c = jobj.getString("data");
                JSONArray myapps = new JSONArray(c);

                for(int i = 0; i<myapps.length(); i++) {

                    Appln applic = new Appln();
                    String jobstatus = null;
                    String appstatus = null;


                    int jobid = myapps.getJSONObject(i).getInt("job_id");
                    int appid = myapps.getJSONObject(i).getInt("app_id");
                    String jobtitle = myapps.getJSONObject(i).getString("title");
                    String jobdesc = myapps.getJSONObject(i).getString("role_desc");
                    String jobreqts = myapps.getJSONObject(i).getString("job_reqs");
                    String jobwages = myapps.getJSONObject(i).getString("wages");
                    int jstat = myapps.getJSONObject(i).getInt("is_active");
                    int astat = myapps.getJSONObject(i).getInt("job_status");

                    if(jstat == 1)
                        jobstatus = "Active";
                    else if(jstat == 0)
                        jobstatus = "Inactive";

                    if(astat == 0)
                        appstatus = "Applied";
                    else if(astat == 1)
                        appstatus = "Shortlisted";
                    else if(astat == 2)
                        appstatus = "Selected";
                    else if(astat == 3)
                        appstatus = "Rejected";

                    applic.setJobIDAppl(jobid);
                    applic.setApplicationID(appid);
                    applic.setJobTitle(jobtitle);
                    applic.setRoleDescription(jobdesc);
                    applic.setJobRequirements(jobreqts);
                    applic.setWages(jobwages);
                    applic.setJobStatus(jobstatus);
                    applic.setApplicationStatus(appstatus);
                    myapplications.add(applic);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            delegate.processFinishMyApplications(myapplications);
        }
    }

}