package com.cosmic.gatherly.data.model;

import java.util.Date;

public class Channel {
    private String id;
    private String name;
    private String description;
    private ChannelType type;
    private String serverId;
    private Date createdAt;

    public enum ChannelType {
        TEXT("text"),
        VOICE("voice");

        private final String value;

        ChannelType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ChannelType fromString(String type) {
            for (ChannelType t : ChannelType.values()) {
                if (t.value.equals(type)) {
                    return t;
                }
            }
            return TEXT;
        }
    }

    // Constructors
    public Channel() {}

    public Channel(String id, String name, String description, ChannelType type, String serverId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type != null ? type : ChannelType.TEXT;
        this.serverId = serverId;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ChannelType getType() {
        return type;
    }

    public void setType(ChannelType type) {
        this.type = type;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", serverId='" + serverId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}