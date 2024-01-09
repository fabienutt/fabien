package com.example.fabien;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

import android.os.Bundle;

public class bluetothview extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Set<String> foundList = new HashSet<String>();

    private final static int RN42_COD = 0x1F00;
    @SuppressLint("MissingPermission")
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent();
        mBlueToothAdapter.cancelDiscovery();
        String info = ((TextView) view).getText().toString();
        if (info.equals("aucun périphérique trouvé") || info.equals("pas de périphérique appairé")) {
            setResult(RESULT_CANCELED);
            finish();
            return;

        }
        if(info.length()>17){
            info=info.substring(info.length()-17);
            intent.putExtra("device",info);
            setResult(RESULT_OK,intent);
            finish();

        }
    }
    ;

    private enum Action {START, STOP}
    private Toolbar mToolBar;
    private Button mScann;

    private ListView mPairedList;
    private ListView mDiscoveredList;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean mBroadcastRegistered = false;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private BluetoothAdapter mBlueToothAdapter;
    private ArrayAdapter<String> mPairedAdapter;
    private ArrayAdapter<String> mDiscoveredAdapter;

    private ProgressBar mProgressBar;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetothview);

        // installation de la toolbar
        mToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);

        mProgressBar = findViewById(R.id.progress);

        // installation du listener de bouton scann
        mScann = findViewById(R.id.scan_button);

            /* TODO 7 : Mettre en place les listeners du bouton
               et des la listView
            */

        // installation des listeners d'item ListView
        mPairedList = findViewById(R.id.appaired_list);
        mDiscoveredList = findViewById(R.id.discovered_list);
        mDiscoveredList.setOnItemClickListener(this);

        // les adaptateurs associent une présentation à une liste
        mPairedAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mDiscoveredAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        mPairedList.setAdapter(mPairedAdapter);
        mDiscoveredList.setAdapter(mDiscoveredAdapter);

        // création de la liste des périphériques liés
        mBlueToothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBlueToothAdapter != null) {


            Set<BluetoothDevice> pairedDevices = mBlueToothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice pairedDevice : pairedDevices) {
                    mPairedAdapter.add(pairedDevice.getName() + "\n" + pairedDevice.getAddress());
                }
            } else {
                mPairedAdapter.add("pas de périphérique appairé");
            }

        }
            mScann.setOnClickListener(this);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        if (mDiscoveredAdapter.getCount() == 0) {
                            mDiscoveredAdapter.add("aucun peripherique trouvé");

                        }
                        //mScann.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if ((device.getBondState() != BluetoothDevice.BOND_BONDED) && device.getBluetoothClass().getDeviceClass() == RN42_COD) {
                            if (!foundList.contains(device.getAddress())) {
                                foundList.add(device.getAddress());
                                mDiscoveredAdapter.add(device.getName() + "\n" + device.getAddress());
                                mScann.setVisibility(View.GONE);
                            }
                        }

                }
            }

            ;
            /* TODO 1 : instanciation d'un broadcastreceiver (nommé mBroadcastReceiver) + redéfinition
                de la méthode onReceive.
                Explication :
                La méthode onReceive a pour but d'intercepter les 2 signaux BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                et BluetoothDevice.ACTION_FOUND.
               ii Pour BluetoothAdapter.ACTION_DISCOVERY_FINISHED, il faut :
                 1 - il faut compter les éléments de mDiscoveredAdapter et traiter le cas où rien n'a été trouvé
                 2 - Désactiver la progressBar
                Pour BluetoothAdapter.ACTION_DISCOVERY_FINISHED, il faut :
                 1 - Ajouter le device à la liste mDiscoveredAdapter, uniquement s'il n'est pas déjà dans la liste
                     des devices associés.
             */

        };


    }


    /* TODO 4 : Après avoir indiqué que la class BTConnectActivity implémentait l'interface
        View.OnClickListener, redéfinir la méthode onClick.
        Explication :
        Cette méthode est unique pour tous les boutons de l'activité et L'activité devenant ainsi le listener de tous
        les boutons, la méthode onClick est unique : elle doit donc discriminer elle même quel bouton a été appuyé
        La méthode view.getId() permet de connaitre le bouton à l'origine de l'appel de onClick.
          - Si l'interface BT est désactivée :
                1a - il faut l'activer ici (par une intention implicite BluetoothAdapter.ACTION_REQUEST_ENABLE)
          - Sinon :
                1a - on appelle ensuite la méthode toggleBtScan() pour activer ou stopper la recherche.
     */


    @SuppressLint("MissingPermission")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scan_button:
                if (!mBlueToothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                } else {

                    toggleBtScan();
                }
                break;

        }
    }



    /* TODO 3 : implémenter une méthode de bascule entre lancement du scann et arrêt.
       Explication :
       Cette méthode appellera btScan. Si le texte d'un bouton vaut Scanner, appeler btScan(START)
       sinon appeler btScan(STOP)
    */
        private void toggleBtScan() {

            if(mScann.getText().toString().equalsIgnoreCase("Scanner")){
                btScan(Action.START);
            }
            else{
                btScan(Action.STOP);
            }

        }

    /* TODO 2 :
        Créez une méthode btScan destinée à lancer une recherche BT ou à la stopper.
        Explication :
            1 - Définir une enumeration constante Action pouvant prendre la valeur START ou STOP.
                Elle sera utilisée comme argument de la fonction.
            2 - Si l'argument vaut START :
                 2a - Créer un filtre d'intention pour indiquer au BroadcastManager que notre application
                      souhaite être prévenue lorsque la recherche BT est terminée (BluetoothAdapter.ACTION_DISCOVERY_FINISHED).
                      Associer associer à ce filtre l'action à exécuter en cas de réception du message (registerReceiver).
                      L'action est celle programmée dans la méthode onReceive de notre broadcastReceiver
                      donc il suffit de fournir la référence vers notre broadcastReceiver.
                 2b - Idem pour le message BluetoothDevice.ACTION_FOUND
                 2c - Lancer la découverte (méthode startDiscovery()),
                 2d - Activer la progressBar et passer le texte du bouton à "Scanner"
              - Si l'argument vaut STOP :
                 2a - Stopper la recherche (méthode cancelDiscovery())
                 2b - Désactiver la progressBar et passer le texte du bouton à "Stopper"
                 2b - Dissocier le filtre pour chaque action

     */@SuppressLint("MissingPermission")
        private void btScan(Action startstop){

            if (startstop == Action.START){
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                registerReceiver(mBroadcastReceiver,filter);
                mBroadcastRegistered=true;
                mBlueToothAdapter.startDiscovery();
                mProgressBar.setVisibility(View.VISIBLE);
                mScann.setText("Stopper");
            }
            else if(startstop == Action.STOP){

                mBlueToothAdapter.cancelDiscovery();
                if(mBroadcastRegistered){
                    unregisterReceiver(mBroadcastReceiver);
                    mBroadcastRegistered=false;
                }
                mProgressBar.setVisibility(View.GONE);
                mScann.setText("Scanner");
            }
        }

    /* TODO 5 :
        L'intention d'activation du BT peut renvoyer un RESULT_OK ou un refus d'activation
        qu'il faut traiter ici.
        Explication :
        - Si l'utilisateur a accepté :
            1a - on lance ou on stoppe le scanne par toggleBtScan().
        - Sinon,
            1a - on affiche un toast et c'est tout.

     */


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // L'utilisateur a accepté l'activation du Bluetooth
                // Vous pouvez ici lancer ou arrêter la recherche Bluetooth
                toggleBtScan();
            } else {
                // L'utilisateur a refusé l'activation du Bluetooth
                // Afficher un toast ou effectuer d'autres actions nécessaires
                Toast.makeText(this, "Activation du Bluetooth refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }



    /* TODO 6 : Il s'agit ici de traiter la sélection d'un device sur lequel on veut se connecter.
        Après avoir indiqué que la class BTConnectActivity implémentait l'interface
        AdapterView.OnItemClickListener, redéfinir la méthode OnItemClick. Ne pas oublier que cette
        méthode est également appelée pour tous les listView de l'activité.
        Explications :
            1 - Stopper la recherche BT
            2 - Récupérer le nom du device cliqué par un getText sur le view passé en argument
            3 - Traiter le cas où le clic correspond à "Aucun élément appairé" ou "Aucun élément trouvé".
            Dans ce cas, le retour à l'activité principale doit s'accompagner d'un setResult(RESULT_CANCELED);
            et d'un finish()
            4 - Passer l'adresse du device à l'activité principale à l'aide du champ extra d'un intent en
            utilisant la méthode setResult(RESULT_OK, intent).
     */

   /* TODO 8 : Dans onPause s'assurer que la découverte BT est stoppée et que l'application est
               désabonnée des messages de broadcast.
    */

        // @Override
        // protected void onPause() {
        //    super.onPause();
        // }


    }