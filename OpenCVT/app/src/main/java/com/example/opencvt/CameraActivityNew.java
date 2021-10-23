package com.example.opencvt;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;


public class CameraActivityNew extends AppCompatActivity {
    private static final String TAG = "CameraActivity";

    private JavaCameraView mOpenCvCameraView;

    private long mStartTime;
    private int mBlurRadius;
    private int mMinDetectRadius;
    private int mMaxDetectRadius;


    CVLightData cvlightdata = new CVLightData();
    int lightType = 1001;
    private SimpleCvCameraViewListener2 mCameraViewListener = new SimpleCvCameraViewListener2() {

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            mStartTime = System.currentTimeMillis();
            Mat cameraFrame = inputFrame.rgba();
            logTime("rgba()");
//            Mat screen = new Mat(cameraFrame.rows(), cameraFrame.cols(), CvType.CV_8UC4);
//            screen.setTo(new Scalar(127, 127, 127, 255));
            // setQuarterScreen(screen, 0, cameraFrame);
            Mat blurredFrame = new Mat();
            Imgproc.GaussianBlur(cameraFrame, blurredFrame, new Size(mBlurRadius, mBlurRadius), 0);
            logTime("GaussianBlur()");
            // setQuarterScreen(screen, 1, blurredFrame);

            Mat hsv = new Mat();
            Imgproc.cvtColor(blurredFrame, hsv, Imgproc.COLOR_RGB2HSV);
            logTime("RGB2HSV");
            Mat red = filterRed(hsv);
            logTime("filterRed");
            Mat green = filterGreen(hsv);
            logTime("filterGreen");
            Mat yellow = filterYellow(hsv);
            logTime("filterYellow");

            Imgproc.dilate(red, red, new Mat(), new Point(), 2);
            logTime("dilate red");
            Imgproc.dilate(green, green, new Mat(), new Point(), 2);
            logTime("dilate green");
            Imgproc.dilate(yellow, yellow, new Mat(), new Point(), 2);
            logTime("dilate yellow");

//            findAndDrawContours(blurredFrame, red, new Scalar(255, 0, 0, 255), "Red");
//            logTime("findAndDrawContours red");
//            findAndDrawContours(blurredFrame, green, new Scalar(0, 255, 0, 255), "Green");
//            logTime("findAndDrawContours green");
//            findAndDrawContours(blurredFrame, yellow, new Scalar(255, 255, 0, 255), "Yellow");
//
            cvlightdata = new CVLightData();
            findAndDrawContoursRed(red,cvlightdata);
            findAndDrawContoursGreen(green,cvlightdata);
            lightType = cvlightdata.checkLightType();
            Log.d(TAG, "contours.size() lightType=" +lightType);
            return blurredFrame;
        }

