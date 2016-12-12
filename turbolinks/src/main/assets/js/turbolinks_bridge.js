function TLWebView(controller) {
    this.controller = controller
    controller.adapter = this

    var turbolinksIsReady = typeof Turbolinks !== "undefined" && Turbolinks !== null
    TurbolinksNative.setTurbolinksIsReady(turbolinksIsReady);
}

TLWebView.prototype = {
    // -----------------------------------------------------------------------
    // Starting point
    // -----------------------------------------------------------------------

    visitLocationWithActionAndRestorationIdentifier: function(location, action, restorationIdentifier) {
        console.log('visit: visitLocationWithActionAndRestorationIdentifier', location, action, restorationIdentifier);
        this.controller.startVisitToLocationWithAction(location, action, restorationIdentifier)
    },

    // -----------------------------------------------------------------------
    // Current visit
    // -----------------------------------------------------------------------

    issueRequestForVisitWithIdentifier: function(identifier) {
        if (identifier == this.currentVisit.identifier) {
            console.log('visit: issueRequestForVisitWithIdentifier', identifier);
            this.currentVisit.issueRequest()
        }
    },

    changeHistoryForVisitWithIdentifier: function(identifier) {
        if (identifier == this.currentVisit.identifier) {
            console.log('visit: changeHistoryForVisitWithIdentifier', identifier);
            this.currentVisit.changeHistory()
        }
    },

    loadCachedSnapshotForVisitWithIdentifier: function(identifier) {
        if (identifier == this.currentVisit.identifier) {
            console.log('visit: loadCachedSnapshotForVisitWithIdentifier', identifier);
            this.currentVisit.loadCachedSnapshot()
        }
    },

    loadResponseForVisitWithIdentifier: function(identifier) {
        if (identifier == this.currentVisit.identifier) {
            console.log('visit: loadResponseForVisitWithIdentifier', identifier);
            this.currentVisit.loadResponse()
        }
    },

    cancelVisitWithIdentifier: function(identifier) {
        if (identifier == this.currentVisit.identifier) {
            console.log('visit: cancelVisitWithIdentifier', identifier);
            this.currentVisit.cancel()
        }
    },

    // -----------------------------------------------------------------------
    // Adapter
    // -----------------------------------------------------------------------

    visitProposedToLocationWithAction: function(location, action) {
        console.log('adapt: visitProposedToLocationWithAction', location, action);
        TurbolinksNative.visitProposedToLocationWithAction(location.absoluteURL, action);
    },

    visitStarted: function(visit) {
        console.log('visitStarted', visit);
        this.currentVisit = visit
        TurbolinksNative.visitStarted(visit.identifier, visit.hasCachedSnapshot());
    },

    visitRequestStarted: function(visit) {
        console.log('adapt: visitRequestStarted', visit);
        // Purposely left unimplemented. visitStarted covers most cases and we'll keep an eye
        // on whether this is needed in the future
    },

    visitRequestCompleted: function(visit) {
        console.log('adapt: visitRequestCompleted', visit);
        TurbolinksNative.visitRequestCompleted(visit.identifier);
    },

    visitRequestFailedWithStatusCode: function(visit, statusCode) {
        console.log('adapt: visitRequestFailedWithStatusCode', visit, statusCode);
        TurbolinksNative.visitRequestFailedWithStatusCode(visit.identifier, statusCode);
    },

    visitRequestFinished: function(visit) {
        console.log('adapt: visitRequestFinished', visit);
        // Purposely left unimplemented. visitRequestCompleted covers most cases and we'll keep
        // an eye on whether this is needed in the future
    },

    visitRendered: function(visit) {
        console.log('adapt: visitRendered', visit);
        this.afterNextRepaint(function() {
            TurbolinksNative.visitRendered(visit.identifier)
        })
    },

    visitCompleted: function(visit) {
        console.log('adapt: visitCompleted', visit);
        TurbolinksNative.visitCompleted(visit.identifier, visit.restorationIdentifier)
    },

    pageInvalidated: function() {
        console.log('adapt: pageInvalidated');
        TurbolinksNative.pageInvalidated()
    },

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    afterNextRepaint: function(callback) {
      requestAnimationFrame(function() {
        requestAnimationFrame(callback)
      })
    }
}

try {
    window.webView = new TLWebView(Turbolinks.controller)
} catch (e) { // Most likely reached a page where Turbolinks.controller returned "Uncaught ReferenceError: Turbolinks is not defined"
    TurbolinksNative.turbolinksDoesNotExist()
}
