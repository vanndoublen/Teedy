'use strict';

/**
 * Settings dashboard controller.
 */
angular.module('docs').controller('SettingsDashboard', function ($scope, Restangular) {
    //initialize chart data
    $scope.userActivityData = {
        labels: [], datasets: []
    };

    $scope.activityTypeData = {
        labels: [], datasets: [{
            data: [], backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40', '#8AC249']
        }]
    };

    $scope.activeDocuments = [];
    $scope.recentActivity = [];

    // load dashboard data
    $scope.loadDashboard = function () {
        Restangular.one('auditlog/dashboard').get().then(function (data) {
            const userActivity = data.userActivity;
            const users = Object.keys(userActivity);
            const allDates = new Set();

            // Collect all dates
            users.forEach(user => {
                Object.keys(userActivity[user]).forEach(date => allDates.add(date));
            });

            // Sort dates
            const sortedDates = Array.from(allDates).sort();
            $scope.userActivityData.labels = sortedDates;

            // create datasets for each user
            const colors = ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'];
            $scope.userActivityData.datasets = users.map((user, index) => {
                const color = colors[index % colors.length];
                const userData = sortedDates.map(date => userActivity[user][date] || 0);

                return {
                    label: user, data: userData, borderColor: color, backgroundColor: color + '33', // Add transparency
                    fill: true
                };
            });

            const activityByType = data.activityByType;
            $scope.activityTypeData.labels = Object.keys(activityByType);
            $scope.activityTypeData.datasets[0].data = Object.values(activityByType);

            $scope.activeDocuments = data.activeDocuments;


// recent activity
            if (data.recentActivity && data.recentActivity.length > 0) {
                console.log("Raw recent activity data:", data.recentActivity);
                $scope.recentActivity = data.recentActivity;

                // Group activities by username
                $scope.groupedActivity = {};
                angular.forEach($scope.recentActivity, function(log) {
                    if (!$scope.groupedActivity[log.username]) {
                        $scope.groupedActivity[log.username] = [];
                    }
                    $scope.groupedActivity[log.username].push(log);
                });
            } else {

                $scope.groupedActivity = {};
                angular.forEach($scope.recentActivity, function(log) {
                    if (!$scope.groupedActivity[log.username]) {
                        $scope.groupedActivity[log.username] = [];
                    }
                    $scope.groupedActivity[log.username].push(log);
                });
            }




            initCharts();
        });
    };

    // init charts
    function initCharts() {
        // User activity chart
        new Chart(document.getElementById('userActivityChart'), {
            type: 'line', data: $scope.userActivityData, options: {
                responsive: true, plugins: {
                    title: {
                        display: true, text: 'User Activity Over Time'
                    }
                }
            }
        });

        // Activity type chart
        new Chart(document.getElementById('activityTypeChart'), {
            type: 'pie', data: $scope.activityTypeData, options: {
                responsive: true, plugins: {
                    title: {
                        display: true, text: 'Activity by Type'
                    }
                }
            }
        });

    }

    $scope.loadDashboard();
});
