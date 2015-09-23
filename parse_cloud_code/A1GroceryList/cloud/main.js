// Use Parse.Cloud.define to define as many cloud functions as you want.

Parse.Cloud.define("initializeNewUser", function (request, response) {
    var InitialItemsObject = Parse.Object.extend("Initial_Items");
    var itemsQuery = new Parse.Query(InitialItemsObject);
    itemsQuery.ascending("itemName");
    itemsQuery.limit(500);

    itemsQuery.find().then(function (results) {
        var user = request.user;
        console.log('Found ' + results.length + ' Items for user: ' + user.get("name"));
        var ACL = new Parse.ACL(user);
        var unsavedItemObjects = [];
        for (var i = 0; i < results.length; i++) {
            var item = new Parse.Object("Item");
            item.set('author', user);
            var itemName = results[i].get('itemName');
            item.set('itemName', itemName);
            item.set('itemNameLowercase', itemName.toLowerCase());
            item.set('itemNote', '');
            item.set('group', results[i].get('group'));
            item.set("isChecked", false);
            item.set("isFavorite", false);
            item.set("isSelected", false);
            item.set("isStruckOut", false);
            item.set("isItemDirty", false);
            item.set('sortKey', i);
            item.set('barcodeNumber', '');
            item.set('barcodeFormat', '');
            item.setACL(ACL);
            unsavedItemObjects[i] = item;
        }

        console.log('Ready to save ' + unsavedItemObjects.length + ' Items for user: ' + user.get("name"));

        Parse.Object.saveAll(unsavedItemObjects, {
            success: function (list) {
                // All the objects were saved.
                console.log('Successfully initialized ' + list.length + ' Items for user: ' + user.get("name"));
                response.success(list.length);
            },
            error: function (error) {
                // An error occurred while saving one of the objects.
                console.log('An error occurred while saving one of the Item objects. Error code: ' + error.code + ' ' + error.message);
                response.error('An error occurred while saving one of the Item objects. Error code: ' + error.code + ' ' + error.message);
            }
        });

    }, function (error) {
        console.log('Failed to find initial Items. Error code: ' + error.code + ' ' + error.message);
        response.error("Failed to find initial Items. Error code: " + error.code + ": " + error.message);
    });

});


