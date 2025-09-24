package com.example.btms.service.match;

import org.springframework.stereotype.Service;

import com.example.btms.model.match.BadmintonMatch;

@Service
public class MatchService {
    private final Object LOCK = new Object();
    private final BadmintonMatch match = new BadmintonMatch();

    public BadmintonMatch match() {
        return match;
    };

    public <T> T withLock(java.util.function.Supplier<T> fn) {
        synchronized (LOCK) {
            return fn.get();
        }
    }
}
