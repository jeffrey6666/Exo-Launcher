package myapplication.applauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    DrawerAdapter drawerAdapterObject;
    GridView drawerGrid;
    SlidingDrawer slidingDrawer;
    RelativeLayout homeView;

    PackageManager pm;
    protected static ArrayList<Application> apps;
    static boolean appLaunchable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apps = new ArrayList<>();

        drawerGrid = (GridView) findViewById(R.id.content);
        slidingDrawer = (SlidingDrawer) findViewById(R.id.drawer);
        homeView = (RelativeLayout) findViewById(R.id.home_view);
        pm = getPackageManager();
        getPackages();
        drawerAdapterObject = new DrawerAdapter(this, apps);
        drawerGrid.setAdapter(drawerAdapterObject);

        setDrawerListeners();
        setReceiver();
    }

    public void setReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(new Receiver(), filter);
    }

    public void getPackages() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> packages = pm.queryIntentActivities(mainIntent, 0);
        for (int i = 0; i < packages.size(); i++) {
            Application p = new Application();
            p.icon = packages.get(i).loadIcon(pm);
            p.name = packages.get(i).activityInfo.packageName;
            p.label = packages.get(i).loadLabel(pm).toString();
            apps.add(p);
        }
    }



    private void setDrawerListeners(){
        //Active dragging mode when long click at each Grid view item
        drawerGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView parent, View item, int position, long id) {
                MainActivity.appLaunchable=false;
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(item.getWidth(),item.getHeight());
                lp.leftMargin = (int) item.getX();
                lp.topMargin = (int) item.getY();

                LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout ll = (LinearLayout) li.inflate(R.layout.drawer_item, null);

                ImageView img = ((ImageView)item.findViewById(R.id.icon_image));
                TextView txt = ((TextView)item.findViewById(R.id.icon_text));

                ((ImageView)ll.findViewById(R.id.icon_image)).setImageDrawable(img.getDrawable());
                ((TextView)ll.findViewById(R.id.icon_text)).setText(txt.getText());

                homeView.addView(ll, lp);
                slidingDrawer.animateClose();
                slidingDrawer.bringToFront();
                return true;
            }
        });
        //Handling click event of each Grid view item
         drawerGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {

                PackageManager manager = getPackageManager();

                Intent i = manager.getLaunchIntentForPackage(apps.get(position).name);


                if (i != null) {
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(i);
                }
                else{
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Error")
                            .setMessage( apps.get(position).name + " could not open. Please try a different " +
                                    "app")
                            .setPositiveButton(android.R.string.yes, null)

                            .setIcon(apps.get(position).icon)
                            .show();
                }
            }
        });
    }

    public class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            getPackages();
            drawerAdapterObject = new DrawerAdapter(MainActivity.this, apps);
            drawerGrid.setAdapter(drawerAdapterObject);
            setDrawerListeners();
        }

    }

}