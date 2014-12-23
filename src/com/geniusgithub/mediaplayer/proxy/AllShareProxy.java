
package com.geniusgithub.mediaplayer.proxy;

import java.util.List;

import org.cybergarage.upnp.Device;
import org.cybergarage.util.CommonLog;
import org.cybergarage.util.LogFactory;

import com.geniusgithub.mediaplayer.center.DlnaService;
import com.geniusgithub.mediaplayer.upnp.AbstractMediaMng;
import com.geniusgithub.mediaplayer.upnp.MediaServerMng;
import com.geniusgithub.mediaplayer.upnp.UpnpUtil;

import android.content.Context;
import android.content.Intent;

public class AllShareProxy implements IDeviceOperator, IDeviceOperator.IDMRDeviceOperator {

    private static final CommonLog log = LogFactory.createLog();

    private static AllShareProxy instance;

    private Context mContext;

    private AbstractMediaMng dmsMediaMng;

    private AllShareProxy(Context context) {
        mContext = context;
        dmsMediaMng = new MediaServerMng(context);
    }

    public static synchronized AllShareProxy getInstance(Context context) {
        if (instance == null) {
            instance = new AllShareProxy(context);
        }
        return instance;
    }

    public void startSearch() {
        mContext.startService(new Intent(DlnaService.SEARCH_DEVICES));
    }

    public void resetSearch() {
        mContext.startService(new Intent(DlnaService.RESET_SEARCH_DEVICES));
        clearDevice();
    }

    public void exitSearch() {
        mContext.stopService(new Intent(mContext, DlnaService.class));
        clearDevice();
    }

    /**
     * 添加DMR Device
     */
    @Override
    public void addDevice(Device d) {
        if (UpnpUtil.isValidRenderDevice(d)) {
            dmsMediaMng.addDevice(d);
        }
    }

    @Override
    public void removeDevice(Device d) {
        if (UpnpUtil.isValidRenderDevice(d)) {
            dmsMediaMng.removeDevice(d);
        }
    }

    @Override
    public void clearDevice() {
        dmsMediaMng.clear();
    }

    @Override
    public List<Device> getDMRDeviceList() {
        return dmsMediaMng.getDeviceList();
    }

    @Override
    public void setDMRSelectedDevice(Device selectedDevice) {
        dmsMediaMng.setSelectedDevice(selectedDevice);
    }

    @Override
    public Device getDMRSelectedDevice() {
        return dmsMediaMng.getSelectedDevice();
    }

}
