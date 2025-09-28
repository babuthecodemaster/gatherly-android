package com.cosmic.gatherly.data.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";
    private static final String DEFAULT_SERVER_BASE_URL = "http://10.0.2.2:3000/";
    private static final int CONNECTION_TIMEOUT = 10000; // 10 seconds for connectivity check - increased
    private static final int READ_TIMEOUT = 15000; // 15 seconds for read timeout

    /**
     * Check if device has network connectivity with enhanced detection
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (connectivityManager == null) {
                Log.w(TAG, "ConnectivityManager is null");
                return false;
            }
            
            // Use modern API for Android M (API 23) and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network activeNetwork = connectivityManager.getActiveNetwork();
                if (activeNetwork == null) {
                    Log.d(TAG, "No active network found");
                    return false;
                }
                
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                if (networkCapabilities == null) {
                    Log.d(TAG, "Network capabilities are null");
                    return false;
                }
                
                boolean hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                boolean isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                
                Log.d(TAG, String.format("Network check - Has Internet: %s, Is Validated: %s", hasInternet, isValidated));
                return hasInternet && isValidated;
            } else {
                // Fallback for older Android versions
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
                Log.d(TAG, String.format("Legacy network check - Is Connected: %s", isConnected));
                return isConnected;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking network availability", e);
            return false;
        }
    }
    
    /**
     * Get detailed network connection type information
     */
    public static String getNetworkType(Context context) {
        try {
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (connectivityManager == null) {
                return "Unknown";
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network activeNetwork = connectivityManager.getActiveNetwork();
                if (activeNetwork == null) {
                    return "No Connection";
                }
                
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                if (networkCapabilities == null) {
                    return "Unknown";
                }
                
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return "WiFi";
                } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return "Cellular";
                } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return "Ethernet";
                } else {
                    return "Other";
                }
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo == null) {
                    return "No Connection";
                }
                return activeNetworkInfo.getTypeName();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting network type", e);
            return "Error";
        }
    }

    /**
     * Check if the server is reachable with enhanced error handling
     */
    public static boolean isServerReachable() {
        return isServerReachable(DEFAULT_SERVER_BASE_URL);
    }
    
    /**
     * Check if the server is reachable using context to get current configuration
     */
    public static boolean isServerReachable(Context context) {
        try {
            // Try to get server config, fallback to default if not available
            String baseUrl = DEFAULT_SERVER_BASE_URL;
            try {
                com.cosmic.gatherly.data.network.ServerConfig config = 
                    new com.cosmic.gatherly.data.network.ServerConfig(context);
                baseUrl = config.getBaseUrl();
            } catch (Exception e) {
                Log.w(TAG, "Could not get server config, using default URL", e);
            }
            
            return isServerReachable(baseUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error checking server reachability with context", e);
            return false;
        }
    }
    
    /**
     * Check if a specific server URL is reachable
     */
    public static boolean isServerReachable(String baseUrl) {
        try {
            URL url = new URL(baseUrl + "health");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            
            // Add user agent to avoid potential blocking
            connection.setRequestProperty("User-Agent", "Gatherly-Android-App/1.0");
            connection.setRequestProperty("Accept", "application/json");
            
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            Log.d(TAG, String.format("Server health check response: %d for URL: %s", responseCode, baseUrl));
            return responseCode >= 200 && responseCode < 300;
        } catch (IOException e) {
            Log.w(TAG, String.format("Server is not reachable at %s: %s", baseUrl, e.getMessage()));
            return false;
        } catch (Exception e) {
            Log.e(TAG, String.format("Error checking server reachability for %s", baseUrl), e);
            return false;
        }
    }
    
    /**
     * Asynchronously check server reachability with callback
     */
    public static CompletableFuture<Boolean> isServerReachableAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return isServerReachable();
            } catch (Exception e) {
                Log.e(TAG, "Error in async server reachability check", e);
                return false;
            }
        }).orTimeout(CONNECTION_TIMEOUT + READ_TIMEOUT, TimeUnit.MILLISECONDS)
          .exceptionally(throwable -> {
              Log.w(TAG, "Server reachability check timed out or failed", throwable);
              return false;
          });
    }

    /**
     * Get user-friendly network error message with detailed diagnostics
     */
    public static String getNetworkErrorMessage(Context context) {
        if (!isNetworkAvailable(context)) {
            String networkType = getNetworkType(context);
            Log.w(TAG, String.format("No network connection available. Network type: %s", networkType));
            return "No internet connection detected. Please check your " + networkType.toLowerCase() + " connection and try again.";
        }
        
        // Check server reachability asynchronously
        isServerReachableAsync().thenAccept(serverReachable -> {
            if (serverReachable) {
                Log.d(TAG, "✅ Server is reachable - network issue may be transient");
            } else {
                Log.w(TAG, "❌ Server is not reachable - may be down or network blocked");
            }
        });
        
        return "Connection timeout. The server may be temporarily unavailable. Please check your internet connection and try again.";
    }
    
    /**
     * Perform comprehensive network diagnostics
     */
    public static String performNetworkDiagnostics(Context context) {
        StringBuilder diagnostics = new StringBuilder("Network Diagnostics:\n");
        
        try {
            // Basic connectivity
            boolean hasNetwork = isNetworkAvailable(context);
            diagnostics.append("• Network Available: ").append(hasNetwork ? "✅ Yes" : "❌ No").append("\n");
            
            // Network type
            String networkType = getNetworkType(context);
            diagnostics.append("• Connection Type: ").append(networkType).append("\n");
            
            // Server reachability
            boolean serverReachable = isServerReachable();
            diagnostics.append("• Server Reachable: ").append(serverReachable ? "✅ Yes" : "❌ No").append("\n");
            
            // Additional network capabilities (Android M+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    Network activeNetwork = cm.getActiveNetwork();
                    if (activeNetwork != null) {
                        NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
                        if (caps != null) {
                            diagnostics.append("• Has Internet Capability: ")
                                      .append(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ? "✅ Yes" : "❌ No")
                                      .append("\n");
                            diagnostics.append("• Network Validated: ")
                                      .append(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ? "✅ Yes" : "❌ No")
                                      .append("\n");
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            diagnostics.append("• Error during diagnostics: ").append(e.getMessage()).append("\n");
            Log.e(TAG, "Error performing network diagnostics", e);
        }
        
        return diagnostics.toString();
    }

    /**
     * Check if error is likely due to server being down
     */
    public static boolean isServerError(Throwable error) {
        if (error == null) return false;
        
        String message = error.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("connection refused") ||
                   lowerMessage.contains("failed to connect") ||
                   lowerMessage.contains("unable to resolve host") ||
                   lowerMessage.contains("network is unreachable");
        }
        
        return error instanceof java.net.ConnectException ||
               error instanceof java.net.UnknownHostException;
    }
}