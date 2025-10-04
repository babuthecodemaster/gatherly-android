import { Button } from "@/components/ui/button";
import { Volume2, Mic, MicOff, Headphones, HeadphonesIcon, PhoneOff, Settings } from "lucide-react";
import { useVoiceChannel } from "@/hooks/use-voice-channel";
import UserAvatar from "@/components/ui/user-avatar";

interface VoiceChannelPanelProps {
  channelName: string;
}

export default function VoiceChannelPanel({ channelName }: VoiceChannelPanelProps) {
  const { voiceState, disconnectFromVoiceChannel, toggleMute, toggleDeafen } = useVoiceChannel();

  if (!voiceState.isConnected) {
    return null;
  }

  return (
    <div className="bg-cosmic-navy border-t border-gray-800 p-3">
      {/* Voice Channel Header */}
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center">
          <Volume2 className="w-4 h-4 text-green-500 mr-2" />
          <div>
            <div className="text-sm font-medium text-green-500">Voice Connected</div>
            <div className="text-xs text-cosmic-gray">{channelName}</div>
          </div>
        </div>
        <div className="flex items-center">
          {voiceState.connectionQuality && (
            <div className={`w-2 h-2 rounded-full mr-2 ${
              voiceState.connectionQuality === 'excellent' ? 'bg-green-500' :
              voiceState.connectionQuality === 'good' ? 'bg-yellow-500' : 'bg-red-500'
            }`} />
          )}
          <Button
            variant="ghost"
            size="icon"
            onClick={disconnectFromVoiceChannel}
            className="w-6 h-6 hover:bg-red-600 hover:text-white"
            data-testid="button-disconnect-voice"
          >
            <PhoneOff className="w-3 h-3" />
          </Button>
        </div>
      </div>

      {/* Connected Users */}
      {voiceState.connectedUsers.length > 0 && (
        <div className="mb-3">
          <div className="text-xs text-cosmic-gray uppercase tracking-wider mb-2">
            Connected ({voiceState.connectedUsers.length})
          </div>
          <div className="space-y-1">
            {voiceState.connectedUsers.map((user) => (
              <div key={user.userId} className="flex items-center text-sm">
                <UserAvatar
                  src=""
                  fallback={user.username[0].toUpperCase()}
                  className="w-6 h-6 mr-2"
                />
                <span className={`flex-1 ${user.isSpeaking ? 'text-green-400' : 'text-cosmic-gray'}`}>
                  {user.username}
                </span>
                <div className="flex items-center space-x-1">
                  {user.isMuted && <MicOff className="w-3 h-3 text-red-400" />}
                  {user.isDeafened && <HeadphonesIcon className="w-3 h-3 text-red-400" />}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Voice Controls */}
      <div className="flex items-center justify-center space-x-2">
        <Button
          variant="ghost"
          size="icon"
          onClick={toggleMute}
          className={`w-8 h-8 rounded-full ${
            voiceState.isMuted 
              ? 'bg-red-600 hover:bg-red-700 text-white' 
              : 'hover:bg-gray-700'
          }`}
          data-testid="button-toggle-mute"
        >
          {voiceState.isMuted ? (
            <MicOff className="w-4 h-4" />
          ) : (
            <Mic className="w-4 h-4" />
          )}
        </Button>
        
        <Button
          variant="ghost"
          size="icon"
          onClick={toggleDeafen}
          className={`w-8 h-8 rounded-full ${
            voiceState.isDeafened 
              ? 'bg-red-600 hover:bg-red-700 text-white' 
              : 'hover:bg-gray-700'
          }`}
          data-testid="button-toggle-deafen"
        >
          {voiceState.isDeafened ? (
            <HeadphonesIcon className="w-4 h-4" />
          ) : (
            <Headphones className="w-4 h-4" />
          )}
        </Button>

        <Button
          variant="ghost"
          size="icon"
          className="w-8 h-8 rounded-full hover:bg-gray-700"
          onClick={() => {
            // TODO: Implement voice settings
            console.log("Open voice settings");
          }}
          data-testid="button-voice-settings"
        >
          <Settings className="w-4 h-4" />
        </Button>
      </div>
    </div>
  );
}