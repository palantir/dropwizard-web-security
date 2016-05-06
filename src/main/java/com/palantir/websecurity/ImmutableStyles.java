/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.websecurity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.immutables.value.Value.Style;

/**
 * Styles for immutable classes.
 */

@Target({ElementType.PACKAGE, ElementType.TYPE})
@JsonSerialize
@Style(
        typeImmutable = "*",
        visibility = Style.ImplementationVisibility.PUBLIC
)
@interface ImmutableStyles {}