Parse.Cloud.define("initializeStoreMap", function (request, response) {
    var storeID = request.params.storeID;
    //console.log('Start initializeStoreMap for storeID = ' + storeID);

    var storesObjects = Parse.Object.extend("Store");
    var storeQuery = new Parse.Query(storesObjects);

    storeQuery.get(storeID, {
        success: function (store) {
            // The store was retrieved successfully.

            // get the default location
            var locationObjects = Parse.Object.extend("Location");
            var locationsQuery = new Parse.Query(locationObjects);
            locationsQuery.equalTo("isDefault", true);
            locationsQuery.find({
                success: function (locations) {
                    // console.log('Found ' + locations.length + ' default location.');
                    var defaultLocation = locations[0];

                    // get all groups
                    var queryGroups = new Parse.Query("Group");

                    queryGroups.find({
                        success: function (groups) {
                            //  console.log('Found ' + groups.length + ' Groups.');
                            var user = request.user;
                            var ACL = new Parse.ACL(user);
                            ACL.setPublicReadAccess(true);
                            ACL.setPublicWriteAccess(true);
                            var unsavedStoreMapObjects = [];
                            for (var i = 0; i < groups.length; ++i) {
                                var map = new Parse.Object("StoreMap");
                                map.set('author', user);
                                map.set('store', store);
                                map.set('group', groups[i]);
                                map.set('location', defaultLocation);
                                map.setACL(ACL);
                                unsavedStoreMapObjects[i] = map;
                            }
                            // console.log('Ready to save ' + unsavedStoreMapObjects.length + ' map objects for storeID = ' + storeID);
                            Parse.Object.saveAll(unsavedStoreMapObjects, {
                                success: function (list) {
                                    // All the objects were saved.
                                    // console.log('Successfully initialize ' + list.length + ' StoreMaps objects for storeID = ' + storeID);
                                    response.success('Successfully initialize ' + list.length + ' StoreMaps objects for storeID = ' + storeID);
                                },
                                error: function (error) {
                                    // An error occurred while saving one of the objects.
                                    console.log('An error occurred while saving one of the StoreMaps objects. Error code: ' + error.code + ' ' + error.message);
                                    response.error('An error occurred while saving one of the StoreMaps objects. Error code: ' + error.code + ' ' + error.message);
                                }
                            });
                        },
                        error: function (error) {
                            console.log('initializeStoreMap: Groups lookup failed. Error code: ' + error.code + ' ' + error.message);
                            response.error('initializeStoreMap: Groups lookup failed. Error code: ' + error.code + ' ' + error.message);
                        }
                    });


                },
                error: function (error) {
                    console.log('initializeStoreMap: Locations lookup failed. Error code: ' + error.code + ' ' + error.message);
                    response.error('initializeStoreMap: Locations lookup failed. Error code: ' + error.code + ' ' + error.message);
                }
            });

        },
        error: function (object, error) {
            // The store was not retrieved successfully.
            console.log('initializeStoreMap: Store lookup failed. Error code: ' + error.code + ' ' + error.message);
            response.error('initializeStoreMap: Store lookup failed. Error code: ' + error.code + ' ' + error.message);
        }
    });


});
Parse.Cloud.define("getAddress", function (request, response) {
    var latitude = request.params.latitude;
    var longitude = request.params.longitude;

    // https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&key=API_KEY
    // https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452&location_type=ROOFTOP&result_type=street_address&key=API_KEY
    //console.log('Seeking address for: ' + latitude + ', ' + longitude);
    Parse.Cloud.httpRequest({
        url: 'https://maps.googleapis.com/maps/api/geocode/json?latlng=' + latitude + ',' + longitude + '&location_type=ROOFTOP&result_type=street_address&key=AIzaSyAebBA9NuwNy3K_aUEdVl3XoFArrc4vDs0'
    }).then(function (httpResponse) {
        // success
        var jsonObject = JSON.parse(httpResponse.text);
        var geocodeStatus = jsonObject.status;
        //console.log('httpRequest success: geocodeStatus:' + geocodeStatus);
        if (geocodeStatus.substring(0, 2) == "OK") {
            var place_id = jsonObject.results[0].place_id;
            //console.log('place_id: ' + place_id);

            var street_number = extractFromAdress(jsonObject.results[0].address_components, "street_number");
            var route = extractFromAdress(jsonObject.results[0].address_components, "route");
            var address1 = street_number + ' ' + route;
            //console.log('address1: ' + address1);
            var city = extractFromAdress(jsonObject.results[0].address_components, "locality");
            var state = extractFromAdress(jsonObject.results[0].address_components, "administrative_area_level_1");
            var zip = extractFromAdress(jsonObject.results[0].address_components, "postal_code");
            var country = extractFromAdress(jsonObject.results[0].address_components, "country");

            //var formatted_address = jsonObject.results[0].formatted_address;
            //console.log('formatted_address: ' + formatted_address);

            var addressObj = {
                'place_id': place_id,
                'address1': address1,
                'city': city,
                'state': state,
                'zip': zip,
                'country': country
            };
            response.success(addressObj);

        } else {
            console.log('Failure: ' + geocodeStatus);
            response.error(geocodeStatus);
        }
    }, function (httpResponse) {
        // error
        console.log('Error: Request failed with response code ' + httpResponse.status);
        response.error('Error: Request failed with response code ' + httpResponse.status);
    });

});

function extractFromAdress(components, type) {
    for (var i = 0; i < components.length; i++)
        for (var j = 0; j < components[i].types.length; j++)
            if (components[i].types[j] == type) return components[i].short_name;
    return "";
}

