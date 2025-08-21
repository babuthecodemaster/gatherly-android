import { useState, KeyboardEvent } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { PlusCircle, Gift, Image, Smile } from "lucide-react";

interface MessageInputProps {
  onSendMessage: (content: string) => void;
  placeholder?: string;
  disabled?: boolean;
}

export default function MessageInput({ onSendMessage, placeholder = "Type a message...", disabled = false }: MessageInputProps) {
  const [message, setMessage] = useState("");

  const handleSend = () => {
    if (message.trim() && !disabled) {
      onSendMessage(message);
      setMessage("");
    }
  };

  const handleKeyPress = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="p-4 border-t border-gray-800">
      <div className="bg-cosmic-navy rounded-lg p-3 cosmic-glow">
        <div className="flex items-center space-x-3">
          <Button
            variant="ghost"
            size="icon"
            className="text-cosmic-gray hover:text-cosmic-blue transition-colors flex-shrink-0"
            data-testid="button-add-attachment"
          >
            <PlusCircle className="w-5 h-5" />
          </Button>
          
          <Input
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder={placeholder}
            disabled={disabled}
            className="flex-1 bg-transparent border-none text-white placeholder-cosmic-gray focus-visible:ring-0 focus-visible:ring-offset-0"
            data-testid="input-message-content"
          />
          
          <div className="flex space-x-2 flex-shrink-0">
            <Button
              variant="ghost"
              size="icon"
              className="text-cosmic-gray hover:text-cosmic-purple transition-colors"
              data-testid="button-gift"
            >
              <Gift className="w-5 h-5" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className="text-cosmic-gray hover:text-cosmic-purple transition-colors"
              data-testid="button-image"
            >
              <Image className="w-5 h-5" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className="text-cosmic-gray hover:text-cosmic-blue transition-colors"
              data-testid="button-emoji"
            >
              <Smile className="w-5 h-5" />
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
