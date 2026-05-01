package uk.gov.hmcts.opal.documents.tmp;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import lombok.Data;

@Data
@SuppressWarnings("AbbreviationAsWordInName")
public class HMRCDocumentDto {

    @JsonProperty("first_name")
    private String firstName;

    private Individual individual;

    @JsonProperty("contact_details")
    private ContactDetails contactDetails;

    private List<Employment> employments;

    private Paye paye;

    @JsonProperty("self_assessment")
    private SelfAssessment selfAssessment;

    @JsonProperty("residences_rows")
    private List<ResidencesRow> residencesRows;

    // getters/setters
    @Data
    public static class Individual {

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;

        @JsonProperty("date_of_birth")
        private String dateOfBirth;

        private String nino;

        @JsonProperty("account_number")
        private String accountNumber;
        // getters/setters
    }

    @Data
    public static class ContactDetails {

        @JsonProperty("daytime_telephones")
        private List<String> daytimeTelephones;

        @JsonProperty("evening_telephones")
        private List<String> eveningTelephones;

        @JsonProperty("mobile_telephones")
        private List<String> mobileTelephones;
        // getters/setters
    }

    @Data
    public static class Employment {

        private Employer employer;

        @JsonProperty("start_date")
        private String startDate;

        @JsonProperty("end_date")
        private String endDate;

        @JsonProperty("to_date")
        private String toDate;
        // getters/setters
    }

    @Data
    public static class Employer {

        private String name;

        @JsonProperty("paye_reference")
        private String payeReference;

        private Address address;
        // getters/setters
    }

    @Data
    public static class Address {

        private String line1;
        private String line2;
        private String line3;
        private String line4;
        private String line5;

        private String postcode;

        @JsonProperty("postal_code")
        private String postalCode;
        // getters/setters
    }

    @Data
    public static class Paye {

        private List<Income> income;
        // getters/setters
    }

    @Data
    public static class Income {

        @JsonProperty("employer_paye_reference")
        private String employerPayeReference;

        private Payroll payroll;

        @JsonProperty("payment_date")
        private String paymentDate;

        @JsonProperty("gross_earnings_for_nics")
        private GrossEarningsForNics grossEarningsForNics;
        // getters/setters
    }

    @Data
    public static class Payroll {

        private String id;
        // getters/setters
    }

    @Data
    public static class GrossEarningsForNics {

        @JsonProperty("in_pay_period1")
        private Double inPayPeriod1;

        @JsonProperty("in_pay_period2")
        private Double inPayPeriod2;

        @JsonProperty("in_pay_period3")
        private Double inPayPeriod3;

        @JsonProperty("in_pay_period4")
        private Double inPayPeriod4;
        // getters/setters
    }

    @Data
    public static class SelfAssessment {

        private List<Registration> registrations;

        @JsonProperty("tax_returns")
        private List<TaxReturn> taxReturns;
        // getters/setters
    }

    @Data
    public static class Registration {

        @JsonProperty("registration_date")
        private String registrationDate;
        // getters/setters
    }

    @Data
    public static class TaxReturn {

        @JsonProperty("tax_year")
        private String taxYear;

        private List<Submission> submissions;

        private List<Source> sources;
        // getters/setters
    }

    @Data
    public static class Submission {

        @JsonProperty("received_date")
        private String receivedDate;
        // getters/setters
    }

    @Data
    public static class Source {

        @JsonProperty("business_description")
        private String businessDescription;

        @JsonProperty("business_address")
        private Address businessAddress;

        @JsonProperty("telephone_number")
        private String telephoneNumber;
        // getters/setters
    }

    @Data
    public static class ResidencesRow {

        private Residence base;
        private Residence correspondence;
        // getters/setters
    }

    @Data
    public static class Residence {

        @JsonProperty("in_use")
        private String inUse;

        private Address address;
        // getters/setters
    }
}