package com.tayek.mf;

import java.util.Optional;

/**
 * Definitions of SGF properties that mf currently understands.
 *
 * This is interpretation metadata, not the document model. SgfProperty can
 * retain properties that are absent from this catalog.
 */
public enum SgfPropertyDefinition {
    AB(Type.SETUP, "1234", "Add Black", "list of stone"),
    AE(Type.SETUP, "1234", "Add Empty", "list of point"),
    AW(Type.SETUP, "1234", "Add White", "list of stone"),
    PL(Type.SETUP, "--34", "Player to play", "color"),

    B(Type.MOVE, "1234", "Black", "move"),
    W(Type.MOVE, "1234", "White", "move"),
    BL(Type.MOVE, "1234", "Black time left", "real"),
    WL(Type.MOVE, "1234", "White time left", "real"),
    BM(Type.MOVE, "1234", "Bad move", "double"),
    DO(Type.MOVE, "1234", "Doubtful", "none"),
    IT(Type.MOVE, "1234", "Interesting", "none"),
    KO(Type.MOVE, "1234", "Ko", "none"),
    MN(Type.MOVE, "--34", "Set move number", "number"),
    TE(Type.MOVE, "1234", "Tesuji", "double"),

    FF(Type.ROOT, "1234", "File format", "number"),
    GM(Type.ROOT, "1234", "Game", "number"),
    SZ(Type.ROOT, "1234", "Size", "number or composed number:number"),
    CA(Type.ROOT, "---4", "Charset", "simpletext"),
    AP(Type.ROOT, "---4", "Application", "composed simpletext:simpletext"),
    ST(Type.ROOT, "---4", "Style", "number"),

    AN(Type.INFO, "1234", "Annotation", "simpletext"),
    BR(Type.INFO, "1234", "Black rank", "simpletext"),
    BT(Type.INFO, "--34", "Black team", "simpletext"),
    CP(Type.INFO, "1234", "Copyright", "simpletext"),
    DT(Type.INFO, "1234", "Date", "simpletext"),
    EV(Type.INFO, "1234", "Event", "simpletext"),
    GN(Type.INFO, "1234", "Game name", "simpletext"),
    GC(Type.INFO, "1234", "Game comment", "text"),
    HA(Type.INFO, "1234", "Handicap", "number"),
    KM(Type.INFO, "1234", "Komi", "real"),
    ON(Type.INFO, "1234", "Opening", "simpletext"),
    OT(Type.INFO, "--34", "Overtime", "simpletext"),
    PB(Type.INFO, "1234", "Black player", "simpletext"),
    PC(Type.INFO, "1234", "Place", "simpletext"),
    PW(Type.INFO, "1234", "White player", "simpletext"),
    RE(Type.INFO, "1234", "Result", "simpletext"),
    RO(Type.INFO, "1234", "Round", "simpletext"),
    RU(Type.INFO, "1234", "Rules", "simpletext"),
    SO(Type.INFO, "1234", "Source", "simpletext"),
    TM(Type.INFO, "1234", "Time limit", "real"),
    US(Type.INFO, "1234", "User", "simpletext"),
    WR(Type.INFO, "1234", "White rank", "simpletext"),
    WT(Type.INFO, "--34", "White team", "simpletext"),

    C(Type.NONE, "1234", "Comment", "text"),
    CR(Type.NONE, "--34", "Circle", "list of point"),
    LB(Type.NONE, "--34", "Label", "list of composed point:simpletext"),
    MA(Type.NONE, "--34", "Mark with X", "list of point"),
    SQ(Type.NONE, "--34", "Square", "list of point"),
    TR(Type.NONE, "--34", "Triangle", "list of point"),
    VW(Type.NONE, "--34", "View", "list of point"),

    // RTGo extensions. Preserve them as ordinary SGF properties even if a
    // different producer does not know their semantics.
    RT(Type.ROOT, "1234", "Tgo Root", "simpletext"),
    ZB(Type.MOVE, "1234", "Black resign", "none"),
    ZW(Type.MOVE, "1234", "White resign", "none");

    public enum Type { SETUP, INFO, ROOT, MOVE, NONE }

    SgfPropertyDefinition(Type type, String ff, String description, String valueType) {
        this.type = type;
        this.ff = ff;
        this.description = description;
        this.valueType = valueType;
    }

    public Type type() { return type; }
    public String ff() { return ff; }
    public String description() { return description; }
    public String valueType() { return valueType; }

    public static Optional<SgfPropertyDefinition> find(String id) {
        try {
            return Optional.of(valueOf(id));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private final Type type;
    private final String ff;
    private final String description;
    private final String valueType;
}
