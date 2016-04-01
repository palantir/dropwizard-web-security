/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.websecurity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.immutables.value.Value.Style;

/**
 * Styles for immutable classes.
 */

@Target({ElementType.PACKAGE, ElementType.TYPE})
@Style(
        visibility = Style.ImplementationVisibility.PACKAGE
)
@interface ImmutableStyles {}
