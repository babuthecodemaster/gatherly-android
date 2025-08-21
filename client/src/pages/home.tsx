import { useState, useEffect } from "react";
import ServerSidebar from "@/components/layout/server-sidebar";
import ChannelSidebar from "@/components/layout/channel-sidebar";
import ChatArea from "@/components/layout/chat-area";
import MembersSidebar from "@/components/layout/members-sidebar";
import { useIsMobile } from "@/hooks/use-mobile";
import { mockServers, getServerWithChannels } from "@/lib/mock-data";
import type { ServerWithChannels } from "@shared/schema";

export default function HomePage() {
  const [selectedServerId, setSelectedServerId] = useState<string>("default-server");
  const [selectedChannelId, setSelectedChannelId] = useState<string>("cosmic-gaming-channel");
  const [showMobileChannels, setShowMobileChannels] = useState(false);
  const isMobile = useIsMobile();

  const servers = mockServers;
  const currentServer = getServerWithChannels(selectedServerId);

  // Auto-select first available channel when server changes
  useEffect(() => {
    if (currentServer?.channels && currentServer.channels.length > 0) {
      setSelectedChannelId(currentServer.channels[0].id);
    }
  }, [currentServer]);

  const handleChannelSelect = (channelId: string) => {
    setSelectedChannelId(channelId);
    if (isMobile) {
      setShowMobileChannels(false);
    }
  };

  return (
    <div className="flex h-screen bg-cosmic-black text-white">
      {/* Server Sidebar */}
      <ServerSidebar
        servers={servers}
        selectedServerId={selectedServerId}
        onServerSelect={setSelectedServerId}
        data-testid="sidebar-servers"
      />

      {/* Channel Sidebar */}
      <ChannelSidebar
        server={currentServer as ServerWithChannels}
        selectedChannelId={selectedChannelId}
        onChannelSelect={handleChannelSelect}
        showMobile={showMobileChannels}
        onCloseMobile={() => setShowMobileChannels(false)}
        data-testid="sidebar-channels"
      />

      {/* Main Chat Area */}
      <div className="flex-1 flex flex-col">
        <ChatArea
          server={currentServer as ServerWithChannels}
          channelId={selectedChannelId}
          onToggleMobileChannels={() => setShowMobileChannels(true)}
          data-testid="area-chat"
        />
      </div>

      {/* Members Sidebar */}
      <MembersSidebar
        members={currentServer?.members || []}
        data-testid="sidebar-members"
      />
    </div>
  );
}
