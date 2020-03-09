package com.ml.sdk;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.squareup.picasso.Picasso;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ImageView ivLocalImage;//图片显示控件
    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;
    private Bitmap localBitmap;//手机相册图片转bitmap
    private String localResult;//手机图片识别结果
    private TextView tvResult;//显示结果的文本框
    private Uri imageUri;
    private HWMLClientToken ocrToken;
    //请求状态码
    public String result = "";
    private static final int REQUEST_PERMISSION_CODE = 1;
    private static final int REQUEST_GALLERY_CODE = 2;
    private static final int REQUEST_TAKE_PHOTO_CODE = 3;

    //首先声明一个数组permissions，将所有需要申请的权限都放在里面
    String[] permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //创建一个mPermissionList，逐个判断哪些权限未授权，将未授权的权限存储到mPermissionList中
    List<String> mPermissionList = new ArrayList<>();

    String domainName = ""; // if the user isn't IAM user, domain_name is the same with username
    String userName = "";
    String password = "";
    private String region = "cn-north-4";
    private String url = "https://dc0a161005e640fe8f5674cdd15e81d9.apigw.cn-north-4.huaweicloud.com/v1/infers/78c4cac9-4687-4d45-a491-1675ad6b2219";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_takephoto).setOnClickListener(this);
        findViewById(R.id.btn_choose).setOnClickListener(this);
        findViewById(R.id.jump).setOnClickListener(this);
        findViewById(R.id.baidu).setOnClickListener(this);
        ivLocalImage = findViewById(R.id.iv_img);
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        imageView3 = (ImageView) findViewById(R.id.imageView3);
        tvResult = findViewById(R.id.tv_result);
        localBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.cat);
        initData();
        //动态申请相机和读写权限
        initPermission();
        //用户认证
        ocrToken = new HWMLClientToken(domainName, userName, password, region);
    }

    private void initData() {
        domainName = getMetaDataValue("DNAME");
        userName = getMetaDataValue("UNAME");
        password = getMetaDataValue("UPWD");
    }
    File outputImage;
    @Override
    public void onClick(View v) {


        switch (v.getId()) {
            case R.id.btn_takephoto://拍照
                //创建File对象，用于存储拍照后的图片
                outputImage = new File(getExternalCacheDir(), "outputImage.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(this,
                            "com.ml.sdk.provider", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }
                //启动相机程序
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO_CODE);
                break;
            case R.id.btn_choose://选择手机相册图片
                Intent intentFromGallery = new Intent(Intent.ACTION_PICK, null);
                intentFromGallery.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intentFromGallery, REQUEST_GALLERY_CODE);
                break;
            case R.id.jump:
                intent=new Intent(MainActivity.this,MainActivity2.class);
//                intent.putExtra("url","https://image.baidu.com/" +
//                        "search/index?tn=baiduimage&ipn=r&ct=201326592&cl=2&lm=-1&st=-1&sf=1&fmq=" +
//                        "&pv=&ic=0&nc=1&z=&se=1&showtab=0&fb=0&width=&height=&face=0&istype=2&ie" +
//                        "=utf-8&fm=result&pos=history&word=" + result);
                intent.putExtra("url",
                        "https://www.google.com/search?newwindow=1&biw=1920&bih=891&tbm=" +
                                "isch&sxsrf=ACYBGNRy3QZlTgCFi_W2Y56OwYYB0luhsw%3A1576772794577&sa=" +
                                "1&ei=uqT7Xd_MIouQr7wPxbCo6A8&q=" + result);
                startActivity(intent);
                break;
            case R.id.baidu:
                Intent i;
                PackageManager manager = getPackageManager();
                try {
                    i = manager.getLaunchIntentForPackage("com.google.ar.lens");
                    if (i == null)
                        throw new PackageManager.NameNotFoundException();
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(i);
                } catch (PackageManager.NameNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Please install Google Lens", Toast.LENGTH_SHORT).show();
                }
                break;
