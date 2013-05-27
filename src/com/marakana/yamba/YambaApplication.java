package com.marakana.yamba;

import java.util.List;

import winterwell.jtwitter.Twitter;
import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class YambaApplication extends Application implements OnSharedPreferenceChangeListener {
	private static final String TAG = YambaApplication.class.getSimpleName();
	public Twitter twitter;
	private SharedPreferences prefs;
	
	private boolean serviceRunning = false; // clk
	
	private StatusData statusData; //
	
	public static final String LOCATION_PROVIDER_NONE = "NONE";
	public static final long INTERVAL_NEVER = 0;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Set up Preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    prefs.registerOnSharedPreferenceChangeListener(this);
	    Log.i(TAG, "onCreated");
	    
	    // initialize StatusData (omitted in book)
	    this.statusData = new StatusData(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(TAG, "onTerminated");
	}
	
	
	// implement OnSharedPreferenceChangeListener
	@Override
	public synchronized void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		// invalidate twitter object so getTwitter() will create new one with updated prefs
		this.twitter = null;
	}
	
	
	// getter and setter for serviceRunning
	public boolean isServiceRunning() {
	    return serviceRunning;
	}
	public void setServiceRunning(boolean svcRunning) {
	    this.serviceRunning = svcRunning;
	}
	
	// getter for location provider preference
	public String getProvider() {
	    return prefs.getString("provider", LOCATION_PROVIDER_NONE);
	}
	
	// getter for updater interval preference
	public long getInterval() {
	    // For some reason storing interval as long doesn't work
	    return Long.parseLong(prefs.getString("interval", "0"));
	}
	
	
	// Helper method for returning a twitter object
	public synchronized Twitter getTwitter() {
		if (this.twitter == null) {
			String username = prefs.getString("username", "student");
		    String password = prefs.getString("password", "password");
		    String apiRoot  = prefs.getString("apiRoot",  "http://yamba.marakana.com/api");
		    if ( !TextUtils.isEmpty(username) &&
		    	 !TextUtils.isEmpty(password) &&
		    	 !TextUtils.isEmpty(apiRoot)     ) {
		    	// Connect to twitter.com
		        this.twitter = new Twitter(username, password);
		        this.twitter.setAPIRootUrl(apiRoot);
		    }
		}
		return this.twitter;
	}
	
	
	// clk Helper for returning Default SharedPreferences object; needed in TimelineActivity
	public SharedPreferences getPrefs() {
		return prefs;
	}
	
	
	public StatusData getStatusData() { // 
		  return statusData;
	}

	// Connects to the online service and puts the latest statuses into DB.
	// Returns the count of new statuses
	public synchronized int fetchStatusUpdates() {  // 
		  Log.d(TAG, "Fetching status updates");
		  this.twitter = this.getTwitter();
		  //Twitter twitter = this.getTwitter();
		  if (twitter == null) {
		    Log.d(TAG, "Twitter connection info not initialized");
		    return 0;
		  }
		  try {
			//List<Status> statusUpdates = twitter.getFriendsTimeline();
		    //long latestStatusCreatedAtTime = this.getStatusData()
		    List<Twitter.Status> statusUpdates = twitter.getFriendsTimeline();
		    long latestStatusCreatedAtTime = this.statusData
		        .getLatestStatusCreatedAtTime();
		    int count = 0;
		    ContentValues values = new ContentValues();
		    /*
		    for (Status status : statusUpdates) {
		      values.put(StatusData.C_ID, status.getId());
		      long createdAt = status.getCreatedAt().getTime();
		      values.put(StatusData.C_CREATED_AT, createdAt);
		      values.put(StatusData.C_TEXT, status.getText());
		      values.put(StatusData.C_USER, status.getUser().getName());
		    */		    
		    for (Twitter.Status status : statusUpdates) {
		      values.put(StatusData.C_ID, status.id);
		      long createdAt = status.createdAt.getTime();
		      values.put(StatusData.C_CREATED_AT, createdAt);
		      values.put(StatusData.C_TEXT, status.text);
		      values.put(StatusData.C_USER,	status.user.getName());
		      Log.d(TAG, "Got update with id " + status.getId() + ". Saving");
		      this.statusData.insertOrIgnore(values); //this.getStatusData().insertOrIgnore(values);
		      if (latestStatusCreatedAtTime < createdAt) {
		        count++;
		      }
		    }
		    Log.d(TAG, count > 0 ? "Got " + count + " status updates"
		        : "No new status updates");
		    return count;
		  } catch (RuntimeException excp) {
		    Log.e(TAG, "Failed to fetch status updates", excp);
		    return 0;
		  }
	}	
}
