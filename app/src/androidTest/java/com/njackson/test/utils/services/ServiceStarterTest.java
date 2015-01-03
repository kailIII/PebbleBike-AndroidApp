package com.njackson.test.utils.services;

import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.njackson.events.UI.StartButtonTouchedEvent;
import com.njackson.events.UI.StopButtonTouchedEvent;
import com.njackson.gps.GPSService;
import com.njackson.live.LiveService;
import com.njackson.test.testUtils.Services;
import com.njackson.utils.services.ServiceStarter;
import com.njackson.virtualpebble.PebbleService;

import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by njackson on 03/01/15.
 */
public class ServiceStarterTest extends AndroidTestCase {

    Context _mockContext;
    private ServiceStarter _serviceStarter;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());

        _mockContext = mock(Context.class);
        _serviceStarter = new ServiceStarter(_mockContext);
    }

    @SmallTest
    public void testStartsGPSService() throws Exception {
        _serviceStarter.startLocationServices();
        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);

        verify(_mockContext,times(3)).startService(intentArgumentCaptor.capture());

        List<Intent> intents = intentArgumentCaptor.getAllValues();
        assertTrue(checkComponentInCaptor(intents,GPSService.class));
    }

    @SmallTest
    public void testRespondsToStopButtonTouchedEventStopsGPS() throws Exception {
        _serviceStarter.stopLocationServices();
        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);

        verify(_mockContext,times(3)).stopService(intentArgumentCaptor.capture());

        List<Intent> intents = intentArgumentCaptor.getAllValues();
        assertTrue(checkComponentInCaptor(intents,GPSService.class));
    }

    @SmallTest
    public void testRespondsToStartButtonTouchedEventStartsPebbleBikeService() throws Exception {
        _serviceStarter.startLocationServices();
        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);

        verify(_mockContext,times(3)).startService(intentArgumentCaptor.capture());

        List<Intent> intents = intentArgumentCaptor.getAllValues();
        assertTrue(checkComponentInCaptor(intents,PebbleService.class));
    }

    @SmallTest
    public void testRespondsToStopButtonTouchedEventStopsPebbleBikeService() throws Exception {
        _serviceStarter.stopLocationServices();
        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);

        verify(_mockContext,times(3)).stopService(intentArgumentCaptor.capture());

        List<Intent> intents = intentArgumentCaptor.getAllValues();
        assertTrue(checkComponentInCaptor(intents,PebbleService.class));
    }

    @SmallTest
    public void testRespondsToStartButtonTouchedEventStartsLiveTrackingService() throws Exception {
        _serviceStarter.startLocationServices();
        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);

        verify(_mockContext,times(3)).startService(intentArgumentCaptor.capture());

        List<Intent> intents = intentArgumentCaptor.getAllValues();
        assertTrue(checkComponentInCaptor(intents,LiveService.class));
    }

    @SmallTest
    public void testRespondsToStopButtonTouchedEventStopsLiveTrackingService() throws Exception {
        _serviceStarter.stopLocationServices();
        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);

        verify(_mockContext,times(3)).stopService(intentArgumentCaptor.capture());

        List<Intent> intents = intentArgumentCaptor.getAllValues();
        assertTrue(checkComponentInCaptor(intents,LiveService.class));
    }

    private boolean checkComponentInCaptor(List<Intent> intents, Class<?> serviceClass) {
        for(Intent intent : intents) {
            if(intent.getComponent().getClassName().compareTo(serviceClass.getName()) == 0) {
                return true;
            }
        }

        return false;
    }

}
