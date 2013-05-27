package com.marakana.yamba;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UpdaterService extends Service {
	static final String TAG = UpdaterService.class.getSimpleName(); // "UpdaterService"
	
	static final int DELAY = 30000; // milliseconds 
	private boolean runFlag = false;
	private Updater updater;
	private YambaApplication yamba; // a helper object to call helper methods in YambaApplication
	
	public static final String NEW_STATUS_INTENT = "com.marakana.yamba.NEW_STATUS"; // used in TimelineActivity
	public static final String NEW_STATUS_EXTRA_COUNT = "NEW_STATUS_EXTRA_COUNT";   // key for # status updates
	
	//DbHelper dbHelper;
	//SQLiteDatabase db;
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null; // used in bound services to return the actual implementation of a binder; not bound service
	}

	@Override
	public void onCreate() {
		// do work that needs to be done only once; not called for subsequent startService() calls
		super.onCreate();
		
		this.updater = new Updater();
		this.yamba = (YambaApplication) getApplication();
		
		//dbHelper = new DbHelper(this);
		
		Log.d(TAG, "onCreated");
	}

	@Override
	public void onDestroy() {
		// any clean up of things initialized in onCreate(); called just before service destroyed by stopService()
		super.onDestroy();
		
		this.runFlag = false;
		this.updater.interrupt();
	    this.updater = null;
	    this.yamba.setServiceRunning(false); 
		
		Log.d(TAG, "onDestroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// called each time receive a startService() intent; running service could get multiple onStartCommand()
		super.onStartCommand(intent, flags, startId);
		
		if (!runFlag) {	// swCL: otherwise Exception thrown when NetworkReceiver calls startService(); in downloaded code, not in book
			this.runFlag = true;
			this.updater.start();
			this.yamba.setServiceRunning(true);
		
			Log.d(TAG, "onStarted");
		}
		return Service.START_STICKY;
	}

	
	/**
	 * Thread that performs the actual update from the online service
	 */
	private class Updater extends Thread {
		static final String RECEIVE_TIMELINE_NOTIFICATIONS = "com.marakana.yamba.RECEIVE_TIMELINE_NOTIFICATIONS";

		Intent intent;
		//List<Twitter.Status> timeline;

		
	    public Updater() {
	    	super("UpdaterService-Updater");
	    }
	    
	    
	    @Override
	    public void run() { // 
	    	UpdaterService updaterService = UpdaterService.this;
	    	while (updaterService.runFlag) {
	    	    /*
	    		Log.d(TAG, "Updater running");
	    		try {
	    			// Get the timeline from the cloud
	    	        try {
	    	            timeline = yamba.getTwitter().getFriendsTimeline(); // 
	    	        } catch (TwitterException excp) {
	    	            Log.e(TAG, "Failed to connect to twitter service", excp);
	    	        }
	    	        
	    	        // Open the database for writing
	    	        db = dbHelper.getWritableDatabase(); 
	    	        
	    	        // Loop over the timeline and print it out
	    	        ContentValues values = new ContentValues();
	    	        for (Twitter.Status status : timeline) {
	    	        	// Insert into database
	    	            values.clear();
	    	            values.put(DbHelper.C_ID, status.id);
	    	            values.put(DbHelper.C_CREATED_AT, status.createdAt.getTime());
	    	            values.put(DbHelper.C_SOURCE, status.source);
	    	            values.put(DbHelper.C_TEXT, status.text);
	    	            values.put(DbHelper.C_USER, status.user.name);
	    	            try {
							db.insertOrThrow(DbHelper.TABLE, null, values);
							
							Log.d(TAG, String.format("%s: %s", status.user.name, status.text));
						} catch (SQLException e) {
							// ignore exception; likely duplicates
						}
	    	        }
	    	        
	    	        // Close the database
	    	        db.close(); 
	    	        
	    	        Log.d(TAG, "Updater ran");
	    	    */
	    		Log.d(TAG, "Running background thread");
	    		try {
	    	        //YambaApplication yamba = (YambaApplication) updaterService
	    	        //    . getApplication();  // clk: already initialized in onCreate()
	    	        int newUpdates = yamba.fetchStatusUpdates(); // 
	    	        if (newUpdates > 0) { // 
	    	          Log. d(TAG, "We have a new status");
	    	          intent = new Intent(NEW_STATUS_INTENT); // 
	    	          intent.putExtra(NEW_STATUS_EXTRA_COUNT, newUpdates); // 
	    	          updaterService.sendBroadcast(intent, RECEIVE_TIMELINE_NOTIFICATIONS); //
	    	        }
	    	        //Log.d( TAG, "runFlag value is "+Boolean.toString(updaterService.runFlag) ); // clk: debugging
					Thread.sleep(DELAY);
	    		} catch (InterruptedException e) {
	    			updaterService.runFlag = false;
	    			//Log.d(TAG, "Background thread InterruptedException thrown"); // clk: debug service stop after bootup
	    		}
	    	}
	    }
	}
	
}
