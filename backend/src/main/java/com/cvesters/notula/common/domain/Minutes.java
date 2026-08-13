package com.cvesters.notula.common.domain;

import org.apache.commons.lang3.Validate;

public record Minutes(int value) {

	public Minutes {
		Validate.isTrue(value >= 0);
	}

}
