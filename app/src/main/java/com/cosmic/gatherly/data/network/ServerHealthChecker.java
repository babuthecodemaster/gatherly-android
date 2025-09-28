package com.cosmic.gatherly.data.network;

import android.content.Context;
import android.util.Log;

import com.cosmic.gatherly.data.util.Logger;
import com.cosmic.gatherly.data.util.NetworkUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service for checking server health and connectivity status
 */
public class ServerHealthChecker {
    private static final String TAG = "ServerHealthChecker";
    
    // Server configuration - can be updated for different environments
    private static final String DEFAULT_BASE_URL = "http://10.0.2.2:5000/";
    private static final String PRODUCTION_BASE_URL = "https://your-production-server.com/";
    
    private static final int HEALTH_CHECK_TIMEOUT = 8000; // 8 seconds
    private static final int QUICK_CHECK_TIMEOUT = 3000; // 3 seconds for quick checks
    
    private final Context context;
    private String baseUrl;
    
    public ServerHealthChecker(Context context) {
        this.context = context;
        this.baseUrl = getConfiguredBaseUrl();
        Logger.d(Logger.TAG_NETWORK, "ServerHealthChecker initialized with base URL: %s", baseUrl);
    }
    
    /**
     * Get the configured base URL based on build type or configuration
     */
    private String getConfiguredBaseUrl() {
        // In a real app, this would check build config or shared preferences
        // For now, we'll use the development URL
        return DEFAULT_BASE_URL;
    }
    