Parse.Cloud.define("getLatitudeAndLongitude", function (request, response) {
    var address1 = request.params.address1;
    var address2 = request.params.address2;
    var city = request.params.city;
    var state = request.params.state;
    var zip = request.params.zip;
    var country = request.params.country;

    var address = address1 + ',';
    if (address2 != '') {
        address = address + address2 + ',';
    }
    address = address + city + ',' + state + ',' + zip + ',' + country;
    address = encodeURIComponent(address.trim());

    //console.log('START getGeoPoint');

    Parse.Cloud.httpRequest({
        url: 'https://maps.googleapis.com/maps/api/geocode/json?address=' + address + ':ES&key=AIzaSyAebBA9NuwNy3K_aUEdVl3XoFArrc4vDs0'
    }).then(function (httpResponse) {
        // success
        var jsonObject = JSON.parse(httpResponse.text);
        var geocodeStatus = jsonObject.status;
        //  console.log('httpRequest success: geocodeStatus:' + geocodeStatus);
        if (geocodeStatus.substring(0, 2) == "OK") {
            var latitude = jsonObject.results[0].geometry.location.lat;
            var longitude = jsonObject.results[0].geometry.location.lng;
            //    console.log('latitude = ' + latitude + '; longitude = ' + longitude);

            var point = new Parse.GeoPoint({latitude: latitude, longitude: longitude});
            if (point != null) {
                //console.log('success: geoPoint not null.');
                response.success(point);
            } else {
                console.log('error: geoPoint is null!');
                response.error('error: geoPoint is null!');
            }

        } else {
            console.log('Failure: geocodeStatus NOT OK');
            response.error('Failure: geocodeStatus NOT OK');
        }
    }, function (httpResponse) {
        // error
        console.log('Error: Request failed with response code ' + httpResponse.status);
        response.error('Error: Request failed with response code ' + httpResponse.status);
    });
});

//function getGeoPoint(address) {
//
//    console.log('Seeking geoPoint for: ' + address);
//    Parse.Cloud.httpRequest({
//        url: 'https://maps.googleapis.com/maps/api/geocode/json?address=' + address + ':ES&key=AIzaSyAebBA9NuwNy3K_aUEdVl3XoFArrc4vDs0'
//    }).then(function (httpResponse) {
//        // success
//        console.log('httpRequest success:');
//        var jsonObject = JSON.parse(httpResponse.text);
//        var geocodeStatus = jsonObject.status;
//        console.log('httpRequest success: geocodeStatus:' + geocodeStatus);
//        if (geocodeStatus.substring(0, 2) == "OK") {
//            var latitude = jsonObject.results[0].geometry.location.lat;
//            var longitude = jsonObject.results[0].geometry.location.lng;
//            console.log('latitude = ' + latitude + '; longitude = ' + longitude);
//
//            var point = new Parse.GeoPoint({latitude: latitude, longitude: longitude});
//            if (point != null) {
//                console.log('geoPoint: latitude = ' + point.getLatitude() + '; longitude = ' + point.getLongitude());
//            } else {
//                console.log('geoPoint is null.');
//            }
//            return point;
//
//        } else {
//            console.log('Failure: geocodeStatus NOT OK');
//            return null;
//        }
//    }, function (httpResponse) {
//        // error
//        console.log('Error: getGeoPoint httpRequest failed with response code ' + httpResponse.status);
//        return null
//    });
//}

