import { Button } from "@/components/ui/button";
import { Rocket, Plus } from "lucide-react";
import UserAvatar from "@/components/ui/user-avatar";
import type { Server } from "@shared/schema";

interface ServerSidebarProps {
  servers: Server[];
  selectedServerId: string;
  onServerSelect: (serverId: string) => void;
}

export default function ServerSidebar({ servers, selectedServerId, onServerSelect }: ServerSidebarProps) {
  const mockServerImages = [
    "https://images.unsplash.com/photo-1446776877081-d282a0f896e2?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&h=400",
    "https://images.unsplash.com/photo-1502134249126-9f3755a50d78?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&h=400",
    "https://images.unsplash.com/photo-1419242902214-272b3f66ee7a?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&h=400",
    "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&h=400",
    "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&h=400",
    "https://images.unsplash.com/photo-1531366936337-7c912a4589a7?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&h=400",
  ];

  return (
    <div className="w-16 lg:w-20 bg-cosmic-navy flex flex-col items-center py-4 space-y-4 border-r border-gray-800 nebula-bg">
      {/* Home Button */}
      <Button
        variant="ghost"
        size="icon"
        className="server-icon w-12 h-12 bg-gradient-to-br from-cosmic-blue to-cosmic-purple rounded-2xl cosmic-glow p-0"
        data-testid="button-home"
      >
        <Rocket className="w-6 h-6 text-white" />
      </Button>

      <div className="w-8 h-0.5 bg-gray-600 rounded-full"></div>

      {/* Server Icons */}
      <div className="space-y-3">
        {servers.map((server, index) => (
          <Button
            key={server.id}
            variant="ghost"
            size="icon"
            className={`server-icon w-12 h-12 rounded-2xl overflow-hidden p-0 ${
              selectedServerId === server.id ? 'ring-2 ring-cosmic-blue' : ''
            } glow-hover`}
            onClick={() => onServerSelect(server.id)}
            data-testid={`button-server-${server.id}`}
          >
            {server.icon ? (
              <img 
                src={server.icon} 
                alt={server.name}
                className="w-full h-full object-cover"
              />
            ) : (
              <img 
                src={mockServerImages[index % mockServerImages.length]} 
                alt={server.name}
                className="w-full h-full object-cover"
              />
            )}
          </Button>
        ))}
      </div>

      {/* Add Server Button */}
      <Button
        variant="ghost"
        size="icon"
        className="server-icon w-12 h-12 bg-gray-700 rounded-2xl hover:bg-cosmic-blue transition-colors p-0"
        data-testid="button-add-server"
      >
        <Plus className="w-6 h-6 text-cosmic-gray hover:text-white" />
      </Button>
    </div>
  );
}
