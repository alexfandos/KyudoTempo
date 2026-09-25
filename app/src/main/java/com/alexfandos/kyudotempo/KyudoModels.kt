package com.alexfandos.kyudotempo

enum class ArcherState(val label: String, val actionHint: String) {
    PREPARING_ARROWS("Preparing (2 arrows)", "Preparing bow with 2 arrows"),
    PREPARING_SECOND_ARROW("Preparing (1 arrow)", "Preparing bow with 1 arrow"),
    SITTING_WAITING("Waiting", "Sitting in line"),
    STANDING_REMOVING_ARROW("Standing Up", "Stands up & Ashibumi & Dozukuri"),
    STANDBY("Standby", "Waiting, no change"),
    SHOOTING("Shooting", "Yugamae → … → Hanare"),
    SITTING_DOWN("Sitting Down", "Zanshin & Yudaoshi & Sit down"),
    LEAVING_DOJO("Leaving Dojo", "Exiting dojo (2nd arrow complete)"),
    GONE("Done", "Exited dojo")
}

data class Archer(
    val id: Int,
    val name: String,
    val state: ArcherState = ArcherState.PREPARING_ARROWS,
    val arrowCount: Int = 2
)

enum class TachiPhase(val title: String) {
    FIRST_ARROW("Haya (First Arrow)"),
    SECOND_ARROW("Otoya (Second Arrow)"),
    COMPLETED("Tachi Completed")
}
