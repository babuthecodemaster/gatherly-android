package com.cosmic.gatherly.data.api;

import android.content.Context;
import android.util.Log;

import com.cosmic.gatherly.data.network.ServerConfig;
import com.cosmic.gatherly.data.util.Logger;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    private static ServerConfig serverConfig = null;
    
    // Timeout configurations - significantly increased for better reliability and Firebase compatibility
    private static final int CONNECT_TIMEOUT = 60; // seconds - increased from 30
    private static final int READ_TIMEOUT = 120; // seconds - increased from 60
    private static final int WRITE_TIMEOUT = 120; // seconds - increased from 60

    public static Retrofit getClient(Context context) {
        if (retrofit == null || serverConfig == null) {
            if (serverConfig == null) {
                serverConfig = new ServerConfig(context);
            }
            
            String baseUrl = serverConfig.getBaseUrl();
            Logger.d(Logger.TAG_API, "Initializing Retrofit client with base URL: %s", baseUrl);
            
            // Enhanced logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> 
                Logger.d(Logger.TAG_API, "OkHttp: %s", message));
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new DetailedLoggingInterceptor())
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(new ErrorLoggingInterceptor())
                    .addInterceptor(new RetryInterceptor())
                    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
                    
            Logger.i(Logger.TAG_API, "Retrofit client initialized successfully");
        }
        return retrofit;
    }

    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            // Use mock service for development/testing
            if (shouldUseMockService(context)) {
                Logger.i(Logger.TAG_API, "Using Mock API Service for development");
                apiService = new MockApiService();
            } else {
                apiService = getClient(context).create(ApiService.class);
            }
        }
        return apiService;
    }
    
    /**
     * Determine if we should use mock service
     */
    private static boolean shouldUseMockService(Context context) {
        // Always use mock service for now to enable testing without server
        return true;
        
        // In the future, you can make this configurable:
        // return BuildConfig.DEBUG || 
        //        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        //               .getBoolean("use_mock_api", false);
    }
    
    /**
     * Get server configuration
     */
    public static ServerConfig getServerConfig(Context context) {
        if (serverConfig == null) {
            serverConfig = new ServerConfig(context);
        }
        return serverConfig;
    }

    /**
     * Update server configuration and reset client
     */
    public static void updateServerConfig(Context context, String baseUrl) {
        if (serverConfig == null) {
            serverConfig = new ServerConfig(context);
        }
        serverConfig.setCustomBaseUrl(baseUrl);
        resetClient();
        Logger.i(Logger.TAG_API, "Server configuration updated to: %s", baseUrl);
    }
    
    /**
     * Reset client instances to force recreation with new configuration
     */
    public static void resetClient() {
        retrofit = null;
        apiService = null;
        Logger.d(Logger.TAG_API, "API client instances reset");
    }
    
    /**
     * Detailed logging interceptor for comprehensive request/response logging
     */
    private static class DetailedLoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long startTime = System.currentTimeMillis();
            
            // Log request details
            try {
                String requestBody = null;
                if (request.body() != null) {
                    requestBody = bodyToString(request.body());
                }
                Logger.logApiRequest(request.method(), request.url().toString(), requestBody);
            } catch (Exception e) {
                Logger.w(Logger.TAG_API, "Failed to log request details", e);
            }
            
            Response response = null;
            try {
                response = chain.proceed(request);
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                // Log response details
                String responseBody = null;
                if (response.body() != null) {
                    ResponseBody body = response.body();
                    String content = body.string();
                    responseBody = content;
                    
                    // Recreate response body since we consumed it
                    response = response.newBuilder()
                        .body(ResponseBody.create(body.contentType(), content))
                        .build();
                }
                
                Logger.logApiResponse(request.method(), request.url().toString(), 
                    response.code(), responseBody, duration);
                
                return response;
                
            } catch (IOException e) {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                Logger.logNetworkError("API Request", e, request.url().toString());
                Logger.e(Logger.TAG_API, "Request failed after %dms: %s %s", 
                    duration, request.method(), request.url().toString(), e);
                throw e;
            }
        }
        
        private String bodyToString(RequestBody body) {
            try {
                Buffer buffer = new Buffer();
                body.writeTo(buffer);
                
                Charset charset = StandardCharsets.UTF_8;
                MediaType contentType = body.contentType();
                if (contentType != null) {
                    charset = contentType.charset(StandardCharsets.UTF_8);
                }
                
                return buffer.readString(charset);
            } catch (Exception e) {
                Logger.w(Logger.TAG_API, "Failed to read request body", e);
                return "[BODY_READ_ERROR]";
            }
        }
    }
    
    /**
     * Interceptor for logging network errors and responses
     */
    private static class ErrorLoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long startTime = System.currentTimeMillis();
            
            try {
                Response response = chain.proceed(request);
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                Logger.d(Logger.TAG_API, "Request to %s completed in %dms with status %d", 
                    request.url(), duration, response.code());
                
                if (!response.isSuccessful()) {
                    Logger.w(Logger.TAG_API, "HTTP Error %d for %s", response.code(), request.url());
                    
                    // Log additional error details for debugging
                    if (response.code() >= 500) {
                        Logger.e(Logger.TAG_API, "Server error detected: %d %s", 
                            response.code(), response.message());
                    } else if (response.code() == 401 || response.code() == 403) {
                        Logger.logSecurityEvent("Authentication/Authorization Error", 
                            "HTTP " + response.code() + " for " + request.url());
                    }
                }
                
                return response;
            } catch (IOException e) {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                Logger.logNetworkError("HTTP Request", e, request.url().toString());
                Logger.e(Logger.TAG_NETWORK, "Network error for %s after %dms: %s", 
                    request.url(), duration, e.getMessage(), e);
                throw e;
            }
        }
    }
    
    /**
     * Enhanced interceptor for retrying failed requests with comprehensive logging
     */
    private static class RetryInterceptor implements Interceptor {
        private static final int MAX_RETRIES = 2;
        private static final long RETRY_DELAY_MS = 1000;
        
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = null;
            IOException lastException = null;
            
            Logger.startTiming("API_REQUEST_" + request.method() + "_" + request.url().encodedPath());
            
            for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
                try {
                    if (attempt > 0) {
                        Logger.i(Logger.TAG_API, "🔄 Retrying request to %s (attempt %d/%d)", 
                            request.url(), attempt + 1, MAX_RETRIES + 1);
                        
                        // Add exponential backoff delay
                        long delay = RETRY_DELAY_MS * (long) Math.pow(2, attempt - 1);
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            Logger.e(Logger.TAG_API, "Request retry interrupted", e);
                            throw new IOException("Request interrupted during retry", e);
                        }
                    }
                    
                    response = chain.proceed(request);
                    
                    // Log successful retry
                    if (attempt > 0 && response.isSuccessful()) {
                        Logger.i(Logger.TAG_API, "✅ Request succeeded on retry attempt %d", attempt + 1);
                    }
                    
                    // Don't retry on successful responses or client errors (4xx)
                    if (response.isSuccessful() || (response.code() >= 400 && response.code() < 500)) {
                        Logger.endTiming("API_REQUEST_" + request.method() + "_" + request.url().encodedPath());
                        return response;
                    }
                    
                    // Log server error for retry consideration
                    Logger.w(Logger.TAG_API, "Server error %d on attempt %d, will retry if attempts remain", 
                        response.code(), attempt + 1);
                    
                    // Close the response body to avoid resource leaks
                    if (response.body() != null) {
                        response.body().close();
                    }
                    
                } catch (IOException e) {
                    lastException = e;
                    Logger.w(Logger.TAG_API, "Request attempt %d failed: %s", attempt + 1, e.getMessage());
                    
                    // Log specific error types
                    if (e instanceof java.net.SocketTimeoutException) {
                        Logger.w(Logger.TAG_API, "⏰ Timeout on attempt %d", attempt + 1);
                    } else if (e instanceof java.net.ConnectException) {
                        Logger.w(Logger.TAG_API, "🔌 Connection failed on attempt %d", attempt + 1);
                    }
                    
                    // Don't retry on the last attempt
                    if (attempt == MAX_RETRIES) {
                        Logger.e(Logger.TAG_API, "❌ All retry attempts exhausted for %s", request.url());
                        Logger.endTiming("API_REQUEST_" + request.method() + "_" + request.url().encodedPath());
                        throw e;
                    }
                }
            }
            
            // This should not happen, but just in case
            Logger.endTiming("API_REQUEST_" + request.method() + "_" + request.url().encodedPath());
            if (lastException != null) {
                throw lastException;
            }
            
            return response;
        }
    }
}