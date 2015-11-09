package local.mbl402t3.communications;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private BufferedWriter out;
    private String text;
    private String messageDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        messageDialog = "";
        String server = "irc.freenode.net";
        String nick = "test";
        MyTask task = new MyTask();
        task.executeOnExecutor(MyTask.THREAD_POOL_EXECUTOR, server, nick);
        text = "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickInteractBtn(View view) {

        sendMessage();
    }
    public void postMessage(Boolean clear, String str)
    {
        if (clear == true)
            messageDialog = "";
        messageDialog = str + "\n" + messageDialog;
        TextView tvwMessages = (TextView) findViewById(R.id.txtMessages);
        tvwMessages.setText(messageDialog);
    }
public void sendMessage()
{
    try {
        out.write("PRIVMSG #IRCHACKS :Hello\r\n");
        out.flush();
    }
    catch (IOException e)
    {
        e.printStackTrace();
    }
}
    private class MyTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Socket socket = new Socket("irc.freenode.net", 6667);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                socket.setKeepAlive(true);

                updateDisplay("Client message: Init complete.");
                String len = "";
                int suffix = 1;
                boolean registered = false;
                while (registered == false) {
                    out.write("NICK Android_Dev_" + "nick" + Integer.toString(suffix) + "\r\n");
                    out.write("USER " + "Android_Dev" + Integer.toString(suffix) + " 8 * : Nov-8-2015\r\n");
                    out.flush();
                    len = in.readLine();
                    if (len.indexOf("433") >= 0) {
                        publishProgress("Client message: Nick used");
                        suffix++;
                    } else {
                        registered = true;
                    }
                }
                out.write("JOIN #IRCHACKS\r\n");
                out.flush();
                while (true) {
                    len = in.readLine();
                    if (len.toLowerCase().startsWith("ping ")) {
                        out.write("PONG " + len.substring(5) + "\r\n");
                        out.flush();
                        publishProgress("Client message: PING? PONG!");
                    } else {
                        publishProgress("Server message: " + len);
                    }
                    publishProgress(len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "0";
        }
        @Override
        protected void onProgressUpdate(String... values)
        {
            updateDisplay(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
        }
        private void updateDisplay(String s)
        {
            Log.d("RAW:", s);
            if (s.toLowerCase().startsWith(":"))
            {
                text = s.split("\\!")[0];
                if (!text.contains("."))
                {
                    text = text + " says, " + s.substring(s.lastIndexOf(":") + 1);
                    postMessage(false, text);

                }
            }
        }
    }
}
