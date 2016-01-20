package com.github.skoryupina.filedispatcher;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


/**
 * Created by Ekaterina on 28.10.2015.
 */
public class SchedulerItem {

    public String contact;
    public String phone;
    public String file;

    public SchedulerItem(){}
    public SchedulerItem(JSONObject object) {
        try {
            this.contact = object.getString("contact");
            this.phone = object.getString("phone");
            this.file = object.getString("file");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("contact", contact);
            obj.put("phone", phone);
            obj.put("file",  file);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }


    // Factory method to convert an array of JSON objects into a list of objects
    // User.fromJson(jsonArray);
    public static ArrayList<SchedulerItem> getListFromJson(JSONArray jsonArray) {
        ArrayList<SchedulerItem> schedulerItems = new ArrayList<SchedulerItem>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                schedulerItems.add(new SchedulerItem(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return schedulerItems;
    }

    public static JSONArray getJsonFromList(ArrayList<SchedulerItem> schedulerItems) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < schedulerItems.size(); i++) {
            jsonArray.put(schedulerItems.get(i).getJSONObject());
        }
        return jsonArray;
    }

}