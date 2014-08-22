var app = angular.module('commafeed', ['ngRoute', 'ngTouch', 'ngAnimate', 'ui.utils', 'ui.bootstrap', 'ui.router', 'ui.select2',
		'commafeed.directives', 'commafeed.controllers', 'commafeed.services', 'commafeed.filters', 'commafeed.i18n', 'ngSanitize',
		'infinite-scroll', 'ngGrid', 'chieffancypants.loadingBar', 'pascalprecht.translate']);

app.config([
		'$routeProvider',
		'$stateProvider',
		'$urlRouterProvider',
		'$httpProvider',
		'$compileProvider',
		'cfpLoadingBarProvider',
		'$translateProvider',
		function($routeProvider, $stateProvider, $urlRouterProvider, $httpProvider, $compileProvider, cfpLoadingBarProvider,
				$translateProvider) {
			
			// ie ajax cache workaround
			if (!$httpProvider.defaults.headers.get) {
				$httpProvider.defaults.headers.get = {};
			}
			$httpProvider.defaults.headers.get['If-Modified-Since'] = '0';

			$translateProvider.useStaticFilesLoader({
				prefix : 'i18n/',
				suffix : '.js'
			});
			$translateProvider.preferredLanguage('en');

			cfpLoadingBarProvider.includeSpinner = false;

			$compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|ftp|mailto|javascript):/);
			var interceptor = ['$rootScope', '$q', '$injector', function(scope, $q, $injector) {

				var success = function(response) {
					return response;
				};
				var error = function(response) {
					var status = response.status;
					if (status == 401) {
						$injector.get('$state').transitionTo('welcome');
					}
					return $q.reject(response);
				};

				var promise = function(promise) {
					return promise.then(success, error);
				};

				return promise;
			}];

			$httpProvider.responseInterceptors.push(interceptor);

			$stateProvider.state('feeds', {
				'abstract' : true,
				url : '/feeds',
				templateUrl : 'templates/feeds.html'
			});
			$stateProvider.state('feeds.view', {
				url : '/view/:_type/:_id',
				templateUrl : 'templates/feeds.view.html',
				controller : 'FeedListCtrl'
			});
			$stateProvider.state('feeds.subscribe', {
				url : '/subscribe',
				templateUrl : 'templates/feeds.subscribe.html',
				controller : 'SubscribeCtrl'
			});
			$stateProvider.state('feeds.new_category', {
				url : '/add_category',
				templateUrl : 'templates/feeds.new_category.html',
				controller : 'NewCategoryCtrl'
			});
			$stateProvider.state('feeds.import', {
				url : '/import',
				templateUrl : 'templates/feeds.import.html',
				controller : 'ImportCtrl'
			});
			$stateProvider.state('feeds.search', {
				url : '/search/:_keywords',
				templateUrl : 'templates/feeds.view.html',
				controller : 'FeedListCtrl'
			});
			$stateProvider.state('feeds.feed_details', {
				url : '/details/feed/:_id',
				templateUrl : 'templates/feeds.feed_details.html',
				controller : 'FeedDetailsCtrl'
			});
			$stateProvider.state('feeds.category_details', {
				url : '/details/category/:_id',
				templateUrl : 'templates/feeds.category_details.html',
				controller : 'CategoryDetailsCtrl'
			});
			$stateProvider.state('feeds.tag_details', {
				url : '/details/tag/:_id',
				templateUrl : 'templates/feeds.tag_details.html',
				controller : 'TagDetailsCtrl'
			});
			$stateProvider.state('feeds.help', {
				url : '/help',
				templateUrl : 'templates/feeds.help.html',
				controller : 'HelpController'
			});
			$stateProvider.state('feeds.settings', {
				url : '/settings',
				templateUrl : 'templates/settings.html',
				controller : 'SettingsCtrl'
			});
			$stateProvider.state('feeds.profile', {
				url : '/profile',
				templateUrl : 'templates/profile.html',
				controller : 'ProfileCtrl'
			});

			$stateProvider.state('admin', {
				'abstract' : true,
				url : '/admin',
				templateUrl : 'templates/admin.html'
			});
			$stateProvider.state('admin.userlist', {
				url : '/user/list',
				templateUrl : 'templates/admin.userlist.html',
				controller : 'ManageUsersCtrl'
			});
			$stateProvider.state('admin.useradd', {
				url : '/user/add',
				templateUrl : 'templates/admin.useradd.html',
				controller : 'ManageUserCtrl'
			});
			$stateProvider.state('admin.useredit', {
				url : '/user/edit/:_id',
				templateUrl : 'templates/admin.useredit.html',
				controller : 'ManageUserCtrl'
			});
			$stateProvider.state('admin.settings', {
				url : '/settings',
				templateUrl : 'templates/admin.settings.html',
				controller : 'ManageSettingsCtrl'
			});
			$stateProvider.state('admin.metrics', {
				url : '/metrics',
				templateUrl : 'templates/admin.metrics.html',
				controller : 'MetricsCtrl'
			});

			$stateProvider.state('welcome', {
				url : '/welcome',
				templateUrl : 'templates/welcome.html'
			});

			$urlRouterProvider.when('/', '/feeds/view/category/all');
			$urlRouterProvider.when('/admin', '/admin/settings');
			$urlRouterProvider.otherwise('/');

		}]);