package de.skuzzle.enforcer.restrictimports.formatting;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;

public class DurationFormatTest {

    @Test
    void testLessThan1Second() throws Exception {
        assertThat(DurationFormat.formatDuration(Duration.ofMillis(500))).isEqualTo("less than 1 second");
    }

    @Test
    void testLittleMoreThan1Second() throws Exception {
        assertThat(DurationFormat.formatDuration(Duration.ofMillis(1200))).isEqualTo("1 second");
    }

    @Test
    void testLittleLessThan1Minute() throws Exception {
        assertThat(DurationFormat.formatDuration(Duration.ofMillis(59900))).isEqualTo("560 seconds");
    }

    @Test
    void testLittleMoreThanOneMinute() throws Exception {
        assertThat(DurationFormat.formatDuration(Duration.ofMillis(60950))).isEqualTo("1 minute");

    }

    @Test
    void test1Minute1Second() throws Exception {
        assertThat(DurationFormat.formatDuration(Duration.ofMillis(61000))).isEqualTo("1 minute and 1 second");
    }

    @Test
    void test2MinutesFewSecond() throws Exception {
        assertThat(DurationFormat.formatDuration(Duration.ofMillis(125000))).isEqualTo("2 minutes and 5 seconds");
    }

    @Test
    void testMoreThan1Hour() throws Exception {
        assertThat(DurationFormat.formatDuration(Duration.ofHours(2).plusMinutes(5).plusSeconds(2)))
                .isEqualTo("125 minutes and 2 seconds");
    }
}