//                intent=new Intent(MainActivity.this,MainActivity2.class);
////                intent.putExtra("url","https://image.baidu.com/" +
////                        "search/index?tn=baiduimage&ipn=r&ct=201326592&cl=2&lm=-1&st=-1&sf=1&fmq=" +
////                        "&pv=&ic=0&nc=1&z=&se=1&showtab=0&fb=0&width=&height=&face=0&istype=2&ie" +
////                        "=utf-8&fm=result&pos=history&word=" + result);
//                intent.putExtra("url",
//                        "https://www.google.com/search?newwindow=1&biw=1920&bih=891&tbm=" +
//                                "isch&sxsrf=ACYBGNRy3QZlTgCFi_W2Y56OwYYB0luhsw%3A1576772794577&sa=" +
//                                "1&ei=uqT7Xd_MIouQr7wPxbCo6A8&q=" + result);
//                startActivity(intent);
//                break;
//                intent=new Intent(MainActivity.this,Main3Activity.class);
////                intent.putExtra("url","https://image.baidu.com/" +
////                        "search/index?tn=baiduimage&ipn=r&ct=201326592&cl=2&lm=-1&st=-1&sf=1&fmq=" +
////                        "&pv=&ic=0&nc=1&z=&se=1&showtab=0&fb=0&width=&height=&face=0&istype=2&ie" +
////                        "=utf-8&fm=result&pos=history&word=" + result);
//                startActivity(intent);
//                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GALLERY_CODE://相册返回结果
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(getApplication(), "点击取消从相册选择", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    Uri uri = data.getData();
                    String filePath = getRealPathFromURI(uri);
                    localBitmap = getResizePhoto(filePath);
                    ivLocalImage.setImageBitmap(localBitmap);
                    if (localBitmap != null) {
                        //请求机器识别服务
                        tvResult.setText("");
                        requestMlTokenService(new File(filePath));
                    } else {
                        Log.e("error", "localBitmap is null");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case REQUEST_TAKE_PHOTO_CODE://拍照结果
                if (resultCode == RESULT_OK) {
                    try {
                        //将拍摄的照片显示出来
//                        File fileLocation = new File(String.valueOf(imageUri));
                        Picasso.get().load(outputImage).into(ivLocalImage);
//                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
//                        ivLocalImage.setImageBitmap(bitmap);
                        tvResult.setText("");
                        requestMlTokenService(outputImage);
                    } catch (Exception e) {
//                        tvResult.setText(e.toString());
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /**
     * 请求机器识别服务
     *
     * @param file
     */
    private void requestMlTokenService(File file) {
        ocrToken.requestmlTokenServiceByFile(url, file, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                localResult = e.toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvResult.setText(localResult);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                localResult = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //显示识别结果
                        try {
                            String[] temp = localResult.split("\\[\"");
                            final String[] name = new String[temp.length - 1];
                            String[] possi = new String[temp.length - 1];
                            for (int i = 1; i < temp.length; i++) {
                                temp[i] = temp[i].substring(0, temp[i].length() - 4);
                                String[] tempo = temp[i].split("\", \"");
                                name[i - 1] = tempo[0];
                                possi[i - 1] = tempo[1];
                            }
                            result = name[0];
                            String output = "Result: \n" + name[0] + "\n\n" + "Possibilities: \n";
                            for (int i = 0; i < name.length; i++) {
                                output += name[i] + ": " + possi[i] + "\n";
                            }
                            output += "\nThe following are some pictures of the result. For more pictures, click the \"More Pictures\" button.\n";
                            final String finalResult = output;
                            tvResult.setText(finalResult);
                            getPictures(result, 1);
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Please start service on ModelArts", Toast.LENGTH_SHORT).show();
                        }
//                        tvResult.setText(Html.fromHtml("<a href='http://www.baidu.com'>打电话</a>"));
//                        tvResult.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                });

            }
        });
    }

    /**
     * 从URI获取String类型的文件路径
     *
     * @param contentUri
     * @return
     */
    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            //由context.getContentResolver()获取contentProvider再获取cursor(游标）用游标获取文件路径返回
            cursor = this.getContentResolver().query(contentUri, projection, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            return cursor.getString(columnIndex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    /**
     * 根据文件路径调整图片大小防止OOM并且返回bitmap
     *
     * @param ImagePath
     * @return
     */
    private Bitmap getResizePhoto(String ImagePath) {
        if (ImagePath != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(ImagePath, options);
            double ratio = Math.max(options.outWidth * 1.0d / 1024f, options.outHeight * 1.0d / 1024);
            options.inSampleSize = (int) Math.ceil(ratio);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(ImagePath, options);
            return bitmap;
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (REQUEST_PERMISSION_CODE == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                    break;
                }
            }
        }
        if (hasPermissionDismiss) {//如果有没有被允许的权限
            showPermissionDialog();
        } else {
            //权限已经都通过了，可以将程序继续打开了
        }
    }

    /**
     * 权限未通过弹框
     */
    private AlertDialog alertDialog;

    private void showPermissionDialog() {
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(this)
                    .setMessage("有权限未通过，部分功能可能不能正常使用，请手动授予")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();
                            //去手机系统设置权限
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (Build.VERSION.SDK_INT >= 9) {
                                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                            } else if (Build.VERSION.SDK_INT <= 8) {
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                                intent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
                            }
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //关闭页面或者做其他操作
                            cancelPermissionDialog();
                            MainActivity.this.finish();
                        }
                    })
                    .create();
        }
        alertDialog.show();
    }

    /**
     * 取消弹框
     */
    private void cancelPermissionDialog() {
        alertDialog.cancel();
    }

    //权限判断和申请
    private void initPermission() {
        mPermissionList.clear();//清空已经允许的没有通过的权限
        //逐个判断是否还有未通过的权限
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) !=
                    PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限到mPermissionList中
            }
        }
        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE);
        } else {
            //权限已经都通过了，可以将程序继续打开了
        }
    }

    /**
     * 获取metadata
     * @param metaDataName
     * @return
     */
    public  String getMetaDataValue( String metaDataName) {
        PackageManager pm = getPackageManager();
        ApplicationInfo appinfo;
        String metaDataValue = "";
        try {
            appinfo = pm.getApplicationInfo(getPackageName(),PackageManager.GET_META_DATA);
            Bundle metaData = appinfo.metaData;
            metaDataValue = metaData.getString(metaDataName);
            return metaDataValue;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return metaDataValue;
    }
    public void getPictures(String keywordList, int max) { // key为关键词,max作为爬取的页数
//        String gsm=Integer.toHexString(max)+"";
        String finalURL = "";
        for(int page=0;page<=max;page++) {
            Document document = null;
            try {
//                int temp = 0;
                String url ="http://image.baidu.com/search/avatarjson?tn=resultjsonavatarnew&ie=utf-8&word="+keywordList+"&cg=star&pn="+page*30+"&rn=30&itg=0&z=0&fr=&width=&height=&lm=-1&ic=0&s=0&st=-1&gsm="+Integer.toHexString(page*30);
                document = Jsoup.connect(url).data("query", "Java")//请求参数
                        .userAgent("Mozilla/4.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)")//设置urer-agent  get();
                        .timeout(5000)
                        .get();
                String xmlSource = document.toString();
                xmlSource = StringEscapeUtils.unescapeHtml3(xmlSource);
                String reg = "objURL\":\"http://.+?\\.jpg";
                Pattern pattern = Pattern.compile(reg);
                Matcher m = pattern.matcher(xmlSource);
//                while (m.find() && temp++ < 1) {
                if(m.find()) {
                    finalURL = m.group().substring(9);
                    String[] furl = finalURL.split("\"");
                    Picasso.get().load(furl[0]).into(imageView1);
                }
                if(m.find()) {
                    finalURL = m.group().substring(9);
                    String[] furl = finalURL.split("\"");
                    Picasso.get().load(furl[0]).into(imageView2);
                }
                if(m.find()) {
                    finalURL = m.group().substring(9);
                    String[] furl = finalURL.split("\"");
                    Picasso.get().load(furl[0]).into(imageView3);
                }
//                tvResult.setText("url: " + furl[0]);
//                    String imageUrl1 = "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2877216507,170326398&fm=26&gp=0.jpg";
//                }
            } catch (IOException e) {
//                tvResult.setText("getpic error: "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

//    public void display(String imageUrl){
////        imageView = (ImageView) findViewById(R.id.imageView2);
//
//    }
}
