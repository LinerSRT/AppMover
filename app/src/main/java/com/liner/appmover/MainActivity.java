package com.liner.appmover;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private AppHelper appHelper;
    private AppAdapter appAdapter;
    private Button moveSystem, deleteSystem;
    private AppHolder selectedApp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appHelper = new AppHelper(this);
        RecyclerView appRecycler = findViewById(R.id.appRecycler);
        moveSystem = findViewById(R.id.moveToSystem);
        deleteSystem = findViewById(R.id.deleteFromSystem);
        appAdapter = new AppAdapter(appHelper.getAppList(), this, new AppAdapter.ISelectionListener() {
            @Override
            public void onItemSelected(AppHolder appHolder) {
                deleteSystem.setEnabled(appHolder.isMovedToSystem());
                moveSystem.setEnabled(!appHolder.isMovedToSystem());
                selectedApp = appHolder;
            }
        });
        moveSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedApp != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setCancelable(false);
                    builder.setView(generateProgressView("Перемещение, подождите..."));
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                    Window window = dialog.getWindow();
                    if (window != null) {
                        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                        layoutParams.copyFrom(dialog.getWindow().getAttributes());
                        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        dialog.getWindow().setAttributes(layoutParams);
                    }
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runShellCommand(new String[]{"sh", getApplicationInfo().dataDir + File.separator + "script.sh", "move", selectedApp.getAppSourceDir(), selectedApp.getAppPackageName()}, new IExecutor() {
                                @Override
                                public void onExecuteFinished() {
                                    dialog.dismiss();
                                    if (appHelper.searchAPK(new File("/system/priv-app/"), selectedApp.getAppPackageName())){
                                        selectedApp.setMovedToSystem(true);
                                        appAdapter.clearSelection();
                                        appAdapter.notifyDataSetChanged();
                                        selectedApp = null;
                                        deleteSystem.setEnabled(false);
                                        moveSystem.setEnabled(false);
                                        appHelper.saveAppList(appAdapter.getAppHolderList());
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                createDialog("Отлично!", "Приложение было перемещено в системные приложения!", R.drawable.ic_done_black_24dp);
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                createDialog("Внимание!", "Не удалось переместить приложение, попробуйте позже или другое приложение", R.drawable.ic_warning_black_24dp);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                    thread.start();
                }
            }
        });
        deleteSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedApp != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setCancelable(false);
                    builder.setView(generateProgressView("Перемещение, подождите..."));
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                    Window window = dialog.getWindow();
                    if (window != null) {
                        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                        layoutParams.copyFrom(dialog.getWindow().getAttributes());
                        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        dialog.getWindow().setAttributes(layoutParams);
                    }
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runShellCommand(new String[]{"sh", getApplicationInfo().dataDir + File.separator + "script.sh", "delete", selectedApp.getAppSourceDir(), selectedApp.getAppPackageName()}, new IExecutor() {
                                @Override
                                public void onExecuteFinished() {
                                    dialog.dismiss();
                                    if (appHelper.isAppInstalled(selectedApp.getAppPackageName())){
                                        selectedApp.setMovedToSystem(false);
                                        appAdapter.clearSelection();
                                        appAdapter.notifyDataSetChanged();
                                        selectedApp = null;
                                        deleteSystem.setEnabled(false);
                                        moveSystem.setEnabled(false);
                                        appHelper.saveAppList(appAdapter.getAppHolderList());
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                createDialog("Отлично!", "Приложение было перемещено в пользовательские приложения!", R.drawable.ic_done_black_24dp);
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                createDialog("Внимание!", "Не удалось переместить приложение, попробуйте позже или другое приложение", R.drawable.ic_warning_black_24dp);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                    thread.start();
                }
            }
        });
        appRecycler.setLayoutManager(new LinearLayoutManager(this));
        appRecycler.setHasFixedSize(true);
        appRecycler.setAdapter(appAdapter);
    }

    private String runShellCommand(String[] command, IExecutor executor) {
        try {
            File scriptFile = new File(getApplicationInfo().dataDir + File.separator + "script.sh"); //
            if (scriptFile.exists()) {
                scriptFile.delete();
            }
            byte[] buff = new byte[1024];
            int read;
            try (InputStream in = getResources().openRawResource(R.raw.script);
                 FileOutputStream out = new FileOutputStream(getApplicationInfo().dataDir + File.separator + "script.sh")) {
                 while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                 }
                 Process process = Runtime.getRuntime().exec(new String[]{"sh", "chmod", "0644", scriptFile.getAbsolutePath()});
                 process.waitFor();
            }
            if (executor != null) {
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder log = new StringBuilder();
                String line_string;
                while ((line_string = bufferedReader.readLine()) != null) {
                    log.append(line_string + "\n");
                }
                executor.onExecuteFinished();
                return log.toString();
            } else {
                return "Exec failed";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Exec failed";
        }
    }
    private interface IExecutor{
        void onExecuteFinished();
    }

    private void createDialog(String title, String message, int icon){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(icon);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private View generateProgressView(String message){
        int llPadding = 30;
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, llPadding, 0);
        progressBar.setLayoutParams(llParam);
        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        TextView tvText = new TextView(this);
        tvText.setText(message);
        tvText.setTextColor(getColor(R.color.textColor));
        tvText.setTextSize(16);
        tvText.setLayoutParams(llParam);
        ll.addView(progressBar);
        ll.addView(tvText);
        return ll;
    }


}
