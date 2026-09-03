@file:Suppress("NonAsciiCharacters")

package com.pheeeew.core.audio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BreathStrengthScorerTest {
    @Test
    fun `일반 음량만으로도 유효한 강도를 만든다`() {
        val score = BreathStrengthScorer.score(0.5f, 0f, 0f, 0f)
        assertTrue(score > 0f)
    }

    @Test
    fun `저주파와 질감은 음량에 가중된다`() {
        val plain = BreathStrengthScorer.score(0.5f, 0f, 0f, 0f)
        val textured = BreathStrengthScorer.score(0.5f, 1f, 1f, 0f)
        assertTrue(textured > plain)
    }

    @Test
    fun `무음은 이전 강도도 즉시 초기화한다`() {
        assertEquals(0f, BreathStrengthScorer.score(0.01f, 1f, 1f, 0.8f))
    }
}