Parse.Cloud.beforeSave("Store", function (request, response) {
    // console.log("Stores: beforeSave");
    var geoPoint = request.object.get("storeGeoPoint");
    if (geoPoint != null) {
        console.log('GeoPoint exists. No need to find it.');
        // the geoPoint exists ... no need to find it
        response.success();
    } else {
        // the geoPoint does not exists ... so go find it
        var address1 = request.object.get("address1");
        var address2 = request.object.get("address2");
        var city = request.object.get("city");
        var state = request.object.get("state");
        var zip = request.object.get("zip");
        var country = request.object.get("country");
        var address = address1 + ',';
        if (address2 != '') {
            address = address + address2 + ',';
        }
        address = address + city + ',' + state + ',' + zip + ',' + country;
        address = encodeURIComponent(address.trim());

        Parse.Cloud.httpRequest({
            url: 'https://maps.googleapis.com/maps/api/geocode/json?address=' + address + ':ES&key=AIzaSyAebBA9NuwNy3K_aUEdVl3XoFArrc4vDs0'
        }).then(function (httpResponse) {
            // success
            var jsonObject = JSON.parse(httpResponse.text);
            var geocodeStatus = jsonObject.status;
            //  console.log('httpRequest success: geocodeStatus:' + geocodeStatus);
            if (geocodeStatus.substring(0, 2) == "OK") {
                var latitude = jsonObject.results[0].geometry.location.lat;
                var longitude = jsonObject.results[0].geometry.location.lng;
                //    console.log('latitude = ' + latitude + '; longitude = ' + longitude);

                var point = new Parse.GeoPoint({latitude: latitude, longitude: longitude});
                //TODO: Determine if the store already exists in the Stores table
                // Find the closes store to the calculated point.
                // then if the distance is within a minimum distance it exists
                request.object.set("storeGeoPoint", point);
                response.success();

            } else {
                console.log('Failure: geocodeStatus NOT OK');
                response.error('Failure: geocodeStatus NOT OK');
            }
        }, function (httpResponse) {
            // error
            console.log('Error: Request failed with response code ' + httpResponse.status);
            response.error('Error: Request failed with response code ' + httpResponse.status);
        });
    }
});

Parse.Cloud.afterSave("Store", function (request) {
    var storeID = request.object.id;
    //console.log('Start Store: afterSave; storeID = ' + storeID);
    var store = request.object;

    var StoreMapsObject = Parse.Object.extend("StoreMap");
    var storeMapsQuery = new Parse.Query(StoreMapsObject);
    storeMapsQuery.equalTo("store", store);
    storeMapsQuery.limit(50);

    storeMapsQuery.find().then(function (results) {
        console.log('Found ' + results.length + ' map entries for store = ' + storeID);
        if (results.length == 0) {
            console.log('No map entries found for storeID = ' + storeID + '. initializeStoreMap');
            Parse.Cloud.run('initializeStoreMap', {storeID: storeID}, {
                success: function () {
                    console.log('Store:afterSave; initializeStoreMap Success.');
                },
                error: function (error) {
                    console.log('Store:afterSave; initializeStoreMap FAIL: ');
                }
            });

        } else {
            console.log('Store found in the StoreMaps table. Do NOT initializeStoreMap');
        }

    }, function (error) {
        console.log('ERROR storeMapsQuery. Error code: ' + error.code + ' ' + error.message);
    });
});

Parse.Cloud.define("itemsBatchDelete", function (request, response) {
    var itemIDs = request.params.itemIDs;
    var length = itemIDs.length;
    var itemID;
    console.log('Received ' + length + ' Item IDs for deletion.');

    var ItemObjects = Parse.Object.extend("Item");
    var itemQuery = new Parse.Query(ItemObjects);
    itemQuery.containedIn("objectId", itemIDs);
    itemQuery.limit(500);
    itemQuery.find({
        success: function (itemObjects) {
            var length = itemObjects.length
            console.log('Found ' + length + ' Item objects.');
            Parse.Object.destroyAll(itemObjects).then(function (success) {
                // All the objects were deleted
                response.success(length);
            }, function (error) {
                console.error("Oops! Something went wrong destroying items: " + error.message + " (" + error.code + ")");
                response.error('All Items may not have been destroyed!');
            });
        },

        error: function (error) {
            console.log('Error finding Items. ' + error.message);
            response.error('Items not found!');
        }
    });
});


