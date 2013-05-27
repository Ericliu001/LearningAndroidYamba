package com.marakana.yamba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class TimelineActivity extends BaseActivity { //
	  static final String SEND_TIMELINE_NOTIFICATIONS = "com.marakana.yamba.SEND_TIMELINE_NOTIFICATIONS";
	
	  //DbHelper dbHelper;
	  //SQLiteDatabase db;
	  Cursor cursor;
	  //TextView textTimeline;
	  ListView listTimeline;  // 
	  SimpleCursorAdapter adapter;  // 
	  static final String[] FROM = { StatusData.C_CREATED_AT, StatusData.C_USER,
		    StatusData.C_TEXT };
	  //static final String[] FROM = { DbHelper.C_CREATED_AT, DbHelper.C_USER,
	  //    DbHelper.C_TEXT };  // 
	  static final int[] TO = { R.id.textCreatedAt, R.id.textUser, R.id.textText }; // 
	  TimelineReceiver receiver;
	  IntentFilter filter;

	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.timeline);

	    // Check whether preferences have been set
	    if (yamba.getPrefs().getString("username", null) == null) { //
	      startActivity(new Intent(this, PrefsActivity.class)); // 
	      Toast.makeText(this, R.string.msgSetupPrefs, Toast.LENGTH_LONG).show(); // 
	    }
	    
	    // Find your views
	    listTimeline = (ListView) findViewById(R.id.listTimeline);  //
	    //textTimeline = (TextView) findViewById(R.id.textTimeline);
	    
	    // Create the receiver
	    receiver = new TimelineReceiver();
	    filter = new IntentFilter( UpdaterIntentService.NEW_STATUS_INTENT );
	    
	    // Connect to database
	    //dbHelper = new DbHelper(this);  // 
	    //db = dbHelper.getReadableDatabase();  // 
	  }

	  @Override
	  public void onDestroy() {
	    super.onDestroy();

	    // Close the database
	    yamba.getStatusData().close(); // clk TODO: closes dbHelper, created in YambaApplication; db object closed?
	    //db.close(); // 
	  }

	  @Override
	  protected void onResume() {
	    super.onResume();
	    
	    // Setup List
	    this.setupList();

	    // Register the receiver
	    registerReceiver(receiver, filter, SEND_TIMELINE_NOTIFICATIONS, null);   //
	    
	    /* done by setupList() method
	    // Get the data from the database
	    cursor = db.query(DbHelper.TABLE, null, null, null, null, null,
	        DbHelper.C_CREATED_AT + " DESC"); // 
	    startManagingCursor(cursor);  // 

	    // Set up the adapter
	    adapter = new SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO);  // 
	    
	    adapter. setViewBinder(VIEW_BINDER); //
	    
	    listTimeline.setAdapter(adapter); // 
	    */
	    
	    /*
	    // Iterate over all the data and print it out
	    String user, text, output;
	    while (cursor.moveToNext()) {  // 
	      user = cursor.getString(cursor.getColumnIndex(DbHelper.C_USER));  // 
	      text = cursor.getString(cursor.getColumnIndex(DbHelper.C_TEXT));
	      output = String.format("%s: %s\n", user, text); // 
	      textTimeline.append(output); // 
	    }
	    */
	  }
	  
	  @Override
	  protected void onPause() {
		super.onPause();
		
		// UNregister the receiver
		unregisterReceiver(receiver);  //
	  }

	  // Responsible for fetching data and setting up the list and the adapter
	  private void setupList() { // 
	    // Get the data
	    cursor = yamba.getStatusData().getStatusUpdates();
	    startManagingCursor(cursor); // clk: deprecated, use CursorLoader instead
	    	// see android.app.LoaderManager reference
	    	// and use ‘standard constructor’ signature for SimpleCursorAdapter()
	    // Setup Adapter
	    // 	need sdk 11 to use new signature SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO, 0)
	    adapter = new SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO); // deprecated in sdk 11
	    adapter.setViewBinder(VIEW_BINDER); // 
	    listTimeline.setAdapter(adapter);
	  }
	  
	  // View binder constant to inject business logic that converts a timestamp to
	  // relative time
	  static final ViewBinder VIEW_BINDER = new ViewBinder() { // 

	    public boolean setViewValue(View view, Cursor cursor, int columnIndex) { // 
	      if (view.getId() != R.id.textCreatedAt)
	        return false; // 

	      // Update the created at text to relative time
	      long timestamp = cursor.getLong(columnIndex); // 
	      //CharSequence relTime = DateUtils.getRelativeTimeSpanString(view
	      //    .getContext(), timestamp); // 
	      CharSequence relTime = DateUtils.getRelativeTimeSpanString(timestamp);
	      ((TextView) view).setText(relTime); // 

	      return true; // 
	    }

	  };
	  
	  // Receiver to wake up when UpdaterIntentService gets new status
	  class TimelineReceiver extends BroadcastReceiver { // 
		  @Override
		  public void onReceive(Context context, Intent intent) { // 
			cursor = yamba.getStatusData().getStatusUpdates();
		    adapter.changeCursor(cursor);
		    //cursor.requery(); // deprecated
			adapter.notifyDataSetChanged(); // 
		    Log.d("TimelineReceiver", "onReceived");
		  }
	  }
}

