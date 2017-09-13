/*
By Shaun Zia
Completed October 12, 2015
For Stradigi Inc.
Define a GridView Object for displaying user thumbnails
 */

package com.tag.instagramdemo.example;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.tag.instagramdemo.R;

public class AllMediaFiles extends Activity {
	private GridView gvAllImages;
	private HashMap<String, String> userInfo;
	private ArrayList<String> imageThumbList = new ArrayList<String>();
	private Context context;
	private int WHAT_FINALIZE = 0;
	private static int WHAT_ERROR = 1;
	private ProgressDialog pd;
	private Button btnHomeScreen;

	public static final String TAG_DATA = "data";
	public static final String TAG_IMAGES = "images";
	public static final String TAG_THUMBNAIL = "thumbnail";
	public static final String TAG_URL = "url";

	private Handler handler = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (pd != null && pd.isShowing())
				pd.dismiss();
			if (msg.what == WHAT_FINALIZE) {
				setImageGridAdapter();
			} else {
				Toast.makeText(context, "Check your network.",
						Toast.LENGTH_SHORT).show();
			}
			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.all_media_list_files);

		// all_media_list_files.xml
		gvAllImages = (GridView) findViewById(R.id.gvAllImages);
		userInfo = (HashMap<String, String>) getIntent().getSerializableExtra("userInfo");
		context = AllMediaFiles.this;
		getAllMediaImages();

		// Return to home screen
		btnHomeScreen = (Button) findViewById(R.id.btnHomeScreen);
		btnHomeScreen.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Intent homeScreen = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(homeScreen);
			}
		});
	}

	private void setImageGridAdapter() {
		gvAllImages.setAdapter(new MyGridListAdapter(context,imageThumbList));
	}

	private void getAllMediaImages() {
		pd = ProgressDialog.show(context, "", "Loading images...");
		new Thread(new Runnable() {

			@Override
			public void run() {
				int what = WHAT_FINALIZE;
				try {
					JSONParser jsonParser = new JSONParser();
					JSONObject jsonObject = jsonParser
							.getJSONFromUrlByGet("https://api.instagram.com/v1/users/"
									+ userInfo.get(InstagramApp.TAG_ID)
									+ "/media/recent/?client_id="
									+ ApplicationData.CLIENT_ID
									+ "&count="
									+ userInfo.get(InstagramApp.TAG_COUNTS));
					JSONArray data = jsonObject.getJSONArray(TAG_DATA);

					for (int data_i = 0; data_i < data.length(); data_i++) {
						JSONObject data_obj = data.getJSONObject(data_i);
						JSONObject images_obj = data_obj.getJSONObject(TAG_IMAGES);
						JSONObject thumbnail_obj = images_obj.getJSONObject(TAG_THUMBNAIL);
						String str_url = thumbnail_obj.getString(TAG_URL);
						imageThumbList.add(str_url);
					}
					System.out.println("jsonObject::" + jsonObject);

				} catch (Exception exception) {
					exception.printStackTrace();
					what = WHAT_ERROR;
				}
				handler.sendEmptyMessage(what);
			}
		}).start();
	}
}
