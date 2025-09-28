import type { Server, Channel, MessageWithAuthor, ServerMember, UserSession } from "@shared/schema";

export const mockUser: UserSession = {
  id: "user-1",
  username: "CosmicExplorer",
  email: "cosmic@explorer.com",
  avatar: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop&crop=face",
  status: "online" as const,
};

export const mockServers: Server[] = [
  {
    id: "default-server",
    name: "Cosmic Gaming Hub",
    description: "The ultimate space for cosmic gamers",
    icon: "https://images.unsplash.com/photo-1446776877081-d282a0f896e2?w=100&h=100&fit=crop",
    ownerId: "user-1",
    createdAt: new Date("2024-01-01"),
  },
  {
    id: "space-explorers",
    name: "Space Explorers",
    description: "Exploring the cosmos together",
    icon: "https://images.unsplash.com/photo-1502134249126-9f3755a50d78?w=100&h=100&fit=crop",
    ownerId: "user-2",
    createdAt: new Date("2024-01-15"),
  },
  {
    id: "nebula-chat",
    name: "Nebula Chat",
    description: "Colorful conversations in the void",
    icon: "https://images.unsplash.com/photo-1419242902214-272b3f66ee7a?w=100&h=100&fit=crop",
    ownerId: "user-3",
    createdAt: new Date("2024-02-01"),
  },
];

export const mockChannels: Channel[] = [
  {
    id: "cosmic-gaming-channel",
    name: "general",
    description: "General discussion about cosmic gaming",
    type: "text",
    serverId: "default-server",
    createdAt: new Date("2024-01-01"),
  },
  {
    id: "announcements",
    name: "announcements",
    description: "Important server announcements",
    type: "text",
    serverId: "default-server",
    createdAt: new Date("2024-01-01"),
  },
  {
    id: "voice-general",
    name: "General Voice",
    description: "General voice chat",
    type: "voice",
    serverId: "default-server",
    createdAt: new Date("2024-01-01"),
  },
  {
    id: "space-talk",
    name: "space-talk",
    description: "Discuss the wonders of space",
    type: "text",
    serverId: "space-explorers",
    createdAt: new Date("2024-01-15"),
  },
  {
    id: "mission-planning",
    name: "mission-planning",
    description: "Plan your next space mission",
    type: "text",
    serverId: "space-explorers",
    createdAt: new Date("2024-01-15"),
  },
];

export const mockMessages: MessageWithAuthor[] = [
  {
    id: "msg-1",
    content: "Welcome to the Cosmic Gaming Hub! 🚀",
    authorId: "user-1",
    channelId: "cosmic-gaming-channel",
    attachments: [],
    reactions: [],
    createdAt: new Date("2024-03-15T10:00:00Z"),
    author: {
      id: "user-1",
      username: "CosmicExplorer",
      email: "cosmic@explorer.com",
      password: "hashed-password",
      avatar: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop&crop=face",
      status: "online",
      createdAt: new Date("2024-01-01"),
    },
  },
  {
    id: "msg-2",
    content: "This place looks amazing! The cosmic theme is perfect ✨",
    authorId: "user-2",
    channelId: "cosmic-gaming-channel",
    attachments: [],
    reactions: [],
    createdAt: new Date("2024-03-15T10:05:00Z"),
    author: {
      id: "user-2",
      username: "StarDust",
      email: "star@dust.com",
      password: "hashed-password",
      avatar: "https://images.unsplash.com/photo-1494790108755-2616b612b47c?w=100&h=100&fit=crop&crop=face",
      status: "online",
      createdAt: new Date("2024-01-01"),
    },
  },
  {
    id: "msg-3",
    content: "Anyone up for some cosmic gaming tonight? 🎮",
    authorId: "user-3",
    channelId: "cosmic-gaming-channel",
    attachments: [],
    reactions: [],
    createdAt: new Date("2024-03-15T10:10:00Z"),
    author: {
      id: "user-3",
      username: "NebulaNinja",
      email: "nebula@ninja.com",
      password: "hashed-password",
      avatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=face",
      status: "online",
      createdAt: new Date("2024-01-01"),
    },
  },
  {
    id: "msg-4",
    content: "I'm in! What game are we playing? 🎯",
    authorId: "user-4",
    channelId: "cosmic-gaming-channel",
    attachments: [],
    reactions: [],
    createdAt: new Date("2024-03-15T10:15:00Z"),
    author: {
      id: "user-4",
      username: "GalaxyGamer",
      email: "galaxy@gamer.com",
      password: "hashed-password",
      avatar: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&h=100&fit=crop&crop=face",
      status: "online",
      createdAt: new Date("2024-01-01"),
    },
  },
];

export const mockMembers: ServerMember[] = [
  {
    id: "member-1",
    userId: "user-1",
    serverId: "default-server",
    role: "owner",
    joinedAt: new Date("2024-01-01"),
  },
  {
    id: "member-2",
    userId: "user-2",
    serverId: "default-server",
    role: "member",
    joinedAt: new Date("2024-01-15"),
  },
  {
    id: "member-3",
    userId: "user-3",
    serverId: "default-server",
    role: "member",
    joinedAt: new Date("2024-02-01"),
  },
  {
    id: "member-4",
    userId: "user-4",
    serverId: "default-server",
    role: "member",
    joinedAt: new Date("2024-02-15"),
  },
];

export function getServerWithChannels(serverId: string) {
  const server = mockServers.find(s => s.id === serverId);
  if (!server) return null;

  const channels = mockChannels.filter(c => c.serverId === serverId);
  const members = mockMembers.filter(m => m.serverId === serverId).map(member => {
    const user = mockMessages.find(msg => msg.authorId === member.userId)?.author;
    return user ? { ...member, user } : null;
  }).filter((member): member is ServerMember & { user: any } => member !== null);
  
  return {
    ...server,
    channels,
    members,
  };
}

export function getChannelMessages(channelId: string) {
  return mockMessages.filter(m => m.channelId === channelId);
}

export function getServerMembers(serverId: string) {
  return mockMembers.filter(m => m.serverId === serverId);
}