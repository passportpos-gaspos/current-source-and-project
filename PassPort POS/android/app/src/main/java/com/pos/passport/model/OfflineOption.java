package com.pos.passport.model;

/**
 * Created by karim on 1/29/16.
 */
public class OfflineOption {
    private boolean showingMessage;
    private long timestamp;
    private boolean offline;

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public boolean isShowingMessage() {
        return showingMessage;
    }

    public void setShowingMessage(boolean showingMessage) {
        this.showingMessage = showingMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
