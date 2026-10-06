package fr.audeladedonnees.examen.civique

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewEligibilityTest {
    private val now = 2 * REVIEW_COOLDOWN
    @Test fun newPlayerIsNotInterrupted() {
        assertFalse(reviewEligible(false, 0, 0, now, 0))
        assertFalse(reviewEligible(false, 2, 50, now, 0))
        assertFalse(reviewEligible(false, 5, 19, now, 0))
    }
    @Test fun experiencedPlayerCanBeInvited() {
        assertTrue(reviewEligible(false, 3, 20, now, 0))
    }
    @Test fun optingOutOrAlreadyReviewedStopsInvitations() {
        assertFalse(reviewEligible(true, 100, 1000, now, 0))
    }
    @Test fun cooldownAppliesIncludingClockMovingBackwards() {
        assertFalse(reviewEligible(false, 3, 20, now, now - REVIEW_COOLDOWN + 1))
        assertFalse(reviewEligible(false, 3, 20, now, now + 1))
        assertTrue(reviewEligible(false, 3, 20, now, now - REVIEW_COOLDOWN))
    }
}
