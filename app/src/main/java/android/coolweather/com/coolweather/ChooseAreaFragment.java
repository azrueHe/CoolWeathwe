package android.coolweather.com.coolweather;

import android.app.ProgressDialog;
import android.content.Context;
import android.coolweather.com.coolweather.db.City;
import android.coolweather.com.coolweather.db.County;
import android.coolweather.com.coolweather.db.Province;
import android.coolweather.com.coolweather.util.HttpUtil;
import android.coolweather.com.coolweather.util.Utility;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleView;
    private Button backButton;
    private ListView listView;

    private ArrayAdapter<String> arrayAdapter;
    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectProvince;
    private City selectCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleView = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_expandable_list_item_1, dataList);
        listView.setAdapter(arrayAdapter);
        return view;
    }


    /**
     * 给各个按钮添加事件
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(LEVEL_PROVINCE == currentLevel){
                    selectProvince = provinceList.get(position);
                    queryCities();
                }else if(LEVEL_CITY == currentLevel){
                    selectCity = cityList.get(position);
                    queryCounties();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LEVEL_CITY == currentLevel){
                    queryProvinces();
                }else if(LEVEL_COUNTY == currentLevel){
                    queryCities();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询对应市的区县信息，先从数据库查询，如果没有便从服务器查询
     */
    private void queryCounties(){
        titleView.setText(selectCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid=?",String.valueOf(selectCity.getId())).find(County.class);
        if (countyList.size() > 0){
            dataList.clear();
            for(County county : countyList){
                dataList.add(county.getCountyName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectProvince.getProvinceCode();
            int cityCode = selectCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromService(address, "county");
        }
    }

    /**
     * 查询对应省的市，先从数据库查询，如果没有便从服务器查询
     */
    private void queryCities(){
        titleView.setText(selectProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid=?", String.valueOf(selectProvince.getId())).find(City.class);
        if (cityList.size() > 0){
            dataList.clear();
            for(City city : cityList){
                dataList.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            String address = "http://guolin.tech/api/china/" + selectProvince.getProvinceCode();
            queryFromService(address, "city");
        }
    }

    /**
     * 查询所有的省，先从数据库查询，如果没有便从服务器查询
     */
    private void queryProvinces(){
        titleView.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0){
            dataList.clear();
            for(Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            String address = "http://guolin.tech/api/china/";
            queryFromService(address, "province");
        }
   }

    /**
     * 根据传入的地址和类型在服务器中进行查询
     * @param address
     * @param type
     */
    private void queryFromService(String address, final String type){
//        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败...", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvinceResponce(responseText);
                    Log.e("~~~~~~~~~~~~~~~~", "province"+ result);
                }else if("city".equals(type)){
                    Log.e("~~~~~~~~~~~~~~~~", "city"+ result);
                    result = Utility.handleCityResponce(responseText, selectProvince.getId());
                }else if("county".equals(type)){
                    Log.e("~~~~~~~~~~~~~~~~", "county"+ result);
                    result = Utility.handleCountyResponce(responseText, selectCity.getId());
                }
                Log.e("~~~~~~~~~~~~~~~~", ""+ result);
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                                Log.e("queryCounties", "进入查询"+type);
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
