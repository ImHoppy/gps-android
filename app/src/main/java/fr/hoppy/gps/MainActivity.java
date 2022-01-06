package fr.hoppy.gps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
	RequestQueue requestQueue;

	private final Handler handler = new Handler();
	private Boolean switchOn = false;
	private GPSTracker gps;

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			gps.getLocation();
//			Log.i("gps", String.valueOf(gps.getLatitude()) + " " + String.valueOf(gps.getLongitude()));
			SendData(gps.getLatitude(), gps.getLongitude());
			if (switchOn)
				handler.postDelayed(this, 2000); // 2000 = 2 seconds. This time is in millis.
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Location current = new Location("");
		current.setLatitude(23.9569596);
		current.setLongitude(12.567567);

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		gps = new GPSTracker(this);

		@SuppressLint("UseSwitchCompatOrMaterialCode")
		Switch switch1 = findViewById(R.id.send_data);
		switch1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean checked = ((Switch) v).isChecked();
				TextView text = findViewById(R.id.hello);
				TextView debug = findViewById(R.id.debug);
				if (checked){
					text.setText("ON");
					switchOn = true;
					handler.postDelayed(runnable, 2000);

				}
				else{
					text.setText("OFF");
					switchOn = false;
				}
			}
		});
	}
	public void SendData(double latitude, double longitude) {
		String url = "http://193.26.14.111:8080/gps/post";
		TextView debug = findViewById(R.id.debug);
		@SuppressLint("SetTextI18n")
		StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
				response -> debug.setText("Success"),
				error -> {
					debug.setText("Error");
					Log.e("gps", String.valueOf(error));
				})
		{
			@Override
			public byte[] getBody() throws AuthFailureError {
				HashMap<String, Double> params2 = new HashMap<String, Double>();
				params2.put("latitude", latitude);
				params2.put("longitude", longitude);
				return new JSONObject(params2).toString().getBytes();
			}

			@Override
			public String getBodyContentType() {
				return "application/json";
			}
		};
		requestQueue = Volley.newRequestQueue(MainActivity.this);
		requestQueue.add(stringRequest);
	}
}