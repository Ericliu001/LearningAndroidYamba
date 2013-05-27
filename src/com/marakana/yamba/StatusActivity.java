package com.marakana.yamba;

import java.util.Locale;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StatusActivity extends BaseActivity
							implements OnClickListener, TextWatcher,
									   //OnSharedPreferenceChangeListener,
									   LocationListener,
									   OnInitListener,OnUtteranceCompletedListener	// clk: TextToSpeech
							{ 
	private static final String TAG = StatusActivity.class.getSimpleName(); // "StatusActivity"
	
	//SharedPreferences prefs;
	
	private static final long LOCATION_MIN_TIME = 10000;							// clk: debug
	//private static final long LOCATION_MIN_TIME = 3600000; // One hour
	private static final float LOCATION_MIN_DISTANCE = 1000; // One kilometer
	
	private static final int	M_CHECK_TTS_ACTIVITY_ID = 20110514;					// clk: TextToSpeech
	
	EditText editText;
	Button updateButton;
	//Twitter twitter;
	TextView textCount;
	
	LocationManager locationManager; // 
	Location location;
	String provider;
	
	TextToSpeech ttsEngine;															// clk: TextToSpeech
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super. onCreate(savedInstanceState);
	    
	    /* moved to YambaApplication class
	    // Setup Preferences
	    prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    prefs.registerOnSharedPreferenceChangeListener(this);
	    */
	    
	    setContentView(R.layout.status);
	    // Find views
	    editText = (EditText) findViewById(R.id.editText);
	    updateButton = (Button) findViewById(R.id.buttonUpdate);
	    updateButton.setOnClickListener(this);
	    
	    textCount = (TextView) findViewById(R.id.textCount);
	    textCount.setText(Integer.toString(140));
	    textCount.setTextColor(Color.GREEN);
	    
	    editText.addTextChangedListener(this);
	    
	    // check for the presence of Text-To-Speech TTS resources					// clk: TextToSpeech
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, M_CHECK_TTS_ACTIVITY_ID);
	    
	    //twitter = new Twitter("student", "password"); // 
	    //twitter. setAPIRootUrl("http://yamba.marakana.com/api");
	}
	
	@Override
	protected void onResume() {
	    super. onResume();
	    // Setup location information
	    provider = yamba.getProvider(); //	clk: default in method is LOCATION_PROVIDER_NONE
	    if (! YambaApplication.LOCATION_PROVIDER_NONE.equals(provider)) { // 
	      locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); // 
	      Log.d(TAG, "onResume, provider is "+provider);							// clk: debug
	    } else {
	      Log.e(TAG, "onResume, provider is LOCATION_PROVIDER_NONE");				// clk: debug
	    }
	    if (locationManager != null) {
	      Log.d(TAG, "onResume, locationManager is "+locationManager.toString());	// clk: debug
	      location = locationManager.getLastKnownLocation(provider); // 
	      locationManager.requestLocationUpdates(provider, LOCATION_MIN_TIME,
	          LOCATION_MIN_DISTANCE, this); // 
	    }
	    
	    String loglocation;															// clk: debug
	    if (location == null) {														// clk: debug
	    	loglocation = "snap! isnull";
	    } else {
	    	loglocation = location.toString();
	    }
	    Log.d(TAG, "onResume, location is "+loglocation);							// clk: debug
	}
	
	@Override
	protected void onPause() {
	    super. onPause();
	    if (locationManager != null) {
	      locationManager.removeUpdates(this);  // 
	    }
	}
	
	@Override
    public void onDestroy() {
    	super.onDestroy();
    	ttsEngine.shutdown();														// clk: TextToSpeech
    }
	
	
	// Asynchronously posts to twitter
	class PostToTwitter extends AsyncTask<String, Integer, String> { // 
	    // Called to initiate the background activity
	    @Override
	    protected String doInBackground(String... statuses) { // 
	      try {
	    	// Check if we have the location
	        if (location != null) { //
	        	double latlong[] = {location.getLatitude(), location.getLongitude()};
	            yamba.getTwitter().setMyLocation(latlong);
	        }
	    	Twitter.Status status = yamba.getTwitter().updateStatus(statuses[0]);  
	        return status.text;
	      } catch (TwitterException excp) {
	        Log.e(TAG, excp.toString());
	        //excp.printStackTrace();
	        return "Failed to post";
	      }
	    }

	    // Called when there's a progress display to be updated
	    @Override
	    protected void onProgressUpdate(Integer... values) { 
	      super.onProgressUpdate(values);
	      // Not used in this case
	    }

	    // Called once the background activity has completed
	    @Override
	    protected void onPostExecute(String result) {
	      Toast.makeText(StatusActivity.this, result, Toast.LENGTH_LONG).show();
	      ttsEngine.speak(result, TextToSpeech.QUEUE_FLUSH, null);
	    }
	}
	
	/* moved to YambaApplication class
	// Helper method for returning a twitter object, used by PostToTwitter
	private Twitter getTwitter() {
		if (twitter == null) {  
		    String username, password, apiRoot;
		    username = prefs.getString("username", "student");     
		    password = prefs.getString("password", "password");
		    apiRoot  = prefs.getString("apiroot",  "");
		    // Connect to twitter.com
		    twitter = new Twitter(username, password);      
		    twitter.setAPIRootUrl(apiRoot); 
		}
		return twitter;
	}*/
	
	
	// Called when button is clicked
	@Override
	public void onClick(View v) {
	    String status = editText.getText().toString();
	    new PostToTwitter().execute(status); //twitter. setStatus(editText.getText().toString());
	    Log.d(TAG, "onClicked");
	}
	
	
	// TextWatcher methods
	@Override
	public void afterTextChanged(Editable s) {
		int count = 140 - editText.length(); // 
	    textCount.setText(Integer.toString(count));
	    textCount.setTextColor(Color.GREEN); // 
	    if (count < 10)
	      textCount.setTextColor(Color.YELLOW);
	    if (count < 0)
	      textCount.setTextColor(Color.RED);
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
	}
	
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	
	// LocationListener methods
	public void onLocationChanged(Location location) { // 
	    this.location = location;
	    Log.d(TAG, "onloacationChanged "+location.toString());						// clk: debug
	}
	
	public void onProviderDisabled(String provider) { // 
	    if (this.provider.equals(provider))
	      locationManager.removeUpdates(this);
	}
	
	public void onProviderEnabled(String provider) { // 
	    if (this.provider.equals(provider))
	      locationManager.requestLocationUpdates(this.provider, LOCATION_MIN_TIME,
	    		  								 LOCATION_MIN_DISTANCE, this);
	    Log.d(TAG, "onProviderEnabled, provider is "+provider);						// clk: debug
	}
	
	public void onStatusChanged(String provider, int status, Bundle extras) { // 
	}
	
	
	/**
     *  event handler on return of Activity checking presence of TTS resources		
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {	// clk: TextToSpeech
        if (requestCode == M_CHECK_TTS_ACTIVITY_ID) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                ttsEngine = new TextToSpeech(this, this);
            } else {
                // missing resources, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                    TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    
    /**
     *  Called when the TTS engine is fully loaded; can start configuring it
     */
	@Override
	public void onInit(int status) {												// clk: TextToSpeech
		ttsEngine.setLanguage(Locale.US);
		ttsEngine.setOnUtteranceCompletedListener(this);
	}
	
	/**
	 *  Called when TTS engine has finished utterance 
	 */
	@Override
	public void onUtteranceCompleted(String arg0) {									// clk: TextToSpeech
		// TODO Auto-generated method stub
		
	}
	
	
	/* moved to BaseActivity
	// Called first time user clicks on the menu button
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflator = getMenuInflater();
		inflator.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu); // better than: return true;
	}

	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemServiceStart:
			startService(new Intent(this, UpdaterService.class));
			break;
		case R.id.itemServiceStop:
			stopService(new Intent(this, UpdaterService.class));
			break;
		case R.id.itemPrefs:
			startActivity(new Intent(this, PrefsActivity.class));
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	*/

	/* moved to YambaApplication class
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		// invalidate twitter object so getTwitter() will create new one with updated prefs
        twitter = null;
	}*/

}