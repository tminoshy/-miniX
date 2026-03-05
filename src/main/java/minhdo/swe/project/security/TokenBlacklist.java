package minhdo.swe.project.security;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {

    private final Set<String> blacklistedJtis = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void blacklist(String jti) {
        blacklistedJtis.add(jti);
    }

    public boolean isBlacklisted(String jti) {
        return blacklistedJtis.contains(jti);
    }
}
