'use strict';

/**
 * Settings user request page controller.
 */
angular.module('docs').controller('SettingsUserRequest', function($scope, $state, $dialog, UserRequest, $translate) {
    /**
     * Load pending user requests.
     */
    $scope.loadRequests = function() {
        UserRequest.getPendingRequests().then(function(data) {
            console.log('Received data:', data);
            $scope.requests = data.requests || [];
        }).catch(function (error){
            console.error('Load requests error:', error);
            $dialog.error($translate.instant('settings.userrequest.load_error'));
        });
    };

    $scope.loadRequests();

    /**
     * Approve a user request.
     */
    $scope.approve = function(request) {
        var title = $translate.instant('settings.userrequest.approve_title');
        var msg = $translate.instant('settings.userrequest.approve_message', { username: request.username });
        var btns = [
            { result:'cancel', label: $translate.instant('cancel') },
            { result:'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }
        ];

        $dialog.messageBox(title, msg, btns, function(result) {
            if (result === 'ok') {
                UserRequest.approve(request.id).then(function() {
                    $scope.loadRequests();
                });
            }
        });
    };

    /**
     * Reject a user request.
     */
    $scope.reject = function(request) {
        var title = $translate.instant('settings.userrequest.reject_title');
        var msg = $translate.instant('settings.userrequest.reject_message', { username: request.username });
        var btns = [
            { result:'cancel', label: $translate.instant('cancel') },
            { result:'ok', label: $translate.instant('ok'), cssClass: 'btn-danger' }
        ];

        $dialog.messageBox(title, msg, btns, function(result) {
            if (result === 'ok') {
                UserRequest.reject(request.id).then(function() {
                    $scope.loadRequests();
                });
            }
        });
    };
});