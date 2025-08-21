import { 
  type User, 
  type InsertUser, 
  type Server, 
  type InsertServer,
  type Channel,
  type InsertChannel,
  type Message,
  type InsertMessage,
  type ServerMember,
  type InsertServerMember,
  type MessageWithAuthor,
  type ServerWithChannels,
} from "@shared/schema";
import { randomUUID } from "crypto";

export interface IStorage {
  // User operations
  getUser(id: string): Promise<User | undefined>;
  getUserByUsername(username: string): Promise<User | undefined>;
  getUserByEmail(email: string): Promise<User | undefined>;
  createUser(user: InsertUser): Promise<User>;
  updateUserStatus(id: string, status: "online" | "offline" | "away" | "busy"): Promise<void>;
  
  // Server operations
  getServer(id: string): Promise<Server | undefined>;
  getServerWithChannels(id: string): Promise<ServerWithChannels | undefined>;
  getUserServers(userId: string): Promise<Server[]>;
  createServer(server: InsertServer): Promise<Server>;
  
  // Channel operations
  getChannel(id: string): Promise<Channel | undefined>;
  getServerChannels(serverId: string): Promise<Channel[]>;
  createChannel(channel: InsertChannel): Promise<Channel>;
  
  // Message operations
  getMessage(id: string): Promise<Message | undefined>;
  getChannelMessages(channelId: string, limit?: number): Promise<MessageWithAuthor[]>;
  createMessage(message: InsertMessage): Promise<MessageWithAuthor>;
  
  // Server member operations
  getServerMember(userId: string, serverId: string): Promise<ServerMember | undefined>;
  getServerMembers(serverId: string): Promise<(ServerMember & { user: User })[]>;
  addServerMember(member: InsertServerMember): Promise<ServerMember>;
  removeServerMember(userId: string, serverId: string): Promise<void>;
}

export class MemStorage implements IStorage {
  private users: Map<string, User>;
  private servers: Map<string, Server>;
  private channels: Map<string, Channel>;
  private messages: Map<string, Message>;
  private serverMembers: Map<string, ServerMember>;

  constructor() {
    this.users = new Map();
    this.servers = new Map();
    this.channels = new Map();
    this.messages = new Map();
    this.serverMembers = new Map();
    
    this.initializeDefaultData();
  }

  private initializeDefaultData() {
    // Create default server and channels for demo
    const defaultUser: User = {
      id: "default-user",
      username: "CosmicExplorer",
      email: "cosmic@example.com",
      password: "hashedpassword",
      avatar: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&h=400",
      status: "online",
      createdAt: new Date(),
    };
    this.users.set(defaultUser.id, defaultUser);

    const defaultServer: Server = {
      id: "default-server",
      name: "Cosmic Gaming Hub",
      description: "A community for space game enthusiasts",
      icon: "https://images.unsplash.com/photo-1446776877081-d282a0f896e2?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&h=400",
      ownerId: defaultUser.id,
      createdAt: new Date(),
    };
    this.servers.set(defaultServer.id, defaultServer);

    const defaultChannels: Channel[] = [
      {
        id: "general-channel",
        name: "general",
        description: "General discussion",
        type: "text",
        serverId: defaultServer.id,
        createdAt: new Date(),
      },
      {
        id: "cosmic-gaming-channel",
        name: "cosmic-gaming",
        description: "Discuss your favorite space games and cosmic adventures",
        type: "text",
        serverId: defaultServer.id,
        createdAt: new Date(),
      },
      {
        id: "space-exploration-channel",
        name: "space-exploration",
        description: "Share your space exploration experiences",
        type: "text",
        serverId: defaultServer.id,
        createdAt: new Date(),
      },
    ];

    defaultChannels.forEach(channel => {
      this.channels.set(channel.id, channel);
    });

    const serverMember: ServerMember = {
      id: "default-member",
      userId: defaultUser.id,
      serverId: defaultServer.id,
      role: "owner",
      joinedAt: new Date(),
    };
    this.serverMembers.set(serverMember.id, serverMember);
  }

  async getUser(id: string): Promise<User | undefined> {
    return this.users.get(id);
  }

  async getUserByUsername(username: string): Promise<User | undefined> {
    return Array.from(this.users.values()).find(user => user.username === username);
  }

