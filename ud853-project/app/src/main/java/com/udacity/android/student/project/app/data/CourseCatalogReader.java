package com.udacity.android.student.project.app.data;

import android.content.ContentValues;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.udacity.android.student.project.app.data.CourseTable.CourseTableColumns.TYPE;
import static com.udacity.android.student.project.app.data.CourseTable.CourseTableColumns.CODE;
import static com.udacity.android.student.project.app.data.CourseTable.CourseTableColumns.HOMEPAGE;
import static com.udacity.android.student.project.app.data.CourseTable.CourseTableColumns.PROVIDER;
import static com.udacity.android.student.project.app.data.CourseTable.CourseTableColumns.SUBTITLE;
import static com.udacity.android.student.project.app.data.CourseTable.CourseTableColumns.TITLE;
import static com.udacity.android.student.project.app.data.CourseTable.CourseTableColumns.LEVEL;
import static com.udacity.android.student.project.app.data.CourseTable.CourseTableColumns.SYLLABUS;
import static com.udacity.android.student.project.app.data.CourseTable.CourseTableColumns.SHORT_SUMMARY;
import static com.udacity.android.student.project.app.util.Util.close;

public class CourseCatalogReader {

	private final String LOG_TAG = getClass().getSimpleName();

	private static final String UDACITY_COURSE_LIST_HTTP_URL = "https://www.udacity.com/public-api/v0/courses";

	private static final Map<DatabaseField, String> fieldMap=new LinkedHashMap<>();

	static
	{
		fieldMap.put(CODE, "key");
		fieldMap.put(TITLE, "title");
		fieldMap.put(SUBTITLE, "subtitle");
		fieldMap.put(LEVEL, "level");
		fieldMap.put(HOMEPAGE, "homepage");
		fieldMap.put(SHORT_SUMMARY, "short_summary");
		fieldMap.put(SYLLABUS, "syllabus");
	}

	public List<ContentValues> read() {

		File directory=new File(System.getProperty("user.dir"));

		log("read");

		//these are logged for use during future development
		log("USER.DIRECTORY "+directory.getAbsolutePath());
		log("getExternalStorageDirectory "+Environment.getExternalStorageDirectory().getAbsolutePath());
		log("getDataDirectory "+Environment.getDataDirectory().getAbsolutePath());
		log("getDownloadCacheDirectory "+Environment.getDownloadCacheDirectory().getAbsolutePath());

	//offline development use...
	File jsonFile=new File(directory,"sdcard/Download/courses.json");

	if(jsonFile.isFile() && jsonFile.length() > 0) {

		return read(jsonFile);
	}

		return read(UDACITY_COURSE_LIST_HTTP_URL);

	}



	private List<ContentValues> read(File file) {
		List<ContentValues> courseList = null;

		log("read "+file.getAbsolutePath()+ " file: "+file.isFile());

		try {
			if(file.isFile() && file.length() > 0) {
				courseList=read(file.toURI().toURL());
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Exception", e);
			e.printStackTrace();
		}

		return courseList != null ? courseList : new ArrayList<ContentValues>();
	}

	private List<ContentValues> read(String urlText) {
		List<ContentValues> courseList = null;

		try {
				courseList=read(new URL(urlText));
		} catch (Exception e) {
			Log.e(LOG_TAG, "Exception", e);
			e.printStackTrace();
		}

		return courseList != null ? courseList : new ArrayList<ContentValues>();
	}

	private List<ContentValues> read(URL url) {
		List<ContentValues> courseList = null;

		URLConnection urlConnection = null;

		log("read "+url.toString());


		try {


			urlConnection = url.openConnection();

			if(urlConnection instanceof HttpURLConnection) {
				((HttpURLConnection)urlConnection).setRequestMethod("GET");
			}

			urlConnection.connect();
			courseList = read(urlConnection.getInputStream());
		} catch (Exception e) {
			Log.e(LOG_TAG, "Exception", e);
			e.printStackTrace();
		} finally {

			if(urlConnection instanceof HttpURLConnection) {
				close((HttpURLConnection)urlConnection);
			}

		}

		return courseList != null ? courseList : new ArrayList<ContentValues>();
	}

	private List<ContentValues> read(InputStream inputStream) {
		List<ContentValues> courseList = new ArrayList<>();

		log("read "+inputStream);
		BufferedReader reader = null;

		try {
			// String name="Developing Android Apps Subtitles.zip";
			// String baseURL="http://zips.udacity-data.com/ud853/";

			if (inputStream != null) {
				reader = new BufferedReader(new InputStreamReader(inputStream));

				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}

				if (sb.length() > 0) {
					process(sb.toString(), courseList);
				} else {
					log("NO DATA FROM INPUT STREAM");
				}

			} else {
				log("INPUT STREAM IS NULL");
			}

		} catch (IOException e) {
			error("Error ", e);
		} finally {
			close(reader);
			close(inputStream);
		}

		return courseList;
	}

	public void process(String jsonString, List<ContentValues> courseList) {

		try {
			JSONObject object = new JSONObject(jsonString);

			JSONArray courses = object.getJSONArray("courses");

			log("Found " + courses.length() + " course objects");

			for (int i = 0; i < courses.length(); i++) {
				ContentValues map = new ContentValues();
				map.put(PROVIDER.getFieldName(), "udacity");
				map.put(TYPE.getFieldName(), "course");

				JSONObject course = courses.getJSONObject(i);

				for(Map.Entry<DatabaseField, String> entry : fieldMap.entrySet()) {
					String value=course.getString(entry.getValue());
					map.put(entry.getKey().getFieldName(), value != null ? value : "");
				}


				//shorten course homepage url...
				try {
					String homepage = map.getAsString(HOMEPAGE.getFieldName());
					int c = homepage.indexOf('?');
					if (c != -1) {
						map.put(HOMEPAGE.getFieldName(), homepage.substring(0, c));
					}

				}catch(Exception e)
				{

				}

				courseList.add(map);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void log(String message) {
		Log.i(LOG_TAG, message);
	}



	private void error(String message, Exception e) {
		Log.e(LOG_TAG, message + e.getMessage());
		e.printStackTrace();
	}

}
