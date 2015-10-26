package cn.liao.myweather.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.liao.myweather.R;
import cn.liao.myweather.db.MyWeatherDB;
import cn.liao.myweather.model.City;
import cn.liao.myweather.model.County;
import cn.liao.myweather.model.Province;
import cn.liao.myweather.util.HttpCallbackListener;
import cn.liao.myweather.util.HttpUtil;
import cn.liao.myweather.util.Utility;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private MyWeatherDB myWeatherDB;
	private List<String> dataList = new ArrayList<String>();

	/**
	 * ʡ�б�
	 */
	private List<Province> provincesList;
	/**
	 * ���б�
	 */
	private List<City> citiesList;
	/**
	 * ���б�
	 */
	private List<County> countiesList;
	/**
	 * ѡ�е�ʡ��
	 */
	private Province selectedProvince;
	/**
	 * ѡ�еĳ���
	 */
	private City selectedCity;
	/**
	 * ��ǰѡ�еļ���
	 */
	private int currentLevel;
	/**
	 * �Ƿ��WeatherActivity����ת������
	 */
	private boolean isFromWeatherActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		isFromWeatherActivity = getIntent().getBooleanExtra(
				"from_weather_activity", false);
		if (preferences.getBoolean("city_selected", false)
				&& !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_area);
		titleText = (TextView) findViewById(R.id.title_text);
		listView = (ListView) findViewById(R.id.list_view);
		adapter = new ArrayAdapter<>(this,
				android.R.layout.simple_expandable_list_item_1, dataList);
		listView.setAdapter(adapter);
		myWeatherDB = MyWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provincesList.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = citiesList.get(position);
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
					String countyCode = countiesList.get(position)
							.getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,
							WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvince(); // ����ʡ������
	}

	/**
	 * ��ѯȫ�����е�ʡ�ݣ����ȴ����ݿ��в�ѯ�����û�о͵��������ϲ�ѯ
	 */
	private void queryProvince() {
		provincesList = myWeatherDB.loadProvinces();
		if (provincesList.size() > 0) {
			dataList.clear();
			for (Province p : provincesList) {
				dataList.add(p.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}

	/**
	 * ��ѯ��ѡʡ���еĳ��У����ȴ����ݿ��в�ѯ�����û�о͵��������ϲ�ѯ
	 */
	private void queryCities() {
		citiesList = myWeatherDB.loadCities(selectedProvince.getId());
		if (citiesList.size() > 0) {
			dataList.clear();
			for (City city : citiesList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}

	/**
	 * ��ѯ��ѡ�����е��أ����ȴ����ݿ��в�ѯ�����û�о͵��������ϲ�ѯ
	 */
	private void queryCounties() {
		countiesList = myWeatherDB.loadCounties(selectedCity.getId());
		if (countiesList.size() > 0) {
			dataList.clear();
			for (County county : countiesList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}

	/**
	 * ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ�������ݡ�
	 */
	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvinceResponse(myWeatherDB,
							response);
				} else if ("city".equals(type)) {
					result = Utility.handleCityResponse(myWeatherDB, response,
							selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountyResponse(myWeatherDB,
							response, selectedCity.getId());
				}
				if (result) {
					// ͨ��runOnUiThread()�����ص����̴߳����߼�
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvince();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// ͨ��runOnUiThread()�����ص����̴߳����߼�
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��",
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	/**
	 * ��ʾ���ȶԻ���
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("����ƴ��������...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	/**
	 * �رս��ȶԻ�
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	/**
	 * ��׽Back���������ݵ�ǰ�ļ������жϴ�ʱӦ�÷���ɶ�б�����ֱ���˳�
	 */
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvince();
		} else {
			if(isFromWeatherActivity){
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}

}
