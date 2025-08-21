import { useState, useEffect, useRef } from "react";
import { Button } from "@/components/ui/button";
import { Hash, Menu, Users, Search, AtSign, HelpCircle } from "lucide-react";
import MessageInput from "@/components/ui/message-input";
import MessageItem from "@/components/ui/message-item";
import { useMessages } from "@/hooks/use-messages";
import type { ServerWithChannels } from "@shared/schema";

interface ChatAreaProps {
  server?: ServerWithChannels;
  channelId: string;
  onToggleMobileChannels: () => void;
}

export default function ChatArea({ server, channelId, onToggleMobileChannels }: ChatAreaProps) {
  const { messages, isLoading, sendMessage, isMessageSending } = useMessages(channelId);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const currentChannel = server?.channels?.find(channel => channel.id === channelId);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSendMessage = async (content: string) => {
    if (content.trim()) {
      try {
        await sendMessage({ content });
      } catch (error) {
        console.error("Failed to send message:", error);
      }
    }
  };

  if (!currentChannel) {
    return (
      <div className="flex-1 flex items-center justify-center bg-cosmic-black">
        <div className="text-cosmic-gray text-center">
          <Hash className="w-16 h-16 mx-auto mb-4 opacity-50" />
          <h3 className="text-xl font-semibold mb-2">No channel selected</h3>
          <p>Select a channel to start chatting</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 flex flex-col bg-cosmic-black">
      {/* Chat Header */}
      <div className="h-16 border-b border-gray-800 flex items-center px-6 bg-cosmic-navy">
        <div className="flex items-center flex-1">
          <Button
            variant="ghost"
            size="icon"
            className="md:hidden mr-4 text-cosmic-gray hover:text-white"
            onClick={onToggleMobileChannels}
            data-testid="button-toggle-mobile-channels"
          >
            <Menu className="w-5 h-5" />
          </Button>
          <Hash className="w-5 h-5 text-cosmic-gray mr-2" />
          <h3 className="font-semibold text-lg" data-testid="text-channel-name">
            {currentChannel.name}
          </h3>
          {currentChannel.description && (
            <>
              <div className="ml-4 h-6 w-px bg-gray-600"></div>
              <p className="ml-4 text-sm text-cosmic-gray" data-testid="text-channel-description">
                {currentChannel.description}
              </p>
            </>
          )}
        </div>
        <div className="flex items-center space-x-4">
          <Button
            variant="ghost"
            size="icon"
            className="text-cosmic-gray hover:text-white"
            onClick={() => {
              // TODO: Toggle member list
              console.log("Toggle member list");
            }}
            data-testid="button-member-list"
          >
            <Users className="w-5 h-5" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            className="text-cosmic-gray hover:text-white"
            onClick={() => {
              // TODO: Open search
              console.log("Open search");
            }}
            data-testid="button-search"
          >
            <Search className="w-5 h-5" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            className="text-cosmic-gray hover:text-white"
            onClick={() => {
              // TODO: Show mentions
              console.log("Show mentions");
            }}
            data-testid="button-mentions"
          >
            <AtSign className="w-5 h-5" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            className="text-cosmic-gray hover:text-white"
            onClick={() => {
              // TODO: Open help
              console.log("Open help");
            }}
            data-testid="button-help"
          >
            <HelpCircle className="w-5 h-5" />
          </Button>
        </div>
      </div>

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 scrollbar-cosmic" data-testid="area-messages">
        {isLoading ? (
          <div className="flex items-center justify-center h-full">
            <div className="text-cosmic-blue animate-pulse-glow">Loading cosmic messages...</div>
          </div>
        ) : messages.length === 0 ? (
          <div className="flex items-center justify-center h-full">
            <div className="text-cosmic-gray text-center">
              <Hash className="w-12 h-12 mx-auto mb-4 opacity-50" />
              <h3 className="text-lg font-semibold mb-2">Welcome to #{currentChannel.name}</h3>
              <p>This is the beginning of this cosmic channel.</p>
            </div>
          </div>
        ) : (
          <>
            {messages.map((message) => (
              <MessageItem key={message.id} message={message} />
            ))}
            <div ref={messagesEndRef} />
          </>
        )}
      </div>

      {/* Message Input */}
      <MessageInput
        onSendMessage={handleSendMessage}
        placeholder={`Message #${currentChannel.name}`}
        disabled={isMessageSending}
        data-testid="input-message"
      />
    </div>
  );
}
