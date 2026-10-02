package com.tayek.mf;

import javafx.application.Application;

/** Plain Java launcher for environments that do not launch JavaFX Application classes directly. */
public final class MfLauncher {

    private MfLauncher() {
    }

    public static void main(String[] args) {
        Application.launch(Mf.class, args);
    }
}
