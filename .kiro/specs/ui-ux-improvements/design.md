# Design Document

## Overview

This design addresses critical UI/UX issues in the Gatherly chat application by implementing proper state management, visual feedback systems, and functional improvements. The solution focuses on React state management, component communication, and user interface enhancements while maintaining the existing cosmic theme and architecture.

## Architecture

The improvements will be implemented within the existing React-based frontend architecture:

- **State Management**: Utilize React's useState and useEffect hooks for local component state
- **Component Communication**: Implement proper prop passing and callback functions between parent and child components
- **Visual Feedback**: Add CSS classes and conditional rendering for visual indicators
- **File Upload**: Integrate HTML5 File API with existing message system
- **Event Handling**: Implement proper event handlers for button interactions

## Components and Interfaces

### 1. Channel Sidebar State Management

**Component**: `client/src/components/layout/channel-sidebar.tsx`

**Current Issue**: The sidebar doesn't properly reflect the selected channel state when switching between text channels.

**Design Solution**:
- Ensure the `selectedChannelId` prop is properly used in conditional styling
- Add proper CSS classes for active/inactive states
- Implement visual highlighting that persists across re-renders

**Interface Changes**:
```typescript
// Enhanced button styling logic
const getChannelButtonClass = (channelId: string, selectedChannelId: string) => {
  return `w-full justify-start px-2 py-1 h-auto text-left ${
    selectedChannelId === channelId 
      ? 'bg-gray-800 text-white border-l-2 border-cosmic-blue' 
      : 'text-cosmic-gray hover:text-white hover:bg-gray-800'
  }`;
};
```

### 2. Voice Channel Behavior Correction

**Component**: `client/src/components/layout/channel-sidebar.tsx`

**Current Issue**: Voice channels open text chat interfaces instead of voice functionality.

**Design Solution**:
- Implement separate click handlers for voice channels
- Add voice channel state management
- Create voice-specific UI components and states

**Interface Changes**:
```typescript
interface VoiceChannelState {
  isConnected: boolean;
  isMuted: boolean;
  isDeafened: boolean;
  connectedUsers: string[];
}

const handleVoiceChannelClick = (channelId: string) => {
  // Voice connection logic instead of text channel selection
  connectToVoiceChannel(channelId);
};
```

### 3. Button Functionality Implementation

**Components**: 
- `client/src/components/layout/chat-area.tsx` (search, members, mentions buttons)
- `client/src/components/layout/members-sidebar.tsx` (members display)

**Current Issue**: Multiple buttons are non-functional with only console.log statements.

**Design Solution**:
- Implement search functionality with modal/overlay
- Create members sidebar toggle functionality
- Add mentions/notifications system
- Implement proper error handling and loading states

**Interface Changes**:
```typescript
interface ChatAreaState {
  showMembersSidebar: boolean;
  showSearchModal: boolean;
  searchQuery: string;
  searchResults: Message[];
}
```

### 4. Server Selection Visual Indicator

**Component**: `client/src/components/layout/server-sidebar.tsx`

**Current Issue**: No visual indicator shows which server is currently active.

**Design Solution**:
- Add a vertical line indicator next to the active server
- Implement CSS positioning for the indicator
- Ensure the indicator moves smoothly when switching servers

**Interface Changes**:
```typescript
// Add visual indicator element
<div className={`absolute left-0 top-1/2 transform -translate-y-1/2 w-1 h-8 bg-cosmic-blue rounded-r-full transition-all duration-200 ${
  selectedServerId === server.id ? 'opacity-100' : 'opacity-0'
}`} />
```

### 5. Toast Message Removal

**Component**: `client/src/pages/home.tsx`

**Current Issue**: Toast messages appear when switching channels, disrupting user experience.

**Design Solution**:
- Remove or conditionally disable toast notifications for channel switches
- Implement silent state transitions
- Maintain toast functionality for important notifications only

### 6. File Upload Functionality

**Component**: `client/src/components/ui/message-input.tsx`

**Current Issue**: File upload buttons are non-functional.

**Design Solution**:
- Implement HTML5 File API integration
- Add file type validation and size limits
- Create upload progress indicators
- Integrate with existing message system

**Interface Changes**:
```typescript
interface FileUploadState {
  isUploading: boolean;
  uploadProgress: number;
  selectedFiles: File[];
}

interface UploadedFile {
  id: string;
  name: string;
  size: number;
  type: string;
  url: string;
}
```

## Data Models

### Enhanced Message Model
```typescript
interface Message {
  id: string;
  content: string;
  authorId: string;
  channelId: string;
  timestamp: Date;
  attachments?: UploadedFile[]; // New field for file attachments
  edited?: boolean;
  editedAt?: Date;
}
```

### Voice Channel State Model
```typescript
interface VoiceChannelState {
  channelId: string;
  connectedUsers: {
    userId: string;
    isMuted: boolean;
    isDeafened: boolean;
    isSpeaking: boolean;
  }[];
  isConnected: boolean;
  connectionQuality: 'excellent' | 'good' | 'poor';
}
```

### UI State Model
```typescript
interface UIState {
  selectedServerId: string;
  selectedChannelId: string;
  showMembersSidebar: boolean;
  showSearchModal: boolean;
  voiceChannelState?: VoiceChannelState;
  fileUploadState: FileUploadState;
}
```

## Error Handling

### File Upload Errors
- **File Size Limit**: Display user-friendly error for files exceeding size limits
- **File Type Validation**: Show supported file types when invalid files are selected
- **Network Errors**: Implement retry mechanism with exponential backoff
- **Storage Errors**: Handle server-side storage failures gracefully

### Voice Channel Errors
- **Connection Failures**: Display connection status and retry options
- **Permission Errors**: Handle microphone permission denials
- **Network Issues**: Show connection quality indicators

### General UI Errors
- **State Synchronization**: Implement error boundaries for component failures
- **Navigation Errors**: Fallback to safe states when navigation fails

## Testing Strategy

### Unit Tests
- Component rendering with different props
- State management logic
- Event handler functionality
- File upload validation logic

### Integration Tests
- Channel switching workflow
- Server selection with visual indicators
- File upload end-to-end process
- Voice channel connection flow

### Visual Regression Tests
- Server selection indicator positioning
- Channel highlighting states
- File upload progress indicators
- Responsive design on mobile devices

### User Experience Tests
- Channel switching without toast messages
- Smooth transitions between servers
- File upload progress feedback
- Voice channel connection feedback

## Implementation Considerations

### Performance
- Debounce search functionality to prevent excessive API calls
- Implement virtual scrolling for large member lists
- Optimize file upload with chunking for large files
- Use React.memo for components that don't need frequent re-renders

### Accessibility
- Ensure keyboard navigation works for all interactive elements
- Add proper ARIA labels for screen readers
- Implement focus management for modal dialogs
- Provide alternative text for uploaded images

### Mobile Responsiveness
- Ensure touch targets are appropriately sized
- Implement swipe gestures for mobile channel navigation
- Optimize file upload interface for mobile devices
- Test voice channel functionality on mobile browsers

### Security
- Validate file types on both client and server
- Implement file size limits to prevent abuse
- Sanitize file names to prevent XSS attacks
- Ensure proper authentication for file uploads