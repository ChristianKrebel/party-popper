package com.partypopper.app.utils;

/**
 * A class just for storing widely used constants (used only in code).
 * This could also be solved via a resource file but that would
 * need a context.
 * Best use is to 'import static' this class, so
 * constants can be called without the class name directly.
 */
public final class Constants {

    /** Number of events to get in query. */
    public static final int EVENTS_AMOUNT = 50;

    /** Distance in miles for locating nearby events */
    public static final int STANDARD_DISTANCE = 50;

    /** The quality compression for bitmaps in per cent */
    public static final int COMPRESSION_QUALITY = 98;

    /** How many ms a handler waits */
    public static final int HANDLER_DELAY = 3000;

    private Constants(){
        //this prevents even the native class from
        //calling this ctor as well :
        throw new AssertionError();
    }
}
