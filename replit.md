# Overview

Cosmic Chat is a modern real-time chat application built with React and Express.js. It provides Discord-like functionality with servers, channels, and messaging capabilities. The application features a space-themed UI with cosmic visual elements and supports user authentication, server management, and real-time messaging.

# User Preferences

Preferred communication style: Simple, everyday language.

# System Architecture

## Frontend Architecture
- **Framework**: React with TypeScript using Vite as the build tool
- **State Management**: TanStack Query for server state management and caching
- **Routing**: Wouter for lightweight client-side routing
- **UI Components**: shadcn/ui component library built on Radix UI primitives
- **Styling**: Tailwind CSS with custom cosmic theme colors and animations
- **Form Handling**: React Hook Form with Zod validation

## Backend Architecture
- **Framework**: Express.js with TypeScript
- **API Design**: RESTful API endpoints for authentication, servers, channels, and messages
- **Session Management**: Express sessions with configurable storage
- **Password Security**: bcrypt for password hashing
- **Development Tools**: Hot module replacement with Vite integration in development

## Data Storage Solutions
- **Database**: PostgreSQL configured through Drizzle ORM
- **Schema Management**: Drizzle Kit for migrations and schema management
- **Database Provider**: Neon Database (serverless PostgreSQL)
- **Development Storage**: In-memory storage implementation for development/testing

## Authentication and Authorization
- **Session-based Authentication**: Express sessions with secure cookie configuration
- **Password Hashing**: bcrypt with salt rounds for secure password storage
- **Route Protection**: Middleware-based authentication checks for protected routes
- **User Registration**: Email and username uniqueness validation

## Data Models
- **Users**: Authentication credentials, profile information, and online status
- **Servers**: Community spaces with ownership and member management
- **Channels**: Text and voice channels within servers
- **Messages**: User messages with attachments and reactions support
- **Server Members**: Role-based membership with owner/admin/moderator/member roles

## Real-time Features
- **Message Polling**: Client-side polling for new messages every 3 seconds
- **Optimistic Updates**: Immediate UI updates with server reconciliation
- **Live Status**: User online/offline status tracking

# External Dependencies

## Database Services
- **Neon Database**: Serverless PostgreSQL hosting
- **Drizzle ORM**: Type-safe database operations and schema management

## UI and Styling
- **Radix UI**: Accessible component primitives for dialogs, dropdowns, etc.
- **Tailwind CSS**: Utility-first CSS framework
- **Lucide React**: Icon library for consistent iconography

## Development Tools
- **Vite**: Fast build tool and development server
- **TypeScript**: Type safety across frontend and backend
- **ESBuild**: Fast JavaScript bundler for production builds

## Authentication and Security
- **bcrypt**: Password hashing library
- **express-session**: Session management middleware

## Date and Utilities
- **date-fns**: Date formatting and manipulation
- **nanoid**: Unique ID generation
- **clsx/class-variance-authority**: Conditional CSS class utilities