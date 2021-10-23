package com.example.opencvt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 *
 * todo
 * 禁止后 相关操作前 提示开通权限 去开通
 */
public class OCVPermissionUtil {
    public interface PermissionGrant {
        void onPermissionGranted(int requestCode);
    }

    /**
     * Requests permission.
     *
     * @param requestCode request code, e.g. if you need request CAMERA permission,parameters is
     * PermissionUtils.CODE_CAMERA
     */
    public static void requestPermission(Activity activity, String permission, int requestCode,
                                         PermissionGrant permissionGrant) {
        if (activity == null) {
            return;
        }

        //Log.v("requestPermission:" + permission);

        //如果是6.0以下的手机，ActivityCompat.checkSelfPermission()会始终等于PERMISSION_GRANTED，
        // 但是，如果用户关闭了你申请的权限，ActivityCompat.checkSelfPermission(),会导致程序崩溃(java.lang
        // .RuntimeException: Unknown exception code: 1 msg null)，
        // 你可以使用try{}catch(){},处理异常，也可以判断系统版本，低于23就不申请权限，直接做你想做的。permissionGrant
        // .onPermissionGranted(requestCode);
        //        if (Build.VERSION.SDK_INT < 23) {
        //            permissionGrant.onPermissionGranted(requestCode);
        //            return;
        //        }

        int checkSelfPermission;
        try {
            checkSelfPermission = ActivityCompat.checkSelfPermission(activity, permission);
        } catch (RuntimeException e) {
            //CKLog.v("RuntimeException:" + e.getMessage());
            return;
        }

        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            //CKLog.v("ActivityCompat.checkSelfPermission != PackageManager.PERMISSION_GRANTED");

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                //CKLog.v("requestPermission shouldShowRequestPermissionRationale");
                shouldShowRationale(activity, requestCode, permission);
            } else {
                //CKLog.v("requestCameraPermission else");
                ActivityCompat.requestPermissions(activity, new String[] { permission },
                        requestCode);
            }
        } else {
            //CKLog.v("ActivityCompat.checkSelfPermission ==== PackageManager.PERMISSION_GRANTED");
            //Toast.makeText(activity, "opened:" + permission, Toast.LENGTH_SHORT).show();
            permissionGrant.onPermissionGranted(requestCode);
        }
    }

    /**
     * 一次申请多个权限
     */
    public static void requestMultiPermissions(final Activity activity, String[] permissions,
                                               final int requestCode, PermissionGrant permissionGrant) {

        final List<String> permissionsList = getNoGrantedPermission(activity, permissions, false);
        final List<String> shouldRationalePermissionsList =
                getNoGrantedPermission(activity, permissions, true);

        //TODO checkSelfPermission
        if (permissionsList == null || shouldRationalePermissionsList == null) {
            return;
        }

//        //CKLog.v("requestMultiPermissions permissionsList:"
//                + permissionsList.size()
//                + ",shouldRationalePermissionsList:"
//                + shouldRationalePermissionsList.size());

        if (permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
            //CKLog.v("showMessageOKCancel requestPermissions");
        } else if (shouldRationalePermissionsList.size() > 0) {
            ActivityCompat.requestPermissions(activity,
                    shouldRationalePermissionsList.toArray(
                            new String[shouldRationalePermissionsList.size()]), requestCode);
            //CKLog.v("showMessageOKCancel requestPermissions");
        } else {
            permissionGrant.onPermissionGranted(requestCode);
        }
    }

    public static void requestMultiPermissions_back(final Activity activity, String[] permissions,
                                               final int requestCode, PermissionGrant permissionGrant) {

        final List<String> permissionsList = getNoGrantedPermission(activity, permissions, false);
        final List<String> shouldRationalePermissionsList =
                getNoGrantedPermission(activity, permissions, true);

        //TODO checkSelfPermission
        if (permissionsList == null || shouldRationalePermissionsList == null) {
            return;
        }

//        CKLog.v("requestMultiPermissions permissionsList:"
//                + permissionsList.size()
//                + ",shouldRationalePermissionsList:"
//                + shouldRationalePermissionsList.size());

        if (permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
            //CKLog.v("showMessageOKCancel requestPermissions");
        } else if (shouldRationalePermissionsList.size() > 0) {
            showMessageOKCancel(activity, "should open those permission",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity,
                                    shouldRationalePermissionsList.toArray(
                                            new String[shouldRationalePermissionsList.size()]), requestCode);
                            //CKLog.v("showMessageOKCancel requestPermissions");
                        }
                    });
        } else {
            permissionGrant.onPermissionGranted(requestCode);
        }
    }

    /**
     * @param requestCode Need consistent with requestPermission
     */
    public static void requestPermissionsResult(final Activity activity, final int requestCode,
                                                String[] permissions, int[] grantResults, PermissionGrant permissionGrant) {

        if (activity == null) {
            return;
        }
       // CKLog.v("requestPermissionsResult requestCode:" + requestCode);

        if (permissions.length > 1) {
            requestMultiResult(activity, requestCode, permissions, grantResults, permissionGrant);
            return;
        }

//        CKLog.v("onRequestPermissionsResult requestCode:"
//                + requestCode
//                + ",permissions:"
//                + permissions.toString()
//                + ",grantResults:"
//                + grantResults.toString()
//                + ",length:"
//                + grantResults.length);

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //CKLog.v("onRequestPermissionsResult PERMISSION_GRANTED");
            //TODO success, do something, can use callback
            permissionGrant.onPermissionGranted(requestCode);
        } else {
            //TODO hint user this permission function
            //CKLog.v("onRequestPermissionsResult PERMISSION NOT GRANTED");
            //TODO
            String permissionsHint = "策策策策策策策额";
            openSettingActivity(activity, "Result" + permissionsHint, requestCode);
        }
    }

    private static void requestMultiResult(Activity activity, int requestCode, String[] permissions,
                                           int[] grantResults, PermissionGrant permissionGrant) {

        if (activity == null) {
            return;
        }

        //TODO
        //CKLog.v("onRequestPermissionsResult permissions length:" + permissions.length);
        Map<String, Integer> perms = new HashMap<>();

        ArrayList<String> notGranted = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
//            CKLog.v("permissions: [i]:"
//                    + i
//                    + ", permissions[i]"
//                    + permissions[i]
//                    + ",grantResults[i]:"
//                    + grantResults[i]);
            perms.put(permissions[i], grantResults[i]);
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                notGranted.add(permissions[i]);
            }
        }

        if (notGranted.size() == 0) {
            permissionGrant.onPermissionGranted(requestCode);
        } else {
            openSettingActivity(activity, "those permission need granted!", requestCode);
        }
    }

    private static void shouldShowRationale(final Activity activity, final int requestCode,
                                            final String requestPermission) {
        //TODO
        //String[] permissionsHint = activity.getResources().getStringArray(R.array.permissions);
        String permissionsHint = "测试测试测试卷";
        showMessageOKCancel(activity, "Rationale: " + permissionsHint,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(activity, new String[] { requestPermission },
                                requestCode);
                       // CKLog.v("showMessageOKCancel requestPermissions:" + requestPermission);
                    }
                });
    }

    private static void showMessageOKCancel(final Activity context, String message,
                                            DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context).setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private static void openSettingActivity(final Activity activity, String message, final int requestCode) {

        showMessageOKCancel(activity, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
               // CKLog.v("getPackageName(): " + activity.getPackageName());
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivityForResult(intent, requestCode);
            }
        });
    }

    public static ArrayList<String> getNoGrantedPermission(Activity activity,
                                                           String[] requestPermissions, boolean isShouldRationale) {

        ArrayList<String> permissions = new ArrayList<>();

        for (int i = 0; i < requestPermissions.length; i++) {
            String requestPermission = requestPermissions[i];

            //TODO checkSelfPermission
            int checkSelfPermission = -1;
            try {
                checkSelfPermission =
                        ActivityCompat.checkSelfPermission(activity, requestPermission);
            } catch (RuntimeException e) {
                //Toast.makeText(activity, "please open those permission", Toast.LENGTH_SHORT).show();
                //CKLog.v("RuntimeException:" + e.getMessage());
                return null;
            }

            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
//                CKLog.v(
//                        "getNoGrantedPermission ActivityCompat.checkSelfPermission != PackageManager"
//                                + ".PERMISSION_GRANTED:"
//                                + requestPermission);

                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        requestPermission)) {
                    //CKLog.v("shouldShowRequestPermissionRationale if");
                    if (isShouldRationale) {
                        permissions.add(requestPermission);
                    }
                } else {

                    if (!isShouldRationale) {
                        permissions.add(requestPermission);
                    }
                    //CKLog.v("shouldShowRequestPermissionRationale else");
                }
            }
        }

        return permissions;
    }
}
