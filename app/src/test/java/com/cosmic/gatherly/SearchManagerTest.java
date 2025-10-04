package com.cosmic.gatherly;

import android.content.Context;

import com.cosmic.gatherly.data.manager.SearchManager;
import com.cosmic.gatherly.data.model.SearchResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class SearchManagerTest {

    @Mock
    private Context mockContext;
    
    private SearchManager searchManager;
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
        searchManager = SearchManager.getInstance(context);
    }

    @Test
    public void testSearchMessages_withValidQuery_returnsResults() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final List<SearchResult>[] results = new List[1];
        final String[] error = new String[1];

        searchManager.searchMessages("cosmic", new SearchManager.SearchCallback() {
            @Override
            public void onSearchResults(List<SearchResult> searchResults) {
                results[0] = searchResults;
                latch.countDown();
            }

            @Override
            public void onSearchError(String errorMessage) {
                error[0] = errorMessage;
                latch.countDown();
            }
        });

        assertTrue("Search should complete within 5 seconds", latch.await(5, TimeUnit.SECONDS));
        assertNull("Should not have error", error[0]);
        assertNotNull("Should have results", results[0]);
        assertTrue("Should find messages containing 'cosmic'", results[0].size() > 0);
        
        // Verify that results contain the search term
        boolean foundCosmicTerm = false;
        for (SearchResult result : results[0]) {
            if (result.getContent().toLowerCase().contains("cosmic") || 
                result.getAuthorName().toLowerCase().contains("cosmic")) {
                foundCosmicTerm = true;
                break;
            }
        }
        assertTrue("Results should contain the search term 'cosmic'", foundCosmicTerm);
    }

    @Test
    public void testSearchMessages_withEmptyQuery_returnsEmptyResults() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final List<SearchResult>[] results = new List[1];

        searchManager.searchMessages("", new SearchManager.SearchCallback() {
            @Override
            public void onSearchResults(List<SearchResult> searchResults) {
                results[0] = searchResults;
                latch.countDown();
            }

            @Override
            public void onSearchError(String errorMessage) {
                latch.countDown();
            }
        });

        assertTrue("Search should complete within 5 seconds", latch.await(5, TimeUnit.SECONDS));
        assertNotNull("Should have results", results[0]);
        assertEquals("Empty query should return empty results", 0, results[0].size());
    }

    @Test
    public void testSearchMessages_withNonExistentTerm_returnsEmptyResults() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final List<SearchResult>[] results = new List[1];

        searchManager.searchMessages("nonexistentterm12345", new SearchManager.SearchCallback() {
            @Override
            public void onSearchResults(List<SearchResult> searchResults) {
                results[0] = searchResults;
                latch.countDown();
            }

            @Override
            public void onSearchError(String errorMessage) {
                latch.countDown();
            }
        });

        assertTrue("Search should complete within 5 seconds", latch.await(5, TimeUnit.SECONDS));
        assertNotNull("Should have results", results[0]);
        assertEquals("Non-existent term should return empty results", 0, results[0].size());
    }

    @Test
    public void testSearchMessages_withChannelFilter_returnsFilteredResults() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final List<SearchResult>[] results = new List[1];

        searchManager.searchMessages("welcome", "announcements", new SearchManager.SearchCallback() {
            @Override
            public void onSearchResults(List<SearchResult> searchResults) {
                results[0] = searchResults;
                latch.countDown();
            }

            @Override
            public void onSearchError(String errorMessage) {
                latch.countDown();
            }
        });

        assertTrue("Search should complete within 5 seconds", latch.await(5, TimeUnit.SECONDS));
        assertNotNull("Should have results", results[0]);
        
        // Verify all results are from the specified channel
        for (SearchResult result : results[0]) {
            assertEquals("All results should be from announcements channel", 
                        "announcements", result.getChannelId());
        }
    }

    @Test
    public void testAddMessage_increasesSearchResults() throws InterruptedException {
        // First, search for a unique term that doesn't exist
        String uniqueTerm = "uniquetestmessage12345";
        
        CountDownLatch latch1 = new CountDownLatch(1);
        final List<SearchResult>[] initialResults = new List[1];

        searchManager.searchMessages(uniqueTerm, new SearchManager.SearchCallback() {
            @Override
            public void onSearchResults(List<SearchResult> searchResults) {
                initialResults[0] = searchResults;
                latch1.countDown();
            }

            @Override
            public void onSearchError(String errorMessage) {
                latch1.countDown();
            }
        });

        assertTrue("Initial search should complete", latch1.await(5, TimeUnit.SECONDS));
        assertEquals("Should initially have no results", 0, initialResults[0].size());

        // Add a message with the unique term
        SearchResult newMessage = new SearchResult(
            "test123",
            "This is a " + uniqueTerm + " for testing",
            "TestUser",
            "general",
            "general",
            new java.util.Date()
        );
        searchManager.addMessage(newMessage);

        // Search again
        CountDownLatch latch2 = new CountDownLatch(1);
        final List<SearchResult>[] newResults = new List[1];

        searchManager.searchMessages(uniqueTerm, new SearchManager.SearchCallback() {
            @Override
            public void onSearchResults(List<SearchResult> searchResults) {
                newResults[0] = searchResults;
                latch2.countDown();
            }

            @Override
            public void onSearchError(String errorMessage) {
                latch2.countDown();
            }
        });

        assertTrue("Second search should complete", latch2.await(5, TimeUnit.SECONDS));
        assertEquals("Should now have one result", 1, newResults[0].size());
        assertEquals("Result should match added message", newMessage.getContent(), newResults[0].get(0).getContent());
    }
}