package uk.co.novinet.web

import geb.spock.GebSpec
import org.apache.commons.io.FileUtils
import uk.co.novinet.e2e.TestUtils
import uk.co.novinet.e2e.User

import static uk.co.novinet.e2e.TestUtils.*

class FormSubmissionIT extends GebSpec {

    static final mpName = "mpName"
    static final mpConstituency = "mpConstituency"
    static final mpParty = "mpParty"
    static final mpEngaged = 1
    static final mpSympathetic = 1
    static final schemes = "schemes"
    static final industry = "industry"
    static final howDidYouHearAboutLcag = "howDidYouHearAboutLcag"
    static final memberOfBigGroup = 1
    static final bigGroupUsername = "bigGroupUsername"

    def setup() {
        setupDatabaseSchema()
    }

    def "membership form is pre-populated with member data"() {
        given:
            prepopulateMemberDataInDb()
            go "http://localhost:8383?token=1234_1"

        when:
            waitFor { at MembershipFormPage }

        then:
            waitFor { nameInput.value() == 'Test Name1' }
            waitFor { mpNameInput.value() == mpName }
            waitFor { mpConstituencyInput.value() == mpConstituency }
            waitFor { mpPartyInput.value() == mpParty }
            waitFor { mpEngagedInput[0].value() == "true" }
            waitFor { mpSympatheticInput[0].value() == "true" }
            waitFor { schemesInput.value() == schemes }
            waitFor { industryInput.value() == industry }
            waitFor { howDidYouHearAboutLcagInput.value() == howDidYouHearAboutLcag }
            waitFor { memberOfBigGroupInput[0].value() == "true" }
            waitFor { bigGroupUsernameInput.value() == bigGroupUsername }
    }

    def "membership form cannot be submitted when fields are blank"() {
        given:
            go "http://localhost:8383?token=1234_1"
            waitFor { at MembershipFormPage }
            nameInput.value("")
            emailAddressInput.value("")

        when:
            submitButton.click()

        then:
            waitFor { at MembershipFormPage }
            waitFor { nameInputError.displayed }
            assert nameInputError.displayed
            assert emailAddressError.displayed
            assert mpNameError.displayed
            assert mpPartyError.displayed
            assert mpConstituencyError.displayed
            assert mpEngagedError.displayed
            assert mpSympatheticError.displayed
            assert schemesError.displayed
            assert industryError.displayed
            assert howDidYouHearAboutLcagError.displayed
            assert memberOfBigGroupError.displayed
    }

    def "big group username field is displayed when i say i am a member of big group"() {
        given:
            go "http://localhost:8383?token=1234_1"
            waitFor { at MembershipFormPage }
            nameInput.value("")
            emailAddressInput.value("")
            assert bigGroupUsernameInput.displayed == false
            assert identificationInput.displayed == false
            assert proofOfSchemeInvolvementInput.displayed == false

        when:
            memberOfBigGroupInput.value(true)

        then:
            waitFor { bigGroupUsernameInput.displayed }
            assert bigGroupUsernameInput.displayed == true
            assert identificationInput.displayed == false
            assert proofOfSchemeInvolvementInput.displayed == false

        when: "i try to submit the form"
            submitButton.click()

        then: "all previous errors are displayed as well as the big group username validation error"
            waitFor { at MembershipFormPage }
            waitFor { nameInputError.displayed }
            assert nameInputError.displayed
            assert emailAddressError.displayed
            assert mpNameError.displayed
            assert mpPartyError.displayed
            assert mpConstituencyError.displayed
            assert mpEngagedError.displayed
            assert mpSympatheticError.displayed
            assert schemesError.displayed
            assert industryError.displayed
            assert howDidYouHearAboutLcagError.displayed
            assert memberOfBigGroupError.displayed == false
            assert bigGroupUsernameError.displayed
    }

    def "id and scheme doc upload fields are displayed when i say i am not a member of big group"() {
        given:
            go "http://localhost:8383?token=1234_1"
            waitFor { at MembershipFormPage }
            nameInput.value("")
            emailAddressInput.value("")
            assert bigGroupUsernameInput.displayed == false
            assert identificationInput.displayed == false
            assert proofOfSchemeInvolvementInput.displayed == false

        when:
            memberOfBigGroupInput.value(false)

        then:
            waitFor { identificationInput.displayed }
            waitFor { proofOfSchemeInvolvementInput.displayed }
            assert bigGroupUsernameInput.displayed == false
            assert identificationInput.displayed == true
            assert proofOfSchemeInvolvementInput.displayed == true

        when: "i try to submit the form"
            submitButton.click()

        then: "all previous errors are displayed as well as the big group username validation error"
            waitFor { at MembershipFormPage }
            waitFor { nameInputError.displayed }
            assert nameInputError.displayed
            assert emailAddressError.displayed
            assert mpNameError.displayed
            assert mpPartyError.displayed
            assert mpConstituencyError.displayed
            assert mpEngagedError.displayed
            assert mpSympatheticError.displayed
            assert schemesError.displayed
            assert industryError.displayed
            assert howDidYouHearAboutLcagError.displayed
            assert bigGroupUsernameError.present() == false
            assert identificationError.displayed
            assert proofOfSchemeInvolvementError.displayed
    }

