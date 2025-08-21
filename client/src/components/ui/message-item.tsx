import { formatDistanceToNow } from "date-fns";
import UserAvatar from "@/components/ui/user-avatar";
import { Button } from "@/components/ui/button";
import type { MessageWithAuthor } from "@shared/schema";

interface MessageItemProps {
  message: MessageWithAuthor;
}

export default function MessageItem({ message }: MessageItemProps) {
  const formatTime = (date: Date) => {
    return formatDistanceToNow(new Date(date), { addSuffix: true });
  };

  const getRoleColor = (username: string) => {
    // Simple hash to determine color consistency
    const colors = ["text-cosmic-blue", "text-cosmic-purple", "text-yellow-400", "text-green-400"];
    const hash = username.split("").reduce((acc, char) => acc + char.charCodeAt(0), 0);
    return colors[hash % colors.length];
  };

  const mockReactions = [
    { emoji: "🚀", count: Math.floor(Math.random() * 5) + 1 },
    { emoji: "✨", count: Math.floor(Math.random() * 8) + 1 },
    { emoji: "🌌", count: Math.floor(Math.random() * 3) + 1 },
    { emoji: "💻", count: Math.floor(Math.random() * 4) + 1 },
    { emoji: "🎨", count: Math.floor(Math.random() * 6) + 1 },
  ];

  const reactions = mockReactions.filter(() => Math.random() > 0.7); // Show some reactions randomly

  return (
    <div className="flex items-start space-x-3 message-glow p-3 rounded-lg" data-testid={`message-${message.id}`}>
      <UserAvatar
        src={message.author.avatar}
        fallback={message.author.username[0].toUpperCase()}
        className="w-10 h-10 cosmic-glow flex-shrink-0"
        data-testid={`avatar-${message.author.id}`}
      />
      <div className="flex-1 min-w-0">
        <div className="flex items-baseline space-x-2 mb-1">
          <span className={`font-medium ${getRoleColor(message.author.username)}`} data-testid="text-message-author">
            {message.author.username}
          </span>
          <span className="text-xs text-cosmic-gray" data-testid="text-message-time">
            {formatTime(message.createdAt!)}
          </span>
        </div>
        <div className="text-sm text-white break-words" data-testid="text-message-content">
          {message.content}
        </div>
        
        {/* Message attachments placeholder */}
        {message.attachments && message.attachments.length > 0 && (
          <div className="mt-2">
            {message.attachments.map((attachment, index) => (
              <div key={index} className="bg-cosmic-navy p-3 rounded-lg border border-gray-700">
                <img 
                  src={attachment} 
                  alt="Attachment" 
                  className="max-w-md w-full h-auto rounded"
                  data-testid={`attachment-${index}`}
                />
              </div>
            ))}
          </div>
        )}
        
        {/* Reactions */}
        {reactions.length > 0 && (
          <div className="flex items-center mt-2 space-x-2">
            {reactions.map((reaction, index) => (
              <Button
                key={index}
                variant="ghost"
                size="sm"
                className="h-7 px-2 bg-gray-800 hover:bg-gray-700 text-cosmic-gray hover:text-cosmic-blue rounded-full"
                data-testid={`reaction-${index}`}
              >
                <span className="mr-1">{reaction.emoji}</span>
                <span className="text-xs">{reaction.count}</span>
              </Button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
