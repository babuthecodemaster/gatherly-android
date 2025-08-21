import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiRequest } from "@/lib/queryClient";
import type { MessageWithAuthor } from "@shared/schema";
import { useToast } from "@/hooks/use-toast";

interface SendMessageData {
  content: string;
  attachments?: string[];
}

export function useMessages(channelId: string) {
  const queryClient = useQueryClient();
  const { toast } = useToast();

  const { data: messages = [], isLoading, error } = useQuery({
    queryKey: ["/api/channels", channelId, "messages"],
    queryFn: async () => {
      const response = await fetch(`/api/channels/${channelId}/messages`, {
        credentials: "include",
      });
      if (!response.ok) {
        throw new Error("Failed to fetch messages");
      }
      return response.json();
    },
    refetchInterval: 3000, // Poll for new messages every 3 seconds
    enabled: !!channelId,
  });

  const sendMessage = useMutation({
    mutationFn: async (messageData: SendMessageData) => {
      const response = await apiRequest("POST", `/api/channels/${channelId}/messages`, messageData);
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["/api/channels", channelId, "messages"],
      });
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
    messages: messages as MessageWithAuthor[],
    isLoading,
    error,
    sendMessage: sendMessage.mutateAsync,
    isMessageSending: sendMessage.isPending,
  };
}
