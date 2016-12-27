package com.nuvoton.nudoorbell;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.nuvoton.utility.EditDeviceDialogFragment;
import com.orm.SugarContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SetDBFragment.SetDBInterface, EditDBFragment.EditDBInterface, EditDeviceDialogFragment.EditDeviceDialogInterface {
    private boolean isSetting = false;
    private final String TAG = "grid";
    private GridView gridView;
    private SimpleAdapter adapter;
    private List<Map<String, Object>> items = new ArrayList<>();
    private List<DeviceData> deviceDataList;
    private int[] indicator = {R.mipmap.status_r, R.mipmap.status_g, R.mipmap.status_y, R.mipmap.status_n};
    private int[] button = {R.mipmap.db, R.mipmap.plus};
    private int index = 0;

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
    }

    @Override
    public void onBackPressed() {
        if (isSetting){
            backFromSettingsFragment();
        }else{
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
                    SetDBFragment mSetDBFragment = SetDBFragment.newInsatnce(bundle);
                    mSetDBFragment.setInterface(MainActivity.this);
                    FragmentTransaction trans = getFragmentManager().beginTransaction();
                    trans.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
                    trans.replace(android.R.id.content, mSetDBFragment).addToBackStack("db").commit();
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


    //MARK: SetDBDelegate
    @Override
    public void addNewDoorbell(String serial, String type, String name, String url) {;
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
            deviceData.save();
            items.remove(index);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void spinnerChosen(EditDeviceDialogFragment fragment, int index) {

    }

    @Override
    public void editDB(String category) {
        if (category.compareTo("Selection") == 0){
            final Bundle bundle = new Bundle();
            bundle.putInt("Serial", index);
            EditDBFragment frag = EditDBFragment.newInsatnce(bundle);
            frag.setInterface(this);
            FragmentTransaction trans = getFragmentManager().beginTransaction();
            trans.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
            trans.replace(android.R.id.content, frag).addToBackStack("editDb").commit();
        }
    }

    //MARK: EditDialogInterface
    @Override
    public void editNewDoorbell(String serial, String name, String type, String url) {

    }
}
