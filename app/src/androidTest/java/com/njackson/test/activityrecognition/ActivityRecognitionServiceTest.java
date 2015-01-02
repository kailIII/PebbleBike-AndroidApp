package com.njackson.test.activityrecognition;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionApi;
import com.njackson.activityrecognition.ActivityRecognitionService;
import com.njackson.application.modules.PebbleBikeModule;
import com.njackson.events.ActivityRecognitionService.CurrentState;
import com.njackson.test.application.TestApplication;
import com.njackson.utils.googleplay.IGooglePlayServices;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by njackson on 01/01/15.
 */
public class ActivityRecognitionServiceTest extends ServiceTestCase<ActivityRecognitionService> {

    @Inject Bus _bus;
    @Inject GoogleApiClient _googleApiClient;

    static IGooglePlayServices _playServices;

    private CurrentState _activityStatusEvent;
    private CountDownLatch _stateLatch;
    private ActivityRecognitionService _service;

    @Module(
            includes = PebbleBikeModule.class,
            injects = ActivityRecognitionServiceTest.class,
            overrides = true,
            complete = false
    )
    static class TestModule {
        @Provides IGooglePlayServices providesGooglePlayServices() { return _playServices; }
        @Provides @Singleton GoogleApiClient provideActivityRecognitionClient() { return mock(GoogleApiClient.class); }
    }

    /**
     * Constructor
     *
     * @param serviceClass The type of the service under test.
     */
    public ActivityRecognitionServiceTest(Class<ActivityRecognitionService> serviceClass) {
        super(serviceClass);
    }

    public ActivityRecognitionServiceTest() {
        super(ActivityRecognitionService.class);
    }

    @Subscribe
    public void onGPSStatusEvent(CurrentState event) {
        _activityStatusEvent = event;
        _stateLatch.countDown();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        System.setProperty("dexmaker.dexcache", getSystemContext().getCacheDir().getPath());

        _playServices = mock(IGooglePlayServices.class);

        TestApplication app = new TestApplication();
        app.setObjectGraph(ObjectGraph.create(TestModule.class));
        app.inject(this);
        _bus.register(this);

        setApplication(app);

        _stateLatch = new CountDownLatch(1);
    }

    private void startService() throws Exception {
        Intent startIntent = new Intent(getSystemContext(), ActivityRecognitionService.class);
        startService(startIntent);
        _service = getService();
        _stateLatch.await(2000, TimeUnit.MILLISECONDS);
    }

    @SmallTest
    public void testOnBindReturnsNull() throws Exception {
        startService();

        IBinder binder = _service.onBind(new Intent());

        assertNull(binder);
    }

    @SmallTest
    public void testGooglePlayDisableMessageReceived() throws Exception {
        when(_playServices.isGooglePlayServicesAvailable(any(ActivityRecognitionService.class))).thenReturn(ConnectionResult.API_UNAVAILABLE);
        startService();

        assertEquals(CurrentState.State.PLAY_SERVICES_NOT_AVAILABLE, _activityStatusEvent.getState());
    }

    @SmallTest
    public void testServiceStartedMessageReceived() throws Exception {
        when(_playServices.isGooglePlayServicesAvailable(any(ActivityRecognitionService.class))).thenReturn(ConnectionResult.SUCCESS);
        startService();

        assertEquals(CurrentState.State.STARTED, _activityStatusEvent.getState());
    }

    @SmallTest
    public void testRegistersActivityRecogntionConnectedEvent() throws Exception {
        when(_playServices.isGooglePlayServicesAvailable(any(ActivityRecognitionService.class))).thenReturn(ConnectionResult.SUCCESS);
        startService();

        verify(_googleApiClient,times(1)).registerConnectionCallbacks(any(ActivityRecognitionService.class));
    }

    @SmallTest
    public void testRegistersActivityRecogntionFailedEvent() throws Exception {
        when(_playServices.isGooglePlayServicesAvailable(any(ActivityRecognitionService.class))).thenReturn(ConnectionResult.SUCCESS);
        startService();

        verify(_googleApiClient,times(1)).registerConnectionFailedListener(any(ActivityRecognitionService.class));
    }

    @SmallTest
    public void testUnRegistersActivityRecogntionConnectedEvent() throws Exception {
        when(_playServices.isGooglePlayServicesAvailable(any(ActivityRecognitionService.class))).thenReturn(ConnectionResult.SUCCESS);
        startService();
        shutdownService();
        verify(_googleApiClient,times(1)).unregisterConnectionCallbacks(any(ActivityRecognitionService.class));
    }

    @SmallTest
    public void testUnRegistersActivityRecogntionFailedEvent() throws Exception {
        when(_playServices.isGooglePlayServicesAvailable(any(ActivityRecognitionService.class))).thenReturn(ConnectionResult.SUCCESS);
        startService();
        shutdownService();
        verify(_googleApiClient,times(1)).unregisterConnectionFailedListener(any(ActivityRecognitionService.class));
    }

    @SmallTest
    public void testRegistersActivityRecognitionUpdatesOnConnect() throws Exception {
        when(_playServices.isGooglePlayServicesAvailable(any(ActivityRecognitionService.class))).thenReturn(ConnectionResult.SUCCESS);
        startService();
        _service.onConnected(new Bundle());

        verify(_playServices,times(1)).requestActivityUpdates(any(GoogleApiClient.class), anyLong(), any(PendingIntent.class));
    }

    /*
    @SmallTest
    public void testRegistersActivityRecognitionUpdatesOnDisconnect() throws Exception {
        when(_playServices.isGooglePlayServicesAvailable(any(ActivityRecognitionService.class))).thenReturn(ConnectionResult.SUCCESS);
        startService();


        verify(_playServices, times(1)).removeActivityUpdates(any(GoogleApiClient.class), any(PendingIntent.class));
    }
    */
    @SmallTest
    public void testRegistersActivityRecognitionUpdatesOnDestroy() throws Exception {
        when(_playServices.isGooglePlayServicesAvailable(any(ActivityRecognitionService.class))).thenReturn(ConnectionResult.SUCCESS);
        startService();
        shutdownService();

        verify(_playServices,times(1)).removeActivityUpdates(any(GoogleApiClient.class), any(PendingIntent.class));
    }

}
