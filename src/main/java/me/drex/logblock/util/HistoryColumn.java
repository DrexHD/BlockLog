package me.drex.logblock.util;

public enum HistoryColumn {
    ID("id"),
    ENTITYID("entity"),
    XPOS("x"),
    YPOS("y"),
    ZPOS("z"),
    DIMENSIONID("dimension"),
    BLOCKID("block"),
    PBLOCKID("pblock"),
    BLOCKSTATEID("blockstate"),
    PBLOCKSTATEID("pblockstate"),
    BLOCKTAGID("blocktag"),
    PBLOCKTAGID("pblocktag"),
    TIME("time"),
    PLACED("placed"),
    UNDONE("undone");


    private final String name;

    HistoryColumn(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
