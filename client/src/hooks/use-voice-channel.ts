import { useState, useCallback, createContext, useContext } from 'react';
import type { ReactNode } from 'react';

export interface VoiceChannelState {
  channelId: string | null;
  isConnected: boolean;
  isMuted: boolean;
  isDeafened: boolean;
  connectedUsers: {
    userId: string;
    username: string;
    isMuted: boolean;
    isDeafened: boolean;
    isSpeaking: boolean;
  }[];
  connectionQuality: 'excellent' | 'good' | 'poor' | null;
}

interface VoiceChannelContextType {
  voiceState: VoiceChannelState;
  connectToVoiceChannel: (channelId: string) => Promise<void>;
  disconnectFromVoiceChannel: () => void;
  toggleMute: () => void;
  toggleDeafen: () => void;
}

const VoiceChannelContext = createContext<VoiceChannelContextType | undefined>(undefined);

const initialVoiceState: VoiceChannelState = {
  channelId: null,
  isConnected: false,
  isMuted: false,
  isDeafened: false,
  connectedUsers: [],
  connectionQuality: null,
};

export function VoiceChannelProvider({ children }: { children: ReactNode }) {
  const [voiceState, setVoiceState] = useState<VoiceChannelState>(initialVoiceState);

  const connectToVoiceChannel = useCallback(async (channelId: string) => {
    try {
      // TODO: Implement actual voice connection logic
      // For now, simulate connection
      setVoiceState(prev => ({
        ...prev,
        channelId,
        isConnected: true,
        connectionQuality: 'excellent',
        connectedUsers: [
          // Mock current user as connected
          {
            userId: 'current-user',
            username: 'You',
            isMuted: false,
            isDeafened: false,
            isSpeaking: false,
          }
        ]
      }));
      
      console.log(`Connected to voice channel: ${channelId}`);
    } catch (error) {
      console.error('Failed to connect to voice channel:', error);
    }
  }, []);

  const disconnectFromVoiceChannel = useCallback(() => {
    setVoiceState(initialVoiceState);
    console.log('Disconnected from voice channel');
  }, []);

  const toggleMute = useCallback(() => {
    setVoiceState(prev => ({
      ...prev,
      isMuted: !prev.isMuted
    }));
  }, []);

  const toggleDeafen = useCallback(() => {
    setVoiceState(prev => ({
      ...prev,
      isDeafened: !prev.isDeafened,
      // When deafening, also mute
      isMuted: !prev.isDeafened ? true : prev.isMuted
    }));
  }, []);

  const value: VoiceChannelContextType = {
    voiceState,
    connectToVoiceChannel,
    disconnectFromVoiceChannel,
    toggleMute,
    toggleDeafen,
  };

  return (
    <VoiceChannelContext.Provider value={value}>
      {children}
    </VoiceChannelContext.Provider>
  );
}

export function useVoiceChannel() {
  const context = useContext(VoiceChannelContext);
  if (context === undefined) {
    throw new Error('useVoiceChannel must be used within a VoiceChannelProvider');
  }
  return context;
}