package com.nuvoton.nudoorbell;

import android.app.FragmentTransaction;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.nuvoton.socketmanager.UDPSocketService;
import com.nuvoton.utility.EditDeviceDialogFragment;
import com.orm.SugarContext;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements AddDBFragment.SetDBInterface, EditDBFragment.EditDBInterface, EditDeviceDialogFragment.EditDeviceDialogInterface{
    private boolean isSetting = false;
    private int iterateCount = 0;
    private final String TAG = "grid";
    private GridView gridView;
    private SimpleAdapter adapter;
    private List<Map<String, Object>> items = new ArrayList<>();
    private List<DeviceData> deviceDataList;
    private int[] indicator = {R.mipmap.status_r, R.mipmap.status_g, R.mipmap.status_y, R.mipmap.status_n};
    private int[] button = {R.mipmap.db, R.mipmap.plus};
    private int index = 0;
    private UDPSocketService udpSocketService;
    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            udpSocketService = ((UDPSocketService.MyBinder) iBinder).getService();
            Log.d(TAG, "onServiceConnected: ");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            udpSocketService = null;
            Log.d(TAG, "onServiceDisconnected: ");
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putSerializable("items", (Serializable) items);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        items = (List) savedInstanceState.getSerializable("items");
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SugarContext.init(this);
        initElement();
        initUI();
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateIndicator();
            }
        }).start();
        Intent intent = new Intent(MainActivity.this, UDPSocketService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        if (isSetting){
            backFromSettingsFragment();
        }else{
            unbindService(serviceConnection);
            super.onBackPressed();
        }
    }

    void initUI(){
        gridView = (GridView) findViewById(R.id.main_page_gridview);
        gridView.setNumColumns(3);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), "OnItemClick" + items.get(i).toString(), Toast.LENGTH_SHORT).show();
                if (i==items.size() - 1){
                    index = i;
                    isSetting = true;
                    final Bundle bundle = new Bundle();
                    bundle.putString("serial", String.valueOf(i+1));
                    AddDBFragment mAddDBFragment = AddDBFragment.newInsatnce(bundle);
                    mAddDBFragment.setInterface(MainActivity.this);
                    FragmentTransaction trans = getFragmentManager().beginTransaction();
                    trans.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
                    trans.replace(android.R.id.content, mAddDBFragment).addToBackStack("db").commit();
                }else{
                    Map<String, Object> map = items.get(i);
                    long id = (long) map.get("ID");
                    Intent intent = new Intent(MainActivity.this, Streaming.class);
                    intent.putExtra("ID", id);
                    startActivity(intent);
                }
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                index = i;
                EditDeviceDialogFragment dialog = new EditDeviceDialogFragment();
                dialog.setInterface(MainActivity.this);
                dialog.setLabel("Selection");
                dialog.setType("Selection");
                dialog.setContent("Remove or enter setup?");
                dialog.show(getFragmentManager(), "Selection");
                return true;
            }
        });
    }

    void initElement(){
        try{
            deviceDataList = DeviceData.listAll(DeviceData.class);
            if (deviceDataList.size() == 0){
                DeviceData one = new DeviceData();
                one.save();
                Log.d(TAG, "initElement: one id=" + one.getId());
            }
        }catch (Exception e){
            DeviceData one = new DeviceData();
            one.save();
            deviceDataList = DeviceData.listAll(DeviceData.class);
        }

        Log.d(TAG, "initElement: " + deviceDataList);
        for (int i=0; i<deviceDataList.size(); i++){
            Map<String, Object> item = new HashMap<>();
            item.put("indicator", indicator[0]);
            item.put("button", button[0]);
            String text = deviceDataList.get(i).getDeviceType();
            item.put("text", text);
            long id = deviceDataList.get(i).getId();
            item.put("ID", id);
            items.add(item);
            Log.d(TAG, "initElement: " + items);
        }
        Map<String, Object> item = new HashMap<>();
        item.put("indicator", indicator[3]);
        item.put("button", button[1]);
        String text = "";
        item.put("text", text);
        items.add(item);
        setAdapter();
    }

    private void backFromSettingsFragment()
    {
        isSetting = false;
        Toast.makeText(this, "Device is not saved.", Toast.LENGTH_SHORT);
        getFragmentManager().popBackStackImmediate();
    }


    //MARK: AddDBDelegate
    @Override
    public void addNewDoorbell(String serial, String type, String name, String url) {
        DeviceData deviceData = new DeviceData();
        deviceData.setDeviceType(type);
        deviceData.setName(name);
        deviceData.setPublicIP(url);
        deviceData.setPrivateIP(url);
        deviceData.save();
        Log.d(TAG, "addNewDoorbell: sugar id=" + deviceData.getId());
        Map<String, Object> item = new HashMap<>();
        item.put("indicator", indicator[0]);
        item.put("button", button[0]);
        item.put("text", name);
        item.put("ID", deviceData.getId());
        item.put("PublicIP", deviceData.getPublicIP());
        items.add(items.size() -1, item);
        adapter.notifyDataSetChanged();
        initUI();
    }

    public void setAdapter(){
        Log.d(TAG, "before: " + items);
        adapter = new SimpleAdapter(this,
                items, R.layout.db_button, new String[]{"indicator", "button", "text"},
                new int[]{R.id.indicator_image, R.id.button_image, R.id.text});
        Log.d(TAG, "after: " + items);
    }

    //MARK: EditDialogInterface
    @Override
    public void removeDevice(String category) {
        Toast.makeText(getApplicationContext(), "OnLongClick: " + index, Toast.LENGTH_SHORT).show();
        if (index < items.size() - 1){
            Map<String, Object> item = items.get(index);
            long sugarID = (long) item.get("ID");
            DeviceData deviceData = DeviceData.findById(DeviceData.class, sugarID);
            deviceData.delete();
            items.remove(index);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void spinnerChosen(EditDeviceDialogFragment fragment, int index) {

    }

    @Override
    public void enterEditPage(String category) {
        Log.d(TAG, "enterEditPage: ");
        if (category.compareTo("Selection") == 0){
            final Bundle bundle = new Bundle();
            Map<String, Object> item = items.get(index);
            long sugarID = (long) item.get("ID");
            bundle.putLong("DeviceID", sugarID);
            EditDBFragment frag = EditDBFragment.newInsatnce(bundle);
            frag.setInterface(this);
            FragmentTransaction trans = getFragmentManager().beginTransaction();
            trans.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
            trans.replace(android.R.id.content, frag).addToBackStack("editDb").commit();
        }
    }

    @Override
    public void restartChosen(int index, String type) {

    }

    //MARK: Utility
    public void updateIndicator(){
        for (Map<String, Object> m: items) {
            try {
                String publicIP = (String)m.get("PublicIP");
                boolean isReachable = InetAddress.getByName(publicIP).isReachable(10);
                if (isReachable){
                    m.put("indicator", indicator[1]);
                }else{
                    m.put("indicator", indicator[0]);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged();
    }

    //MARK: EditDialogInterface
    @Override
    public void editDevice(String serial, String name, String type, String url) {

    }
}
