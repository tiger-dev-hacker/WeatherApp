
package com.example.weatherapp;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private RecyclerView weatherRV;
    private TextView cityNameTV,temperatureTV,conditionTV;
    private TextInputEditText cityEdt;
    private ImageView backIV,iconIV,searchIV;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;

    @Override
    protected void OnCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRVWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idTVIcon);
        searchIV = findViewById(R.id.idTVSearch);
        weatherRVModelArrayList =  new ArrayList<>();
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }
        Location location =  locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude(),location.getLatitude());
        getWeatherInfo(cityName);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityEdt.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter City Name",Toast.LENGTH_SHORT).show();
                } else {
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this,"Permission granted..", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Please Provide the Permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void getWeatherInfo(String cityName){
        String url = "http://api.weatherapi.com/v1/forecast.json?key=65503137e69747d4936232326232308&q=" + cityName + "&days=1&aqi=yes&alerts=yes\n";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(DownloadManager.Request.Method.GET,url,null, new Response.Listener<JSONObject>);
            @Override
                    public void onResponse(JSONObject response){
                        loadingPB.setVisibility(View.GONE);
                        homeRL.setVisibility(View.VISIBLE);
                        weatherRVModelArrayList.clear();

                       try {
                           String temperature = response.getJSONObject("current").getString("temp_c");
                           temperatureTV.setText(temperature+ "°F");
                           int isDay = response.getJSONObject("current").getInt("is_day");
                           String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                           String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                           Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                           conditionTV.set(condition);
                           //morning
                           if(isDay==1)
                           {
                               Picasso.get().load("https://www.bing.com/images/search?view=detailV2&ccid=CKlO3IHA&id=333959737553B514D2655DB42BB0B756B0800765&thid=OIP.CKlO3IHARfLeums4HwgbBAHaEo&mediaurl=https%3a%2f%2fcdn.wallpapersafari.com%2f48%2f46%2fVsM4Gb.jpg&cdnurl=https%3a%2f%2fth.bing.com%2fth%2fid%2fR.08a94edc81c045f2deba6b381f081b04%3frik%3dZQeAsFa3sCu0XQ%26pid%3dImgRaw%26r%3d0&exph=1200&expw=1920&q=bing+open+source+morning+images&simid=608042424791025455&FORM=IRPRST&ck=D3357FB6CFCEAB51EC2EED8CB6FFCB06&selectedIndex=14&ajaxhist=0&ajaxserp=0").into(backIV);
                           }
                           //evening
                           else
                           {
                               Picasso.get().load("https://h2.gifposter.com/bingImages/AugustStargazing_EN-US7610682262_1920x1080.jpg").into(backIV);
                           }

                           JSONObject forecastObj = response.getJSONObject("forecast");
                           JSONObject forcastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                           JSONArray hourArray = forcastO.getJSONArray("hour");

                            for(int i=0; i < hourArray.length();i++) {
                                JSONObject hourObj = hourArray.getJSONObject(i);
                                String time = hourObj.getString("time");
                                String temper = hourObj.getString("temp_c");
                                String img = hourObj.getJSONObject("condition").getString("icon");
                                String wind = hourObj.getString("wind_kph");
                                weatherRVModelArrayList.add(new WeatherRVModel(time, temper, img, wind));
                            }
                       }
            weatherRVAdapter.notifyDataSetChanged();


        }
                       catch(JSONException e){
                           e.printStackTrace();
                       }

        }
    }

    private String getCityName(double longitude,double latitude) {
        String cityName="Not found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);
            for((Address adr : addresses)){
                if(adr!=null){
                    String City = adr.getLocality();
                    if(City!=null && !City.equals())
                    {
                        cityName = City;
                    }else{
                        Log.d("TAG", "CITY NOT FOUND");
                        Toast.makeText(this,"User City Not Found..", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }
}