    /**
     * Set custom base URL for testing or different environments
     */
    public void setBaseUrl(String baseUrl) {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
            Logger.i(Logger.TAG_NETWORK, "Base URL updated to: %s", this.baseUrl);
        }
    }
    
    /**
     * Perform comprehensive server health check
     */
    public CompletableFuture<ServerHealthStatus> checkServerHealth() {
        Logger.methodEntry(Logger.TAG_NETWORK, "checkServerHealth");
        
        return CompletableFuture.supplyAsync(() -> {
            Logger.startTiming("SERVER_HEALTH_CHECK");
            
            try {
                // For mock mode, always return healthy
                if (shouldUseMockMode()) {
                    Logger.i(Logger.TAG_NETWORK, "✅ Mock server health check - always healthy");
                    return new ServerHealthStatus(true, "Mock server is healthy", 
                        "Connected to mock server successfully");
                }
                
                // First check basic network connectivity
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    Logger.w(Logger.TAG_NETWORK, "No network connectivity available");
                    return new ServerHealthStatus(false, "No network connection", 
                        "Please check your internet connection and try again.");
                }
                
                // Check server health endpoint
                boolean isHealthy = checkHealthEndpoint();
                
                if (isHealthy) {
                    Logger.i(Logger.TAG_NETWORK, "✅ Server health check passed");
                    return new ServerHealthStatus(true, "Server is healthy", "Connected successfully");
                } else {
                    Logger.w(Logger.TAG_NETWORK, "❌ Server health check failed");
                    
                    // Try fallback check to API endpoint
                    boolean apiReachable = checkApiEndpoint();
                    if (apiReachable) {
                        Logger.i(Logger.TAG_NETWORK, "✅ API endpoint reachable (health endpoint may be missing)");
                        return new ServerHealthStatus(true, "Server is reachable", 
                            "Server is responding but health endpoint unavailable");
                    } else {
                        return new ServerHealthStatus(false, "Server unreachable", 
                            "Unable to connect to server. Please check if the server is running.");
                    }
                }
                
            } catch (Exception e) {
                Logger.e(Logger.TAG_NETWORK, "Error during server health check", e);
                return new ServerHealthStatus(false, "Health check error: " + e.getMessage(), 
                    "An error occurred while checking server status.");
            } finally {
                Logger.endTiming("SERVER_HEALTH_CHECK");
                Logger.methodExit(Logger.TAG_NETWORK, "checkServerHealth");
            }
        }).orTimeout(HEALTH_CHECK_TIMEOUT + 2000, TimeUnit.MILLISECONDS)
          .exceptionally(throwable -> {
              Logger.w(Logger.TAG_NETWORK, "Server health check timed out", throwable);
              return new ServerHealthStatus(false, "Health check timeout", 
                  "Server health check timed out. The server may be slow or unreachable.");
          });
    }
    
    /**
     * Check if we should use mock mode
     */
    private boolean shouldUseMockMode() {
        // Always use mock mode for now
        return true;
    }
    
    /**
     * Quick server reachability check (faster, less comprehensive)
     */
    public CompletableFuture<Boolean> isServerReachable() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // For mock mode, always return true
                if (shouldUseMockMode()) {
                    return true;
                }
                
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    return false;
                }
                
                return checkHealthEndpoint() || checkApiEndpoint();
                
            } catch (Exception e) {
                Logger.w(Logger.TAG_NETWORK, "Quick server reachability check failed", e);
                return false;
            }
        }).orTimeout(QUICK_CHECK_TIMEOUT, TimeUnit.MILLISECONDS)
          .exceptionally(throwable -> {
              Logger.w(Logger.TAG_NETWORK, "Quick server check timed out", throwable);
              return false;
          });
    }
    
    /**
     * Check the dedicated health endpoint
     */
    private boolean checkHealthEndpoint() {
        try {
            URL url = new URL(baseUrl + "health");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(HEALTH_CHECK_TIMEOUT);
            connection.setReadTimeout(HEALTH_CHECK_TIMEOUT);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            
            // Add headers
            connection.setRequestProperty("User-Agent", "Gatherly-Android-HealthCheck/1.0");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Cache-Control", "no-cache");
            
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            Logger.d(Logger.TAG_NETWORK, "Health endpoint response: %d", responseCode);
            return responseCode >= 200 && responseCode < 300;
            
        } catch (IOException e) {
            Logger.w(Logger.TAG_NETWORK, "Health endpoint check failed: %s", e.getMessage());
            return false;
        } catch (Exception e) {
            Logger.e(Logger.TAG_NETWORK, "Error checking health endpoint", e);
            return false;
        }
    }
    
    /**
     * Check API endpoint as fallback
     */
    private boolean checkApiEndpoint() {
        try {
            URL url = new URL(baseUrl + "api/health");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(HEALTH_CHECK_TIMEOUT);
            connection.setReadTimeout(HEALTH_CHECK_TIMEOUT);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            
            // Add headers
            connection.setRequestProperty("User-Agent", "Gatherly-Android-HealthCheck/1.0");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Cache-Control", "no-cache");
            
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            Logger.d(Logger.TAG_NETWORK, "API health endpoint response: %d", responseCode);
            return responseCode >= 200 && responseCode < 300;
            
        } catch (IOException e) {
            Logger.w(Logger.TAG_NETWORK, "API health endpoint check failed: %s", e.getMessage());
            return false;
        } catch (Exception e) {
            Logger.e(Logger.TAG_NETWORK, "Error checking API health endpoint", e);
            return false;
        }
    }
    
    /**
     * Get server status with detailed diagnostics
     */
    public CompletableFuture<String> getServerDiagnostics() {
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder diagnostics = new StringBuilder("Server Diagnostics:\n");
            
            try {
                // Network diagnostics
                diagnostics.append(NetworkUtils.performNetworkDiagnostics(context));
                diagnostics.append("\n");
                
                // Server-specific checks
                diagnostics.append("Server Connectivity:\n");
                diagnostics.append("• Base URL: ").append(baseUrl).append("\n");
                
                boolean healthEndpoint = checkHealthEndpoint();
                diagnostics.append("• Health Endpoint: ").append(healthEndpoint ? "✅ Reachable" : "❌ Unreachable").append("\n");
                
                boolean apiEndpoint = checkApiEndpoint();
                diagnostics.append("• API Health Endpoint: ").append(apiEndpoint ? "✅ Reachable" : "❌ Unreachable").append("\n");
                
                if (!healthEndpoint && !apiEndpoint) {
                    diagnostics.append("• Status: ❌ Server appears to be down or unreachable\n");
                    diagnostics.append("• Recommendation: Check if server is running and network allows connections\n");
                } else if (!healthEndpoint && apiEndpoint) {
                    diagnostics.append("• Status: ⚠️ Server is running but health endpoint may be missing\n");
                    diagnostics.append("• Recommendation: Server is functional for API calls\n");
                } else {
                    diagnostics.append("• Status: ✅ Server is healthy and reachable\n");
                }
                
            } catch (Exception e) {
                diagnostics.append("• Error during diagnostics: ").append(e.getMessage()).append("\n");
                Logger.e(TAG, "Error during server diagnostics", e);
            }
            
            return diagnostics.toString();
        });
    }
    
    /**
     * Server health status result
     */
    public static class ServerHealthStatus {
        private final boolean isHealthy;
        private final String statusMessage;
        private final String userMessage;
        
        public ServerHealthStatus(boolean isHealthy, String statusMessage, String userMessage) {
            this.isHealthy = isHealthy;
            this.statusMessage = statusMessage;
            this.userMessage = userMessage;
        }
        
        public boolean isHealthy() {
            return isHealthy;
        }
        
        public String getStatusMessage() {
            return statusMessage;
        }
        
        public String getUserMessage() {
            return userMessage;
        }
        
        @Override
        public String toString() {
            return String.format("ServerHealthStatus{healthy=%s, status='%s', message='%s'}", 
                isHealthy, statusMessage, userMessage);
        }
    }
}