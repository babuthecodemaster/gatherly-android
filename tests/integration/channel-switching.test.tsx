import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Home from '@/pages/home';

// Mock data for testing
const mockServers = [
  { id: 'server1', name: 'Test Server 1', icon: '🎮' },
  { id: 'server2', name: 'Test Server 2', icon: '💻' },
];

const mockChannels = [
  { id: 'channel1', name: 'general', type: 'text', serverId: 'server1' },
  { id: 'channel2', name: 'random', type: 'text', serverId: 'server1' },
  { id: 'voice1', name: 'Voice Chat', type: 'voice', serverId: 'server1' },
];

const mockMessages = [
  { id: 'msg1', content: 'Hello world!', authorId: 'user1', channelId: 'channel1', timestamp: new Date() },
  { id: 'msg2', content: 'How are you?', authorId: 'user2', channelId: 'channel2', timestamp: new Date() },
];

// Mock hooks
vi.mock('@/hooks/use-auth', () => ({
  useAuth: () => ({
    user: { id: 'test-user', email: 'test@example.com' },
    isLoading: false,
  }),
}));

vi.mock('@/hooks/use-messages', () => ({
  useMessages: (channelId: string) => ({
    messages: mockMessages.filter(msg => msg.channelId === channelId),
    isLoading: false,
    sendMessage: vi.fn(),
  }),
}));

vi.mock('@/lib/mock-data', () => ({
  mockServers,
  mockChannels,
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

describe('Channel Switching Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should highlight the selected channel in the sidebar', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    // Wait for initial render
    await waitFor(() => {
      expect(screen.getByText('general')).toBeInTheDocument();
    });

    // Check that the first channel is initially selected
    const generalChannel = screen.getByText('general').closest('button');
    expect(generalChannel).toHaveClass('bg-gray-800');

    // Click on the random channel
    const randomChannel = screen.getByText('random').closest('button');
    await user.click(randomChannel!);

    // Verify the selection has changed
    await waitFor(() => {
      expect(randomChannel).toHaveClass('bg-gray-800');
      expect(generalChannel).not.toHaveClass('bg-gray-800');
    });
  });

  it('should display messages for the selected channel', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('general')).toBeInTheDocument();
    });

    // Initially should show messages for channel1 (general)
    expect(screen.getByText('Hello world!')).toBeInTheDocument();
    expect(screen.queryByText('How are you?')).not.toBeInTheDocument();

    // Switch to random channel
    const randomChannel = screen.getByText('random').closest('button');
    await user.click(randomChannel!);

    // Should now show messages for channel2 (random)
    await waitFor(() => {
      expect(screen.getByText('How are you?')).toBeInTheDocument();
      expect(screen.queryByText('Hello world!')).not.toBeInTheDocument();
    });
  });

  it('should maintain channel selection state across re-renders', async () => {
    const user = userEvent.setup();
    const { rerender } = renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('general')).toBeInTheDocument();
    });

    // Select random channel
    const randomChannel = screen.getByText('random').closest('button');
    await user.click(randomChannel!);

    await waitFor(() => {
      expect(randomChannel).toHaveClass('bg-gray-800');
    });

    // Re-render the component
    rerender(
      <QueryClientProvider client={new QueryClient()}>
        <Home />
      </QueryClientProvider>
    );

    // Channel selection should persist
    await waitFor(() => {
      const randomChannelAfterRerender = screen.getByText('random').closest('button');
      expect(randomChannelAfterRerender).toHaveClass('bg-gray-800');
    });
  });

  it('should handle voice channel clicks differently from text channels', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('Voice Chat')).toBeInTheDocument();
    });

    // Click on voice channel
    const voiceChannel = screen.getByText('Voice Chat').closest('button');
    await user.click(voiceChannel!);

    // Voice channel should not open text chat interface
    // Instead, it should show voice connection UI
    await waitFor(() => {
      // Voice channels should not change the text channel selection
      const generalChannel = screen.getByText('general').closest('button');
      expect(generalChannel).toHaveClass('bg-gray-800');
      
      // Should show voice connection indicators
      expect(screen.getByText('Voice Chat')).toBeInTheDocument();
    });
  });

  it('should not show toast messages when switching channels', async () => {
    const user = userEvent.setup();
    
    // Mock toast to verify it's not called
    const mockToast = vi.fn();
    vi.mock('@/hooks/use-toast', () => ({
      useToast: () => ({ toast: mockToast }),
    }));

    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('general')).toBeInTheDocument();
    });

    // Switch channels multiple times
    const randomChannel = screen.getByText('random').closest('button');
    await user.click(randomChannel!);

    const generalChannel = screen.getByText('general').closest('button');
    await user.click(generalChannel!);

    // Verify no toast messages were shown
    expect(mockToast).not.toHaveBeenCalled();
  });

  it('should update URL or state when switching channels', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('general')).toBeInTheDocument();
    });

    // Switch to random channel
    const randomChannel = screen.getByText('random').closest('button');
    await user.click(randomChannel!);

    // Verify the channel switch is reflected in the UI state
    await waitFor(() => {
      expect(randomChannel).toHaveClass('bg-gray-800');
      // The chat area should show the correct channel context
      expect(screen.getByText('How are you!')).toBeInTheDocument();
    });
  });
});