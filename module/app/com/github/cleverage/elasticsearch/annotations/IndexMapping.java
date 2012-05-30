package com.github.cleverage.elasticsearch.annotations;

import java.lang.annotation.*;

/**
 * User: nboire
 * Date: 17/05/12
 */

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IndexMapping {

    String value();
}