        @Override
        public void onCameraViewStarted(int width, int height) {
//            Camera.Parameters params = mOpenCvCameraView.getCamera().getParameters();
//            params.setExposureCompensation(params.getMinExposureCompensation());
//            mExposureSeekBar.setMax(params.getMaxExposureCompensation() - params.getMinExposureCompensation());
//            mOpenCvCameraView.getCamera().setParameters(params);
            // 设置最小值
            mExposureSeekBar.setProgress(0);
        }
    };

    private SeekBar mBlurRadiusSeekBar;
    private CheckBox mShowIgnoredRectCheckBox;
    private TextView mBlurRadiusText;
    private View mControlPanelContainer;
    private boolean mShowIgnoredRect;
    private SeekBar mExposureSeekBar;

    private void logTime(String text) {
        Log.d(TAG, text + ": " + (System.currentTimeMillis() - mStartTime) + "ms");
    }

    private void findAndDrawContours(Mat cameraFrame, Mat rangedAndDilatedMat, Scalar drawColor, String text) {
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(rangedAndDilatedMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.d(TAG, "contours.size(): " +text +" "+ contours.size());
        for (MatOfPoint contour : contours) {
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect rect = Imgproc.boundingRect(points);
            if (rect.area() > mMinDetectRadius * mMinDetectRadius && rect.area() < mMaxDetectRadius * mMaxDetectRadius && closeToSquare(rect)) {
                Imgproc.rectangle(cameraFrame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), drawColor, 2);
                Imgproc.putText(cameraFrame, text + String.format("%dx%d", rect.width, rect.height), new Point(rect.x + rect.width / 2, rect.y - 3), Core.FONT_HERSHEY_SIMPLEX, 0.6, drawColor, 2);
            } else {
                if (mShowIgnoredRect) {
                    Imgproc.rectangle(cameraFrame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), drawColor, 1);
                }
            }
        }
    }

    private void findAndDrawContoursRed(Mat rangedAndDilatedMat, CVLightData cvlightdata) {
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(rangedAndDilatedMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.d(TAG, "contours.size(): "  +" "+ contours.size());
        cvlightdata.setRedCount( contours.size());

    }

    private void findAndDrawContoursGreen(Mat rangedAndDilatedMat, CVLightData cvlightdata) {
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(rangedAndDilatedMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.d(TAG, "contours.size(): "  +" "+ contours.size());
        cvlightdata.setGreenCount( contours.size());

    }

    private static boolean closeToSquare(Rect rect) {
        float ratio = (float) rect.width / rect.height;
        if (ratio > 1) {
            ratio = 1 / ratio;
        }
        return ratio > 0.8;
    }

    private static Mat gray2rgba(Mat gray) {
        Mat rgba = new Mat();
        Imgproc.cvtColor(gray, rgba, Imgproc.COLOR_GRAY2RGBA);
        return rgba;
    }

    private void setQuarterScreen(Mat screen, int index, Mat mat) {
        final int width = screen.width() / 2;
        final int height = screen.height() / 2;
        Mat resizedMat = mat;
        if (mat.width() != width || mat.height() != height) {
            resizedMat = new Mat();
            Imgproc.resize(mat, resizedMat, new Size(width, height));
        }
        final Mat subScreen;
        switch (index) {
            case 0:
                subScreen = screen.submat(0, height, 0, width);
                break;
            case 1:
                subScreen = screen.submat(0, height, width, width * 2);
                break;
            case 2:
                subScreen = screen.submat(height, height * 2, 0, width);
                break;
            case 3:
            default:
                subScreen = screen.submat(height, height * 2, width, width * 2);
                break;
        }
        resizedMat.copyTo(subScreen);
        // debugBitmap(screen);
    }

    private static final int SENSITIVITY1 = 10;
    private static final int SENSITIVITY2 = 5;

    private Mat filterRed(Mat hsv) {
        Mat result = new Mat();
        Mat lower_red_hue_range = new Mat();
        Mat upper_red_hue_range = new Mat();
        Core.inRange(hsv, new Scalar(0, 100, 100), new Scalar(SENSITIVITY1, 255, 255), lower_red_hue_range);
        Core.inRange(hsv, new Scalar(180 - SENSITIVITY1, 100, 100), new Scalar(180, 255, 255), upper_red_hue_range);
        Core.bitwise_or(lower_red_hue_range, upper_red_hue_range, result);
        return result;
    }

    private Mat filterGreen(Mat hsv) {
        Mat result = new Mat();
        Core.inRange(hsv, new Scalar(60 - SENSITIVITY1, 100, 100), new Scalar(60 + SENSITIVITY1, 255, 255), result);
        return result;
    }

    private Mat filterYellow(Mat hsv) {
        Mat result = new Mat();
        Core.inRange(hsv, new Scalar(30 - SENSITIVITY2, 100, 100), new Scalar(30 + SENSITIVITY2, 255, 255), result);
        return result;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };

    private static void debugBitmap(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        Log.d(TAG, "" + bitmap);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMinDetectRadius = getResources().getInteger(R.integer.min_radius_default);
        mMaxDetectRadius = getResources().getInteger(R.integer.max_radius_default);
        mBlurRadius = getResources().getInteger(R.integer.blur_radius_default);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera_new);
        mControlPanelContainer = findViewById(R.id.control_panel);
        mOpenCvCameraView = findViewById(R.id.camera_view);
        mOpenCvCameraView.setMaxFrameSize(540, 960);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(mCameraViewListener);
        mOpenCvCameraView.setOnClickListener(v -> {
//            boolean isVisible = mControlPanelContainer.getVisibility() == View.VISIBLE;
//            isVisible = !isVisible;
//            mControlPanelContainer.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        });
//        mBlurRadiusSeekBar = findViewById(R.id.blur_radius_seek_bar);
//        mBlurRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (progress % 2 == 0) {
//                    progress++;
//                }
//                mBlurRadius = progress;
//                mBlurRadiusText.setText(getString(R.string.blur_radius) + mBlurRadius);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        });
        mBlurRadiusText = findViewById(R.id.blur_radius_text);
        mShowIgnoredRectCheckBox = findViewById(R.id.show_ignored_rect_checkbox);
        mShowIgnoredRectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mShowIgnoredRect = isChecked;
        });
        TextView minText = findViewById(R.id.color_area_min_radius_text);
        SeekBar minSeekBar = findViewById(R.id.color_area_min_radius_seek_bar);
        minSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mMinDetectRadius = progress;
                minText.setText(getString(R.string.color_area_min_radius) + mMinDetectRadius);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        TextView maxText = findViewById(R.id.color_area_max_radius_text);
        SeekBar maxSeekBar = findViewById(R.id.color_area_max_radius_seek_bar);
        maxSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mMaxDetectRadius = progress;
                maxText.setText(getString(R.string.color_area_max_radius) + mMaxDetectRadius);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        TextView exposureText = findViewById(R.id.exposure_text);
        mExposureSeekBar = findViewById(R.id.exposure_seek_bar);
        mExposureSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                Camera camera = mOpenCvCameraView.getCamera();
//                Camera.Parameters params = camera.getParameters();
//                progress += params.getMinExposureCompensation();
//                exposureText.setText(getString(R.string.exposure) + progress);
//                params.setExposureCompensation(progress);
//                camera.setParameters(params);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        disableCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        disableCamera();
    }

    public void disableCamera() {
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

}
