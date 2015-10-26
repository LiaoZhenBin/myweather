package cn.liao.myweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.liao.myweather.R;
import cn.liao.myweather.util.HttpCallbackListener;
import cn.liao.myweather.util.HttpUtil;
import cn.liao.myweather.util.Utility;

public class WeatherActivity extends Activity implements OnClickListener{

	private LinearLayout weather_info_layout;

	// 城市名
	private TextView city_name;
	// 发布时间
	private static TextView publish_text;
	// 天气描述信息
	private TextView weather_desp;
	// 最低气温
	private TextView temp1;
	// 最高气温
	private TextView temp2;
	// 当前日期
	private TextView current_date;
	
	private Button bt_select_city;
	private Button bt_refresh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		weather_info_layout = (LinearLayout) findViewById(R.id.weather_info_layout);
		city_name = (TextView) findViewById(R.id.city_name);
		publish_text = (TextView) findViewById(R.id.publish_text);
		weather_desp = (TextView) findViewById(R.id.weather_desp);
		temp1 = (TextView) findViewById(R.id.temp1);
		temp2 = (TextView) findViewById(R.id.temp2);
		current_date = (TextView) findViewById(R.id.current_date);
		bt_select_city = (Button) findViewById(R.id.bt_select_city);
		bt_refresh = (Button) findViewById(R.id.bt_refresh);
		bt_select_city.setOnClickListener(this);
		bt_refresh.setOnClickListener(this);
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			// 有县级代号就去查天气
			publish_text.setText("同步中...");
			weather_info_layout.setVisibility(View.INVISIBLE);
			city_name.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			// 如果没有县级代号就直接显示本地天气
			showWeather();
		}
	}

	/**
	 * 查询县级代号所对应的天气代号。
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}

	/**
	 * 查询天气代号所对应的天气。
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}

	/**
	 * 根据传入的地址和类型向服务器查询天气代号或者天气信息
	 */
	private void queryFromServer(String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						// 从服务器返回的数据中解析出天气代号
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					// 处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherActivity.this,
							response);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						publish_text.setText("同步失败");
					}
				});
			}
		});

	}

	/**
	 * 从SharedPreferences文件中读取存储的天气信息，并显示到页面上
	 */
	private void showWeather() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		city_name.setText(preferences.getString("city_name", ""));
		temp1.setText(preferences.getString("temp1", ""));
		temp2.setText(preferences.getString("temp2", ""));
		weather_desp.setText(preferences.getString("weather_desp", ""));
		publish_text.setText("今天" + preferences.getString("publish_time", "")
				+ "发布");
		current_date.setText(preferences.getString("current_time", ""));
		weather_info_layout.setVisibility(View.VISIBLE);
		city_name.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_select_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.bt_refresh:
			publish_text.setText("同步中...");
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = preferences.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)){
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	}

	
}
