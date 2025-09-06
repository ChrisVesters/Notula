package com.cvesters.notula.common.domain;

class EmailTest {

	// Local-part (before @): letters, numbers, dots, underscores, hyphens.
	// Domain (after @): valid domain name (example.com).

	// REGEX
	// ^[^\s@]+@[^\s@]+\.[^\s@]+$

	// non-latin characters
// 	用户@例子.广告
// почта@пример.рус
// θσερ@εχαμπλε.ψομ
// Dörte@Sörensen.example.com

// ^[\p{L}\p{N}\p{M}\p{S}\p{P}\p{Cf}\p{Cs}._%+-]+@[\p{L}\p{N}\-\.]+$

}
