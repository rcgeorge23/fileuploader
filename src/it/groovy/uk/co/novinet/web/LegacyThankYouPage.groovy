package uk.co.novinet.web

import geb.Page

import static uk.co.novinet.e2e.TestUtils.applicationHost
import static uk.co.novinet.e2e.TestUtils.applicationPort

class LegacyThankYouPage extends Page {

    static url = "http://${applicationHost()}:${applicationPort()}/legacyThankYou"

    static at = { title == "Loan Charge Action Group | Thank you for submitting the membership form" }

    static content = {

    }
}
