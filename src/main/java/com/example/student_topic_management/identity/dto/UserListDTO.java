package com.example.student_topic_management.identity.dto;

// Read-Only DTO -> get list data
public class UserListDTO {
    private Long id;
    private String username;
    private String fullname;
    private String email;
    private Boolean enabled;
    private String roleNames;

    public UserListDTO() {
    }

    public UserListDTO(Long id, String username, String fullname, String email, Boolean enabled, String roleNames) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.enabled = enabled;
        this.roleNames = roleNames;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(String roleNames) {
        this.roleNames = roleNames;
    }

}
