'use strict';

/**
 * User Request service.
 */
angular.module('docs').factory('UserRequest', function(Restangular) {
    return {
        /**
         * Returns all pending user requests.
         */
        getPendingRequests: function() {
            return Restangular.one('userrequest').get();
        },

        /**
         * Approve a user request.
         */
        approve: function(id) {
            return Restangular.one('userrequest', id).one('approve').put();
        },

        /**
         * Reject a user request.
         */
        reject: function(id) {
            return Restangular.one('userrequest', id).one('reject').put();
        }
    }
});