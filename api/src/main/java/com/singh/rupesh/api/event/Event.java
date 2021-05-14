package com.singh.rupesh.api.event;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@NoArgsConstructor
@Getter
public class Event<K,T> {
    private EventType eventType;
    private K key;
    private T data;
    private LocalDateTime eventCreateAt;

    public Event(EventType eventType, K key, T data) {
        this.eventType = eventType;
        this.key = key;
        this.data = data;
        this.eventCreateAt = now();
    }
}
