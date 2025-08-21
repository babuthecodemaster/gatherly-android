package com.cosmic.gatherly.data.model;

import java.util.Date;
import java.util.List;

public class Server {
    private String id;
    private String name;
    private String description;
    private String icon;
    private String ownerId;
    private Date createdAt;
    private List<Channel> channels;
    private List<ServerMember> members;

    // Constructors
    public Server() {}

    public Server(String id, String name, String description, String icon, String ownerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.ownerId = ownerId;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public List<ServerMember> getMembers() {
        return members;
    }

    public void setMembers(List<ServerMember> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "Server{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}