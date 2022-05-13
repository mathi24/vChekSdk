package com.v_chek_host.vcheckhostsdk.service;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.v_chek_host.vcheckhostsdk.downloadutil.CheckForSDCard;
import com.v_chek_host.vcheckhostsdk.downloadutil.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;

import static android.content.ContentValues.TAG;

public class ModelDownloadService extends Service {
	int counter = 0;
	public String[] uris;
	File outputFile = null;
	static final int UPDATE_INTERVAL = 1000;
	private Timer timer = new Timer();

	private final IBinder binder = new MyBinder();
	
	public class MyBinder extends Binder {
		ModelDownloadService getService() {
			return ModelDownloadService.this;
		}
	}	
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		//return null;
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		 Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        /*Object[] objUrls = (Object[]) intent.getExtras().get("URLs");
		Log.d("check", "onStartCommand: "+objUrls.length);
		String[] urls = new String[objUrls.length];
        for (int i=0; i<objUrls.length; i++) {
        	urls[i] = (String) objUrls[i];
			Log.d("check", "onStartCommand: "+urls[i]);
		}*/
    	//new DoBackgroundTask().execute(urls);
	//	new DownloadTask(Utils.modelOldUrl,"xxxx.tflite");
		new DownloadingTask().execute();
		return START_STICKY;
	}	


		
	@Override
    public void onDestroy() {
        super.onDestroy();     
        if (timer != null){
        	timer.cancel();
        }
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }
	
	private int DownloadFile(String url,int i) {
		try {
			//---simulate taking some time to download a file---
			/*outputFile = new File(
					Environment.getExternalStorageDirectory() + "/"
							);*/
			int j=i+1;
			DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
			Uri uri = Uri.parse(url);
			DownloadManager.Request request = new DownloadManager.Request(uri);
			request.setTitle("File"+j);
			request.setDescription("Downloading");
			//request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			//request.setVisibleInDownloadsUi(false);
			request.setDestinationUri(Uri.parse("files://Downloads/"));
			request.setDestinationInExternalFilesDir(getApplicationContext(), String.valueOf(Environment.getExternalStorageDirectory()+"/VChek"),"/File"+(j)+".tflite");
			//request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"/"+"File"+(j)+".tflite");

			downloadmanager.enqueue(request);

			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 100;
	}

	private class DownloadingTask extends AsyncTask<Void, Void, Void> {

		File apkStorage = null;
		File outputFile = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
           /* buttonText.setEnabled(false);
            buttonText.setText(R.string.downloadStarted);*///Set Button Text when download started
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				if (outputFile != null) {
					Toast.makeText(ModelDownloadService.this, "Service Stoped", Toast.LENGTH_LONG).show();
					stopSelf();
                   /* buttonText.setEnabled(true);
                    buttonText.setText(R.string.downloadCompleted);*///If Download completed then change button text
				} else {
					//  buttonText.setText(R.string.downloadFailed);//If download failed change button text
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
                           /* buttonText.setEnabled(true);
                            buttonText.setText(R.string.downloadAgain);*///Change button text again after 3sec
						}
					}, 3000);

					Log.e(TAG, "Download Failed");

				}
			} catch (Exception e) {
				e.printStackTrace();

				//Change button text if exception occurs
				//  buttonText.setText(R.string.downloadFailed);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
                       /* buttonText.setEnabled(true);
                        buttonText.setText(R.string.downloadAgain);*/
					}
				}, 3000);
				Log.e(TAG, "Download Failed with Exception - " + e.getLocalizedMessage());

			}


			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				URL url = new URL("http://jumpstartninja.ai/Sathya/Models/BlueGreen.tflite");//Create Download URl
				HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
				c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
				c.connect();//connect the URL Connection

				//If Connection response is not OK then show Logs
				if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
					Log.e(TAG, "Server returned HTTP " + c.getResponseCode()
							+ " " + c.getResponseMessage());

				}


				//Get File if SD card is present
				if (new CheckForSDCard().isSDCardPresent()) {

					apkStorage = new File(
							Environment.getExternalStorageDirectory() + "/"
									+ Utils.downloadDirectory);
				} else
					 Toast.makeText(getApplicationContext(), "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();

					//If File is not present create directory
					if (!apkStorage.exists()) {
						apkStorage.mkdir();
						Log.e(TAG, "Directory Created.");
					}

				outputFile = new File(apkStorage, "BlueGreen.tflite");//Create Output file in Main File

				//Create New File if not present
				if (!outputFile.exists()) {
					outputFile.createNewFile();
					Log.e(TAG, "File Created");
				}

				FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

				InputStream is = c.getInputStream();//Get InputStream for connection

				byte[] buffer = new byte[1024];//Set buffer type
				int len1 = 0;//init length
				while ((len1 = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len1);//Write new file
				}

				//Close all connection after doing task
				fos.close();
				is.close();

			} catch (Exception e) {

				//Read exception if something went wrong
				e.printStackTrace();
				outputFile = null;
				Log.e(TAG, "Download Error Exception " + e.getMessage());
			}

			return null;
		}
	}

	private class DoBackgroundTask extends AsyncTask<String, Integer, Long> {
        protected Long doInBackground(String... uris) {
            int count = uris.length;
			Log.d("Check", "doInBackground: "+uris.length);
			long totalBytesDownloaded = 0;
            for (int i = 0; i < count; i++) {
                totalBytesDownloaded += DownloadFile(uris[i],i);
                //---calculate precentage downloaded and 
                // report its progress---
                publishProgress((int) (((i+1) / (float) count) * 100));                
            }
            return totalBytesDownloaded;
        }

        protected void onProgressUpdate(Integer... progress) {        	            
        	Log.d("Downloading files", 
        			String.valueOf(progress[0]) + "% downloaded");
        	Toast.makeText(getBaseContext(), 
        			String.valueOf(progress[0]) + "% downloaded", 
        			Toast.LENGTH_LONG).show();
        }

        protected void onPostExecute(Long result) {
        	Toast.makeText(getBaseContext(), 
        			"Downloaded " + result + " bytes", 
        			Toast.LENGTH_LONG).show();
        	stopSelf();
        }        
	}
}