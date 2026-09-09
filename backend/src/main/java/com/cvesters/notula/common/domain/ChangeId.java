package com.cvesters.notula.common.domain;

import java.util.UUID;

public record ChangeId(UUID value) {

	public static final ChangeId NONE = new ChangeId(null);
}
