package de.peeeq.jmpq.adpcm;

import static java.lang.annotation.ElementType.TYPE_USE;

import java.lang.annotation.Target;

/**
 * This is just an annotation, to remember which things are unsigned
 */
@Target({ TYPE_USE })
public @interface Unsigned {

}
