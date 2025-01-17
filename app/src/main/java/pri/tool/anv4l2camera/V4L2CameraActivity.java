package pri.tool.anv4l2camera;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;


import java.io.DataOutputStream;

import pri.tool.v4l2camera.IDataCallback;
import pri.tool.v4l2camera.IStateCallback;
import pri.tool.v4l2camera.V4L2Camera;

public class V4L2CameraActivity extends Activity {
    private final static String TAG = "V4L2CameraActivity";

    V4L2Camera adCamera;
    CameraStateCallback cameraStateCallback;
    CameraDataCallback cameraDataCallback;

    AutoFitSurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    private int previewWidth = 1920;
    private int previewHeight = 1080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        String apkRoot="chmod 777 "+getPackageCodePath();
        RootCommand(apkRoot);

        initView();
    }

    public static boolean RootCommand(String command)
    {
        Process process = null;
        DataOutputStream os = null;
        try
        {
            process = Runtime.getRuntime().exec("su");
            Log.e(TAG, "---process:" + process.getOutputStream());
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e)
        {
            Log.d(TAG, "ROOT failed" + e.getMessage());
            return false;
        } finally
        {
            try
            {
                if (os != null)
                {
                    os.close();
                }
                process.destroy();
            } catch (Exception e)
            {
            }
        }
        Log.d(TAG, "Root succ ");
        return true;
    }

    public void initView() {
        surfaceView = findViewById(R.id.cameraSurface);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                Log.e(TAG, "Surface create");
                initCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });

    }



    public void initCamera() {

        cameraStateCallback = new CameraStateCallback();
        adCamera = new V4L2Camera();
        adCamera.init(cameraStateCallback, this);
        adCamera.open();
    }

    class CameraStateCallback implements IStateCallback {

        @Override
        public void onOpened() {
            Log.d(TAG, "onOpened");

            Size chooseSize = adCamera.chooseOptimalSize(previewWidth, previewHeight);
            if (chooseSize != null) {
                previewWidth = chooseSize.getWidth();
                previewHeight = chooseSize.getHeight();

            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    surfaceView.setAspectRatio(previewWidth, previewHeight);
                    Log.e(TAG, "setAspectRatio width:" + previewWidth + ", height:" + previewHeight);
                }
            });


            adCamera.setSurface(surfaceHolder);

            cameraDataCallback = new CameraDataCallback();
            adCamera.startPreview(cameraDataCallback);
        }

        @Override
        public void onError(int error) {

        }
    }

    class CameraDataCallback implements IDataCallback {

        @Override
        public void onDataCallback(byte[] data, int dataType, int width, int height) {
            Log.e(TAG, "onDataCallbakck  dataType:" + dataType + ", width:" + width + ", height:" + height);
            //处理camera preview 数据
        }
    }
}
