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

	// ������
	private TextView city_name;
	// ����ʱ��
	private static TextView publish_text;
	// ����������Ϣ
	private TextView weather_desp;
	// �������
	private TextView temp1;
	// �������
	private TextView temp2;
	// ��ǰ����
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
			// ���ؼ����ž�ȥ������
			publish_text.setText("ͬ����...");
			weather_info_layout.setVisibility(View.INVISIBLE);
			city_name.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			// ���û���ؼ����ž�ֱ����ʾ��������
			showWeather();
		}
	}

	/**
	 * ��ѯ�ؼ���������Ӧ���������š�
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}

	/**
	 * ��ѯ������������Ӧ��������
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}

	/**
	 * ���ݴ���ĵ�ַ���������������ѯ�������Ż���������Ϣ
	 */
	private void queryFromServer(String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						// �ӷ��������ص������н�������������
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					// ������������ص�������Ϣ
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
						publish_text.setText("ͬ��ʧ��");
					}
				});
			}
		});

	}

	/**
	 * ��SharedPreferences�ļ��ж�ȡ�洢��������Ϣ������ʾ��ҳ����
	 */
	private void showWeather() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		city_name.setText(preferences.getString("city_name", ""));
		temp1.setText(preferences.getString("temp1", ""));
		temp2.setText(preferences.getString("temp2", ""));
		weather_desp.setText(preferences.getString("weather_desp", ""));
		publish_text.setText("����" + preferences.getString("publish_time", "")
				+ "����");
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
			publish_text.setText("ͬ����...");
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
