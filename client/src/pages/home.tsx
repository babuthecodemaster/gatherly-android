import { useState, useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import ServerSidebar from "@/components/layout/server-sidebar";
import ChannelSidebar from "@/components/layout/channel-sidebar";
import ChatArea from "@/components/layout/chat-area";
import MembersSidebar from "@/components/layout/members-sidebar";
import { useIsMobile } from "@/hooks/use-mobile";
import type { ServerWithChannels } from "@shared/schema";

export default function HomePage() {
  const [selectedServerId, setSelectedServerId] = useState<string>("default-server");
  const [selectedChannelId, setSelectedChannelId] = useState<string>("cosmic-gaming-channel");
  const [showMobileChannels, setShowMobileChannels] = useState(false);
  const isMobile = useIsMobile();

  const { data: servers = [] } = useQuery({
    queryKey: ["/api/servers"],
    queryFn: async () => {
      const response = await fetch("/api/servers", {
        credentials: "include",
      });
      if (!response.ok) throw new Error("Failed to fetch servers");
      return response.json();
    },
  });

  const { data: currentServer } = useQuery({
    queryKey: ["/api/servers", selectedServerId],
    queryFn: async () => {
      const response = await fetch(`/api/servers/${selectedServerId}`, {
        credentials: "include",
      });
      if (!response.ok) throw new Error("Failed to fetch server");
      return response.json();
    },
    enabled: !!selectedServerId,
  });

  // Auto-select first available channel when server changes
  useEffect(() => {
    if (currentServer?.channels?.length > 0) {
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
