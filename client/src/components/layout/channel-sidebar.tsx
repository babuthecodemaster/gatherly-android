import { Button } from "@/components/ui/button";
import { Hash, Volume2, ChevronDown, Settings, Headphones, Mic, X, Menu } from "lucide-react";
import UserAvatar from "@/components/ui/user-avatar";
import { useAuth } from "@/hooks/use-auth";
import type { ServerWithChannels, Channel } from "@shared/schema";

interface ChannelSidebarProps {
  server?: ServerWithChannels;
  selectedChannelId: string;
  onChannelSelect: (channelId: string) => void;
  showMobile?: boolean;
  onCloseMobile?: () => void;
}

export default function ChannelSidebar({ 
  server, 
  selectedChannelId, 
  onChannelSelect, 
  showMobile = false,
  onCloseMobile 
}: ChannelSidebarProps) {
  const { user, logout } = useAuth();

  if (!server) {
    return (
      <div className="hidden md:flex w-60 bg-cosmic-navy flex-col border-r border-gray-800">
        <div className="flex-1 flex items-center justify-center">
          <div className="text-cosmic-gray">Select a server</div>
        </div>
      </div>
    );
  }

  const textChannels = server.channels?.filter(channel => channel.type === "text") || [];
  const voiceChannels = server.channels?.filter(channel => channel.type === "voice") || [];

  const sidebarContent = (
    <>
      {/* Server Header */}
      <div className="h-16 border-b border-gray-800 flex items-center px-4 cursor-pointer hover:bg-gray-800 transition-colors">
        {showMobile && onCloseMobile && (
          <Button
            variant="ghost"
            size="icon"
            onClick={onCloseMobile}
            className="mr-2 md:hidden"
            data-testid="button-close-mobile-channels"
          >
            <X className="w-4 h-4" />
          </Button>
        )}
        <h2 className="font-semibold text-lg flex-1" data-testid="text-server-name">
          {server.name}
        </h2>
        <ChevronDown className="w-4 h-4 text-cosmic-gray" />
      </div>

      {/* Channels List */}
      <div className="flex-1 overflow-y-auto p-2 scrollbar-cosmic">
        {/* Text Channels */}
        {textChannels.length > 0 && (
          <div className="mb-6">
            <div className="flex items-center text-cosmic-gray text-xs font-semibold uppercase tracking-wider mb-2 px-2">
              <ChevronDown className="w-3 h-3 mr-2" />
              Text Channels
            </div>
            
            <div className="space-y-1">
              {textChannels.map((channel) => (
                <Button
                  key={channel.id}
                  variant="ghost"
                  className={`w-full justify-start px-2 py-1 h-auto text-left ${
                    selectedChannelId === channel.id 
                      ? 'bg-gray-800 text-white' 
                      : 'text-cosmic-gray hover:text-white hover:bg-gray-800'
                  }`}
                  onClick={() => onChannelSelect(channel.id)}
                  data-testid={`button-channel-${channel.id}`}
                >
                  <Hash className={`w-4 h-4 mr-3 ${
                    selectedChannelId === channel.id ? 'text-cosmic-blue' : ''
                  }`} />
                  <span className="text-sm">{channel.name}</span>
                </Button>
              ))}
            </div>
          </div>
        )}

        {/* Voice Channels */}
        {voiceChannels.length > 0 && (
          <div className="mb-6">
            <div className="flex items-center text-cosmic-gray text-xs font-semibold uppercase tracking-wider mb-2 px-2">
              <ChevronDown className="w-3 h-3 mr-2" />
              Voice Channels
            </div>
            
            <div className="space-y-1">
              {voiceChannels.map((channel) => (
                <Button
                  key={channel.id}
                  variant="ghost"
                  className="w-full justify-start px-2 py-1 h-auto text-cosmic-gray hover:text-white hover:bg-gray-800"
                  onClick={() => {
                    // TODO: Implement voice channel joining
                    console.log("Joining voice channel:", channel.name);
                  }}
                  data-testid={`button-voice-channel-${channel.id}`}
                >
                  <Volume2 className="w-4 h-4 mr-3" />
                  <span className="text-sm">{channel.name}</span>
                </Button>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* User Panel */}
      {user && (
        <div className="h-16 border-t border-gray-800 flex items-center px-3">
          <div className="flex items-center flex-1">
            <UserAvatar 
              src={user.avatar} 
              fallback={user.username[0].toUpperCase()}
              className="w-8 h-8 mr-3 cosmic-glow"
              data-testid="avatar-current-user"
            />
            <div className="flex-1 min-w-0">
              <div className="text-sm font-medium truncate" data-testid="text-current-username">
                {user.username}
              </div>
              <div className="text-xs text-cosmic-gray">#{user.id.slice(-4)}</div>
            </div>
          </div>
          <div className="flex items-center space-x-1">
            <Button 
              variant="ghost" 
              size="icon"
              className="w-8 h-8 hover:bg-gray-700 flex items-center justify-center rounded-md"
              onClick={() => {
                // TODO: Toggle mute
                console.log("Toggle mute");
              }}
              data-testid="button-mute"
            >
              <Mic className="w-4 h-4 text-cosmic-gray" />
            </Button>
            <Button 
              variant="ghost" 
              size="icon"
              className="w-8 h-8 hover:bg-gray-700 flex items-center justify-center rounded-md"
              onClick={() => {
                // TODO: Toggle deafen
                console.log("Toggle deafen");
              }}
              data-testid="button-deafen"
            >
              <Headphones className="w-4 h-4 text-cosmic-gray" />
            </Button>
            <Button 
              variant="ghost" 
              size="icon"
              className="w-8 h-8 hover:bg-gray-700 flex items-center justify-center rounded-md"
              onClick={logout}
              data-testid="button-logout"
            >
              <Settings className="w-4 h-4 text-cosmic-gray" />
            </Button>
          </div>
        </div>
      )}
    </>
  );

  // Mobile overlay
  if (showMobile) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 z-50 md:hidden">
        <div className="w-80 bg-cosmic-navy h-full">
          {sidebarContent}
        </div>
      </div>
    );
  }

  // Desktop sidebar
  return (
    <div className="hidden md:flex w-60 bg-cosmic-navy flex-col border-r border-gray-800">
      {sidebarContent}
    </div>
  );
}
