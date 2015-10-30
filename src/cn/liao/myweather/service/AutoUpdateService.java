package cn.liao.myweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import cn.liao.myweather.receiver.AutoUpdateReceiver;
import cn.liao.myweather.util.HttpCallbackListener;
import cn.liao.myweather.util.HttpUtil;
import cn.liao.myweather.util.Utility;

public class AutoUpdateService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				updateWeather();
			}
		}).start();
		
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		//每8个小时更新一次
		int hour = 8 *60*60*1000;
		long triggerAtTime = SystemClock.elapsedRealtime() +hour;
		Intent i = new Intent(this, AutoUpdateReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 更新天气信息
	 */
	private void updateWeather() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode = sp.getString("weather_code", "");
		String address = "http://www.weather.com.cn/data/cityinfo/" +weatherCode+".html";
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				Utility.handleWeatherResponse(AutoUpdateService.this, response);
			}
			@Override
			public void onError(Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	

}
