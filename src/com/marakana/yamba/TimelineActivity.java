package com.marakana.yamba;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

public class TimelineActivity extends BaseActivity
                              implements LoaderManager.LoaderCallbacks<Cursor> { //
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
	  TimelineActivity timelineActivity = this;

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

	    // Initialize loader cursor
	    //  set up adapter for listview first
	    this.setupList();
	    getLoaderManager().initLoader(1, null, this); //must be prepared to handle onLoadFinished() loadercallback per the reference doc

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
	    //this.setupList();

	    // clk: If new update came in while receiver was unregistered, e.g. if still on Status Update screen
	    //  restartLoader() would not called in onReceived(). So also need to load any fresh data here.
	    getLoaderManager().restartLoader(1, null, this);

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
	    /*  use LoaderManager and CursorLoader instead
		// Get the data
	    //cursor = yamba.getStatusData().getStatusUpdates();
	    //startManagingCursor(cursor); // clk: deprecated, use CursorLoader instead
	    */
	    // Setup Adapter
		adapter = new SimpleCursorAdapter(this, R.layout.row, null, FROM, TO, 0);
		//adapter = new SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO); //deprecated in sdk 11
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
			getLoaderManager().restartLoader(1, null, timelineActivity);
			/* use above loader manager to manage a cursor loader instead
			cursor = yamba.getStatusData().getStatusUpdates();
		    adapter.changeCursor(cursor);
		    //cursor.requery(); // deprecated
			adapter.notifyDataSetChanged(); //
			*/
			Log.d("TimelineReceiver", "onReceived");
		  }
	  }

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created.
		// We have only one Loader, so we don't care about the ID.
		// First, pick the base URI to use
		Uri baseUri = StatusProvider.CONTENT_URI;

		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new CursorLoader(this, baseUri,
				null, null, null,
				StatusData.C_CREATED_AT + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		adapter.swapCursor(null); // swap in a null cursor to release the old cursor
	}
}

