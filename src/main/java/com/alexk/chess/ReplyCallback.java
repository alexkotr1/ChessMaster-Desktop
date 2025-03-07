package com.alexk.chess;

@FunctionalInterface
public interface ReplyCallback {
    void onReply(Message response);
}
