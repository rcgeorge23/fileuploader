package uk.co.novinet.web

import geb.spock.GebSpec
import org.apache.commons.io.FileUtils
import uk.co.novinet.e2e.SftpDocument
import uk.co.novinet.e2e.TestSftpService

import java.time.Instant

import static uk.co.novinet.e2e.TestUtils.*
import static uk.co.novinet.web.GebTestUtils.createFileWithSize

class UnknownUserWithNewParameterIT extends GebSpec {

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
        new TestSftpService().removeAllDocsForEmailAddress("test@test.com")
        setupDatabaseSchema()
    }

    def "empty membership form is displayed when n=t parameter is passed on query string"() {
        given:
            go "http://localhost:8383?n=t"

        when:
            waitFor { at MembershipFormPage }

        then:
            assert documentUploadErrorBanner.displayed == false
            waitFor { nameInput.value() == '' }
            waitFor { mpNameInput.value() == '' }
            waitFor { mpConstituencyInput.value() == '' }
            waitFor { mpPartyInput.value() == '' }
            waitFor { mpEngagedInput[0].value() == null }
            waitFor { mpSympatheticInput[0].value() == null }
            waitFor { schemesInput.value() == '' }
            waitFor { industryInput.value() == '' }
            waitFor { howDidYouHearAboutLcagInput.value() == '' }
            waitFor { memberOfBigGroupInput[0].value() == null }
    }

    def "when t=f, we go to the not found page"() {
        given:
        go "http://localhost:8383?n=f"

        when:
        waitFor { at NotFoundPage }

        then:
        assert at(NotFoundPage)
    }

    def "can complete membership form with id and proof of scheme docs"() {
        given:
        go "http://localhost:8383?n=t"
        waitFor { at MembershipFormPage }
        File idendificationFile = createFileWithSize(100)
        File proofOfSchemeInvolvementFile = createFileWithSize(100)
        assert new TestSftpService().getAllDocumentsForEmailAddress("test@test.com").size() == 0
        assert documentUploadErrorBanner.displayed == false

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

        then:
        waitFor { at ThankYouPage }
        def enquiry = getEnquiryRows().get(0)
        assert enquiry.name == "john smith"
        assert enquiry.emailAddress == "test@test.com"
        assert enquiry.mpName == "some mp"
        assert enquiry.mpParty == "conservative"
        assert enquiry.mpConstituency == "some constituency"
        assert enquiry.mpEngaged == true
        assert enquiry.mpSympathetic == true
        assert enquiry.schemes == "scheme 1 and scheme 2"
        assert enquiry.industry == "some industry"
        assert enquiry.howDidYouHearAboutLcag == "from contractor uk forum"
        assert enquiry.memberOfBigGroup == false
        assert enquiry.bigGroupUsername == ""
        assert enquiry.hasBeenProcessed == false
        assert enquiry.dateCreated.isAfter(Instant.now().minusMillis(1000))
        assert new TestSftpService().getAllDocumentsForEmailAddress("test@test.com").size() == 2
        assert new TestSftpService().getAllDocumentsForEmailAddress("test@test.com").get(0).getSize() > 0L
        assert new TestSftpService().getAllDocumentsForEmailAddress("test@test.com").get(1).getSize() > 0L
    }

    def "can complete membership form with big group username"() {
        given:
        go "http://localhost:8383?n=t"
        waitFor { at MembershipFormPage }

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
        bigGroupUsernameInput.value(bigGroupUsername)
        submitButton.click()

        then:
        waitFor { at ThankYouPage }
        def enquiry = getEnquiryRows().get(0)
        assert enquiry.name == "john smith"
        assert enquiry.emailAddress == "test@test.com"
        assert enquiry.mpName == "some mp"
        assert enquiry.mpParty == "conservative"
        assert enquiry.mpConstituency == "some constituency"
        assert enquiry.mpEngaged == true
        assert enquiry.mpSympathetic == true
        assert enquiry.schemes == "scheme 1 and scheme 2"
        assert enquiry.industry == "some industry"
        assert enquiry.howDidYouHearAboutLcag == "from contractor uk forum"
        assert enquiry.memberOfBigGroup == true
        assert enquiry.bigGroupUsername == bigGroupUsername
        assert enquiry.hasBeenProcessed == false
        assert enquiry.dateCreated.isAfter(Instant.now().minusMillis(1000))
        assert new TestSftpService().getAllDocumentsForEmailAddress("test@test.com").size() == 0
    }

}
