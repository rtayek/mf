package com.tayek.mf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class GameCollection {
    public GameCollection(List<Game> games) {
        Objects.requireNonNull(games, "games");
        if (games.isEmpty()) {
            throw new IllegalArgumentException("game collection must not be empty");
        }
        this.games = List.copyOf(games);
    }

    public List<Game> games() {
        return games;
    }

    public List<Game> gamesOfType(GameType type) {
        return games.stream().filter(game -> game.type().equals(type)).toList();
    }

    public Map<GameType, List<Game>> groupedByType() {
        Map<GameType, List<Game>> groups = new LinkedHashMap<>();
        for (Game game : games) {
            groups.computeIfAbsent(game.type(), ignored -> new ArrayList<>()).add(game);
        }
        groups.replaceAll((type, gamesOfType) -> List.copyOf(gamesOfType));
        return Collections.unmodifiableMap(groups);
    }

    public boolean isHomogeneous() {
        return commonType().isPresent();
    }

    public Optional<GameType> commonType() {
        GameType first = games.get(0).type();
        return games.stream().allMatch(game -> game.type().equals(first))
                ? Optional.of(first) : Optional.empty();
    }

    private final List<Game> games;
}
