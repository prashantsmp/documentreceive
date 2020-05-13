package com.pepper.sharedocument.document;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.github.barteksc.pdfviewer.PDFView;
import com.justadeveloper96.permissionhelper.PermissionHelper;
import com.pepper.sharedocument.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReceivingDocumentsViaBluetoothActivity extends RobotActivity implements DocumentReceivedListener, DocumentReceiverAdapter.OnItemClickListener {

    private static final int REQUEST_ENABLE_BT = 1000;
    private static final int REQUEST_ENABLE_BT_DISCOVERABLE = 1001;
    private BluetoothAdapter bluetoothAdapter;
    private PermissionHelper permissionHelper;
    private String[] needed_permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private DirectoryFileObserver directoryFileObserver;
    private DocumentReceivedListener documentReceivedListener;
    private PDFView pdfView;
    RecyclerView recyclerView;
    private String BLUETOOTH = "bluetooth";
    private ProgressBar progress;
    private DocumentReceiverAdapter documentReceiverAdapter;
    private List<DocumentReceived> documentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY);
        setContentView(R.layout.activity_receiving_documents_via_bluetooth);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        documentReceivedListener = this;
        pdfView = findViewById(R.id.pdfView);
        progress = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recycler_view);
        documentReceiverAdapter = new DocumentReceiverAdapter(documentList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(documentReceiverAdapter);
        progress.setVisibility(View.GONE);
        permissionHelper = new PermissionHelper(this).setListener(new PermissionHelper.PermissionsListener() {
            @Override
            public void onPermissionGranted(int request_code) {
                File bluetoothFolder = new File(Environment.getExternalStorageDirectory() + File.separator + BLUETOOTH);
                deleteExistingFiles(bluetoothFolder);
                directoryFileObserver = new DirectoryFileObserver(bluetoothFolder.getPath(), documentReceivedListener);
                directoryFileObserver.startWatching();
                if (hasBlueTooth()) {
                    enableBlueTooth();
                }
            }

            @Override
            public void onPermissionRejectedManyTimes(@NotNull List<String> rejectedPerms, int request_code) {
                //if user keeps on denying request
            }
        });
        permissionHelper.requestPermission(needed_permissions, 100);
    }

    private void deleteExistingFiles(File bluetoothFolder) {
        try {
            File[] files = bluetoothFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.exists()) {
                        String name = file.getName();
                        boolean isDeleted = file.delete();
                        Log.d("Delete File ", name + " : deleted = " + isDeleted);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Delete File ", Objects.requireNonNull(e.getMessage()));
        }
    }

    private boolean hasBlueTooth() {
        return bluetoothAdapter != null;
    }

    private void enableBlueTooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            showToast("Bluetooth Already Enabled");
            discoverBluetooth();
        }
    }

    void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(ReceivingDocumentsViaBluetoothActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private void discoverBluetooth() {
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            try {
                Intent discoverableIntent =
                        new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                startActivityForResult(discoverableIntent, REQUEST_ENABLE_BT_DISCOVERABLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ENABLE_BT) {
                showToast("Bluetooth Enabled");
                discoverBluetooth();
            } else if (requestCode == REQUEST_ENABLE_BT_DISCOVERABLE) {
                showToast("Bluetooth Discoverable Enabled");
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == REQUEST_ENABLE_BT) {
                showToast("Bluetooth Cancelled");
            } else if (requestCode == REQUEST_ENABLE_BT_DISCOVERABLE) {
                showToast("Bluetooth Discoverable Cancelled");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onReceived(File receivedFile) {
        Log.d("onReceived - ", "" + receivedFile.getName());
        openPDF(receivedFile);
    }

    @Override
    public void onReceiving(File receivedFile) {
        Log.d("onReceiving - ", "");
        addToAdapter(receivedFile);
        showProgress(true);
    }

    void showProgress(boolean isShow) {
        progress.post(() -> {
            if (isShow) {
                progress.setVisibility(View.VISIBLE);
            } else {
                progress.setVisibility(View.GONE);
            }
        });
    }

    void openPDF(File pdfFile) {
        if (pdfFile.exists()) {
            long size = pdfFile.length();
            if (size == 0) {
                Log.d("openPDF: ", "File is isEmpty");
                //return;
            } else {
                Log.d("openPDF: ", "File is Not Empty");
                showProgress(false);
            }
        }
        /*try {
            pdfView.recycle();
            pdfView.fromFile(pdfFile)
                    .enableSwipe(true)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .pageFitPolicy(FitPolicy.WIDTH) // mode to fit pages in the view
                    .load();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private void addToAdapter(File file) {
        runOnUiThread(() -> {
            documentList.add(new DocumentReceived(file, file.getName()));
            documentReceiverAdapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onDestroy() {
        stopWatching();
        super.onDestroy();
    }

    private void stopWatching() {
        if (directoryFileObserver != null)
            directoryFileObserver.stopWatching();
    }

    @Override
    public void openMimeType(String mimeType, File file) {
        intentChooser(mimeType, file);
    }

    private void intentChooser(String mimeType, File file) {
        String[] mimeTypes =
                {"image/*", "application/pdf", "application/msword", "application/vnd.ms-powerpoint", "application/vnd.ms-excel", "text/plain"};
        Intent intentUrl = new Intent(Intent.ACTION_VIEW);
        if (mimeType.equalsIgnoreCase("application/pdf")) {
            intentUrl.setDataAndType(Uri.fromFile(file), "application/pdf");
        } else if (mimeType.equalsIgnoreCase("image/jpg") || mimeType.equalsIgnoreCase("image/jpeg") || mimeType.equalsIgnoreCase("image/png")) {
            intentUrl.setDataAndType(Uri.fromFile(file), "image/*");
        }
        startActivity(intentUrl);
    }
}
