package com.njackson.test.virtualpebble;

import android.app.Service;
import android.content.Intent;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.njackson.Constants;
import com.njackson.application.modules.PebbleBikeModule;
import com.njackson.events.GPSService.NewLocation;
import com.njackson.gps.GPSService;
import com.njackson.test.application.TestApplication;
import com.njackson.utils.LocationEventConverter;
import com.njackson.virtualpebble.IMessageManager;
import com.njackson.virtualpebble.PebbleService;

import com.squareup.otto.Bus;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import android.content.SharedPreferences;
import android.location.LocationManager;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

public class PebbleServiceTest extends ServiceTestCase<PebbleService>{

    @Inject Bus _bus;
    @Inject IMessageManager _mockMessageManager;

    private PebbleService _service;
    private TestApplication _app;

    public PebbleServiceTest() {
        super(PebbleService.class);
    }

    @Module(
            includes = PebbleBikeModule.class,
            injects = PebbleServiceTest.class,
            overrides = true,
            complete = false
    )
    static class TestModule {
        @Provides
        @Singleton
        public IMessageManager providesMessageManager() {
            return mock(IMessageManager.class);
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _service = new PebbleService();

        System.setProperty("dexmaker.dexcache", getSystemContext().getCacheDir().getPath());

        _app = new TestApplication();
        _app.setObjectGraph(ObjectGraph.create(TestModule.class));
        _app.inject(this);
        _bus.register(this);

        setApplication(_app);
    }

    @Override
    public void tearDown() throws Exception {
        reset(_mockMessageManager);
        super.tearDown();
    }

    private void startService() {
        Intent startIntent = new Intent(getSystemContext(), PebbleService.class);
        startService(startIntent);
        _service = getService();
    }

    @SmallTest
    public void serviceSetsApplicationContext() {
        startService();
        Mockito.verify(_mockMessageManager,Mockito.times(1)).setContext(_app);
    }

    @SmallTest
    public void serviceRespondsToNewGPSLocation() {
        startService();

        ArgumentCaptor<PebbleDictionary> captor = new ArgumentCaptor<PebbleDictionary>();

        NewLocation event = new NewLocation();
        event.setUnits(0);
        event.setSpeed(45.4f);

        _bus.post(event);

        verify(_mockMessageManager, Mockito.times(1)).offer(captor.capture());
        PebbleDictionary dic = captor.getValue();

        byte[] data = dic.getBytes(Constants.PEBBLE_LOCTATION_DATA);
        assertEquals("Speed should be -9",-9,data[LocationEventConverter.BYTE_SPEED1]);
        assertEquals("Speed should be 3",3,data[LocationEventConverter.BYTE_SPEED2]);
    }
}