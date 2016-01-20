package com.github.skoryupina.filedispatcher;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private static final int PICK_CONTACT_AND_FILE = 1;
    private static final int PICK_CONTACT = 2;
    private static final int PICKFILE_RESULT_CODE = 3;
    private static final int RESULT_ACTION = 4;
    private static final String LOG_TAG = "LOG";

    private static final String schedulerFileName = "fileScheduler.txt";
    public JSONArray fileSchedulerArray;
    public SchedulerItemAdapter adapter;

    public SchedulerItem newFile;
    // private JSONObject JSONnewFile;
    public ArrayList<SchedulerItem> schedulerItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        newFile = new SchedulerItem();
        // JSONnewFile = new JSONObject();

        try {
            fileSchedulerArray = new JSONArray(readFileEntries());
            schedulerItems = SchedulerItem.getListFromJson(fileSchedulerArray);
            // Create the adapter to convert the array to views
            adapter = new SchedulerItemAdapter(MainActivity.this, R.layout.item_scheduler, schedulerItems);


            //updateListView();
        } catch (JSONException e) {
            Log.e(LOG_TAG, "In onCreate(...), while processing JSON-obj " + e.toString());
        }


     /*   try {
          /*  fileSchedulerArray = new JSONArray(readFileEntries());
            schedulerItems = SchedulerItem.getListFromJson(fileSchedulerArray);
            // Create the adapter to convert the array to views
            adapter = new SchedulerItemAdapter(MainActivity.this, R.layout.item_scheduler, schedulerItems);*/
        // setContentView(R.layout.activity_main);
        //updateListView();
    /*    } catch (JSONException e) {
            Log.e(LOG_TAG, "In onCreate(...), while processing JSON-obj " + e.toString());
        }*/

        if (Intent.ACTION_MAIN.equals(action)) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setLogo(R.mipmap.ic_launcher);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            setContentView(R.layout.activity_main);

            ListView listView = (ListView) findViewById(R.id.lvSchedulerItems);
            listView.setAdapter(adapter);
            adapter.setListView(listView);
            //updateListView();
        } else if (Intent.ACTION_SEND.equals(action)) {
            processFilePath(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        try {
            fileSchedulerArray = new JSONArray(readFileEntries());
         /*   schedulerItems = SchedulerItem.getListFromJson(fileSchedulerArray);
            // Create the adapter to convert the array to views
            adapter = new SchedulerItemAdapter(MainActivity.this, R.layout.item_scheduler, schedulerItems);

            ListView listView = (ListView) findViewById(R.id.lvSchedulerItems);
            listView.setAdapter(adapter);
            adapter.setListView(listView);*/

            updateListView();
        } catch (JSONException e) {
            Log.e(LOG_TAG, "In onCreate(...), while processing JSON-obj " + e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_help: {
                showPopupHelp();
                return true;
            }
            case R.id.action_add: {
                openPhoneBook(PICK_CONTACT_AND_FILE);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /***
     * Handle of Actionbar help button with popup menu
     */
    public void showPopupHelp() {
        View menuItemView = findViewById(R.id.action_help);
        PopupMenu popup = new PopupMenu(this, menuItemView);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.global, popup.getMenu());
        popup.show();
    }

    /***
     * Start activity for adding new entry to scheduler
     *
     * @param typeOfResult - shows kind of result expected (contact and file OR only contact)
     */
    public void openPhoneBook(int typeOfResult) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, typeOfResult);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about: {
                new AlertDialog.Builder(this)
                        .setTitle("About")
                        .setMessage(R.string.about_toast)
                        .setPositiveButton("OK", null)
                        .show();
            }
            break;

            case R.id.action_exit: {
                //Ask the user if he want to quit
                createExitDialog();
            }
        }
        return true;
    }

    public void createExitDialog() {
        new AlertDialog.Builder(this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.exit_confirmation)
                .setMessage(R.string.exit_question)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Finish activity
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();

    }

    /***
     * Handles results of called activity work
     *
     * @param reqCode    - Code connected with the launch of particular activity
     * @param resultCode - Code shows whether was everything worked properly or not
     * @param intent     - Received data from other activity
     */
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent intent) {
        // try {
        if (resultCode == Activity.RESULT_OK) {
            super.onActivityResult(reqCode, resultCode, intent);
            switch (reqCode) {
                case PICK_CONTACT_AND_FILE: {
                    processContactData(intent);
                    startSelectingFile();
                }
                break;
                case PICKFILE_RESULT_CODE: {
                    processFilePath(intent);
                }
                break;
                case PICK_CONTACT: {
                    processContactData(intent);
                    adapter.add(newFile);
                    adapter.notifyDataSetChanged();
                    //fileSchedulerArray.put(JSONnewFile);
                    updateFileEntries(false);
                    //updateListView();
                    Toast.makeText(this, "File  added to 4:Parcels.", Toast.LENGTH_SHORT).show();
                    //  removeJSONobjectFields();
                    returnShareResult();
                }
            }
        }
    /*    } catch (JSONException e) {
            Log.e(LOG_TAG, "In onActivityResult(...), while processing JSON-obj " + e.toString());
        }*/
    }

    private void returnShareResult() {
        // Create intent to deliver some kind of result data
        Intent result = new Intent(Intent.ACTION_DEFAULT);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    /***
     * Select phone, name from chosen entry and put them into JSONObject
     *
     * @param intent - Received data from other activity
     */
    private void processContactData(Intent intent) {
        // try {
        Uri contactData = intent.getData();
        Cursor managedQuery = managedQuery(contactData, null, null, null, null);
        if (managedQuery.moveToFirst()) {
            String id = managedQuery.getString(managedQuery.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            String hasPhone = managedQuery.getString(managedQuery.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            if (hasPhone.equalsIgnoreCase("1")) {
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                phones.moveToFirst();
                String cName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                newFile.setContact(cName);
                // JSONnewFile.put("contact", cName);
                String cNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                newFile.setPhone(cNumber);
                //JSONnewFile.put("phone", cNumber);
                phones.close();
            }
            //}
        /*} catch (JSONException e) {
            Log.e(LOG_TAG, "In processContactData(intent), while processing JSON-obj " + e.toString());
        }*/
        }
    }

    /***
     * Start appropriate activity for selecting file
     */
    private void startSelectingFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        //intent.setType("file/*");
        // Ensure that there's an activity to handle the intent.
        //if (intent.resolveActivity(getPackageManager()) == null) return;
        startActivityForResult(intent, PICKFILE_RESULT_CODE);
    }

    /***
     * Select file name from filepath
     *
     * @param intent - Received data from other activity
     */
    private void processFilePath(Intent intent) {
        // try {
        String filePath;
        String fileName = null;
        if (intent.getAction() == null) {
            filePath = intent.getData().toString();
        } else {
            filePath = intent.getParcelableExtra(Intent.EXTRA_STREAM).toString();
        }
        if (filePath != null) {
            int lastSlash = filePath.lastIndexOf("/");
            fileName = filePath.substring(++lastSlash);
            newFile.setFile(fileName);
            //JSONnewFile.put("file", fileName);
            if (intent.getAction() == null)//work from this app
            {
                adapter.add(newFile);
                adapter.notifyDataSetChanged();
                // fileSchedulerArray.put(JSONnewFile);
                updateFileEntries(false);
                updateListView();
                // removeJSONobjectFields();
                Toast.makeText(this, "New file: " + fileName, Toast.LENGTH_SHORT).show();
            } else //ACTION_SEND Intent
            {
                openPhoneBook(PICK_CONTACT);
            }
        }
       /* } catch (JSONException e) {
            Log.e(LOG_TAG, "In processFilePath(intent), while processing JSON-obj " + e.toString());
        }*/
    }

    /***
     * Write current entries to file
     *
     * @param removedEntry - shows whether the swipe action changed the list or not
     */
    public void updateFileEntries(boolean removedEntry) {
        try {
            //if (removedEntry) {
            fileSchedulerArray = SchedulerItem.getJsonFromList(schedulerItems);
            //}
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(schedulerFileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(fileSchedulerArray.toString());
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "File write failed: " + e.toString());
        }
    }

    /***
     * Read content of the scheduler file
     *
     * @return String representation of file content
     */
    private String readFileEntries() {
        String filecontent = "";
        try {
            InputStream inputStream = openFileInput(schedulerFileName);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                filecontent = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(LOG_TAG, "Can not read file: " + e.toString());
        }

        return filecontent;
    }

    /***
     * Update view of the scheduler items list
     */
    public void updateListView() {
        schedulerItems = SchedulerItem.getListFromJson(fileSchedulerArray);
        // Create the adapter to convert the array to views
        adapter = new SchedulerItemAdapter(MainActivity.this, R.layout.item_scheduler, schedulerItems);
        // Attach the adapter to a ListView
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.lvSchedulerItems);
        listView.setAdapter(adapter);
        adapter.setListView(listView);
        Log.e(LOG_TAG, "updateListView with :" + newFile.getContact() + newFile.getFile());
        for (int i = 0; i < adapter.schedulerItems.size(); i++) {
            Log.e(LOG_TAG, "Коллекция: " + adapter.getItem(i).getContact() + adapter.getItem(i).getFile());
        }
    }

    /***
     * Send sms on left-swipe action
     *
     * @param item - item of the list with necessary contact data for SmsManager
     */
    public void sendSMSFileName(SchedulerItem item) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("000110", null, "", null, null); // сервис-гиду Мегафон
        //sms.sendTextMessage(item.phone, null, item.file, null, null);
        Toast.makeText(this, "SMS to " + item.contact + " sent.", Toast.LENGTH_SHORT).show();
    }

   /* private void removeJSONobjectFields()
    {
        JSONnewFile = new JSONObject();
    }*/
}
