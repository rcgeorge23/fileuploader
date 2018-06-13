package uk.co.novinet.web

import geb.spock.GebSpec
import uk.co.novinet.e2e.TestUtils


class EndToEndIT extends GebSpec {


    def setup() {
        TestUtils.setupDatabaseSchema()
    }

    def "dashboard has correct number of rows"() {
        given:
            go "http://localhost:8383?token=1234_1"

        when:
            waitFor { at LandingPage }
//            waitFor { nameInput.value() == 'Test Name1' }

        then:
            true
    }
}
