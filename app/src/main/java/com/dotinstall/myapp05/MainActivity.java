package com.dotinstall.myapp05;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.dotinstall.myapp05.R;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.dotinstall.myapp04.R;

public class MainActivity extends Activity implements Callback,
        OnItemClickListener {

    private static final int MSG_LIST = 100;

    private ListView mListView;
    private Handler mHandler = new Handler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.listView1);
        mListView.setOnItemClickListener(this);

        initList();
    }

    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
    //    getMenuInflater().inflate(R.menu.main, menu);
    //    return true;
    //}

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == MSG_LIST) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) msg.obj;
            SimpleAdapter mAdapter = new SimpleAdapter(this, list,
                    android.R.layout.simple_list_item_1,
                    new String[] { "title" }, new int[] { android.R.id.text1 });
            mListView.setAdapter(mAdapter);
        }
        return false;
    }

    private void initList() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                List<Map<String, Object>> list = getData();
                mHandler.sendMessage(mHandler.obtainMessage(MSG_LIST, list));
            }
        });
        t.start();

    }

    protected List<Map<String, Object>> getData() {

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_SAMPLE_CODE);

        PackageManager pm = getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(mainIntent, 0);

        List<Map<String, Object>> myData = new ArrayList<Map<String, Object>>();
        if (null == list)
            return myData;

        String pkgname = getPackageName();
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            CharSequence labelSeq = info.loadLabel(pm);
            String label = labelSeq.toString();
            if (info.activityInfo != null
                    && info.activityInfo.packageName.equals(pkgname)) {
                addItem(myData,
                        label,
                        activityIntent(
                                info.activityInfo.applicationInfo.packageName,
                                info.activityInfo.name));
            }
        }
        Collections.sort(myData, sDisplayNameComparator);
        return myData;
    }

    protected Intent browseIntent(String path) {
        Intent result = new Intent();
        result.setClass(this, MainActivity.class);
        result.putExtra(getPackageName(), path);
        return result;
    }

    protected void addItem(List<Map<String, Object>> data, String name,
                           Intent intent) {
        String cnamefull = intent.getComponent().getClassName();
        int pos = cnamefull.lastIndexOf('.');
        String cname = pos > 0 ? cnamefull.substring(pos + 1) : cnamefull;
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("title", String.format("%s [%s]", cname, name));
        temp.put("intent", intent);
        data.add(temp);
    }

    protected Intent activityIntent(String pkg, String componentName) {
        Intent result = new Intent();
        result.setClassName(pkg, componentName);
        return result;
    }

    private final static Comparator<Map<String, Object>> sDisplayNameComparator = new Comparator<Map<String, Object>>() {
        private final Collator collator = Collator.getInstance();

        public int compare(Map<String, Object> map1, Map<String, Object> map2) {
            return collator.compare(map1.get("title"), map2.get("title"));
        }
    };

    class MenuListItem {
        String mTitle;
        String mDescription;
        String mClassName;
        String mLink;
        boolean hasNext = false;
    }

    @Override
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        Map<String, Object> map = (Map<String, Object>) l
                .getItemAtPosition(position);

        Intent intent = (Intent) map.get("intent");
        startActivity(intent);

    }
}
