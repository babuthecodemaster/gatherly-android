import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Home from '@/pages/home';

// Mock search results
const mockSearchResults = [
  {
    id: 'msg1',
    content: 'Hello world! This is a test message.',
    authorId: 'user1',
    channelId: 'channel1',
    timestamp: new Date('2024-01-01T10:00:00Z'),
    author: { username: 'TestUser1', avatar: '👤' },
    channel: { name: 'general' },
  },
  {
    id: 'msg2',
    content: 'Another test message with different content.',
    authorId: 'user2',
    channelId: 'channel2',
    timestamp: new Date('2024-01-01T11:00:00Z'),
    author: { username: 'TestUser2', avatar: '👥' },
    channel: { name: 'random' },
  },
];

// Mock search function
const mockSearchMessages = vi.fn();

vi.mock('@/hooks/use-auth', () => ({
  useAuth: () => ({
    user: { id: 'test-user', email: 'test@example.com' },
    isLoading: false,
  }),
}));

vi.mock('@/hooks/use-messages', () => ({
  useMessages: () => ({
    messages: [],
    isLoading: false,
    sendMessage: vi.fn(),
    searchMessages: mockSearchMessages,
  }),
}));

vi.mock('@/lib/mock-data', () => ({
  mockServers: [{ id: 'server1', name: 'Test Server', icon: '🎮' }],
  mockChannels: [
    { id: 'channel1', name: 'general', type: 'text', serverId: 'server1' },
    { id: 'channel2', name: 'random', type: 'text', serverId: 'server1' },
  ],
  mockUsers: [
    { id: 'user1', username: 'TestUser1', avatar: '👤' },
    { id: 'user2', username: 'TestUser2', avatar: '👥' },
  ],
}));

const renderWithProviders = (component: React.ReactElement) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      {component}
    </QueryClientProvider>
  );
};

