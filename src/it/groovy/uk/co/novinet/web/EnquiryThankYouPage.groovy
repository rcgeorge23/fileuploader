package uk.co.novinet.web

import geb.Page

import static uk.co.novinet.e2e.TestUtils.applicationHost
import static uk.co.novinet.e2e.TestUtils.applicationPort

class EnquiryThankYouPage extends Page {

    static url = "http://${applicationHost()}:${applicationPort()}/enquiryThankYou"

    static at = { title == "Loan Charge Action Group | Thank you for your enquiry" }

    static content = {

    }
}
