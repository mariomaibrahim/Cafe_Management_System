package aitpcafe;

import java.time.LocalDateTime;

/**
 * Singleton class to manage user session data securely across the application
 * Thread-safe implementation with session timeout
 */
public class UserSession {
    private static volatile UserSession instance;
    private static final Object lock = new Object();
    
    private int employeeId;
    private String username;
    private String fullName;
    private String role;
    private String email;
    private String phone;
    private double salary;
    private LocalDateTime loginTime;
    private LocalDateTime lastActivity;
    private static final int SESSION_TIMEOUT_MINUTES = 120; // 2 hours
    
    // Private constructor to prevent direct instantiation
    private UserSession() {
        this.loginTime = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
    }
    
    /**
     * Get the singleton instance of UserSession (Thread-safe)
     */
    public static UserSession getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new UserSession();
                }
            }
        }
        return instance;
    }
    
    /**
     * Initialize the session with user data after successful login
     */
    public synchronized void setUserData(int employeeId, String username, String fullName, 
                                        String role, String email, String phone, double salary) {
        this.employeeId = employeeId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.salary = salary;
        this.loginTime = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        
        System.out.println("✓ Session initialized for user: " + username + " (Role: " + role + ")");
    }
    
    /**
     * Update last activity time
     */
    public synchronized void updateActivity() {
        this.lastActivity = LocalDateTime.now();
    }
    
    /**
     * Check if session has expired
     */
    public synchronized boolean isSessionExpired() {
        if (lastActivity == null) return true;
        return lastActivity.plusMinutes(SESSION_TIMEOUT_MINUTES).isBefore(LocalDateTime.now());
    }
    
    /**
     * Clear all session data on logout
     */
    public synchronized void clearSession() {
        this.employeeId = 0;
        this.username = null;
        this.fullName = null;
        this.role = null;
        this.email = null;
        this.phone = null;
        this.salary = 0.0;
        this.loginTime = null;
        this.lastActivity = null;
        
        System.out.println("✓ Session cleared successfully");
    }
    
    /**
     * Check if user is logged in and session is valid
     */
    public synchronized boolean isLoggedIn() {
        return username != null && !username.isEmpty() && !isSessionExpired();
    }
    
    /**
     * Check if user is admin
     */
    public synchronized boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
    
    /**
     * Check if user is cashier
     */
    public synchronized boolean isCashier() {
        return "cashier".equalsIgnoreCase(role);
    }
    
    /**
     * Get session duration in minutes
     */
    public synchronized long getSessionDurationMinutes() {
        if (loginTime == null) return 0;
        return java.time.Duration.between(loginTime, LocalDateTime.now()).toMinutes();
    }
    
    // Getters
    public synchronized int getEmployeeId() { 
        updateActivity();
        return employeeId; 
    }
    
    public synchronized String getUsername() { 
        updateActivity();
        return username; 
    }
    
    public synchronized String getFullName() { 
        updateActivity();
        return fullName; 
    }
    
    public synchronized String getRole() { 
        updateActivity();
        return role; 
    }
    
    public synchronized String getEmail() { 
        updateActivity();
        return email; 
    }
    
    public synchronized String getPhone() { 
        updateActivity();
        return phone; 
    }
    
    public synchronized double getSalary() { 
        updateActivity();
        return salary; 
    }
    
    public synchronized LocalDateTime getLoginTime() { 
        return loginTime; 
    }
    
    public synchronized LocalDateTime getLastActivity() { 
        return lastActivity; 
    }
    
    // Setters for updating profile (with validation)
    public synchronized void setFullName(String fullName) { 
        if (fullName != null && !fullName.trim().isEmpty()) {
            this.fullName = fullName;
            updateActivity();
        }
    }
    
    public synchronized void setEmail(String email) { 
        if (email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            this.email = email;
            updateActivity();
        }
    }
    
    public synchronized void setPhone(String phone) { 
        if (phone != null && phone.matches("^[0-9+\\-\\s()]{8,20}$")) {
            this.phone = phone;
            updateActivity();
        }
    }
    
    public synchronized void setSalary(double salary) {
        if (salary >= 0) {
            this.salary = salary;
            updateActivity();
        }
    }
    
    @Override
    public String toString() {
        return String.format("UserSession[username=%s, role=%s, loginTime=%s]", 
                           username, role, loginTime);
    }
}