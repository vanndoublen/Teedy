'use strict';

/**
 * Request account controller.
 */
App.controller('RequestModal', function($scope, $uibModalInstance, Restangular, $translate, $dialog) {
    $scope.request = {};

    // Submit the request
    $scope.submit = function() {
        // Basic validation
        if (!$scope.request.username || !$scope.request.email || !$scope.request.password) {
            return;
        }

        // Call the API endpoint
        Restangular.one('userrequest').customPUT({
            username: $scope.request.username,
            password: $scope.request.password,
            email: $scope.request.email
        }).then(function() {
            // Success
            $dialog.success($translate.instant('request.submit_success'));
            $uibModalInstance.close();
        }, function(response) {
            // Error handling
            if (response.data.type === 'AlreadyExistingUsername') {
                $dialog.error($translate.instant('request.username_exists'));
            } else {
                $dialog.error($translate.instant('request.submit_error'));
            }
        });
    };

    // Cancel and close modal
    $scope.cancel = function() {
        $uibModalInstance.dismiss('cancel');
    };
});