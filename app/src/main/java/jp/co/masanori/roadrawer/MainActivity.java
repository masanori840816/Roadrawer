package jp.co.masanori.roadrawer;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity implements LocationListener
{
    private LocationManager _lcmLocationManager;
    private GoogleMap _ggmMap;
    private String _strBestProvider = "";
    private double _dblCurrentLatitude = 0;
    private double _dblCurrentLongitude = 0;

    private MainActivity _actMain;

    private Timer _tmrAddMarker;
    // タイマーで実行するクラス.
    private CtrlTimer _cttCtrlTimer;
    private CompoundButton _cmbSwtStart;

    private boolean _isTimerStarted = false;

    private final int MESSAGE_ADD_MARKER = 0;
    private Handler _hndBleHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MESSAGE_ADD_MARKER:
                    _actMain.addMarker(_actMain);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _actMain = new MainActivity();

        // 位置情報をコントロールするLocationManagerを取得.
        _lcmLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria crtCriteria = new Criteria();
        // 位置情報の精度を指定する.
        crtCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 消費電力を指定する.
        crtCriteria.setPowerRequirement(Criteria.POWER_MEDIUM);
/*    // 方位情報を取得可能にする.
    crtCriteria.setBearingRequired(true);
    // 方位情報の精度を指定する.
    crtCriteria.setBearingAccuracy(Criteria.ACCURACY_FINE);
    // 速度情報を取得可能にする.
    crtCriteria.setSpeedRequired(true);
    // 速度情報の精度を指定する.
    crtCriteria.setSpeedAccuracy(Criteria.ACCURACY_FINE);
    // 高度情報を取得可能にする.
    crtCriteria.setAltitudeRequired(true);
    // 高度情報の精度を指定する.
    crtCriteria.setVerticalAccuracy(Criteria.ACCURACY_FINE);*/
        // ロケーションプロバイダの取得
        _strBestProvider = _lcmLocationManager.getBestProvider(crtCriteria, true);

        // 記録開始用Switchの設定.
        _cmbSwtStart = (CompoundButton)findViewById(R.id.swtStart);
        _cmbSwtStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 状態が変更されたら実行.
                if(isChecked)
                {
                    if((_strBestProvider != null)
                            &&(_lcmLocationManager.isProviderEnabled(_strBestProvider)))
                    {
                        // LocationListenerを登録
                        _lcmLocationManager.requestLocationUpdates(_strBestProvider, 0, 0, _actMain);

                        // タイマーの設定.ストップすると破棄されるため、毎回生成.
                        _cttCtrlTimer = new CtrlTimer();
                        _tmrAddMarker = new Timer();
                        _tmrAddMarker.schedule(_cttCtrlTimer, 10000, 60000);
                        _isTimerStarted = true;
                    }
                    else
                    {
                        _cmbSwtStart.setChecked(false);
                    }
                }
                else
                {
                    if(_isTimerStarted)
                    {
                        // 位置情報の更新ストップ.
                        _lcmLocationManager.removeUpdates(_actMain);
                        // タイマーをストップ.
                        _tmrAddMarker.cancel();
                        _tmrAddMarker.purge();
                        //_cttCtrlTimer = null;
                        _cttCtrlTimer.cancel();
                    }
                }
            }
        });
        // マップの表示.
        this.showMap();
    }
/*  @Override
  public void onPause()
  {
    super.onPause();
    // 位置情報の更新ストップ.
    _lcmLocationManager.removeUpdates(_actMain);
    // タイマーをストップ.
    _tmrAddMarker.Cancel();
  }*/

    private void showMap()
    {
        if (_ggmMap == null)
        {
            _actMain._ggmMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        }
    }
    public void addMarker(MainActivity actMain)
    {
        if (actMain._ggmMap != null)
        {
            // 現在地にマーカーを追加する.
            actMain._ggmMap.addMarker(new MarkerOptions().position(
                    new LatLng(_dblCurrentLatitude, _dblCurrentLongitude)).title(
                    "Lat:" + _dblCurrentLatitude + " Lon:" + _dblCurrentLongitude));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onProviderEnabled(String str)
    {

    }
    @Override
    public void onProviderDisabled(String str)
    {

    }
    @Override
    public void onLocationChanged(Location lctNewLocation)
    {
        // 取得した緯度、経度を保持する.
        _dblCurrentLatitude = lctNewLocation.getLatitude();
        _dblCurrentLongitude = lctNewLocation.getLongitude();
        //_txtAltitude.setText("Speed:" + lctNewLocation.getSpeed() + " Bearing:" + lctNewLocation.getBearing() + " Altitude:" + lctNewLocation.getAltitude());

    }
    @Override
    public void onStatusChanged(String str, int in, Bundle bnd)
    {

    }
    public class CtrlTimer extends TimerTask
    {
        @Override
        public void run()
        {
            // マーカーの追加.
            //_actMain.addMarker(_actMain);
            _hndBleHandler.sendEmptyMessage(MESSAGE_ADD_MARKER);
        }
    }
}