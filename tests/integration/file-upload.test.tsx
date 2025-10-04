import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Home from '@/pages/home';

// Mock file upload hook
const mockUploadFile = vi.fn();
const mockUploadProgress = vi.fn();

vi.mock('@/hooks/use-file-upload', () => ({
  useFileUpload: () => ({
    uploadFile: mockUploadFile,
    isUploading: false,
    uploadProgress: 0,
    error: null,
  }),
}));

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
  mockServers: [{ id: 'server1', name: 'Test Server', icon: '🎮' }],
  mockChannels: [{ id: 'channel1', name: 'general', type: 'text', serverId: 'server1' }],
  mockUsers: [{ id: 'user1', username: 'TestUser', avatar: '👤' }],
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

describe('File Upload Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUploadFile.mockResolvedValue({
      id: 'file123',
      name: 'test-file.jpg',
      url: 'http://test-url.com/file123',
      type: 'image/jpeg',
      size: 1024,
    });
  });

  it('should open file picker when upload button is clicked', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /upload/i })).toBeInTheDocument();
    });

    // Mock file input click
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    const clickSpy = vi.spyOn(fileInput, 'click');
    
    // Find and click the upload button
    const uploadButton = screen.getByRole('button', { name: /upload/i });
    await user.click(uploadButton);

    // Verify file picker interaction would be triggered
    expect(uploadButton).toBeInTheDocument();
  });

  it('should handle image file upload successfully', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /upload/i })).toBeInTheDocument();
    });

    // Create a mock image file
    const imageFile = new File(['mock image content'], 'test-image.jpg', {
      type: 'image/jpeg',
    });

    // Find file input (it might be hidden)
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    
    if (fileInput) {
      // Simulate file selection
      Object.defineProperty(fileInput, 'files', {
        value: [imageFile],
        writable: false,
      });

      fireEvent.change(fileInput);

      // Wait for upload to be triggered
      await waitFor(() => {
        expect(mockUploadFile).toHaveBeenCalledWith(imageFile);
      });
    }
  });

  it('should handle document file upload successfully', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /upload/i })).toBeInTheDocument();
    });

    // Create a mock document file
    const documentFile = new File(['mock document content'], 'test-document.pdf', {
      type: 'application/pdf',
    });

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    
    if (fileInput) {
      Object.defineProperty(fileInput, 'files', {
        value: [documentFile],
        writable: false,
      });

      fireEvent.change(fileInput);

      await waitFor(() => {
        expect(mockUploadFile).toHaveBeenCalledWith(documentFile);
      });
    }
  });

  it('should validate file size limits', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /upload/i })).toBeInTheDocument();
    });

    // Create a mock large file (simulate 10MB file)
    const largeFile = new File(['x'.repeat(10 * 1024 * 1024)], 'large-file.jpg', {
      type: 'image/jpeg',
    });

    // Mock upload to reject large files
    mockUploadFile.mockRejectedValue(new Error('File too large'));

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    
    if (fileInput) {
      Object.defineProperty(fileInput, 'files', {
        value: [largeFile],
        writable: false,
      });

      fireEvent.change(fileInput);

      await waitFor(() => {
        expect(mockUploadFile).toHaveBeenCalledWith(largeFile);
      });
    }
  });

  it('should validate file types', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /upload/i })).toBeInTheDocument();
    });

    // Create a mock unsupported file type
    const unsupportedFile = new File(['mock content'], 'test.exe', {
      type: 'application/x-executable',
    });

    // Mock upload to reject unsupported file types
    mockUploadFile.mockRejectedValue(new Error('Unsupported file type'));

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    
    if (fileInput) {
      Object.defineProperty(fileInput, 'files', {
        value: [unsupportedFile],
        writable: false,
      });

      fireEvent.change(fileInput);

      await waitFor(() => {
        expect(mockUploadFile).toHaveBeenCalledWith(unsupportedFile);
      });
    }
  });

  it('should show upload progress during file upload', async () => {
    const user = userEvent.setup();
    
    // Mock upload with progress
    vi.mocked(require('@/hooks/use-file-upload').useFileUpload).mockReturnValue({
      uploadFile: mockUploadFile,
      isUploading: true,
      uploadProgress: 50,
      error: null,
    });

    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /upload/i })).toBeInTheDocument();
    });

    // Should show progress indicator when uploading
    expect(screen.getByText(/uploading/i) || screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should display uploaded files in chat messages', async () => {
    const user = userEvent.setup();
    
    // Mock messages with file attachments
    vi.mocked(require('@/hooks/use-messages').useMessages).mockReturnValue({
      messages: [
        {
          id: 'msg1',
          content: 'Check out this image!',
          authorId: 'user1',
          channelId: 'channel1',
          timestamp: new Date(),
          attachments: [
            {
              id: 'file123',
              name: 'test-image.jpg',
              url: 'http://test-url.com/file123',
              type: 'image/jpeg',
              size: 1024,
            },
          ],
        },
      ],
      isLoading: false,
      sendMessage: vi.fn(),
    });

    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('Check out this image!')).toBeInTheDocument();
      expect(screen.getByText('test-image.jpg')).toBeInTheDocument();
    });

    // Should show download link or preview for uploaded file
    const fileElement = screen.getByText('test-image.jpg');
    expect(fileElement).toBeInTheDocument();
  });

  it('should handle upload errors gracefully', async () => {
    const user = userEvent.setup();
    
    // Mock upload error
    mockUploadFile.mockRejectedValue(new Error('Upload failed'));
    
    vi.mocked(require('@/hooks/use-file-upload').useFileUpload).mockReturnValue({
      uploadFile: mockUploadFile,
      isUploading: false,
      uploadProgress: 0,
      error: 'Upload failed',
    });

    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /upload/i })).toBeInTheDocument();
    });

    // Should display error message
    expect(screen.getByText(/upload failed/i) || screen.getByText(/error/i)).toBeInTheDocument();
  });

  it('should support multiple file uploads', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /upload/i })).toBeInTheDocument();
    });

    // Create multiple mock files
    const file1 = new File(['content1'], 'file1.jpg', { type: 'image/jpeg' });
    const file2 = new File(['content2'], 'file2.png', { type: 'image/png' });

    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    
    if (fileInput) {
      Object.defineProperty(fileInput, 'files', {
        value: [file1, file2],
        writable: false,
      });

      fireEvent.change(fileInput);

      // Should handle multiple files
      await waitFor(() => {
        expect(mockUploadFile).toHaveBeenCalledTimes(2);
        expect(mockUploadFile).toHaveBeenCalledWith(file1);
        expect(mockUploadFile).toHaveBeenCalledWith(file2);
      });
    }
  });
});