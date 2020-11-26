package me.drex.logblock.util;

public class Constants {

    public enum Table {
        BLOCKS("blocks"),
        BLOCKSTATES("blockstate"),
        BLOCKTAGS("blocktag"),
        DIMENSIONS("dimension"),
        ENTITIES("entity"),
        HISTORY("history");

        public final String name;

        Table(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    public enum CacheColumn {
        ID("id"),
        VALUE("value");

        public final String name;

        CacheColumn(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }


}