  async getUserByEmail(email: string): Promise<User | undefined> {
    return Array.from(this.users.values()).find(user => user.email === email);
  }

  async createUser(insertUser: InsertUser): Promise<User> {
    const id = randomUUID();
    const user: User = { 
      ...insertUser, 
      id, 
      status: "offline",
      createdAt: new Date(),
    };
    this.users.set(id, user);
    return user;
  }

  async updateUserStatus(id: string, status: "online" | "offline" | "away" | "busy"): Promise<void> {
    const user = this.users.get(id);
    if (user) {
      user.status = status;
      this.users.set(id, user);
    }
  }

  async getServer(id: string): Promise<Server | undefined> {
    return this.servers.get(id);
  }

  async getServerWithChannels(id: string): Promise<ServerWithChannels | undefined> {
    const server = this.servers.get(id);
    if (!server) return undefined;

    const channels = Array.from(this.channels.values()).filter(
      channel => channel.serverId === id
    );

    const members = Array.from(this.serverMembers.values())
      .filter(member => member.serverId === id)
      .map(member => {
        const user = this.users.get(member.userId);
        return { ...member, user: user! };
      })
      .filter(member => member.user);

    return { ...server, channels, members };
  }

  async getUserServers(userId: string): Promise<Server[]> {
    const memberServers = Array.from(this.serverMembers.values())
      .filter(member => member.userId === userId)
      .map(member => this.servers.get(member.serverId))
      .filter(Boolean) as Server[];

    return memberServers;
  }

  async createServer(insertServer: InsertServer): Promise<Server> {
    const id = randomUUID();
    const server: Server = { 
      ...insertServer, 
      id, 
      createdAt: new Date(),
    };
    this.servers.set(id, server);
    return server;
  }

  async getChannel(id: string): Promise<Channel | undefined> {
    return this.channels.get(id);
  }

  async getServerChannels(serverId: string): Promise<Channel[]> {
    return Array.from(this.channels.values()).filter(
      channel => channel.serverId === serverId
    );
  }

  async createChannel(insertChannel: InsertChannel): Promise<Channel> {
    const id = randomUUID();
    const channel: Channel = { 
      ...insertChannel, 
      id, 
      createdAt: new Date(),
    };
    this.channels.set(id, channel);
    return channel;
  }

  async getMessage(id: string): Promise<Message | undefined> {
    return this.messages.get(id);
  }

  async getChannelMessages(channelId: string, limit = 50): Promise<MessageWithAuthor[]> {
    const messages = Array.from(this.messages.values())
      .filter(message => message.channelId === channelId)
      .sort((a, b) => a.createdAt!.getTime() - b.createdAt!.getTime())
      .slice(-limit);

    return messages.map(message => {
      const author = this.users.get(message.authorId);
      return { ...message, author: author! };
    }).filter(message => message.author);
  }

  async createMessage(insertMessage: InsertMessage): Promise<MessageWithAuthor> {
    const id = randomUUID();
    const message: Message = { 
      ...insertMessage, 
      id, 
      createdAt: new Date(),
    };
    this.messages.set(id, message);
    
    const author = this.users.get(message.authorId);
    return { ...message, author: author! };
  }

  async getServerMember(userId: string, serverId: string): Promise<ServerMember | undefined> {
    return Array.from(this.serverMembers.values()).find(
      member => member.userId === userId && member.serverId === serverId
    );
  }

  async getServerMembers(serverId: string): Promise<(ServerMember & { user: User })[]> {
    return Array.from(this.serverMembers.values())
      .filter(member => member.serverId === serverId)
      .map(member => {
        const user = this.users.get(member.userId);
        return { ...member, user: user! };
      })
      .filter(member => member.user);
  }

  async addServerMember(insertMember: InsertServerMember): Promise<ServerMember> {
    const id = randomUUID();
    const member: ServerMember = { 
      ...insertMember, 
      id, 
      joinedAt: new Date(),
    };
    this.serverMembers.set(id, member);
    return member;
  }

  async removeServerMember(userId: string, serverId: string): Promise<void> {
    const member = Array.from(this.serverMembers.entries()).find(
      ([_, member]) => member.userId === userId && member.serverId === serverId
    );
    
    if (member) {
      this.serverMembers.delete(member[0]);
    }
  }
}

export const storage = new MemStorage();
