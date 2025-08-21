package com.cosmic.gatherly.data.model;

import java.util.Date;

public class ServerMember {
    private String id;
    private String userId;
    private String serverId;
    private MemberRole role;
    private Date joinedAt;
    private User user; // For extended member with user info

    public enum MemberRole {
        OWNER("owner"),
        ADMIN("admin"),
        MODERATOR("moderator"),
        MEMBER("member");

        private final String value;

        MemberRole(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static MemberRole fromString(String role) {
            for (MemberRole r : MemberRole.values()) {
                if (r.value.equals(role)) {
                    return r;
                }
            }
            return MEMBER;
        }
    }

    // Constructors
    public ServerMember() {}

    public ServerMember(String id, String userId, String serverId, MemberRole role) {
        this.id = id;
        this.userId = userId;
        this.serverId = serverId;
        this.role = role != null ? role : MemberRole.MEMBER;
        this.joinedAt = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public MemberRole getRole() {
        return role;
    }

    public void setRole(MemberRole role) {
        this.role = role;
    }

    public Date getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Date joinedAt) {
        this.joinedAt = joinedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ServerMember{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", serverId='" + serverId + '\'' +
                ", role=" + role +
                ", joinedAt=" + joinedAt +
                '}';
    }
}