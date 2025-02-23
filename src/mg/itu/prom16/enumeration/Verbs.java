package src.mg.itu.prom16.enumeration;

import src.mg.itu.prom16.exceptions.UnsupportedVerbException;

public enum Verbs {
    GET, POST;

    // public static Verbs fromString(String verb) throws UnsupportedVerbException {
    //     if (verb == null) {
    //         throw new IllegalArgumentException("Le verbe ne peut pas être null");
    //     }
    //     verb = verb.toUpperCase();

    //     for (Verbs v : Verbs.values()) {
    //         if (v.name().equals(verb)) {
    //             return v;
    //         }
    //     }
    //     throw new UnsupportedVerbException("The verb requested isn't supported by the system: : " + verb);
    // }
}