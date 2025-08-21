import type { Express } from "express";
import { createServer, type Server } from "http";
import { storage } from "./storage";
import { insertUserSchema, insertServerSchema, insertChannelSchema, insertMessageSchema } from "@shared/schema";
import bcrypt from "bcrypt";
import session from "express-session";

declare module "express-session" {
  interface SessionData {
    userId?: string;
  }
}

export async function registerRoutes(app: Express): Promise<Server> {
  // Session middleware
  app.use(session({
    secret: process.env.SESSION_SECRET || "cosmic-secret-key",
    resave: false,
    saveUninitialized: false,
    cookie: { 
      secure: false, // Set to true in production with HTTPS
      maxAge: 24 * 60 * 60 * 1000 // 24 hours
    }
  }));

  // Auth middleware
  const requireAuth = (req: any, res: any, next: any) => {
    if (!req.session.userId) {
      return res.status(401).json({ message: "Authentication required" });
    }
    next();
  };

  // Auth routes
  app.post("/api/auth/register", async (req, res) => {
    try {
      const userData = insertUserSchema.parse(req.body);
      
      // Check if user already exists
      const existingUser = await storage.getUserByEmail(userData.email);
      if (existingUser) {
        return res.status(400).json({ message: "User already exists" });
      }

      // Hash password
      const hashedPassword = await bcrypt.hash(userData.password, 10);
      
      const user = await storage.createUser({
        ...userData,
        password: hashedPassword,
      });

      req.session.userId = user.id;
      await storage.updateUserStatus(user.id, "online");

      res.json({ 
        id: user.id, 
        username: user.username, 
        email: user.email,
        avatar: user.avatar,
        status: user.status,
      });
    } catch (error) {
      res.status(400).json({ message: "Invalid input data" });
    }
  });

  app.post("/api/auth/login", async (req, res) => {
    try {
      const { email, password } = req.body;
      
      const user = await storage.getUserByEmail(email);
      if (!user) {
        return res.status(401).json({ message: "Invalid credentials" });
      }

      const isValid = await bcrypt.compare(password, user.password);
      if (!isValid) {
        return res.status(401).json({ message: "Invalid credentials" });
      }

      req.session.userId = user.id;
      await storage.updateUserStatus(user.id, "online");

      res.json({ 
        id: user.id, 
        username: user.username, 
        email: user.email,
        avatar: user.avatar,
        status: user.status,
      });
    } catch (error) {
      res.status(500).json({ message: "Server error" });
    }
  });

  app.post("/api/auth/logout", requireAuth, async (req, res) => {
    if (req.session.userId) {
      await storage.updateUserStatus(req.session.userId, "offline");
    }
    req.session.destroy(() => {
      res.json({ message: "Logged out successfully" });
    });
  });

  app.get("/api/auth/me", requireAuth, async (req, res) => {
    const user = await storage.getUser(req.session.userId!);
    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }

    res.json({ 
      id: user.id, 
      username: user.username, 
      email: user.email,
      avatar: user.avatar,
      status: user.status,
    });
  });

  // Server routes
  app.get("/api/servers", requireAuth, async (req, res) => {
    const servers = await storage.getUserServers(req.session.userId!);
    res.json(servers);
  });

  app.get("/api/servers/:id", requireAuth, async (req, res) => {
    const server = await storage.getServerWithChannels(req.params.id);
    if (!server) {
      return res.status(404).json({ message: "Server not found" });
    }

    // Check if user is member
    const member = await storage.getServerMember(req.session.userId!, req.params.id);
    if (!member) {
      return res.status(403).json({ message: "Access denied" });
    }

    res.json(server);
  });

  app.post("/api/servers", requireAuth, async (req, res) => {
    try {
      const serverData = insertServerSchema.parse({
        ...req.body,
        ownerId: req.session.userId,
      });

      const server = await storage.createServer(serverData);
      
      // Add creator as owner member
      await storage.addServerMember({
        userId: req.session.userId!,
        serverId: server.id,
        role: "owner",
      });

      // Create default channels
      await storage.createChannel({
        name: "general",
        description: "General discussion",
        type: "text",
        serverId: server.id,
      });

      res.json(server);
    } catch (error) {
      res.status(400).json({ message: "Invalid server data" });
    }
  });

  // Channel routes
  app.get("/api/channels/:id", requireAuth, async (req, res) => {
    const channel = await storage.getChannel(req.params.id);
    if (!channel) {
      return res.status(404).json({ message: "Channel not found" });
    }

    // Check if user is member of server
    const member = await storage.getServerMember(req.session.userId!, channel.serverId);
    if (!member) {
      return res.status(403).json({ message: "Access denied" });
    }

    res.json(channel);
  });

  app.post("/api/servers/:serverId/channels", requireAuth, async (req, res) => {
    try {
      // Check if user is member
      const member = await storage.getServerMember(req.session.userId!, req.params.serverId);
      if (!member || (member.role !== "owner" && member.role !== "admin")) {
        return res.status(403).json({ message: "Insufficient permissions" });
      }

      const channelData = insertChannelSchema.parse({
        ...req.body,
        serverId: req.params.serverId,
      });

      const channel = await storage.createChannel(channelData);
      res.json(channel);
    } catch (error) {
      res.status(400).json({ message: "Invalid channel data" });
    }
  });

  // Message routes
  app.get("/api/channels/:id/messages", requireAuth, async (req, res) => {
    const channel = await storage.getChannel(req.params.id);
    if (!channel) {
      return res.status(404).json({ message: "Channel not found" });
    }

    // Check if user is member of server
    const member = await storage.getServerMember(req.session.userId!, channel.serverId);
    if (!member) {
      return res.status(403).json({ message: "Access denied" });
    }

    const limit = parseInt(req.query.limit as string) || 50;
    const messages = await storage.getChannelMessages(req.params.id, limit);
    res.json(messages);
  });

  app.post("/api/channels/:id/messages", requireAuth, async (req, res) => {
    try {
      const channel = await storage.getChannel(req.params.id);
      if (!channel) {
        return res.status(404).json({ message: "Channel not found" });
      }

      // Check if user is member of server
      const member = await storage.getServerMember(req.session.userId!, channel.serverId);
      if (!member) {
        return res.status(403).json({ message: "Access denied" });
      }

      const messageData = insertMessageSchema.parse({
        ...req.body,
        authorId: req.session.userId,
        channelId: req.params.id,
      });

      const message = await storage.createMessage(messageData);
      res.json(message);
    } catch (error) {
      res.status(400).json({ message: "Invalid message data" });
    }
  });

  // Server member routes
  app.get("/api/servers/:id/members", requireAuth, async (req, res) => {
    // Check if user is member
    const member = await storage.getServerMember(req.session.userId!, req.params.id);
    if (!member) {
      return res.status(403).json({ message: "Access denied" });
    }

    const members = await storage.getServerMembers(req.params.id);
    res.json(members);
  });

  const httpServer = createServer(app);
  return httpServer;
}
