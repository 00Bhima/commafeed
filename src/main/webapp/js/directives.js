var module = angular.module('commafeed.directives', []);

module.directive('subscribe', function(SubscriptionService) {
	return {
		scope : {},
		restrict : 'E',
		replace : true,
		templateUrl : 'directives/subscribe.html',
		controller : function($scope, SubscriptionService) {
			$scope.opts = {
				backdropFade : true,
				dialogFade : true
			};

			$scope.isOpen = false;
			$scope.isOpenImport = false;
			$scope.sub = {};

			$scope.SubscriptionService = SubscriptionService;

			$scope.open = function() {
				$scope.sub = {};
				$scope.isOpen = true;
			};

			$scope.close = function() {
				$scope.isOpen = false;
			};

			$scope.save = function() {
				SubscriptionService.subscribe($scope.sub);
				$scope.close();
			};

			$scope.openImport = function() {
				$scope.isOpenImport = true;
			};

			$scope.closeImport = function() {
				$scope.isOpenImport = false;
			};

			$scope.uploadComplete = function(contents, completed) {
				SubscriptionService.init();
				$scope.closeImport();
			};
		}
	};
});

module.directive('category', function($compile) {
	return {
		scope : {
			node : '=',
			selectedType : '=',
			selectedId : '=',
			feedClick : '&',
			categoryClick : '&',
			formatCategoryName : '&',
			formatFeedName : '&'
		},
		restrict : 'E',
		replace : true,
		templateUrl : 'directives/category.html',
		link : function(scope, element) {
			var ul = element.find('ul');
            ul.prepend('<category ng-repeat="child in node.children" node="child" feed-click="feedClick({id:id})" \
            		category-click="categoryClick({id:id})" selected-type="selectedType" selected-id="selectedId" \
            		format-category-name="formatCategoryName({category:category})" format-feed-name="formatFeedName({feed:feed})">\
            		</category>');
			$compile(ul.contents())(scope);
		},
		controller : function($scope, $dialog, SubscriptionService) {
			$scope.unsubscribe = function(subscription) {
				var title = 'Unsubscribe';
				var msg = 'Unsubscribe from ' + subscription.name + ' ?';
				var btns = [ {
					result : 'cancel',
					label : 'Cancel'
				}, {
					result : 'ok',
					label : 'OK',
					cssClass : 'btn-primary'
				} ];

				$dialog.messageBox(title, msg, btns).open().then(
						function(result) {
							if (result == 'ok') {
								SubscriptionService
										.unsubscribe(subscription.id);
							}
						});
			};

			$scope.toggleCategory = function(category) {
				console.log(category.expanded)
				SubscriptionService.collapse({
					id : category.id,
					collapse : !category.expanded
				});
			};
		}
	};
});

module.directive('toolbar', function($routeParams, $route, SettingsService,
		EntryService, SubscriptionService) {
	return {
		scope : {},
		restrict : 'E',
		replace : true,
		templateUrl : 'directives/toolbar.html',
		controller : function($scope, $route, $http, SettingsService) {
			
			function totalActiveAjaxRequests() {
				return ($http.pendingRequests.length + $.active);
			}
			
			$scope.loading = true;
			$scope.$watch(totalActiveAjaxRequests, function () {
				$scope.loading = !(totalActiveAjaxRequests() === 0);
	        });
			
			$scope.settings = SettingsService.settings;
			$scope.refresh = function() {
				$route.reload();
			};
			$scope.markAllAsRead = function() {
				EntryService.mark({
					type : $routeParams._type,
					id : $routeParams._id,
					read : true,
				}, function() {
					SubscriptionService.init(function() {
						$route.reload();
					});
				});
			}
		},
		link : function($scope, element) {
			element.find('.read-mode button').bind('click', function() {
				SettingsService.save();
			});
		}
	};
});

module.directive('spinner', function() {
	return {
		scope : {
			shown : '=',
		},
		restrict : 'A',
		link : function($scope, element) {
			element.addClass('spinner');
			var opts = {
				lines : 11, // The number of lines to draw
				length : 5, // The length of each line
				width : 3, // The line thickness
				radius : 8, // The radius of the inner circle
				corners : 1, // Corner roundness (0..1)
				rotate : 0, // The rotation offset
				color : '#000', // #rgb or #rrggbb
				speed : 1.3, // Rounds per second
				trail : 60, // Afterglow percentage
				shadow : false, // Whether to render a shadow
				hwaccel : true, // Whether to use hardware acceleration
				zIndex : 2e9, // The z-index (defaults to 2000000000)
				top : 50, // Top position relative to parent in px
				left : 'auto' // Left position relative to parent in px
			};
			var spinner = new Spinner(opts);
			$scope.$watch('shown', function(shown) {
				if (shown) {
					spinner.spin();
					element.append(spinner.el);
				} else {
					spinner.stop();
				}
			});
		}
	}
});