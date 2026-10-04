package com.tayek.mf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public final class SgfCorpus {
    private SgfCorpus() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("usage: SgfCorpus SGF_DIRECTORY");
            System.exit(2);
        }

        Path root = Path.of(args[0]).toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) {
            System.err.println("not a directory: " + root);
            System.exit(2);
        }

        List<Path> files;
        try (var stream = Files.walk(root)) {
            files = stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".sgf"))
                    .sorted()
                    .toList();
        }

        int parsed = 0;
        int collections = 0;
        int games = 0;
        int goGames = 0;
        int otherGames = 0;
        int gamesWithVariations = 0;
        Map<Integer,Integer> gameTypes = new TreeMap<>();
        List<Failure> failures = new ArrayList<>();

        for (Path file : files) {
            try {
                GameCollection collection = SgfReader.read(file);
                parsed++;
                collections++;
                games += collection.games().size();
                for (Game game : collection.games()) {
                    int gm = game.type().sgfNumber();
                    gameTypes.merge(gm, 1, Integer::sum);
                    if (game.type().equals(GameType.GO)) goGames++;
                    else otherGames++;
                    if (hasVariation(game.tree().root())) gamesWithVariations++;
                }
            } catch (Exception | AssertionError e) {
                String message = e.getMessage();
                if (message == null || message.isBlank()) message = e.getClass().getSimpleName();
                failures.add(new Failure(root.relativize(file).toString(), message));
            }
        }

        System.out.println("SGF corpus: " + root);
        System.out.printf("files:                 %d%n", files.size());
        System.out.printf("parsed:                %d%n", parsed);
        System.out.printf("rejected:              %d%n", failures.size());
        System.out.printf("collections:           %d%n", collections);
        System.out.printf("games:                 %d%n", games);
        System.out.printf("Go games (GM[1]):      %d%n", goGames);
        System.out.printf("other game types:      %d%n", otherGames);
        System.out.printf("games with variations: %d%n", gamesWithVariations);
        System.out.println("game types:");
        gameTypes.forEach((gm,count) -> System.out.printf("  GM[%d]: %d%n", gm, count));

        if (!failures.isEmpty()) {
            System.out.println("\nREJECTED");
            failures.stream().sorted(Comparator.comparing(Failure::file))
                    .forEach(f -> System.out.println(f.file() + "\t" + f.message()));
        }
    }

    private static boolean hasVariation(GameNode node) {
        if (node.children().size() > 1) return true;
        for (GameNode child : node.children()) if (hasVariation(child)) return true;
        return false;
    }

    private record Failure(String file, String message) { }
}
