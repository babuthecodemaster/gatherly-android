import '@testing-library/jest-dom';

// Mock Socket.IO
vi.mock('socket.io-client', () => ({
  io: vi.fn(() => ({
    on: vi.fn(),
    off: vi.fn(),
    emit: vi.fn(),
    disconnect: vi.fn(),
    connected: true,
  })),
}));

// Mock Firebase
vi.mock('@/lib/firebase', () => ({
  auth: {
    currentUser: { uid: 'test-user-id', email: 'test@example.com' },
    onAuthStateChanged: vi.fn(),
    signOut: vi.fn(),
  },
  storage: {
    ref: vi.fn(() => ({
      put: vi.fn(() => Promise.resolve({ ref: { getDownloadURL: () => Promise.resolve('http://test-url.com') } })),
    })),
  },
}));

// Mock window.matchMedia for responsive tests
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});

// Mock ResizeObserver
global.ResizeObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

// Mock File API
global.File = class MockFile {
  constructor(public chunks: any[], public name: string, public options: any = {}) {}
  get size() { return this.chunks.reduce((acc, chunk) => acc + chunk.length, 0); }
  get type() { return this.options.type || ''; }
} as any;

global.FileReader = class MockFileReader {
  result: any = null;
  error: any = null;
  readyState: number = 0;
  onload: any = null;
  onerror: any = null;
  
  readAsDataURL(file: File) {
    setTimeout(() => {
      this.result = `data:${file.type};base64,mock-base64-data`;
      this.readyState = 2;
      if (this.onload) this.onload({ target: this });
    }, 0);
  }
} as any;