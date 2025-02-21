package edu.rochester.beetrap.component.player;

import java.util.concurrent.atomic.AtomicBoolean;

public class IsPollinatingComponent {
    private final AtomicBoolean isPollinating;

    public IsPollinatingComponent() {
        this.isPollinating = new AtomicBoolean(false);
    }

    public boolean isPollinating() {
        return this.isPollinating.get();
    }

    public void setIsPollinating(boolean flag) {
        this.isPollinating.set(flag);
    }
}
