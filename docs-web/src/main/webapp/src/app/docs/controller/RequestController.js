'use strict';

/**
 * Request account controller.
 */
angular.module('docs').controller('RequestModal', function($scope, $uibModalInstance, Restangular, $translate, $dialog) {
    $scope.request = {};

    $scope.submit = function() {
        if (!$scope.request.username || !$scope.request.password || !$scope.request.email) {
            $dialog.error($translate.instant('request.validation_error'));
            return;
        }

        // Follow the same pattern as password_lost
        Restangular.one('userrequest').customPUT({
            username: $scope.request.username,
            password: $scope.request.password,
            email: $scope.request.email
        }).then(function(response) {
            console.log('Success response:', response);
            $dialog.success($translate.instant('request.submit_success'));
            $uibModalInstance.close();
        }, function(response) {
            console.log('Error response:', response);
            if (response.data.type === 'AlreadyExistingUsername') {
                $dialog.error($translate.instant('request.username_exists'));
            } else {
                $dialog.error($translate.instant('request.submit_error'));
            }
        });
    };

    $scope.cancel = function() {
        $uibModalInstance.dismiss('cancel');
    };
});