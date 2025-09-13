package at.itdo.tcrunner

import at.itdo.tcrunner.run.parsing.TeamCityEventEmitter
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TeamCityEventEmitterTest {

    private val emitter = TeamCityEventEmitter()

    @Test
    fun `should convert seconds to milliseconds`() {
        assertEquals("2000", emitter.convertDurationToMs("2s"))
        assertEquals("1500", emitter.convertDurationToMs("1.5s"))
    }

    @Test
    fun `should convert minutes to milliseconds`() {
        assertEquals("60000", emitter.convertDurationToMs("1m"))
        assertEquals("90000", emitter.convertDurationToMs("1.5m"))
    }

    @Test
    fun `should handle milliseconds directly`() {
        assertEquals("500", emitter.convertDurationToMs("500ms"))
        assertEquals("1250", emitter.convertDurationToMs("1250ms"))
    }

    @Test
    fun `should handle plain numbers as milliseconds`() {
        assertEquals("1000", emitter.convertDurationToMs("1000"))
        assertEquals("0", emitter.convertDurationToMs("invalid"))
    }
}