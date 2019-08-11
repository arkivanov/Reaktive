package com.badoo.reaktive.sample.linux

import libgtk3.gboolean

val gboolean.booleanValue: Boolean get() = this != 0

val Boolean.gbooleanValue: gboolean get() = if (this) 1 else 0
