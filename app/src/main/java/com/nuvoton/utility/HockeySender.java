package com.nuvoton.utility;

import android.content.Context;
import android.util.Log;

import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import java.util.Date;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by v-zhjoh on 2016/5/4.
 */
public class HockeySender implements ReportSender {
    private static final String TAG = "HockeySender";
    private static String BASE_URL = "https://rink.hockeyapp.net/api/2/apps/";
    private static String CRASHES_PATH = "/crashes";

    @Override
    public void send(Context context, CrashReportData report) throws ReportSenderException {

        String log = createCrashLog(report);

        //App id on Hockeyapp dashboard
        String formKey = "fc6fdee015214eab89f243f0ca8590fe";
        String url = BASE_URL + formKey + CRASHES_PATH;
        final OkHttpClient client = new OkHttpClient();
        final RequestBody formBody = new FormBody.Builder()
                .add("raw", log)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            Log.d(TAG, "okhttp response: " + response.body().string());
//            DefaultHttpClient httpClient = new DefaultHttpClient();
//            HttpPost httpPost = new HttpPost(url);
//
//            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
//            parameters.add(new BasicNameValuePair("raw", log));
//            parameters.add(new BasicNameValuePair("userID", report.get(ReportField.INSTALLATION_ID)));
//            parameters.add(new BasicNameValuePair("contact", report.get(ReportField.USER_EMAIL)));
//            parameters.add(new BasicNameValuePair("description", report.get(ReportField.USER_COMMENT)));
//
//            httpPost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));
//
//            httpClient.execute(httpPost);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createCrashLog(CrashReportData report) {
        Date now = new Date();
        StringBuilder log = new StringBuilder();

        log.append("Package: " + report.get(ReportField.PACKAGE_NAME) + "\n");
        log.append("Version: " + report.get(ReportField.APP_VERSION_CODE) + "\n");
        log.append("Android: " + report.get(ReportField.ANDROID_VERSION) + "\n");
        log.append("Manufacturer: " + android.os.Build.MANUFACTURER + "\n");
        log.append("Model: " + report.get(ReportField.PHONE_MODEL) + "\n");
        log.append("Date: " + now + "\n");
        log.append("\n");
        log.append(report.get(ReportField.STACK_TRACE));

        return log.toString();
    }
}