Parse.Cloud.define("syncIsSelectedTrue", function (request, response) {
    var itemIDs = request.params.itemIDs;
    var length = itemIDs.length;
    var user = request.user;
    console.log('Received ' + length + ' Item IDs for selection for user: ' + user.get("name"));

    var ItemObjects = Parse.Object.extend("Item");
    var itemQuery = new Parse.Query(ItemObjects);
    itemQuery.containedIn("objectId", itemIDs);
    itemQuery.limit(500);
    itemQuery.find({
        success: function (itemObjects) {
            var length = itemObjects.length;
            console.log('Found ' + length + ' Item for user: ' + user.get("name"));
            var foundItem;
            var unsavedItemObjects = [];
            for (var i = 0; i < length; i++) {
                foundItem = itemObjects[i];
                foundItem.set("isSelected", true);
                unsavedItemObjects[i] = foundItem;
            }

            console.log('Ready to save ' + unsavedItemObjects.length + ' Items for user: ' + user.get("name"));

            Parse.Object.saveAll(unsavedItemObjects, {
                success: function (list) {
                    // All the objects were saved.
                    console.log('Successfully selected ' + list.length + ' Items for user: ' + user.get("name"));
                    response.success(list.length);
                },
                error: function (error) {
                    // An error occurred while saving one of the objects.
                    console.log('An error occurred while saving one of the Item objects. Error code: ' + error.code + ' ' + error.message);
                    response.error('An error occurred while saving one of the Item objects. Error code: ' + error.code + ' ' + error.message);
                }
            });

        },

        error: function (error) {
            console.log('Error finding Items. ' + error.code + ' ' + error.message);
            response.error('Items not found!');
        }
    });
});


Parse.Cloud.define("syncIsSelectedFalse", function (request, response) {
    var itemIDs = request.params.itemIDs;
    var length = itemIDs.length;
    var user = request.user;
    console.log('Received ' + length + ' Item IDs for deselection for user: ' + user.get("name"));

    var ItemObjects = Parse.Object.extend("Item");
    var itemQuery = new Parse.Query(ItemObjects);
    itemQuery.containedIn("objectId", itemIDs);
    itemQuery.limit(500);
    itemQuery.find({
        success: function (itemObjects) {
            var length = itemObjects.length;
            console.log('Found ' + length + ' Items for user: ' + user.get("name"));
            var foundItem;
            var unsavedItemObjects = [];
            for (var i = 0; i < length; i++) {
                foundItem = itemObjects[i];
                foundItem.set("isSelected", false);
                unsavedItemObjects[i] = foundItem;
            }

            console.log('Ready to save ' + unsavedItemObjects.length + ' Items for user: ' + user.get("name"));

            Parse.Object.saveAll(unsavedItemObjects, {
                success: function (list) {
                    // All the objects were saved.
                    console.log('Successfully deselected ' + list.length + ' Items for user: ' + user.get("name"));
                    response.success(list.length);
                },
                error: function (error) {
                    // An error occurred while saving one of the objects.
                    console.log('An error occurred while saving one of the Item objects. Error code: ' + error.code + ' ' + error.message);
                    response.error('An error occurred while saving one of the Item objects. Error code: ' + error.code + ' ' + error.message);
                }
            });

        },

        error: function (error) {
            console.log('Error finding Items. ' + error.code + ' ' + error.message);
            response.error('Items not found!');
        }
    });
});


