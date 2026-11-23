package univ.fooddeliv.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import univ.fooddeliv.dto.OrderTrackingDto;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class TrackingHub {
    private static final long TIMEOUT_MS = 30 * 60 * 1000L;

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> subscribers = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long orderId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        subscribers.computeIfAbsent(orderId, id -> new CopyOnWriteArrayList<>()).add(emitter);
        Runnable cleanup = () -> remove(orderId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(error -> cleanup.run());
        return emitter;
    }

    public boolean hasSubscribers(Long orderId) {
        List<SseEmitter> emitters = subscribers.get(orderId);
        return emitters != null && !emitters.isEmpty();
    }

    public void publish(Long orderId, OrderTrackingDto tracking) {
        List<SseEmitter> emitters = subscribers.get(orderId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("tracking").data(tracking));
                if (tracking.isFinished()) {
                    emitter.complete();
                }
            } catch (IOException | IllegalStateException e) {
                remove(orderId, emitter);
            }
        }
    }

    private void remove(Long orderId, SseEmitter emitter) {
        List<SseEmitter> emitters = subscribers.get(orderId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            subscribers.remove(orderId, emitters);
        }
    }
}
