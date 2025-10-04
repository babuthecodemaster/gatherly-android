package com.cosmic.gatherly.ui.util;

import com.cosmic.gatherly.data.model.SearchResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock FallbackDataProvider class for testing purposes
 */
public class FallbackDataProvider {
    
    public List<SearchResult> getFallbackSearchResults(String query) {
        List<SearchResult> results = new ArrayList<>();
        results.add(new SearchResult("1", "Fallback search result", "System", System.currentTimeMillis(), "fallback"));
        return results;
    }
    
    public List<String> getFallbackChannelList() {
        List<String> channels = new ArrayList<>();
        channels.add("general");
        channels.add("random");
        return channels;
    }
    
    public List<String> getFallbackServerList() {
        List<String> servers = new ArrayList<>();
        servers.add("Default Server");
        return servers;
    }
}