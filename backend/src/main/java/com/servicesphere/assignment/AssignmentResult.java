package com.servicesphere.assignment;

import com.servicesphere.user.User;

public class AssignmentResult {
    private final User agent;
    private final double score;
    private final String reason;

    public AssignmentResult(User agent, double score, String reason) {
        this.agent = agent;
        this.score = score;
        this.reason = reason;
    }

    public static AssignmentResult unassigned(String reason) {
        return new AssignmentResult(null, 0.0, reason);
    }

    public boolean isAssigned() {
        return agent != null;
    }

    public User getAgent() {
        return agent;
    }

    public double getScore() {
        return score;
    }

    public String getReason() {
        return reason;
    }
}
