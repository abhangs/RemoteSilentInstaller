package com.example.RemoteSilentInstall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.conn.util.InetAddressUtils;

import java.io.*;
import java.lang.Process;
import java.net.*;
import java.util.Enumeration;


public class RemoteSilentInstall extends Activity {

    public static final String TAG = "remoteSilentInstaller";
    public static final Integer SERVER_PORT = 8080;
    public static final String filename = "apk" + (System.currentTimeMillis() / 1000L) + ".apk";
    private ServerSocket server;


    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        try {
            new Thread(serverRunnable).start();
            showLaunched();        }
        catch (Exception ex)
        {

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private void showLaunched()
    {
        Context ctx = getApplicationContext();
        Toast toast = Toast.makeText(
                ctx,
                "Remote Silent Installer is now running in the background with IP " + getLocalIpAddress(),
                Toast.LENGTH_LONG
        );
        toast.show();
    }

    private void toastReceived()
    {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(RemoteSilentInstall.this, "Received new APK", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private synchronized void promptInstall()
    {
        Process process;
        try {

           String fileLocation = Environment.getExternalStorageDirectory().toString() + "/" + this.filename;
           // Process process =  Runtime.getRuntime().exec("su pm install -r "+'"' + fileLocation + '"');
           // process.waitFor();
           // Toast.makeText(RemoteSilentInstall.this,"Installed new APK", Toast.LENGTH_LONG).show();
//            Process process = Runtime.getRuntime().exec("su");
//            this.wait(1000);
//            OutputStream outputStream = process.getOutputStream();
//            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
//            dataOutputStream.writeBytes("pm install -r " + fileLocation);
//            dataOutputStream.flush();

            process = Runtime.getRuntime().exec("su");

            DataOutputStream outputStream =  new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes("pm install -r " + '"' + fileLocation + '"' + "\n");

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            try
            {
                     process.waitFor();
                if(process.exitValue()!=255)
                {

                }
                else
                {

                }
            }
            catch (InterruptedException e)
            {

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress().toString())) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return "";
    }


    Runnable serverRunnable = new Runnable() {

        public void run() {
            // Set up the server
            try {
                server = new ServerSocket(SERVER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            File file = new File(Environment.getExternalStorageDirectory() + "/" + RemoteSilentInstall.filename);
            FileOutputStream fis = null;
            InputStream os = null;
            Socket sock = null;

            // Wait for a response
            try {
                while (true) {
                    // Accept the data connection
                    sock = server.accept();
                    //Load the byteArray with bytes the size of the file.



                    fis = new FileOutputStream(file);
                    os = sock.getInputStream();
                    byte[] byteArray = new byte[os.available()];

                    int count;
                    //Write the contents to the file

                    while ((count = os.read(byteArray)) >= 0)
                    {
                        fis.write(byteArray, 0, count);
                    }

                    toastReceived();
                    // Run the installation
                    promptInstall();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Cleanup.
                try {
                    fis.close();
                    os.close();
                    sock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    };


}