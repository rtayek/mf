package com.tayek.mf;

import java.util.EnumMap;
import javafx.scene.media.AudioClip;

/** Centralized playback of the six sound events used by RTGo. */
public final class GameSounds {
    public enum Sound { challenge, stone, atari, capture, pass, illegal }

    private final EnumMap<Sound, AudioClip> clips = new EnumMap<>(Sound.class);
    private boolean enabled = true;
    private boolean josekiWarningEnabled = true;

    public GameSounds() {
        load(Sound.challenge, "gochlng.wav");
        load(Sound.stone, "stone.wav");
        load(Sound.atari, "goatari.wav");
        load(Sound.capture, "gocaptb.wav");
        load(Sound.pass, "gopass.wav");
        load(Sound.illegal, "goillmv.wav");
    }

    private void load(Sound sound, String filename) {
        var resource = GameSounds.class.getResource("/audio/" + filename);
        if (resource != null) clips.put(sound, new AudioClip(resource.toExternalForm()));
    }

    public void play(Sound sound) {
        if (!enabled) return;
        AudioClip clip = clips.get(sound);
        if (clip != null) clip.play();
    }

    /** Separate control for a click outside the available joseki continuations. */
    public void playJosekiWarning() {
        if (josekiWarningEnabled) play(Sound.illegal);
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }
    public void setJosekiWarningEnabled(boolean enabled) { josekiWarningEnabled = enabled; }
    public boolean isJosekiWarningEnabled() { return josekiWarningEnabled; }
}
