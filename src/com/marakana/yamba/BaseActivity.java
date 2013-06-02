package com.marakana.yamba;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * The base activity with common features shared by TimelineActivity and
 * StatusActivity
 */
public class BaseActivity extends Activity { // 
  YambaApplication yamba; // 
  
  private Intent intent;						// clk
  private PendingIntent pendingIntent;			// clk
  private AlarmManager alarmManager;			// clk

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    yamba = (YambaApplication) getApplication(); // 
    
    // clk: initialize objects needed to set the Alarm that triggers the UpdaterIntentService
    Context context = this;
    intent = new Intent(context, UpdaterIntentService.class);  // 
	pendingIntent = PendingIntent.getService(context, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
  }

  // Called only once first time menu is clicked on
  // for Android 3.0 and higher, called when starting the activity
  @Override
  public boolean onCreateOptionsMenu(Menu menu) { // 
	super.onCreateOptionsMenu(menu); // ref doc says call base implementation
	getMenuInflater().inflate(R.menu.menu, menu);
    return true; // true for the menu to be displayed
  }

  // Called every time user clicks on a menu item
  @Override
  public boolean onOptionsItemSelected(MenuItem item) { // 

    switch (item.getItemId()) {
    case R.id.itemPrefs:
      startActivity(new Intent(this, PrefsActivity.class)
          .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
      break;
    case R.id.itemToggleService:
      if (yamba.isServiceRunning()) {
    	this.mStopIntentService();
    	//startService(new Intent(this, UpdaterIntentService.class)); 
    	//stopService(new Intent(this, UpdaterService.class));
      } else {
    	this.mStartIntentService(this);
    	//startService(new Intent(this, UpdaterIntentService.class));
    	//startService(new Intent(this, UpdaterService.class));
      }
      break;
    case R.id.itemPurge:
      //((YambaApplication) getApplication()).getStatusData().delete();
      yamba.getStatusData().delete();
      Toast.makeText(this, R.string.msgAllDataPurged, Toast.LENGTH_LONG).show();
      break;
    case R.id.itemTimeline:
      startActivity(new Intent(this, TimelineActivity.class)
      	  .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
      	  .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
      	  //);
      // clk: animate
      //this.overridePendingTransition(R.anim.movein, R.anim.moveout);
      break;
    case R.id.itemStatus:
      startActivity(new Intent(this, StatusActivity.class)
          .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
      	  //);
      // clk: animate
      //this.overridePendingTransition(R.anim.movein, R.anim.moveout);
      break;
    }
    return super.onOptionsItemSelected(item); // or: return true;
  }

  // clk: start Alarm for UpdaterIntentService
  private void mStartIntentService(Context context) {
	    // Check if we should do anything at all
	    long interval = ((YambaApplication) context.getApplicationContext())
	        .getInterval(); // 
	    if (interval == YambaApplication.INTERVAL_NEVER) { // 
	    	Log.e("BaseActivity",
	    		  "in mStartIntentService, Forget to reset: INTERVAL_NEVER?"); // clk: don't fail silently
	    	return;
	    }

	    // Create the pending intent
	    //Intent intent = new Intent(context, UpdaterIntentService.class);  // 
	    //PendingIntent pendingIntent = PendingIntent.getService(context, -1, intent,
	    //    PendingIntent.FLAG_UPDATE_CURRENT); // 

	    // Setup alarm service to wake up and start service periodically
	    //AlarmManager alarmManager = (AlarmManager) context
	    //    .getSystemService(Context.ALARM_SERVICE); // 
	    alarmManager.setInexactRepeating(AlarmManager.RTC, System
	        .currentTimeMillis(), interval, pendingIntent); //
	    //alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, System
	    
	    // clk
	    yamba.setServiceRunning(true);
	    Log.d("BaseActivity", "in mStartIntentService, interval="+Long.toString(interval));
  }
  
  // clk: stop Alarm for UpdaterIntentService
  private void mStopIntentService() {
	  alarmManager.cancel(pendingIntent);
	  
	  yamba.setServiceRunning(false);
	  Log.d("BaseActivity", "in mStopIntentService");
  }
  
  // Called every time menu is opened
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
  //public boolean onMenuOpened(int featureId, Menu menu) { // menu in argument is null when running in Nexus4 4.2.2
	super.onPrepareOptionsMenu(menu); // ref doc says call base implementation
    MenuItem toggleItem = menu.findItem(R.id.itemToggleService); // 
    if (yamba.isServiceRunning()) { // 
      toggleItem.setTitle(R.string.titleServiceStop);
      toggleItem.setIcon(android.R.drawable.ic_media_pause);
    } else { // 
      toggleItem.setTitle(R.string.titleServiceStart);
      toggleItem.setIcon(android.R.drawable.ic_media_play);
    }
    return true; // true for the menu to be displayed
  }

}