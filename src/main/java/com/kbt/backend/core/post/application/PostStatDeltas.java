package com.kbt.backend.core.post.application;

import java.util.concurrent.atomic.AtomicLong;

public class PostStatDeltas {

    private final AtomicLong likeDelta = new AtomicLong();
    private final AtomicLong commentDelta = new AtomicLong();
    private final AtomicLong viewDelta = new AtomicLong();

    public void addLike(final long value) {
        likeDelta.addAndGet(value);
    }

    public void addComment(final long value) {
        commentDelta.addAndGet(value);
    }

    public void addView(final long value) {
        viewDelta.addAndGet(value);
    }

    public long getLikeDelta() {
        return likeDelta.get();
    }

    public long getCommentDelta() {
        return commentDelta.get();
    }

    public long getViewDelta() {
        return viewDelta.get();
    }

    public boolean isEmpty() {
        return getLikeDelta() == 0L && getCommentDelta() == 0L && getViewDelta() == 0L;
    }
}
