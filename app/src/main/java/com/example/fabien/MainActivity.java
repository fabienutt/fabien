package com.example.fabien;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OscilloEventsListener{
    OscilloManager mOscilloManager;
    TextView mtv;
    TextView cnct;
    Slider msl;
    Menu menu ;
    BTManager mBTManager;
    ActivityResultLauncher<Intent> connectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result->{
        if(result.getResultCode()== Activity.RESULT_OK){
            String adress=null;
            if(result.getData()!=null){
                adress=result.getData().getStringExtra("device");
            }
            if(adress !=null){
                mOscilloManager.connect(adress);
            }
        }
    });
    int state;
    private float rapcyc;

    public void requestAgain(){
        btPermissionLauncher.launch(new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN});
    }
    ActivityResultLauncher<String[]> btPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            result->
            {if(result.containsValue(false)){
                if(shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN)){
                    showDialog("Permission denied", "Autorisation Requise", "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    requestAgain();

                                }
                            }, "Non, quitter", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }, false


                    );
                }
                else{
                    Intent intentConnect = new Intent(MainActivity.this, bluetothview.class);
                    connectLauncher.launch(intentConnect);


                }
            }


            }
    );


    ActivityResultLauncher<Intent> BTonLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result ->
            {
                new ActivityResultCallback<ActivityResult>(){
                    @Override
                    public void onActivityResult(ActivityResult result){
                        if(result.getResultCode()==RESULT_CANCELED){
                            Toast.makeText(MainActivity.this,"le bluetooth doit etre activé!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        else{
                            activateAdapter();
                        }
                    }
                };
            }
            );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOscilloManager=new OscilloManager();
        mBTManager=new BTManager();
        mBTManager.setState(Transceiver.STATE_NOT_CONNECTED);
        mOscilloManager.attachTransceiver(mBTManager);
        mOscilloManager.setmOscilloEventsListener(this);

        mtv = findViewById(R.id.tv);
        cnct = findViewById(R.id.textView2);
        mtv.setTypeface(Typeface.MONOSPACE);
        cnct.setTypeface(Typeface.MONOSPACE);
        msl = findViewById(R.id.slider);
        mOscilloManager.setmOscilloEventsListener(this);
        if(savedInstanceState != null){
            mtv.setText(String.format("%.1f%%",savedInstanceState.getFloat("valeur")));
        }

        msl.setSliderChangeListener(new Slider.SliderChangeListener() {
            @Override
            public void onChange(float value) {
                mtv.setText(String.format("%.1f%%",value));
                setCalibrationDutyCycle(value);
            }

            @Override
            public void onDoubleClick(float value) {
                mtv.setText(String.format("%.1f%%",value));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        Toast.makeText(this, "menu", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuItem = item.getItemId();
        switch(menuItem){
            case(R.id.connect):
                Toast.makeText(this,"test du menu", Toast.LENGTH_SHORT);
                verifyPermission();

                break;

        }
        return super.onOptionsItemSelected(item);

    }

    private void verifyPermission(){
        if(BluetoothAdapter.getDefaultAdapter()==null){
            Toast.makeText(this,"pas de module bt ",Toast.LENGTH_SHORT);
            return;
        }
        if(!hasBTPermissions()){
            Log.i("Permissions","not granted");
            btPermissionLauncher.launch(new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT});
        }
        else{

            Intent intentConnect = new Intent(MainActivity.this, bluetothview.class);
            connectLauncher.launch(intentConnect);
        }
    }



    private boolean hasBTPermissions(){
        String[] permissions = new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN};
        for (String permission : permissions){
            if (ContextCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;

    }

    public void setCalibrationDutyCycle(float alpha){
        this.rapcyc=alpha;
    }

    private void activateAdapter(){
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
            Intent enableIntent= new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
            BTonLauncher.launch(enableIntent);
        }else{
            Intent intentConnect = new Intent(MainActivity.this, bluetothview.class);
            connectLauncher.launch(intentConnect);

        }

    }
    private AlertDialog showDialog(String title,
                                   String message,
                                   String positivelabel,
                                   DialogInterface.OnClickListener positiveOnClick,
                                   String negativelabel,
                                   DialogInterface.OnClickListener negativeOnClick,
                                   boolean isCancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(isCancelable);
        builder.setPositiveButton(positivelabel,positiveOnClick);
        builder.setNegativeButton(negativelabel,negativeOnClick);
        AlertDialog alert = builder.create();
        alert.show();
        return alert;
    }

    @Override
    public void onConnectionEstablished(String address) {

    }

    @Override
    public void onConnectionLost() {

    }

    @Override
    public void onConnectionError(String errorMessage) {

    }

    @Override
    public void onStateChanged(int state) {
        switch (state) {
            case 0:
                updateTextView("Aucun appareil connecté");
                break;
            case 1:
                updateTextView("Connexion en cours");
                break;
            case 2:
                updateTextView("Appareil connecté");
                break;
        }
    }

    private void updateTextView(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (cnct != null) {
                    cnct.setText(text);
                }
            }
        });
    }

}