Parse.Cloud.define("setAllItemsSelection", function (request, response) {
    var isSelected = request.params.isSelected;
    var user = request.user;
    console.log('Start setting all Items isSelected = ' + isSelected + ' for user: ' + user.get("name"));

    var ItemObjects = Parse.Object.extend("Item");
    var itemQuery = new Parse.Query(ItemObjects);
    itemQuery.equalTo("author", user);
    itemQuery.limit(500);
    itemQuery.find({
        success: function (itemObjects) {
            var length = itemObjects.length;
            console.log('Found ' + length + ' Items for user: ' + user.get("name"));
            var foundItem;
            var unsavedItemObjects = [];
            for (var i = 0; i < length; i++) {
                foundItem = itemObjects[i];
                foundItem.set("isSelected", isSelected);
                unsavedItemObjects[i] = foundItem;
            }

            console.log('Ready to save ' + unsavedItemObjects.length + ' Items for user: ' + user.get("name"));

            Parse.Object.saveAll(unsavedItemObjects, {
                success: function (list) {
                    // All the objects were saved.
                    console.log('Successfully selected ' + list.length + ' Items for user: ' + user.get("name"));
                    response.success(list.length);
                },
                error: function (error) {
                    // An error occurred while saving one of the objects.
                    console.log('An error occurred while saving one of the Item objects. Error code: ' + error.code + ' ' + error.message);
                    response.error('An error occurred while saving one of the Item objects. Error code: ' + error.code + ' ' + error.message);
                }
            });

        },

        error: function (error) {
            console.log('Error finding Items. ' + error.code + ' ' + error.message);
            response.error('Items not found!');
        }
    });
});

Parse.Cloud.define("deselectStruckOutItems", function (request, response) {
    var itemIDs = request.params.itemIDs;
    var length = itemIDs.length;
    var itemID;
    var user = request.user;
    console.log('Received ' + length + ' Item IDs for deselect StruckOut Items for user: ' + user.get("name"));

    var ItemObjects = Parse.Object.extend("Item");
    var itemQuery = new Parse.Query(ItemObjects);
    itemQuery.containedIn("objectId", itemIDs);
    itemQuery.limit(500);
    itemQuery.find({
        success: function (itemObjects) {
            var length = itemObjects.length;
            console.log('Found ' + length + ' Items for user: ' + user.get("name"));
            var foundItem;
            var unsavedItemObjects = [];
            for (var i = 0; i < length; i++) {
                foundItem = itemObjects[i];
                foundItem.set("isSelected", false);
                foundItem.set("isStruckOut", false);
                unsavedItemObjects[i] = foundItem;
            }

            console.log('Ready to save ' + unsavedItemObjects.length + ' Items for user: ' + user.get("name"));

            Parse.Object.saveAll(unsavedItemObjects, {
                success: function (list) {
                    // All the objects were saved.
                    console.log('Successfully deselected ' + list.length + ' StruckOut Items for user: ' + user.get("name"));
                    response.success(list.length);
                },
                error: function (error) {
                    // An error occurred while saving one of the objects.
                    console.log('An error occurred while saving one of the Item objects. Error code: ' + error.code + ' ' + error.message);
                    response.error('An error occurred while saving one of the Item objects. Error code: ' + error.code + ' ' + error.message);
                }
            });

        },

        error: function (error) {
            console.log('Error finding Items. ' + error.code + ' ' + error.message);
            response.error('Items not found!');
        }
    });
});

Parse.Cloud.define("syncIsStruckOutTrue", function (request, response) {
    var itemIDs = request.params.itemIDs;
    var length = itemIDs.length;
    var user = request.user;
    console.log('Received ' + length + ' Item IDs for for user: ' + user.get("name"));

    var ItemObjects = Parse.Object.extend("Item");
    var itemQuery = new Parse.Query(ItemObjects);
    itemQuery.containedIn("objectId", itemIDs);
    itemQuery.limit(500);
    itemQuery.find({
        success: function (itemObjects) {
            var length = itemObjects.length;
            console.log('Found ' + length + ' Item for user: ' + user.get("name"));
            var foundItem;
            var unsavedItemObjects = [];
            for (var i = 0; i < length; i++) {
                foundItem = itemObjects[i];
                foundItem.set("isStruckOut", true);
                unsavedItemObjects[i] = foundItem;
            }

            console.log('Ready to save ' + unsavedItemObjects.length + ' Items for user: ' + user.get("name"));

            Parse.Object.saveAll(unsavedItemObjects, {
                success: function (list) {
                    // All the objects were saved.
                    console.log('Successfully struck out ' + list.length + ' Items for user: ' + user.get("name"));
                    response.success(list.length);
                },
                error: function (error) {
                    // An error occurred while saving one of the objects.
                    console.log('An error occurred while saving one of the Item objects. Error code: ' + error.code + ' ' + error.message);
                    response.error('An error occurred while saving one of the Item objects. Error code: ' + error.code + ' ' + error.message);
                }
            });

        },

        error: function (error) {
            console.log('Error finding Items. ' + error.code + ' ' + error.message);
            response.error('Items not found!');
        }
    });
});

