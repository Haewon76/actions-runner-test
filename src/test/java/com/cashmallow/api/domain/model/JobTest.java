package com.cashmallow.api.domain.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;

class JobTest {

    @Test
    void getJobs() {
        Assertions.assertThat(Job.getJobs(Locale.KOREA)).isEqualTo(Job.getJobs(Locale.KOREAN));
        Assertions.assertThat(Job.getJobs(Locale.TRADITIONAL_CHINESE)).isEqualTo(Job.getJobs(Locale.CHINESE));
        Assertions.assertThat(Job.getJobs(Locale.JAPAN)).isEqualTo(Job.getJobs(Locale.JAPANESE));
        Assertions.assertThat(Job.getJobs(null)).isEqualTo(Job.getJobs(Locale.ENGLISH));
    }
}