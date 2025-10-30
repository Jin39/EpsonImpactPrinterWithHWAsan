package com.epson.epos2_printer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.SecureOption;
import com.epson.epos2.printer.VerifyPasswordListener;
import com.epson.epos2.printer.GetPrinterSettingExListener;
import com.epson.epos2.printer.SetPrinterSettingExListener;

import org.json.JSONException;
import org.json.JSONObject;

public class PrinterSettingExActivity extends Activity implements View.OnClickListener, GetPrinterSettingExListener, SetPrinterSettingExListener,VerifyPasswordListener{
    private static final int DISCONNECT_INTERVAL = 500;//millseconds
    private Context mContext = null;
    private Printer mPrinter = null;

    private EditText mEditPassword = null;
    private EditText mEditText = null;
    private ToggleButton mDoVerification = null;

    public static ProgressIndicator mProgressIndicator = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printersetting_ex);

        mContext = this;

        mProgressIndicator = new ProgressIndicator(mContext);

        mEditPassword = findViewById(R.id.edtPrinterPassword);
        mEditText = findViewById(R.id.edtJsonText);
        mDoVerification = findViewById(R.id.toggleDoVerification);
        final int[] target = {
                R.id.btnGetPrinterSettingEx,
                R.id.btnSetPrinterSettingEx,
                R.id.btnVerifyPassword
        };

        for (int i = 0; i < target.length; i++) {
            Button button = (Button)findViewById(target[i]);
            button.setOnClickListener(this);
            button.setEnabled(false);
        }

        if(MainActivity.mEditTarget.getText().toString().contains("[")) {
            ShowMsg.showMsg(getString(R.string.error_msg_firm_update), mContext);
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!initializeObject()) {
                    return;
                }

                mProgressIndicator.beginProgress(getString(R.string.progress_msg));
                if(!connectPrinter()) {
                    mProgressIndicator.endProgress();
                    finalizeObject();
                    return;
                }

                mProgressIndicator.endProgress();
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        for (int i = 0; i < target.length; i++) {
                            Button button = (Button)findViewById(target[i]);
                            button.setOnClickListener((View.OnClickListener) mContext);
                            button.setEnabled(true);
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                disconnectPrinter();
                finalizeObject();
            }
        }).start();
        super.onDestroy();
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnGetPrinterSettingEx:
                mProgressIndicator.beginProgress(getString(R.string.get_setting_ex_msg));
                if(!getPrinterSettingEx()){
                    mProgressIndicator.endProgress();
                }
                break;

            case R.id.btnSetPrinterSettingEx:
                // Show waiting restart message.
                mProgressIndicator.beginProgress(getString(R.string.restart_SettingEx_msg));
                if(!setPrinterSettingEx()){
                    mProgressIndicator.endProgress();
                }
                break;

            case R.id.btnVerifyPassword:
                mProgressIndicator.beginProgress(getString(R.string.verify_password_msg));
                if(!verifyPassword()){
                    mProgressIndicator.endProgress();
                }
                break;

            default:
                break;
        }
    }

    // Get printer settings at once.
    protected boolean getPrinterSettingEx() {
        final SecureOption secureOption = new SecureOption(mEditPassword.getText().toString(), mDoVerification.isChecked());

        try {
            mPrinter.getPrinterSettingEx(Printer.PARAM_DEFAULT, secureOption);
        }
        catch(Exception e) {
            ShowMsg.showException(e, "getPrinterSettingEx", mContext);
            return false;
        }

        return true;
    }

    protected boolean verifyPassword() {
        final SecureOption secureOption = new SecureOption(mEditPassword.getText().toString(), mDoVerification.isChecked());

        try {
            mPrinter.verifyPassword(Printer.PARAM_DEFAULT, secureOption);
        }
        catch(Exception e) {
            ShowMsg.showException(e, "verifyPassword", mContext);
            return false;
        }

        return true;
    }

    // Set printer settings at once.
    protected boolean setPrinterSettingEx() {
        final SecureOption secureOption = new SecureOption(mEditPassword.getText().toString(), mDoVerification.isChecked());

        try {
            mPrinter.setPrinterSettingEx(Printer.PARAM_DEFAULT, mEditText.getText().toString(), secureOption);
        }
        catch(Exception e) {
            ShowMsg.showException(e, "setPrinterSettingEx", mContext);
            return false;
        }

        return true;
    }
    public void onVerifyPassword(Printer printer, final int code) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {
                mProgressIndicator.endProgress();
                ShowMsg.showResult(code, "onVerifyPassword", mContext);
            }
        });
    }

    public void onGetPrinterSettingEx(Printer printer, final int code, final String jsonString) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {
                mProgressIndicator.endProgress();
                if(code != Epos2CallbackCode.CODE_SUCCESS) {
                    ShowMsg.showResult(code, "onGetPrinterSettingEx", mContext);
                    return;
                }
                JSONObject json = null;
                try {
                    json = new JSONObject(jsonString);
                    mEditText.setText(json.toString(4));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onSetPrinterSettingEx(Printer printer, final int code) {
        runOnUiThread(new Runnable() {
            public synchronized void run() {

                if(code != Epos2CallbackCode.CODE_SUCCESS) {
                    mProgressIndicator.endProgress();
                    ShowMsg.showResult(code, "onSetPrinterSettingEx", mContext);
                    return;
                }
                mEditText.setText("");
                new Thread(new Runnable() {
                    public void run() {
                        reconnectPrinter_And_GetPrinterSetting();
                    }
                }).start();
            }
        });
    }

    protected void reconnectPrinter_And_GetPrinterSetting() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Reconect printer with 3 sec interval until 120 sec done.
                boolean ret = RebootWaitingWorker.reconnect(mPrinter, 120, 3);

                // If error, back connection setting view to confirm connection setting.
                if(!ret) {
                    runOnUiThread(new Runnable() {
                        public synchronized void run() {
                            String msg = String.format(
                                    "\t%s\n\t%s\n\n\t%s\n\t%s\n",
                                    mContext.getString(R.string.title_msg_result),
                                    "ERR_CONNECT",
                                    mContext.getString(R.string.title_msg_description),
                                    "reconnect");
                            ShowMsg.showMsg(msg, mContext);
                            mProgressIndicator.endProgress();
                        }
                    });
                    return;
                }

                // When restart success, get current setting to confirmation.
                mProgressIndicator.beginProgress(getString(R.string.get_setting_ex_msg));
                getPrinterSettingEx();

                //Wait onGetPrinterSetting callback to avoid multiple API calling.
                try {
                    Thread.sleep(200);
                }
                catch(InterruptedException e) {
                }
            }
        }).start();
    }

    // Printer control
    private boolean initializeObject() {
        try {
            mPrinter = new Printer(((SpnModelsItem) MainActivity.mSpnSeries.getSelectedItem()).getModelConstant(),
                    ((SpnModelsItem) MainActivity.mSpnLang.getSelectedItem()).getModelConstant(),
                    mContext);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "Printer", mContext);
            return false;
        }
        mPrinter.setVerifyPasswordListener(this);
        mPrinter.setGetPrinterSettingExListener(this);
        mPrinter.setSetPrinterSettingExListener(this);

        return true;
    }

    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.setVerifyPasswordListener(null);
        mPrinter.setGetPrinterSettingExListener(null);
        mPrinter.setSetPrinterSettingExListener(null);
        mPrinter = null;
    }

    protected boolean connectPrinter(){
        if (mPrinter == null) {
            return false;
        }

        try {
            mPrinter.connect(MainActivity.mEditTarget.getText().toString(), Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "connect", mContext);
            return false;
        }
        return true;
    }

    protected void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        while (true) {
            try {
                mPrinter.disconnect();
                break;
            } catch (final Exception e) {
                if (e instanceof Epos2Exception) {
                    //Note: If printer is processing such as printing and so on, the disconnect API returns ERR_PROCESSING.
                    if (((Epos2Exception) e).getErrorStatus() == Epos2Exception.ERR_PROCESSING) {
                        try {
                            Thread.sleep(DISCONNECT_INTERVAL);
                        } catch (Exception ex) {
                        }
                    }else{
                        runOnUiThread(new Runnable() {
                            public synchronized void run() {
                                ShowMsg.showException(e, "disconnect", mContext);
                            }
                        });
                        break;
                    }
                }else{
                    runOnUiThread(new Runnable() {
                        public synchronized void run() {
                            ShowMsg.showException(e, "disconnect", mContext);
                        }
                    });
                    break;
                }
            }
        }
    }
}