Parse.Cloud.define("syncIsStruckOutFalse", function (request, response) {
    var itemIDs = request.params.itemIDs;
    var length = itemIDs.length;
    var user = request.user;
    console.log('Received ' + length + ' Item IDs for for user: ' + user.get("name"));

    var ItemObjects = Parse.Object.extend("Item");
    var itemQuery = new Parse.Query(ItemObjects);
    itemQuery.containedIn("objectId", itemIDs);
    itemQuery.limit(500);
    itemQuery.find({
        success: function (itemObjects) {
            var length = itemObjects.length;
            console.log('Found ' + length + ' Item for user: ' + user.get("name"));
            var foundItem;
            var unsavedItemObjects = [];
            for (var i = 0; i < length; i++) {
                foundItem = itemObjects[i];
                foundItem.set("isStruckOut", false);
                unsavedItemObjects[i] = foundItem;
            }

            console.log('Ready to save ' + unsavedItemObjects.length + ' Items for user: ' + user.get("name"));

            Parse.Object.saveAll(unsavedItemObjects, {
                success: function (list) {
                    // All the objects were saved.
                    console.log('Successfully removed strikeout from ' + list.length + ' Items for user: ' + user.get("name"));
                    response.success(list.length);
                },
                error: function (error) {
                    // An error occurred while saving one of the objects.
                    console.log('An error occurred while saving one of the Item objects. Error code: ' + error.code + ' ' + error.message);
                    response.error('An error occurred while saving one of the Item objects. Error code: ' + error.code + ' ' + error.message);
                }
            });

        },

        error: function (error) {
            console.log('Error finding Items. ' + error.code + ' ' + error.message);
            response.error('Items not found!');
        }
    });
});

Parse.Cloud.define("syncStoreMapEntry", function (request, response) {
    var storeMapEntryID = request.params.storeMapEntryID;
    var locationID = request.params.locationID;
    var user = request.user;
    console.log('Received locationID: ' + locationID + ' to sync with storeMapEntryID: ' + storeMapEntryID + ': user: ' + user.get("name"));

    var location;

    // get location
    var LocationObjects = Parse.Object.extend("Location");
    var locationsQuery = new Parse.Query(LocationObjects);
    locationsQuery.equalTo("objectId", locationID);
    locationsQuery.first()
        .then(function (locationResult) {
            location = locationResult;
            console.log('Found Location: ' + location.get("locationName"));

            var StoreMapEntryObjects = Parse.Object.extend("StoreMap");
            var storeMapQuery = new Parse.Query(StoreMapEntryObjects);
            storeMapQuery.equalTo("objectId", storeMapEntryID);
            storeMapQuery.first()
                .then(function (storeMapEntryResult) {
                    storeMapEntryResult.set("location", location);
                    storeMapEntryResult.set("author", user);
                    return storeMapEntryResult.save()
                        .then(function (saveResult) {
                            // store map entry saved
                            var locationName = location.get("locationName");
                            response.success("\"" + locationName + "\" saved.");
                        })

                })
        }, function (error) {
            var msg = error.code + ' ' + error.message;
            response.error("The location was not Saved. " + msg);
        });

});