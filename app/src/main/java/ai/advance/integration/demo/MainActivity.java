package ai.advance.integration.demo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import ai.advance.liveness.lib.GuardianLivenessDetectionSDK;
import ai.advance.liveness.lib.LivenessResult;
import ai.advance.liveness.lib.Market;
import ai.advance.liveness.sdk.activity.LivenessActivity;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_CODE_LIVENESS = 1000;
    private static final int PERMISSIONS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initKeyTypeButton();
        initTicketButton();
        initLicenseButton();
    }

    private void initLicenseButton() {
        findViewById(R.id.license_type_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 将此处的 license 修改
                String license = null;
                if (license == null) {
                    new AlertDialog.Builder(MainActivity.this).setMessage("请在 MainActivity 中给 license 赋值").setPositiveButton("确定", null).create().show();
                } else {
                    boolean packageNamePass = checkPackageName();
                    if (packageNamePass) {
                        GuardianLivenessDetectionSDK.init(getApplication(), Market.Indonesia);
                        String checkResult = GuardianLivenessDetectionSDK.setLicenseAndCheck(license);
                        if ("SUCCESS".equals(checkResult)) {
                            // license 有效
                            checkPermissions();
                        } else {// license 不可用，过期或者格式错误
                            Toast.makeText(MainActivity.this, checkResult, Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }
        });
    }

    private void initKeyTypeButton() {
        findViewById(R.id.key_type_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 将此处的 key 修改为您的
                String accessKey = null;
                String secretKey = null;
                Market market = null;
                if (accessKey == null || secretKey == null || market == null) {
                    new AlertDialog.Builder(MainActivity.this).setMessage("请在 MainActivity 中给 Key 赋值").setPositiveButton("确定", null).create().show();
                } else {
                    boolean packageNamePass = checkPackageName();
                    if (packageNamePass) {
                        GuardianLivenessDetectionSDK.init(getApplication(), accessKey, secretKey, market);
                        checkPermissions();
                    }
                }

            }
        });
    }

    private void initTicketButton() {
        findViewById(R.id.ticket_type_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 将此处的 ticket 修改
                String ticket = null;
                String queryId = null;
                if (ticket == null) {
                    new AlertDialog.Builder(MainActivity.this).setMessage("请在 MainActivity 中给 ticket 赋值").setPositiveButton("确定", null).create().show();
                } else {
                    boolean packageNamePass = checkPackageName();
                    if (packageNamePass) {
                        GuardianLivenessDetectionSDK.init(getApplication(), Market.Indonesia);
                        GuardianLivenessDetectionSDK.setTicket(ticket);
                        GuardianLivenessDetectionSDK.setQueryId(queryId);
                        checkPermissions();
                    }

                }

            }
        });

    }

    private boolean checkPackageName() {
        if ("your.app.id".equals(getPackageName())) {
            new AlertDialog.Builder(MainActivity.this).setMessage("请将 build.gradle 中的包名修改为您已备案的").setPositiveButton("确定", null).create().show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_LIVENESS:
//                    Call the following methods to get results
//                    String livenessId = LivenessResult.getLivenessId();
//                    Bitmap livenessBitmap = LivenessResult.getLivenessBitmap();
//                    String transactionId = LivenessResult.getTransactionId();
//                    boolean success = LivenessResult.isSuccess();
//                    String errorMsg = LivenessResult.getErrorMsg();
                if (LivenessResult.isSuccess()) {
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, LivenessResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Detect camera authorization
     */
    public void checkPermissions() {
        if (allPermissionsGranted()) {
            onPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
        }
    }

    public String[] getRequiredPermissions() {
        return new String[]{Manifest.permission.CAMERA};
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            //已授权
            if (allGranted(grantResults)) {
                onPermissionGranted();
            } else {
                onPermissionRefused();
            }
        }
    }

    /**
     * Denied camera permissions
     */
    public void onPermissionRefused() {
        new AlertDialog.Builder(this).setMessage(getString(R.string.liveness_no_camera_permission)).setPositiveButton(getString(R.string.liveness_perform), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).create().show();
    }

    private boolean allGranted(int[] grantResults) {
        boolean hasPermission = true;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
            }
        }
        return hasPermission;
    }

    /**
     * Got camera permissions
     */
    public void onPermissionGranted() {
        Intent intent = new Intent(this, LivenessActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LIVENESS);
    }
}
