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
            // Process user activity data
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
                console.log("No recent activity data found in response");
                //test
                // $scope.recentActivity = [
                //     {
                //         date: new Date().getTime(),
                //         username: "test_user1",
                //         type: "CREATE",
                //         entityClass: "Document",
                //         entityId: "test_id1",
                //         message: "Test Document 1"
                //     },
                //     {
                //         date: new Date().getTime() - 3600000, // 1 hour ago
                //         username: "test_user1",
                //         type: "UPDATE",
                //         entityClass: "Document",
                //         entityId: "test_id2",
                //         message: "Test Document 2"
                //     },
                //     {
                //         date: new Date().getTime() - 7200000, // 2 hours ago
                //         username: "test_user2",
                //         type: "DELETE",
                //         entityClass: "File",
                //         entityId: "test_id3",
                //         message: "Test File"
                //     }
                // ];

                // Group test activities
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

    // Load dashboard on init
    $scope.loadDashboard();
});
