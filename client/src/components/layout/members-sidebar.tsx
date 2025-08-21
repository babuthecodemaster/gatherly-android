import UserAvatar from "@/components/ui/user-avatar";
import type { ServerMember, User } from "@shared/schema";

interface MembersSidebarProps {
  members: (ServerMember & { user: User })[];
}

export default function MembersSidebar({ members }: MembersSidebarProps) {
  const onlineMembers = members.filter(member => member.user?.status === "online");
  const offlineMembers = members.filter(member => member.user?.status === "offline");

  const mockStatuses = [
    "Playing Stellaris",
    "Coding nebula shaders",
    "Digital painting",
    "Exploring galaxies",
    "Moderating",
    "No Man's Sky",
    "Elite Dangerous",
    "Kerbal Space Program",
  ];

  const getRoleColor = (role: string) => {
    switch (role) {
      case "owner":
        return "text-yellow-400";
      case "admin":
        return "text-red-400";
      case "moderator":
        return "text-cosmic-purple";
      default:
        return "text-white";
    }
  };

  const getMemberStatus = (index: number) => {
    return mockStatuses[index % mockStatuses.length];
  };

  return (
    <div className="hidden xl:flex w-60 bg-cosmic-navy flex-col border-l border-gray-800">
      <div className="p-4">
        <h3 className="text-sm font-semibold text-cosmic-gray uppercase tracking-wider mb-4">
          Online — {onlineMembers.length}
        </h3>
        
        <div className="space-y-3">
          {onlineMembers.map((member, index) => (
            <div 
              key={member.id} 
              className="flex items-center space-x-3 p-2 rounded hover:bg-gray-800 cursor-pointer group"
              data-testid={`member-${member.user.id}`}
            >
              <div className="relative">
                <UserAvatar
                  src={member.user.avatar || undefined}
                  fallback={member.user.username?.[0]?.toUpperCase() || "U"}
                  className="w-8 h-8"
                />
                <div className="absolute -bottom-1 -right-1 w-3 h-3 bg-green-500 rounded-full border-2 border-cosmic-navy"></div>
              </div>
              <div className="flex-1 min-w-0">
                <div className={`text-sm font-medium truncate ${getRoleColor(member.role || "member")} group-hover:text-cosmic-blue`}>
                  {member.user.username}
                  {member.role && member.role !== "member" && (
                    <span className="ml-1 text-xs bg-cosmic-blue px-1 py-0.5 rounded text-white">
                      {member.role.toUpperCase()}
                    </span>
                  )}
                </div>
                <div className="text-xs text-cosmic-gray truncate">
                  {getMemberStatus(index)}
                </div>
              </div>
            </div>
          ))}

          {offlineMembers.length > 0 && (
            <>
              <div className="mt-6 mb-3">
                <h3 className="text-sm font-semibold text-cosmic-gray uppercase tracking-wider">
                  Offline — {offlineMembers.length}
                </h3>
              </div>
              {offlineMembers.map((member) => (
                <div 
                  key={member.id} 
                  className="flex items-center space-x-3 p-2 rounded hover:bg-gray-800 cursor-pointer group opacity-50"
                  data-testid={`member-offline-${member.user.id}`}
                >
                  <div className="relative">
                    <UserAvatar
                      src={member.user.avatar || undefined}
                      fallback={member.user.username?.[0]?.toUpperCase() || "U"}
                      className="w-8 h-8 grayscale"
                    />
                    <div className="absolute -bottom-1 -right-1 w-3 h-3 bg-gray-500 rounded-full border-2 border-cosmic-navy"></div>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="text-sm font-medium text-cosmic-gray truncate">
                      {member.user.username}
                    </div>
                    <div className="text-xs text-cosmic-gray">Offline</div>
                  </div>
                </div>
              ))}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
