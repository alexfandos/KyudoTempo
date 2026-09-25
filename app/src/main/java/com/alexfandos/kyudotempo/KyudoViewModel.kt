package com.alexfandos.kyudotempo

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TachiUiState(
    val phase: TachiPhase = TachiPhase.FIRST_ARROW,
    val currentStepIndex: Int = 0,
    val archers: List<Archer> = listOf(
        Archer(1, "Omae", ArcherState.PREPARING_ARROWS, 2),
        Archer(2, "Ni-teki", ArcherState.PREPARING_ARROWS, 2),
        Archer(3, "San-teki", ArcherState.PREPARING_ARROWS, 2),
        Archer(4, "Ochimae", ArcherState.PREPARING_ARROWS, 2),
        Archer(5, "Ochi", ArcherState.PREPARING_ARROWS, 2)
    ),
    val isCompleted: Boolean = false,
    val tachiSize: Int = 5
)

class KyudoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TachiUiState())
    val uiState: StateFlow<TachiUiState> = _uiState.asStateFlow()

    private var currentSteps = generateSteps(5)

    fun setTachiSize(size: Int) {
        if (size in 3..5) {
            currentSteps = generateSteps(size)
            val (phase, archers) = currentSteps[0]
            _uiState.value = TachiUiState(
                phase = phase,
                currentStepIndex = 0,
                archers = archers,
                isCompleted = false,
                tachiSize = size
            )
        }
    }

    private fun generateSteps(size: Int): List<Pair<TachiPhase, List<Archer>>> {
        val n = size
        // The archer before Ochi is always Ochimae; numbered -teki names fill the middle.
        val names = listOf("Omae") + listOf("Ni-teki", "San-teki").take(n - 3) + listOf("Ochimae", "Ochi")

        val standRow1 = IntArray(n + 1)
        val shootRow1 = IntArray(n + 1)
        val sitRow1   = IntArray(n + 1)
        for (p in 1..n) {
            standRow1[p] = p + 1
            shootRow1[p] = p + 2
            sitRow1[p]   = p + 3
        }

        val paRow = IntArray(n + 1)
        for (p in 1..n - 2) paRow[p] = sitRow1[n - 1]
        paRow[n - 1] = sitRow1[n]
        paRow[n]     = sitRow1[n] + 1

        val standRow2 = IntArray(n + 1)
        val shootRow2 = IntArray(n + 1)
        val sitRow2   = IntArray(n + 1)
        val finalRow  = IntArray(n + 1)

        standRow2[1] = sitRow1[n]
        shootRow2[1] = standRow2[1] + 1
        sitRow2[1]   = shootRow2[1] + 1

        standRow2[2] = shootRow2[1]
        shootRow2[2] = standRow2[2] + 1
        sitRow2[2]   = shootRow2[2] + 1

        for (p in 3..n) {
            standRow2[p] = sitRow2[p - 2]
            shootRow2[p] = standRow2[p] + 1
            sitRow2[p]   = shootRow2[p] + 1
        }
        for (p in 1..n) finalRow[p] = sitRow2[p] + 1

        val totalRows = finalRow[n]

        fun checkpoints(p: Int): List<Pair<Int, ArcherState>> {
            val raw = listOf(
                1 to ArcherState.PREPARING_ARROWS,
                standRow1[p] to ArcherState.STANDING_REMOVING_ARROW,
                shootRow1[p] to ArcherState.SHOOTING,
                sitRow1[p]   to ArcherState.SITTING_DOWN,
                paRow[p]     to ArcherState.PREPARING_SECOND_ARROW,
                standRow2[p] to ArcherState.STANDING_REMOVING_ARROW,
                shootRow2[p] to ArcherState.SHOOTING,
                sitRow2[p]   to ArcherState.LEAVING_DOJO,
                finalRow[p]  to ArcherState.GONE
            )
            val withHolds = mutableListOf<Pair<Int, ArcherState>>()
            for (i in raw.indices) {
                withHolds += raw[i]
                val next = raw.getOrNull(i + 1)?.first
                if (next != null && next > raw[i].first + 1) {
                    withHolds += (raw[i].first + 1) to ArcherState.STANDBY
                }
            }
            return withHolds
        }

        fun arrowsAt(p: Int, row: Int) = when {
            row < sitRow1[p] -> 2
            row < sitRow2[p] -> 1
            else              -> 0
        }

        val cp = (1..n).associateWith { checkpoints(it) }

        return (1..totalRows).map { row ->
            val archers = names.mapIndexed { idx, name ->
                val p = idx + 1
                val state = cp.getValue(p).last { it.first <= row }.second
                Archer(p, name, state, arrowsAt(p, row))
            }
            val phase = when {
                row <= sitRow1[n]  -> TachiPhase.FIRST_ARROW
                row == totalRows   -> TachiPhase.COMPLETED
                else                -> TachiPhase.SECOND_ARROW
            }
            phase to archers
        }
    }

    fun nextStep() {
        val currentIndex = _uiState.value.currentStepIndex
        if (currentIndex < currentSteps.size - 1) {
            val nextIndex = currentIndex + 1
            val (phase, archers) = currentSteps[nextIndex]
            _uiState.value = _uiState.value.copy(
                phase = phase,
                currentStepIndex = nextIndex,
                archers = archers,
                isCompleted = (nextIndex == currentSteps.size - 1)
            )
        }
    }

    fun previousStep() {
        val currentIndex = _uiState.value.currentStepIndex
        if (currentIndex > 0) {
            val prevIndex = currentIndex - 1
            val (phase, archers) = currentSteps[prevIndex]
            _uiState.value = _uiState.value.copy(
                phase = phase,
                currentStepIndex = prevIndex,
                archers = archers,
                isCompleted = false
            )
        }
    }

    fun reset() {
        val (phase, archers) = currentSteps[0]
        _uiState.value = _uiState.value.copy(
            phase = phase,
            currentStepIndex = 0,
            archers = archers,
            isCompleted = false
        )
    }
}
