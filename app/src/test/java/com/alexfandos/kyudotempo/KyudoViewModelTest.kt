package com.alexfandos.kyudotempo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KyudoViewModelTest {

    private val sizes = 3..5

    /** Walks the whole tachi for [size] and returns the state at every step. */
    private fun allSteps(size: Int): List<TachiUiState> {
        val viewModel = KyudoViewModel()
        viewModel.setTachiSize(size)
        val steps = mutableListOf(viewModel.uiState.value)
        while (!viewModel.uiState.value.isCompleted) {
            viewModel.nextStep()
            steps += viewModel.uiState.value
        }
        return steps
    }

    // --- Archer names ---

    @Test
    fun names_forThreeArchers() {
        assertEquals(listOf("Omae", "Ochimae", "Ochi"), allSteps(3).first().archers.map { it.name })
    }

    @Test
    fun names_forFourArchers() {
        assertEquals(listOf("Omae", "Ni-teki", "Ochimae", "Ochi"), allSteps(4).first().archers.map { it.name })
    }

    @Test
    fun names_forFiveArchers() {
        assertEquals(
            listOf("Omae", "Ni-teki", "San-teki", "Ochimae", "Ochi"),
            allSteps(5).first().archers.map { it.name }
        )
    }

    @Test
    fun initialState_matchesFirstStepOfFiveArcherTachi() {
        val initial = KyudoViewModel().uiState.value
        assertEquals(allSteps(5).first(), initial)
    }

    // --- Navigation ---

    @Test
    fun previousStep_atStart_doesNothing() {
        val viewModel = KyudoViewModel()
        val before = viewModel.uiState.value
        viewModel.previousStep()
        assertEquals(before, viewModel.uiState.value)
    }

    @Test
    fun nextStep_atEnd_doesNothing() {
        val viewModel = KyudoViewModel()
        while (!viewModel.uiState.value.isCompleted) viewModel.nextStep()
        val end = viewModel.uiState.value
        viewModel.nextStep()
        assertEquals(end, viewModel.uiState.value)
    }

    @Test
    fun nextThenPrevious_returnsToSameStep() {
        val viewModel = KyudoViewModel()
        viewModel.nextStep()
        viewModel.nextStep()
        val step2 = viewModel.uiState.value
        viewModel.nextStep()
        viewModel.previousStep()
        assertEquals(step2, viewModel.uiState.value)
    }

    @Test
    fun previousStep_fromEnd_clearsCompleted() {
        val viewModel = KyudoViewModel()
        while (!viewModel.uiState.value.isCompleted) viewModel.nextStep()
        viewModel.previousStep()
        assertFalse(viewModel.uiState.value.isCompleted)
    }

    @Test
    fun reset_returnsToFirstStep_andKeepsTachiSize() {
        val viewModel = KyudoViewModel()
        viewModel.setTachiSize(3)
        repeat(4) { viewModel.nextStep() }
        viewModel.reset()
        assertEquals(allSteps(3).first(), viewModel.uiState.value)
    }

    @Test
    fun setTachiSize_outOfRange_isIgnored() {
        val viewModel = KyudoViewModel()
        viewModel.nextStep()
        val before = viewModel.uiState.value
        viewModel.setTachiSize(2)
        viewModel.setTachiSize(6)
        assertEquals(before, viewModel.uiState.value)
    }

    @Test
    fun setTachiSize_restartsFromFirstStep() {
        val viewModel = KyudoViewModel()
        repeat(3) { viewModel.nextStep() }
        viewModel.setTachiSize(4)
        assertEquals(0, viewModel.uiState.value.currentStepIndex)
        assertEquals(4, viewModel.uiState.value.tachiSize)
        assertEquals(4, viewModel.uiState.value.archers.size)
    }

    // --- Tachi choreography, checked for every size ---

    @Test
    fun everyArcher_followsTheFullSequence() {
        val expected = listOf(
            ArcherState.PREPARING_ARROWS,
            ArcherState.STANDING_REMOVING_ARROW,
            ArcherState.SHOOTING,
            ArcherState.SITTING_DOWN,
            ArcherState.PREPARING_SECOND_ARROW,
            ArcherState.STANDING_REMOVING_ARROW,
            ArcherState.SHOOTING,
            ArcherState.LEAVING_DOJO,
            ArcherState.GONE
        )
        for (size in sizes) {
            val steps = allSteps(size)
            for (position in 0 until size) {
                val sequence = steps.map { it.archers[position].state }
                    .filter { it != ArcherState.STANDBY }
                    .fold(emptyList<ArcherState>()) { acc, s -> if (acc.lastOrNull() == s) acc else acc + s }
                assertEquals("size $size, archer ${position + 1}", expected, sequence)
            }
        }
    }

    @Test
    fun onlyOneArcherShoots_atATime() {
        for (size in sizes) {
            allSteps(size).forEachIndexed { index, step ->
                val shooting = step.archers.count { it.state == ArcherState.SHOOTING }
                assertTrue("size $size, step ${index + 1}: $shooting shooting", shooting <= 1)
            }
        }
    }

    @Test
    fun arrowCount_matchesPreparationStep() {
        for (size in sizes) {
            for (step in allSteps(size)) {
                for (archer in step.archers) {
                    when (archer.state) {
                        ArcherState.PREPARING_ARROWS -> assertEquals(2, archer.arrowCount)
                        ArcherState.PREPARING_SECOND_ARROW -> assertEquals(1, archer.arrowCount)
                        ArcherState.GONE -> assertEquals(0, archer.arrowCount)
                        else -> Unit
                    }
                }
            }
        }
    }

    @Test
    fun arrowCount_neverIncreases() {
        for (size in sizes) {
            allSteps(size).zipWithNext().forEach { (before, after) ->
                before.archers.zip(after.archers).forEach { (a, b) ->
                    assertTrue("size $size, ${a.name}", b.arrowCount <= a.arrowCount)
                }
            }
        }
    }

    @Test
    fun phases_goForwardFromFirstArrowToCompleted() {
        for (size in sizes) {
            val phases = allSteps(size).map { it.phase }
            assertEquals(TachiPhase.FIRST_ARROW, phases.first())
            assertEquals(TachiPhase.COMPLETED, phases.last())
            assertEquals("size $size", phases.sortedBy { it.ordinal }, phases)
        }
    }

    @Test
    fun onlyLastStep_isCompleted() {
        for (size in sizes) {
            val steps = allSteps(size)
            assertTrue(steps.last().isCompleted)
            assertTrue(steps.dropLast(1).none { it.isCompleted })
        }
    }
}
