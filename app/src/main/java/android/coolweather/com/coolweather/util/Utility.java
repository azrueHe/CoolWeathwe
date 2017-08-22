package android.coolweather.com.coolweather.util;

import android.coolweather.com.coolweather.db.City;
import android.coolweather.com.coolweather.db.County;
import android.coolweather.com.coolweather.db.Province;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/8/21 0021.
 */

public class Utility {

    /**
     * 解析省信息并返回
     * @param response
     * @return
     */
    public static boolean handleProvinceResponce(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray jsonArray = new JSONArray(response);
                for(int index = 0; index < jsonArray.length(); index++){
                    JSONObject jsonObject = jsonArray.getJSONObject(index);
                    Province province = new Province();
                    province.setProvinceName(jsonObject.getString("name"));
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析市级数据并返回
     * @param responce
     * @param provinceId
     * @return
     */
    public static boolean handleCityResponce(String responce, int provinceId){
        if(!TextUtils.isEmpty(responce)){
            try {
                JSONArray jsonArray = new JSONArray(responce);
                for(int index = 0; index < jsonArray.length(); index++){
                    JSONObject jsonObject = jsonArray.getJSONObject(index);
                    City city = new City();
                    city.setCityName(jsonObject.getString("name"));
                    city.setCityCode(jsonObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析区县数据并返回
     * @param responce
     * @param cityId
     * @return
     */
    public static boolean handleCountyResponce(String responce, int cityId){
        if(!TextUtils.isEmpty(responce)){
            try {
                JSONArray jsonArray = new JSONArray(responce);
                for(int index = 0; index < jsonArray.length(); index++){
                    JSONObject jsonObject = jsonArray.getJSONObject(index);
                    County county = new County();
                    county.setCountyName(jsonObject.getString("name"));
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
