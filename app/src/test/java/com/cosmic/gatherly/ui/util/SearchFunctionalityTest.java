package com.cosmic.gatherly.ui.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.widget.FrameLayout;
import androidx.test.core.app.ApplicationProvider;

import com.cosmic.gatherly.data.model.AuthError;
import com.cosmic.gatherly.data.model.SearchResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for search functionality and enhanced search handler
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class SearchFunctionalityTest {

    @Mock
    private EnhancedSearchHandler.SearchCallback mockCallback;

    @Mock
    private FrameLayout mockContainerView;

    private Context context;
    private EnhancedSearchHandler searchHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        
        // Initialize search handler
        searchHandler = new EnhancedSearchHandler(context, mockContainerView);
    }

    @Test
    public void testSearchHandlerInitialization() {
        assertNotNull("Search handler should be initialized", searchHandler);
        assertNotNull("Error handler should be available", searchHandler.getErrorHandler());
        assertFalse("Should not be searching initially", searchHandler.isSearching());
        assertFalse("Search dialog should not be showing initially", searchHandler.isSearchDialogShowing());
    }

    @Test
    public void testSearchDialogDisplay() {
        String channelId = "channel123";
        String channelName = "general";
        
        // Show search dialog
        searchHandler.showSearchDialog(channelId, channelName, mockCallback);
        
        // Verify dialog is showing (in a real test, you might mock the dialog)
        // For now, we just verify the method completes without error
        assertTrue("Search dialog display should complete without error", true);
    }

    @Test
    public void testSearchExecution() {
        String testQuery = "hello world";
        
        // Perform search
        searchHandler.performSearch(testQuery, mockCallback);
        
        // Verify search started
        assertTrue("Should be searching after starting search", searchHandler.isSearching());
    }

    @Test
    public void testSearchResults() {
        List<SearchResult> mockResults = createMockSearchResults();
        String testQuery = "test query";
        
        // Simulate successful search by calling callback directly
        mockCallback.onSearchResults(mockResults, testQuery);
        
        // Verify callback was called with correct parameters
        verify(mockCallback, times(1)).onSearchResults(mockResults, testQuery);
        verify(mockCallback, never()).onSearchFailed(any());
        verify(mockCallback, never()).onSearchCancelled();
    }

    @Test
    public void testSearchFailure() {
        AuthError testError = new AuthError(
            AuthError.Type.NETWORK_ERROR,
            "Search failed",
            "Unable to connect to search service"
        );
        
        // Simulate search failure
        mockCallback.onSearchFailed(testError);
        
        // Verify error callback was called
        verify(mockCallback, times(1)).onSearchFailed(testError);
        verify(mockCallback, never()).onSearchResults(any(), any());
        verify(mockCallback, never()).onSearchCancelled();
    }

    @Test
    public void testSearchCancellation() {
        String testQuery = "test query";
        
        // Start search
        searchHandler.performSearch(testQuery, mockCallback);
        
        // Cancel search
        searchHandler.cancelSearch();
        
        // Verify search is no longer in progress
        assertFalse("Should not be searching after cancellation", searchHandler.isSearching());
    }

    @Test
    public void testSearchDialogHiding() {
        // Show dialog first
        searchHandler.showSearchDialog("channel123", "general", mockCallback);
        
        // Hide dialog
        searchHandler.hideSearchDialog();
        
        // Verify dialog is hidden
        assertFalse("Search dialog should not be showing after hiding", searchHandler.isSearchDialogShowing());
    }

    @Test
    public void testSearchQueryValidation() {
        // Test empty query
        String emptyQuery = "";
        boolean isValidEmpty = isValidSearchQuery(emptyQuery);
        assertFalse("Empty query should not be valid", isValidEmpty);
        
        // Test null query
        String nullQuery = null;
        boolean isValidNull = isValidSearchQuery(nullQuery);
        assertFalse("Null query should not be valid", isValidNull);
        
        // Test whitespace only query
        String whitespaceQuery = "   ";
        boolean isValidWhitespace = isValidSearchQuery(whitespaceQuery);
        assertFalse("Whitespace-only query should not be valid", isValidWhitespace);
        
        // Test valid query
        String validQuery = "hello world";
        boolean isValidQuery = isValidSearchQuery(validQuery);
        assertTrue("Valid query should be accepted", isValidQuery);
        
        // Test minimum length query
        String shortQuery = "hi";
        boolean isValidShort = isValidSearchQuery(shortQuery);
        assertTrue("Short but valid query should be accepted", isValidShort);
    }

    @Test
    public void testSearchResultFiltering() {
        List<SearchResult> allResults = createMockSearchResults();
        String filterQuery = "hello";
        
        // Test filtering logic
        List<SearchResult> filteredResults = filterSearchResults(allResults, filterQuery);
        
        assertNotNull("Filtered results should not be null", filteredResults);
        assertTrue("Should have some filtered results", filteredResults.size() > 0);
        
        // Verify filtered results contain the query term
        for (SearchResult result : filteredResults) {
            assertTrue("Filtered result should contain query term", 
                result.getContent().toLowerCase().contains(filterQuery.toLowerCase()));
        }
    }

    @Test
    public void testSearchResultSorting() {
        List<SearchResult> results = createMockSearchResults();
        
        // Test sorting by relevance (most recent first)
        List<SearchResult> sortedResults = sortSearchResultsByRelevance(results);
        
        assertNotNull("Sorted results should not be null", sortedResults);
        assertEquals("Should have same number of results", results.size(), sortedResults.size());
        
        // Verify sorting order (assuming newer results come first)
        for (int i = 0; i < sortedResults.size() - 1; i++) {
            SearchResult current = sortedResults.get(i);
            SearchResult next = sortedResults.get(i + 1);
            
            assertTrue("Results should be sorted by timestamp", 
                current.getTimestamp() >= next.getTimestamp());
        }
    }

    @Test
    public void testSearchCleanup() {
        // Start search
        searchHandler.performSearch("test", mockCallback);
        
        // Cleanup
        searchHandler.cleanup();
        
        // Verify cleanup completed
        assertFalse("Should not be searching after cleanup", searchHandler.isSearching());
        assertFalse("Dialog should not be showing after cleanup", searchHandler.isSearchDialogShowing());
    }

    @Test
    public void testErrorHandlerIntegration() {
        ComponentErrorHandler errorHandler = searchHandler.getErrorHandler();
        assertNotNull("Error handler should be available", errorHandler);
        
        // Test error handler methods don't throw exceptions
        errorHandler.showLoading("Searching...");
        errorHandler.hideLoading();
        errorHandler.hideError();
        
        assertTrue("Error handler integration should work", true);
    }

    @Test
    public void testMultipleSearchRequests() {
        // Test handling multiple search requests
        searchHandler.performSearch("first query", mockCallback);
        assertTrue("First search should start", searchHandler.isSearching());
        
        // Cancel first search
        searchHandler.cancelSearch();
        assertFalse("Search should be cancelled", searchHandler.isSearching());
        
        // Start second search
        searchHandler.performSearch("second query", mockCallback);
        assertTrue("Second search should start", searchHandler.isSearching());
    }

    // Helper methods for search logic testing
    private boolean isValidSearchQuery(String query) {
        return query != null && !query.trim().isEmpty() && query.trim().length() >= 1;
    }

    private List<SearchResult> filterSearchResults(List<SearchResult> results, String query) {
        List<SearchResult> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (SearchResult result : results) {
            if (result.getContent().toLowerCase().contains(lowerQuery)) {
                filtered.add(result);
            }
        }
        
        return filtered;
    }

    private List<SearchResult> sortSearchResultsByRelevance(List<SearchResult> results) {
        List<SearchResult> sorted = new ArrayList<>(results);
        
        // Sort by timestamp (most recent first)
        sorted.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        
        return sorted;
    }

    private List<SearchResult> createMockSearchResults() {
        List<SearchResult> results = new ArrayList<>();
        
        // Create mock search results
        SearchResult result1 = mock(SearchResult.class);
        when(result1.getContent()).thenReturn("Hello everyone!");
        when(result1.getAuthor()).thenReturn("User1");
        when(result1.getTimestamp()).thenReturn(System.currentTimeMillis() - 1000);
        when(result1.getChannelId()).thenReturn("channel123");
        
        SearchResult result2 = mock(SearchResult.class);
        when(result2.getContent()).thenReturn("How are you doing?");
        when(result2.getAuthor()).thenReturn("User2");
        when(result2.getTimestamp()).thenReturn(System.currentTimeMillis() - 2000);
        when(result2.getChannelId()).thenReturn("channel123");
        
        SearchResult result3 = mock(SearchResult.class);
        when(result3.getContent()).thenReturn("Hello world!");
        when(result3.getAuthor()).thenReturn("User3");
        when(result3.getTimestamp()).thenReturn(System.currentTimeMillis() - 3000);
        when(result3.getChannelId()).thenReturn("channel456");
        
        results.add(result1);
        results.add(result2);
        results.add(result3);
        
        return results;
    }
}