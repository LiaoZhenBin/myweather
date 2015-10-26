package cn.liao.myweather.util;

import android.text.TextUtils;
import cn.liao.myweather.db.MyWeatherDB;
import cn.liao.myweather.model.City;
import cn.liao.myweather.model.County;
import cn.liao.myweather.model.Province;

public class Utility {
	
	/**
	 * �����ʹ�����������ص�ʡ������
	 */
	public synchronized static boolean handleProvinceResponse(MyWeatherDB myWeatherDB,String response){
		if(!TextUtils.isEmpty(response)){
			String[] allProvinces = response.split(",");
			if(allProvinces != null && allProvinces.length>0){
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					//���������������ݴ��浽Province��
					myWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * �����ʹ�����������ص��м�����
	 */
	public synchronized static boolean handleCityResponse(MyWeatherDB myWeatherDB,String response,int provinceId){
		if(!TextUtils.isEmpty(response)){
			String[] allCitys = response.split(",");
			if(allCitys != null && allCitys.length>0){
				for (String c : allCitys) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					//���������������ݴ洢��City��
					myWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * �����ʹ�����������ص��ؼ�����
	 */
	public synchronized static boolean handleCountyResponse(MyWeatherDB myWeatherDB,String response,int CityId){
		if(!TextUtils.isEmpty(response)){
			String[] allCounties = response.split(",");
			if(allCounties != null && allCounties.length>0){
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					County county =new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(CityId);
					//���������������ݴ洢��County��
					myWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
}
