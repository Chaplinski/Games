
package com.example.android.games.data;

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

/**
 * API Contract for the Games app.
 */
public final class GameContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private GameContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.games";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.games/games/ is a valid path for
     * looking at game data. content://com.example.android.games/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_GAMES = "games";

    /**
     * Inner class that defines constant values for the games database table.
     * Each entry in the table represents a single game.
     */
    public static final class GameEntry implements BaseColumns {

        /** The content URI to access the game data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_GAMES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of games.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAMES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single game.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAMES;

        /** Name of database table for games */
        public final static String TABLE_NAME = "games";

        /**
         * Unique ID number for the game (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the game.
         *
         * Type: TEXT
         */
        public final static String COLUMN_GAME_NAME ="name";

        /**
         * Brand of the game.
         *
         * Type: TEXT
         */
        public final static String COLUMN_GAME_BRAND = "brand";

        /**
         * Demographic of the game.
         *
         * The only possible values are {@link #DEMOGRAPHIC_FAMILY}, {@link #DEMOGRAPHIC_CHILDREN},
         * or {@link #DEMOGRAPHIC_ADULT}.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_GAME_DEMOGRAPHIC = "demographic";

        /**
         * Price of the game.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_GAME_PRICE = "price";

        /**
         * Possible values for the demographic of the game.
         */
        public static final int DEMOGRAPHIC_FAMILY = 0;
        public static final int DEMOGRAPHIC_CHILDREN = 1;
        public static final int DEMOGRAPHIC_ADULT = 2;

        /**
         * Returns whether or not the given demographic is {@link #DEMOGRAPHIC_FAMILY}, {@link #DEMOGRAPHIC_CHILDREN},
         * or {@link #DEMOGRAPHIC_ADULT}.
         */
        public static boolean isValidDemographic(int demographic) {
            if (demographic == DEMOGRAPHIC_FAMILY || demographic == DEMOGRAPHIC_CHILDREN || demographic == DEMOGRAPHIC_ADULT) {
                return true;
            }
            return false;
        }
    }

}

