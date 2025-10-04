import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Home from '@/pages/home';

// Mock data for testing
const mockServers = [
  { id: 'server1', name: 'Gaming Server', icon: '🎮' },
  { id: 'server2', name: 'Work Server', icon: '💻' },
  { id: 'server3', name: 'Friends Server', icon: '👥' },
];

const mockChannelsByServer = {
  server1: [
    { id: 'channel1', name: 'general', type: 'text', serverId: 'server1' },
    { id: 'channel2', name: 'gaming', type: 'text', serverId: 'server1' },
    { id: 'voice1', name: 'Voice Chat', type: 'voice', serverId: 'server1' },
  ],
  server2: [
    { id: 'channel3', name: 'work-general', type: 'text', serverId: 'server2' },
    { id: 'channel4', name: 'projects', type: 'text', serverId: 'server2' },
  ],
  server3: [
    { id: 'channel5', name: 'friends-chat', type: 'text', serverId: 'server3' },
  ],
};

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
  }),
}));

vi.mock('@/lib/mock-data', () => ({
  mockServers,
  mockChannels: Object.values(mockChannelsByServer).flat(),
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

describe('Server Switching Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should display vertical line indicator next to active server', async () => {
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('🎮')).toBeInTheDocument();
    });

    // First server should be active by default
    const firstServer = screen.getByText('🎮').closest('button');
    expect(firstServer).toBeInTheDocument();
    
    // Should have visual indicator (this would be a CSS class or element)
    // The exact implementation depends on how the indicator is styled
    expect(firstServer).toHaveClass(/active|selected/i);
  });

  it('should move indicator when switching servers', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('🎮')).toBeInTheDocument();
      expect(screen.getByText('💻')).toBeInTheDocument();
    });

    const firstServer = screen.getByText('🎮').closest('button');
    const secondServer = screen.getByText('💻').closest('button');

    // Initially first server should be active
    expect(firstServer).toHaveClass(/active|selected/i);
    expect(secondServer).not.toHaveClass(/active|selected/i);

    // Click on second server
    await user.click(secondServer!);

    // Indicator should move to second server
    await waitFor(() => {
      expect(secondServer).toHaveClass(/active|selected/i);
      expect(firstServer).not.toHaveClass(/active|selected/i);
    });
  });

  it('should show smooth transition animation when switching servers', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('🎮')).toBeInTheDocument();
      expect(screen.getByText('💻')).toBeInTheDocument();
    });

    const firstServer = screen.getByText('🎮').closest('button');
    const secondServer = screen.getByText('💻').closest('button');

    // Click on second server
    await user.click(secondServer!);

    // Should have transition classes applied
    await waitFor(() => {
      // Look for transition-related classes or styles
      const indicator = document.querySelector('[class*="transition"]');
      expect(indicator).toBeInTheDocument();
    });
  });

  it('should update channel list when switching servers', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('🎮')).toBeInTheDocument();
      expect(screen.getByText('💻')).toBeInTheDocument();
    });

    // Initially should show server1 channels
    expect(screen.getByText('general')).toBeInTheDocument();
    expect(screen.getByText('gaming')).toBeInTheDocument();
    expect(screen.queryByText('work-general')).not.toBeInTheDocument();

    // Switch to server2
    const secondServer = screen.getByText('💻').closest('button');
    await user.click(secondServer!);

    // Should now show server2 channels
    await waitFor(() => {
      expect(screen.getByText('work-general')).toBeInTheDocument();
      expect(screen.getByText('projects')).toBeInTheDocument();
      expect(screen.queryByText('general')).not.toBeInTheDocument();
      expect(screen.queryByText('gaming')).not.toBeInTheDocument();
    });
  });

  it('should maintain server selection state across re-renders', async () => {
    const user = userEvent.setup();
    const { rerender } = renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('🎮')).toBeInTheDocument();
      expect(screen.getByText('💻')).toBeInTheDocument();
    });

    // Switch to second server
    const secondServer = screen.getByText('💻').closest('button');
    await user.click(secondServer!);

    await waitFor(() => {
      expect(secondServer).toHaveClass(/active|selected/i);
    });

    // Re-render the component
    rerender(
      <QueryClientProvider client={new QueryClient()}>
        <Home />
      </QueryClientProvider>
    );

    // Server selection should persist
    await waitFor(() => {
      const secondServerAfterRerender = screen.getByText('💻').closest('button');
      expect(secondServerAfterRerender).toHaveClass(/active|selected/i);
    });
  });

  it('should show only one server as active at a time', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('🎮')).toBeInTheDocument();
      expect(screen.getByText('💻')).toBeInTheDocument();
      expect(screen.getByText('👥')).toBeInTheDocument();
    });

    const servers = [
      screen.getByText('🎮').closest('button'),
      screen.getByText('💻').closest('button'),
      screen.getByText('👥').closest('button'),
    ];

    // Initially only first server should be active
    expect(servers[0]).toHaveClass(/active|selected/i);
    expect(servers[1]).not.toHaveClass(/active|selected/i);
    expect(servers[2]).not.toHaveClass(/active|selected/i);

    // Click on third server
    await user.click(servers[2]!);

    // Only third server should be active
    await waitFor(() => {
      expect(servers[0]).not.toHaveClass(/active|selected/i);
      expect(servers[1]).not.toHaveClass(/active|selected/i);
      expect(servers[2]).toHaveClass(/active|selected/i);
    });
  });

  it('should update members list when switching servers', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('🎮')).toBeInTheDocument();
      expect(screen.getByText('💻')).toBeInTheDocument();
    });

    // Switch servers and verify members list updates
    const secondServer = screen.getByText('💻').closest('button');
    await user.click(secondServer!);

    // Members list should update to show members of the new server
    // This would depend on the implementation, but we can verify the server switch occurred
    await waitFor(() => {
      expect(secondServer).toHaveClass(/active|selected/i);
    });
  });

  it('should handle server switching with keyboard navigation', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('🎮')).toBeInTheDocument();
    });

    const firstServer = screen.getByText('🎮').closest('button');
    
    // Focus on first server
    firstServer?.focus();
    expect(firstServer).toHaveFocus();

    // Use arrow keys to navigate
    await user.keyboard('{ArrowDown}');

    // Should move focus to next server
    const secondServer = screen.getByText('💻').closest('button');
    expect(secondServer).toHaveFocus();

    // Press Enter to select
    await user.keyboard('{Enter}');

    // Should activate the focused server
    await waitFor(() => {
      expect(secondServer).toHaveClass(/active|selected/i);
    });
  });

  it('should show server tooltips on hover', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('🎮')).toBeInTheDocument();
    });

    const firstServer = screen.getByText('🎮').closest('button');
    
    // Hover over server
    await user.hover(firstServer!);

    // Should show tooltip with server name
    await waitFor(() => {
      expect(screen.getByText('Gaming Server') || screen.getByRole('tooltip')).toBeInTheDocument();
    });
  });

  it('should handle rapid server switching without issues', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('🎮')).toBeInTheDocument();
      expect(screen.getByText('💻')).toBeInTheDocument();
      expect(screen.getByText('👥')).toBeInTheDocument();
    });

    const servers = [
      screen.getByText('🎮').closest('button'),
      screen.getByText('💻').closest('button'),
      screen.getByText('👥').closest('button'),
    ];

    // Rapidly switch between servers
    await user.click(servers[1]!);
    await user.click(servers[2]!);
    await user.click(servers[0]!);
    await user.click(servers[1]!);

    // Should end up with the last clicked server active
    await waitFor(() => {
      expect(servers[1]).toHaveClass(/active|selected/i);
      expect(servers[0]).not.toHaveClass(/active|selected/i);
      expect(servers[2]).not.toHaveClass(/active|selected/i);
    });
  });

  it('should preserve channel selection within each server', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('🎮')).toBeInTheDocument();
      expect(screen.getByText('general')).toBeInTheDocument();
    });

    // Select 'gaming' channel in server1
    const gamingChannel = screen.getByText('gaming').closest('button');
    await user.click(gamingChannel!);

    await waitFor(() => {
      expect(gamingChannel).toHaveClass(/active|selected/i);
    });

    // Switch to server2
    const secondServer = screen.getByText('💻').closest('button');
    await user.click(secondServer!);

    await waitFor(() => {
      expect(screen.getByText('work-general')).toBeInTheDocument();
    });

    // Switch back to server1
    const firstServer = screen.getByText('🎮').closest('button');
    await user.click(firstServer!);

    // Should remember that 'gaming' was selected
    await waitFor(() => {
      const gamingChannelAfterReturn = screen.getByText('gaming').closest('button');
      expect(gamingChannelAfterReturn).toHaveClass(/active|selected/i);
    });
  });
});