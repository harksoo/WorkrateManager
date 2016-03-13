package com.sovate.workratemanager.network;

import android.util.Log;

import com.sovate.workratemanager.MainActivity;
import com.sovate.workratemanager.bean.ActivityBaseInfo;
import com.sovate.workratemanager.bean.ActivityDevice;
import com.sovate.workratemanager.bean.ActivityDeviceStudentInfo;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by harks on 2016-03-01.
 */
public class HttpApi {

    private static final String TAG = "HttpApi";

    // Networking
    public static String BASE_URL = "http://210.127.55.205:82";
    //public static final String BASE_URL = "http://192.168.0.6:8080";


    // Interface
    public interface ActivityService {

        @GET("/HealthCare/activity/devices")
        Call<List<ActivityDevice>> getDevices();

        @GET("/HealthCare/activity/devices/{schoolId}/{gradeId}/{classId}")
        Call<List<ActivityDeviceStudentInfo>> getDevicesStudentMap(
                @Path("schoolId") String schoolId,
                @Path("gradeId") String gradeId,
                @Path("classId") String classId
        );

        @GET("/HealthCare/activity/baseInfo")
        Call<ActivityBaseInfo> getBaseInfo();
    }


    // 통신 방식이 귀속적임 메시지 큐나 기타 방식으로 데이터를 전달 하는 구조를 만들어야 함.
    static MainActivity main;


    // function
    public static void setMainActivity(MainActivity main)
    {
        HttpApi.main = main;
    }


    public static void getDevicesRequest() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ActivityService activityService = retrofit.create(ActivityService.class);

        Call<List<ActivityDevice>> call = activityService.getDevices();

        call.enqueue(new Callback<List<ActivityDevice>>() {
            @Override
            public void onResponse(Call<List<ActivityDevice>> call, Response<List<ActivityDevice>> response) {

                Log.i(TAG, "Response status code: " + response.code());
                Log.i(TAG, "Response values : " + response.body());

                // isSuccess is true if response code => 200 and <= 300
                if (!response.isSuccess()) {
                    // print response body if unsuccessful
                    try {
                        Log.e(TAG, response.errorBody().string());
                    } catch (IOException e) {
                        // do nothing
                    }
                    return;
                }

                // TODO 수정요망.
                HttpApi.main.setListActivityDevice(response.body());

            }

            @Override
            public void onFailure(Call<List<ActivityDevice>> call, Throwable t) {
                Log.e(TAG, "onFailure");
                Log.e(TAG, t.getMessage());
            }
        });
    }



    public static void getDevicesStudentMapRequest(String schoolId, String gradeId, String classId) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ActivityService activityService = retrofit.create(ActivityService.class);



        Call<List<ActivityDeviceStudentInfo>> call = activityService.getDevicesStudentMap(schoolId, gradeId, classId);

        call.enqueue(new Callback<List<ActivityDeviceStudentInfo>>() {
            @Override
            public void onResponse(Call<List<ActivityDeviceStudentInfo>> call, Response<List<ActivityDeviceStudentInfo>> response) {

                Log.i(TAG, "Response status code: " + response.code());
                Log.i(TAG, "Response values : " + response.body());

                // isSuccess is true if response code => 200 and <= 300
                if (!response.isSuccess()) {
                    // print response body if unsuccessful
                    try {
                        Log.e(TAG, response.errorBody().string());
                    } catch (IOException e) {
                        // do nothing
                    }
                    return;
                }

                // 내부적으로 AysnTask를 사용하는 방식이라 외부 함수 호출을 해도 됌.
                HttpApi.main.responseGetDevicesStudentMap(response.body(), null);

            }

            @Override
            public void onFailure(Call<List<ActivityDeviceStudentInfo>> call, Throwable t) {

                HttpApi.main.responseGetDevicesStudentMap(null, t);
            }
        });
    }

    public static void getBaseInfoRequest() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ActivityService activityService = retrofit.create(ActivityService.class);

        Call<ActivityBaseInfo> call = activityService.getBaseInfo();

        call.enqueue(new Callback<ActivityBaseInfo>() {
            @Override
            public void onResponse(Call<ActivityBaseInfo> call, Response<ActivityBaseInfo> response) {
                Log.i(TAG, "Response status code: " + response.code());
                Log.i(TAG, "Response values : " + response.body());

                // isSuccess is true if response code => 200 and <= 300
                if (!response.isSuccess()) {
                    // print response body if unsuccessful
                    try {
                        Log.e(TAG, response.errorBody().string());
                    } catch (IOException e) {
                        // do nothing
                    }
                    return;
                }

                // 내부적으로 AysnTask를 사용하는 방식이라 외부 함수 호출을 해도 됌.
                HttpApi.main.responseGetBaseInfo(response.body(), null);
            }

            @Override
            public void onFailure(Call<ActivityBaseInfo> call, Throwable t) {
                HttpApi.main.responseGetBaseInfo(null, t);
            }
        });


    }
}
