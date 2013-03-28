var module = angular.module('commafeed.controllers', []);

module.run(function($rootScope) {
	$rootScope.$on('emitMark', function(event, args) {
		// args.entry - the entry
		$rootScope.$broadcast('mark', args);
	});
});

module.controller('CategoryTreeCtrl', function($scope, $routeParams, $location,
		$route, SubscriptionService) {

	$scope.$on('$routeChangeSuccess', function() {
		$scope.selectedType = $routeParams._type;
		$scope.selectedId = $routeParams._id;
	});

	$scope.SubscriptionService = SubscriptionService;

	var unreadCount = function(category) {
		var count = 0;
		if (category.children) {
			for ( var i = 0; i < category.children.length; i++) {
				count = count + unreadCount(category.children[i]);
			}
		}
		if (category.feeds) {
			for ( var i = 0; i < category.feeds.length; i++) {
				var feed = category.feeds[i];
				count = count + feed.unread;
			}
		}
		return count;
	}

	$scope.formatCategoryName = function(category) {
		var count = unreadCount(category);
		var label = category.name;
		if (count > 0) {
			label = label + " (" + count + ")";
		}
		return label;
	}

	$scope.formatFeedName = function(feed) {
		var label = feed.name;
		if (feed.unread > 0) {
			label = label + " (" + feed.unread + ")";
		}
		return label;
	}

	$scope.feedClicked = function(id) {
		if ($scope.selectedType == 'feed' && id == $scope.selectedId) {
			$route.reload();
		} else {
			$location.path('/feeds/view/feed/' + id);
		}
	};

	$scope.categoryClicked = function(id) {
		if ($scope.selectedType == 'category' && id == $scope.selectedId) {
			$route.reload();
		} else {
			$location.path('/feeds/view/category/' + id);
		}
	};

	var mark = function(node, entry) {
		if (node.children) {
			for ( var i = 0; i < node.children.length; i++) {
				mark(node.children[i], entry);
			}
		}
		if (node.feeds) {
			for ( var i = 0; i < node.feeds.length; i++) {
				var feed = node.feeds[i];
				if (feed.id == entry.feedId) {
					var c = entry.read ? -1 : 1;
					feed.unread = feed.unread + c;
				}
			}
		}
	};

	$scope.$on('mark', function(event, args) {
		mark($scope.SubscriptionService.subscriptions, args.entry)
	});
});

module.controller('FeedListCtrl', function($scope, $routeParams, $http, $route,
		$window, EntryService, SettingsService) {

	$scope.selectedType = $routeParams._type;
	$scope.selectedId = $routeParams._id;

	$scope.name = null;
	$scope.entries = [];

	$scope.settingsService = SettingsService;
	$scope.$watch('settingsService.settings.readingMode', function(newValue,
			oldValue) {
		if (newValue && oldValue && newValue != oldValue) {
			$route.reload();
		}
	});

	$scope.limit = 10;
	$scope.busy = false;
	$scope.hasMore = true;

	$scope.loadMoreEntries = function() {
		if (!$scope.hasMore)
			return;
		if ($scope.busy)
			return;
		$scope.busy = true;

		var limit = $scope.limit;
		if ($scope.entries.length == 0) {
			$window = angular.element($window);
			limit = $window.height() / 33;
			limit = parseInt(limit) + 5;
		}
		EntryService.get({
			type : $scope.selectedType,
			id : $scope.selectedId,
			readType : $scope.settingsService.settings.readingMode,
			offset : $scope.entries.length,
			limit : limit
		}, function(data) {
			for ( var i = 0; i < data.entries.length; i++) {
				$scope.entries.push(data.entries[i]);
			}
			$scope.name = data.name;
			$scope.busy = false;
			$scope.hasMore = data.entries.length == limit
		});
	}

	$scope.mark = function(entry, read) {
		if (entry.read != read) {
			entry.read = read;
			$scope.$emit('emitMark', {
				entry : entry
			});
			EntryService.mark({
				type : 'entry',
				id : entry.id,
				read : read
			});
		}
	};

	$scope.isOpen = false;
	$scope.entryClicked = function(entry, event) {
		if (!entry.read) {
			$scope.mark(entry, true);
		}
		if (!event || (!event.ctrlKey && event.which != 2)) {
			if (event) {
				event.preventDefault();
				event.stopPropagation();
			}
			console.log($scope.current)
			console.log(entry)
			if ($scope.current != entry) {
				$scope.isOpen = true;
			} else {
				$scope.isOpen = !$scope.isOpen;
			}
			$scope.current = entry;
		}
	}

	var openNextEntry = function() {
		var entry = null;
		if ($scope.current) {
			var index;
			for ( var i = 0; i < $scope.entries.length; i++) {
				if ($scope.current == $scope.entries[i]) {
					index = i;
					break;
				}
			}
			index = index + 1;
			if (index < $scope.entries.length) {
				entry = $scope.entries[index];
			}
		} else if ($scope.entries.length > 0) {
			entry = $scope.entries[0];
		}
		if (entry) {
			$scope.entryClicked(entry);
		}
	};

	var openPreviousEntry = function() {
		var entry = null;
		if ($scope.current) {
			var index;
			for ( var i = 0; i < $scope.entries.length; i++) {
				if ($scope.current == $scope.entries[i]) {
					index = i;
					break;
				}
			}
			index = index - 1;
			if (index >= 0) {
				entry = $scope.entries[index];
			}
		}
		if (entry) {
			$scope.entryClicked(entry);
		}
	};

	Mousetrap.bind('space', openNextEntry);
	Mousetrap.bind('j', openNextEntry);
	Mousetrap.bind('k', openPreviousEntry);
});