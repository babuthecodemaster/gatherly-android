import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { getChannelMessages, mockUser } from "@/lib/mock-data";
import { useToast } from "@/hooks/use-toast";
import type { MessageWithAuthor } from "@shared/schema";

interface SendMessageData {
  content: string;
  attachments?: string[];
}

export function useMessages(channelId: string) {
  const { toast } = useToast();
  const [messages, setMessages] = useState<MessageWithAuthor[]>(() => 
    getChannelMessages(channelId)
  );

  const sendMessage = useMutation({
    mutationFn: async (messageData: SendMessageData) => {
      // Simulate API call delay
      await new Promise(resolve => setTimeout(resolve, 500));
      
      const newMessage: MessageWithAuthor = {
        id: `msg-${Date.now()}`,
        content: messageData.content,
        authorId: mockUser.id,
        channelId: channelId,
        attachments: messageData.attachments || null,
        reactions: null,
        createdAt: new Date(),
        author: {
          ...mockUser,
          password: "hashed-password",
          createdAt: new Date(),
          avatar: mockUser.avatar || null,
        },
      };
      
      setMessages(prev => [...prev, newMessage]);
      return newMessage;
    },
    onError: (error: any) => {
      toast({
        title: "Failed to send message",
        description: error.message || "Unable to send message",
        variant: "destructive",
      });
    },
  });

  return {
    messages,
    isLoading: false,
    error: null,
    sendMessage: sendMessage.mutateAsync,
    isMessageSending: sendMessage.isPending,
  };
}