    def "can complete membership form as existing big group user"() {
        given:
            go "http://localhost:8383?token=1234_1"
            waitFor { at MembershipFormPage }
//            File idendificationFile = File.createTempFile("lcag", "test")
//            FileUtils.write(idendificationFile, "This is some text", "UTF-8")

        when:
            nameInput.value("john smith")
            emailAddressInput.value("test@test.com")
            mpNameInput.value("some mp")
            mpPartyInput.value("conservative")
            mpConstituencyInput.value("some constituency")
            mpEngagedInput.value(true)
            mpSympatheticInput.value(true)
            schemesInput.value("scheme 1 and scheme 2")
            industryInput.value("some industry")
            howDidYouHearAboutLcagInput.value("from contractor uk forum")
            memberOfBigGroupInput.value(true)
            bigGroupUsernameInput.value("bigGroupUsername")
            submitButton.click()

        then: "all previous errors are displayed as well as the big group username validation error"
            waitFor { at ThankYouPage }
            def member = getUserRows().get(0)
            assert member.name == "john smith"
            assert member.emailAddress == "test@test.com"
            assert member.mpName == "some mp"
            assert member.mpParty == "conservative"
            assert member.mpConstituency == "some constituency"
            assert member.mpEngaged == true
            assert member.mpSympathetic == true
            assert member.schemes == "scheme 1 and scheme 2"
            assert member.industry == "some industry"
            assert member.howDidYouHearAboutLcag == "from contractor uk forum"
            assert member.memberOfBigGroup == true
            assert member.bigGroupUsername == "bigGroupUsername"
    }

    def "can complete membership form with id and proof of scheme docs"() {
        given:
            go "http://localhost:8383?token=1234_1"
            waitFor { at MembershipFormPage }
            File idendificationFile = File.createTempFile("lcag", "test")
            FileUtils.write(idendificationFile, "Identification", "UTF-8")
            File proofOfSchemeInvolvementFile = File.createTempFile("lcag", "test")
            FileUtils.write(proofOfSchemeInvolvementFile, "Proof of scheme involvement", "UTF-8")

        when:
            nameInput.value("john smith")
            emailAddressInput.value("test@test.com")
            mpNameInput.value("some mp")
            mpPartyInput.value("conservative")
            mpConstituencyInput.value("some constituency")
            mpEngagedInput.value(true)
            mpSympatheticInput.value(true)
            schemesInput.value("scheme 1 and scheme 2")
            industryInput.value("some industry")
            howDidYouHearAboutLcagInput.value("from contractor uk forum")
            memberOfBigGroupInput.value(false)
            identificationInput.value(idendificationFile.getAbsolutePath())
            proofOfSchemeInvolvementInput.value(proofOfSchemeInvolvementFile.getAbsolutePath())
            submitButton.click()

        then: "all previous errors are displayed as well as the big group username validation error"
            waitFor { at ThankYouPage }
            def member = getUserRows().get(0)
            assert member.name == "john smith"
            assert member.emailAddress == "test@test.com"
            assert member.mpName == "some mp"
            assert member.mpParty == "conservative"
            assert member.mpConstituency == "some constituency"
            assert member.mpEngaged == true
            assert member.mpSympathetic == true
            assert member.schemes == "scheme 1 and scheme 2"
            assert member.industry == "some industry"
            assert member.howDidYouHearAboutLcag == "from contractor uk forum"
            assert member.memberOfBigGroup == false
            assert member.bigGroupUsername == ""
    }



    private prepopulateMemberDataInDb() {
        runSqlUpdate("update `i7b0_users` set " +
                "mp_name = '" + mpName + "', " +
                "mp_constituency = '" + mpConstituency + "', " +
                "mp_party = '" + mpParty + "', " +
                "mp_engaged = '" + mpEngaged + "', " +
                "mp_sympathetic = '" + mpSympathetic + "', " +
                "schemes = '" + schemes + "', " +
                "industry = '" + industry + "', " +
                "how_did_you_hear_about_lcag = '" + howDidYouHearAboutLcag + "', " +
                "member_of_big_group = '" + memberOfBigGroup + "', " +
                "big_group_username = '" + bigGroupUsername + "' " +
                "where uid = 1"
        )
    }
}
