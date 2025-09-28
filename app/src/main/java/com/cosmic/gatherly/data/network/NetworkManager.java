package com.cosmic.gatherly.data.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.CopyOnWriteArrayList;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class NetworkManager {
    private static final String TAG = "NetworkManager";
    private static NetworkManager instance;
    
    private ConnectivityManager connectivityManager;
    private BehaviorSubject<Boolean> networkStateSubject;
    private CopyOnWriteArrayList<NetworkStateListener> listeners;
    private boolean isConnected = false;
    
    public interface NetworkStateListener {
        void onNetworkAvailable();
        void onNetworkLost();
    }
    
    private NetworkManager(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkStateSubject = BehaviorSubject.createDefault(false);
        listeners = new CopyOnWriteArrayList<>();
        
        registerNetworkCallback();
        checkInitialNetworkState();
    }
    
    public static synchronized NetworkManager getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkManager(context.getApplicationContext());
        }
        return instance;
    }
    
    private void registerNetworkCallback() {
        if (connectivityManager == null) {
            Log.e(TAG, "ConnectivityManager is null");
            return;
        }
        
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        
        connectivityManager.registerNetworkCallback(builder.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                Log.d(TAG, "Network available");
                setNetworkState(true);
            }
            
            @Override
            public void onLost(@NonNull Network network) {
                Log.d(TAG, "Network lost");
                setNetworkState(false);
            }
            
            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                boolean hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                Log.d(TAG, "Network capabilities changed. Has internet: " + hasInternet);
                setNetworkState(hasInternet);
            }
        });
    }
    
    private void checkInitialNetworkState() {
        if (connectivityManager == null) {
            return;
        }
        
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            if (capabilities != null) {
                boolean hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                setNetworkState(hasInternet);
            }
        } else {
            setNetworkState(false);
        }
    }
    
    private void setNetworkState(boolean connected) {
        if (isConnected != connected) {
            isConnected = connected;
            networkStateSubject.onNext(connected);
            
            if (connected) {
                for (NetworkStateListener listener : listeners) {
                    listener.onNetworkAvailable();
                }
            } else {
                for (NetworkStateListener listener : listeners) {
                    listener.onNetworkLost();
                }
            }
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public Observable<Boolean> getNetworkStateObservable() {
        return networkStateSubject.distinctUntilChanged();
    }
    
    public void addListener(NetworkStateListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(NetworkStateListener listener) {
        listeners.remove(listener);
    }
    
    public void clearListeners() {
        listeners.clear();
    }
    
    public NetworkType getNetworkType() {
        if (connectivityManager == null) {
            return NetworkType.NONE;
        }
        
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            return NetworkType.NONE;
        }
        
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        if (capabilities == null) {
            return NetworkType.NONE;
        }
        
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return NetworkType.WIFI;
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return NetworkType.CELLULAR;
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return NetworkType.ETHERNET;
        }
        
        return NetworkType.OTHER;
    }
    
    public enum NetworkType {
        NONE, WIFI, CELLULAR, ETHERNET, OTHER
    }
}