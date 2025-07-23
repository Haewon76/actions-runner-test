package com.cashmallow.api.interfaces.aml.complyadvantage.dto;

import com.cashmallow.api.domain.model.traveler.Traveler;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

public record ComplyAdvantageCreateCustomerRequest(ComplyAdvantageCustomer customer,
                                                   ComplyAdvantageConfiguration configuration,
                                                   ComplyAdvantageMonitoring monitoring
) {
    public static ComplyAdvantageCreateCustomerRequest of(Traveler traveler, String configId, String birthDate) {

        ComplyAdvantagePersonBirthDate personBirthDate = null;
        if (ObjectUtils.isNotEmpty(birthDate)) {
            int year = Integer.parseInt(birthDate.substring(0, 4));
            int month = Integer.parseInt(birthDate.substring(4, 6));
            int day = Integer.parseInt(birthDate.substring(6, 8));

            personBirthDate = new ComplyAdvantagePersonBirthDate(year, month, day);
        }

        ComplyAdvantagePerson person = new ComplyAdvantagePerson(traveler.getEnFirstName(),
                traveler.getEnLastName(), traveler.getSex().name(), personBirthDate);

        return new ComplyAdvantageCreateCustomerRequest(new ComplyAdvantageCustomer(person, "CM" + traveler.getUserId().toString()),
                new ComplyAdvantageConfiguration(configId),
                new ComplyAdvantageMonitoring());
    }

    @Getter
    @Setter
    private static class ComplyAdvantageConfiguration {
        @JsonProperty("screening_configuration_identifier")
        private String screeningConfigurationIdentifier;

        public ComplyAdvantageConfiguration(String screeningConfigurationIdentifier) {
            this.screeningConfigurationIdentifier = screeningConfigurationIdentifier;
        }
    }

    @Getter
    @Setter
    private static class ComplyAdvantageCustomer {
        private ComplyAdvantagePerson person;
        @JsonProperty("external_identifier")
        private String externalIdentifier;

        public ComplyAdvantageCustomer(ComplyAdvantagePerson person, String externalIdentifier) {
            this.person = person;
            this.externalIdentifier = externalIdentifier;
        }
    }

    @Getter
    @Setter
    private static class ComplyAdvantagePerson {
        @JsonProperty("first_name")
        private String firstName;
        @JsonProperty("last_name")
        private String lastName;
        private String gender;
        @JsonProperty("date_of_birth")
        private ComplyAdvantagePersonBirthDate birthDate;

        public ComplyAdvantagePerson(String firstName, String lastName, String gender, ComplyAdvantagePersonBirthDate birthDate) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.gender = gender;
            this.birthDate = birthDate;
        }
    }

    @Getter
    @Setter
    private static class ComplyAdvantageMonitoring {
        @JsonProperty("entity_screening")
        private ComplyAdvantageEntityScreening entityScreening;

        public ComplyAdvantageMonitoring() {
            this.entityScreening = new ComplyAdvantageEntityScreening();
        }
    }

    @Getter
    @Setter
    private static class ComplyAdvantageEntityScreening {
        private boolean enabled = true;
    }

    @Getter
    @Setter
    private static class ComplyAdvantagePersonBirthDate {
        private int year;
        private int month;
        private int day;

        public ComplyAdvantagePersonBirthDate(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }
    }
}
