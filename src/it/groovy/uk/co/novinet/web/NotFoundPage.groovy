package uk.co.novinet.web

import geb.Page

import static uk.co.novinet.e2e.TestUtils.applicationHost
import static uk.co.novinet.e2e.TestUtils.applicationPort

class NotFoundPage extends Page {

    static url = "http://${applicationHost()}:${applicationPort()}"

    static at = { title == "Loan Charge Action Group | Token Not Found" }

    static content = {

    }
}
