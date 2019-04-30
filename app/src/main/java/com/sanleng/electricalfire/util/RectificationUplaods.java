package com.sanleng.electricalfire.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.sanleng.electricalfire.dialog.MyProgressDialog;
import com.sanleng.electricalfire.net.URLs;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 隐患整改上传
 *
 * @author Qiaoshi
 */
public class RectificationUplaods extends AsyncTask<String, Integer, String> {
    private Context context;
    private List<String> filePathList;
    private long totalSize;
    private MyProgressDialog dp;
    private String serverResponse;
    private Handler handler;
    private String ids;
    private String desc;
    private String username;
    private String unitCode;
    private String app_firecontrol_owner;
    private String type;

    public RectificationUplaods(Context context, List<String> filePathList, String ids, String desc, String app_firecontrol_owner, String unitCode, String username, String type, Handler handler) {
        this.context = context;
        this.filePathList = filePathList;
        this.ids = ids;
        this.desc = desc;
        this.app_firecontrol_owner = app_firecontrol_owner;
        this.unitCode = unitCode;
        this.username = username;
        this.type = type;
        this.handler = handler;
    }

    @Override
    protected void onPreExecute() {
        dp = new MyProgressDialog(context);
        dp.setMessage("正在上传...");
        dp.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dp.setCancelable(false);
        dp.show();
    }

    @Override
    protected String doInBackground(String... params) {
        serverResponse = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext httpContext = new BasicHttpContext();
        HttpPost httpPost = new HttpPost(URLs.ProcessingReport_URL + "?pointId=" + ids + "&username="
                + username + "&platformkey=" + app_firecontrol_owner + "&unitCode=" + unitCode + "&type=fire_alarm");
        try {
            CustomMultipartEntity multipartContent = new CustomMultipartEntity(new CustomMultipartEntity.ProgressListener() {
                @Override
                public void transferred(long num) {
                    publishProgress((int) ((num / (float) totalSize) * 100));
                }
            });
            // 把上传内容添加到MultipartEntity
            for (int i = 0; i < filePathList.size(); i++) {
                multipartContent.addPart("File", new FileBody(new File(filePathList.get(i))));
                multipartContent.addPart("data",
                        new StringBody(filePathList.get(i), Charset.forName(org.apache.http.protocol.HTTP.UTF_8)));
            }
            totalSize = multipartContent.getContentLength();

            httpPost.setEntity(multipartContent);

            org.apache.http.HttpResponse response = httpClient.execute(httpPost, httpContext);
            serverResponse = EntityUtils.toString(response.getEntity());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverResponse;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        dp.setProgress((int) (progress[0]));
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject object = new JSONObject(result);
            String mymsg = object.getString("msg");
            if (serverResponse != null) {
                if (mymsg.equals("电气火灾隐患处理成功！")) {
                    Message msg = new Message();
                    msg.what = 578678;
                    handler.sendMessage(msg);
                    Bimp bimp = new Bimp();
                    bimp.bmp = new ArrayList<Bitmap>();
                    bimp.drr = new ArrayList<String>();
                    bimp.max = 0;
                    FileUtils.deleteDir();
                } else {
                    new SVProgressHUD(context).showErrorWithStatus("上报失败" + mymsg);
                }
            } else {
                new SVProgressHUD(context).showErrorWithStatus("上报失败" + mymsg);
            }
            dp.dismiss();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onCancelled() {

    }

}