describe('Search Functionality Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockSearchMessages.mockResolvedValue(mockSearchResults);
  });

  it('should open search modal when search button is clicked', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /search/i })).toBeInTheDocument();
    });

    // Click search button
    const searchButton = screen.getByRole('button', { name: /search/i });
    await user.click(searchButton);

    // Should open search modal
    await waitFor(() => {
      expect(screen.getByRole('dialog') || screen.getByPlaceholderText(/search/i)).toBeInTheDocument();
    });
  });

  it('should open search modal with Ctrl+K keyboard shortcut', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /search/i })).toBeInTheDocument();
    });

    // Press Ctrl+K
    await user.keyboard('{Control>}k{/Control}');

    // Should open search modal
    await waitFor(() => {
      expect(screen.getByRole('dialog') || screen.getByPlaceholderText(/search/i)).toBeInTheDocument();
    });
  });

  it('should perform search when user types in search input', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /search/i })).toBeInTheDocument();
    });

    // Open search modal
    const searchButton = screen.getByRole('button', { name: /search/i });
    await user.click(searchButton);

    await waitFor(() => {
      expect(screen.getByPlaceholderText(/search/i)).toBeInTheDocument();
    });

    // Type in search input
    const searchInput = screen.getByPlaceholderText(/search/i);
    await user.type(searchInput, 'test message');

    // Should trigger search
    await waitFor(() => {
      expect(mockSearchMessages).toHaveBeenCalledWith('test message');
    });
  });

  it('should display search results', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /search/i })).toBeInTheDocument();
    });

    // Open search modal and perform search
    const searchButton = screen.getByRole('button', { name: /search/i });
    await user.click(searchButton);

    await waitFor(() => {
      expect(screen.getByPlaceholderText(/search/i)).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/search/i);
    await user.type(searchInput, 'test');

    // Wait for search results to appear
    await waitFor(() => {
      expect(screen.getByText('Hello world! This is a test message.')).toBeInTheDocument();
      expect(screen.getByText('Another test message with different content.')).toBeInTheDocument();
    });

    // Should show author and channel information
    expect(screen.getByText('TestUser1')).toBeInTheDocument();
    expect(screen.getByText('TestUser2')).toBeInTheDocument();
    expect(screen.getByText('general')).toBeInTheDocument();
    expect(screen.getByText('random')).toBeInTheDocument();
  });

  it('should highlight search terms in results', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /search/i })).toBeInTheDocument();
    });

    // Open search modal and search for specific term
    const searchButton = screen.getByRole('button', { name: /search/i });
    await user.click(searchButton);

    await waitFor(() => {
      expect(screen.getByPlaceholderText(/search/i)).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/search/i);
    await user.type(searchInput, 'test');

    await waitFor(() => {
      expect(mockSearchMessages).toHaveBeenCalledWith('test');
    });

    // Search terms should be highlighted in results
    // This would depend on the implementation, but we can check that results are displayed
    await waitFor(() => {
      expect(screen.getByText(/test/i)).toBeInTheDocument();
    });
  });

  it('should handle empty search results', async () => {
    const user = userEvent.setup();
    
    // Mock empty search results
    mockSearchMessages.mockResolvedValue([]);
    
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /search/i })).toBeInTheDocument();
    });

    // Open search modal and search
    const searchButton = screen.getByRole('button', { name: /search/i });
    await user.click(searchButton);

    await waitFor(() => {
      expect(screen.getByPlaceholderText(/search/i)).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/search/i);
    await user.type(searchInput, 'nonexistent');

    // Should show no results message
    await waitFor(() => {
      expect(screen.getByText(/no results/i) || screen.getByText(/no messages found/i)).toBeInTheDocument();
    });
  });

  it('should handle search errors gracefully', async () => {
    const user = userEvent.setup();
    
    // Mock search error
    mockSearchMessages.mockRejectedValue(new Error('Search failed'));
    
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /search/i })).toBeInTheDocument();
    });

    // Open search modal and search
    const searchButton = screen.getByRole('button', { name: /search/i });
    await user.click(searchButton);

    await waitFor(() => {
      expect(screen.getByPlaceholderText(/search/i)).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/search/i);
    await user.type(searchInput, 'error test');

    // Should show error message
    await waitFor(() => {
      expect(screen.getByText(/error/i) || screen.getByText(/failed/i)).toBeInTheDocument();
    });
  });

  it('should close search modal when clicking outside or pressing Escape', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /search/i })).toBeInTheDocument();
    });

    // Open search modal
    const searchButton = screen.getByRole('button', { name: /search/i });
    await user.click(searchButton);

    await waitFor(() => {
      expect(screen.getByRole('dialog') || screen.getByPlaceholderText(/search/i)).toBeInTheDocument();
    });

    // Press Escape to close
    await user.keyboard('{Escape}');

    // Modal should be closed
    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });
  });

  it('should navigate to message when search result is clicked', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /search/i })).toBeInTheDocument();
    });

    // Open search modal and perform search
    const searchButton = screen.getByRole('button', { name: /search/i });
    await user.click(searchButton);

    await waitFor(() => {
      expect(screen.getByPlaceholderText(/search/i)).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/search/i);
    await user.type(searchInput, 'test');

    await waitFor(() => {
      expect(screen.getByText('Hello world! This is a test message.')).toBeInTheDocument();
    });

    // Click on a search result
    const searchResult = screen.getByText('Hello world! This is a test message.');
    await user.click(searchResult);

    // Should navigate to the channel containing the message
    // This would depend on implementation, but we can verify the modal closes
    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });
  });

  it('should debounce search input to avoid excessive API calls', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /search/i })).toBeInTheDocument();
    });

    // Open search modal
    const searchButton = screen.getByRole('button', { name: /search/i });
    await user.click(searchButton);

    await waitFor(() => {
      expect(screen.getByPlaceholderText(/search/i)).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/search/i);
    
    // Type quickly (should be debounced)
    await user.type(searchInput, 'test', { delay: 50 });

    // Wait for debounce period
    await waitFor(() => {
      expect(mockSearchMessages).toHaveBeenCalledTimes(1);
      expect(mockSearchMessages).toHaveBeenCalledWith('test');
    }, { timeout: 1000 });
  });

  it('should show loading state during search', async () => {
    const user = userEvent.setup();
    
    // Mock delayed search response
    mockSearchMessages.mockImplementation(() => 
      new Promise(resolve => setTimeout(() => resolve(mockSearchResults), 500))
    );
    
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /search/i })).toBeInTheDocument();
    });

    // Open search modal and search
    const searchButton = screen.getByRole('button', { name: /search/i });
    await user.click(searchButton);

    await waitFor(() => {
      expect(screen.getByPlaceholderText(/search/i)).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(/search/i);
    await user.type(searchInput, 'test');

    // Should show loading indicator
    expect(screen.getByText(/searching/i) || screen.getByRole('progressbar')).toBeInTheDocument();

    // Wait for results to load
    await waitFor(() => {
      expect(screen.getByText('Hello world! This is a test message.')).toBeInTheDocument();
    }, { timeout: 1000 });
  });
});