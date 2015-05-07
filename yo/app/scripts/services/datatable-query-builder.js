'use strict';

/**
 * @ngdoc service
 * @name angularApp.DataTableQueryBuilder
 * @description
 * # DataTableQueryBuilder
 * Factory in the angularApp.
 */
angular.module('angularApp')
    .factory('DataTableQueryBuilder', function() {
        // Service logic

        var ICATDATAPROXYURL = 'https://localhost:3001';
        var RESTAPIURL = ICATDATAPROXYURL + '/icat/entityManager';

        function forEachSorted(obj, iterator, context) {
            var keys = sortedKeys(obj);
            for (var i = 0; i < keys.length; i++) {
                iterator.call(context, obj[keys[i]], keys[i]);
            }
            return keys;
        }

        function sortedKeys(obj) {
            var keys = [];
            for (var key in obj) {
                if (obj.hasOwnProperty(key)) {
                    keys.push(key);
                }
            }
            return keys.sort();
        }

        /**
         * squel expects false for DESC order
         * @param  {[type]} order [description]
         * @return {[type]}       [description]
         */
        function sortOrder(order) {
            if (order.toUpperCase === 'DESC') {
                return false;
            }

            return true;
        }

        function validateRequiredArguments(mySessionId, facility, queryParams, absUrl) {
            //session argument is required and must be a string
            if (!mySessionId && ! angular.isString(mySessionId)) {
                throw new Error('Invalid arguments. Session string is expected');
            }

            //facility argument is required and must be an object
            if (! facility && ! angular.isObject(facility)) {
                throw new Error('Invalid arguments. facility object is expected');
            }

            //facility key facilityId and icatUrl are required
            if (! angular.isDefined(facility.facilityId) || ! angular.isDefined(facility.icatUrl)) {
                throw new Error('Invalid arguments. facility object must have the keys facilityId and icatUrl');
            }

            //queryParams is optional
            if (angular.isDefined(queryParams)) {
                //queryParams must be an object
                if (! angular.isObject(queryParams)) {
                    throw new Error('Invalid arguments. queryParams must be an object');
                }
            }

            //url is optional
            if (angular.isDefined(absUrl)) {
                //url must be an object
                if (typeof absUrl !== 'boolean') {
                    throw new Error('Invalid arguments. url must be a string');
                }
            }
        }


        // Public API here
        return {
            buildUrl: function(url, params) {
                if (!params) {
                    return url;
                }
                var parts = [];
                forEachSorted(params, function(value, key) {
                    if (value === null || value === undefined) {
                        return;
                    }
                    if (angular.isObject(value)) {
                        value = angular.toJson(value);
                    }
                    parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(value));
                });
                return url + ((url.indexOf('?') === -1) ? '?' : '&') + parts.join('&');
            },

            getCycles: function() {

            },

            getCyclesByInstrumentId: function() {

            },


            getInvestigations: function(mySessionId, facility, queryParams, absUrl) {
                validateRequiredArguments(mySessionId, facility, queryParams, absUrl);

                var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('COUNT(inv)')
                    .from('Investigation', 'inv')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                    );

                var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('inv')
                    .from('Investigation', 'inv')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                    );

                if (angular.isDefined(queryParams)) {
                    if (angular.isDefined(queryParams.search) && queryParams.search.trim() !== '') {
                        query.where(
                            squel.expr()
                            .or('UPPER(inv.name) LIKE ?', '%' + queryParams.search.toUpperCase() + '%')
                        );
                    }

                    //set limit
                    if (angular.isDefined(queryParams.start) && angular.isDefined(queryParams.numRows)) {
                        query.limit(queryParams.start, queryParams.numRows);
                    }

                    //set sort
                    if (angular.isDefined(queryParams.sortField) && angular.isDefined(queryParams.order)) {
                        query.order(queryParams.sortField, sortOrder(queryParams.order));
                    }
                }

                var params = {
                    sessionId: mySessionId,
                    query: query.toString(),
                    countQuery: countQuery.toString(),
                    entity: 'Investigation',
                    server: facility.icatUrl
                };

                if (absUrl === true) {
                    return this.buildUrl(RESTAPIURL, params);
                } else {
                    return this.buildUrl('', params);
                }
            },




            /** get instruments **/
            getInstruments: function(mySessionId, facility, queryParams, absUrl) {
                /*console.log('getInstruments session: ', mySessionId);
                console.log('getInstruments queryParams: ', queryParams);
                console.log('getInstruments facility: ', facility);*/

                validateRequiredArguments(mySessionId, facility, queryParams, absUrl);

                var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('COUNT(ins)')
                    .from('Instrument', 'ins')
                    .from('ins.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                    );

                var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('ins')
                    .from('Instrument', 'ins')
                    .from('ins.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                    );

                if (angular.isDefined(queryParams)) {
                    if (angular.isDefined(queryParams.search) && queryParams.search.trim() !== '') {
                        query.where(
                            squel.expr()
                            .or('UPPER(ins.name) LIKE ?', '%' + queryParams.search.toUpperCase() + '%')
                            //.or('UPPER(ins.fullName) LIKE ?', '%' + queryParams.search.toUpperCase() + '%')
                        );
                    }

                    //set limit
                    if (angular.isDefined(queryParams.start) && angular.isDefined(queryParams.numRows)) {
                        query.limit(queryParams.start, queryParams.numRows);
                    }

                    //set sort
                    if (angular.isDefined(queryParams.sortField) && angular.isDefined(queryParams.order)) {
                        query.order(queryParams.sortField, sortOrder(queryParams.order));
                    }
                }

                var params = {
                    sessionId: mySessionId,
                    query: query.toString(),
                    countQuery: countQuery.toString(),
                    entity: 'Instrument',
                    server: facility.icatUrl
                };

                if (absUrl === true) {
                    return this.buildUrl(RESTAPIURL, params);
                } else {
                    return this.buildUrl('', params);
                }
            },

            getInvestigationsByCycleId: function(){

            },


            getInvestigationsByInstrumentId: function(mySessionId, facility, queryParams, absUrl) {
                validateRequiredArguments(mySessionId, facility, queryParams, absUrl);

                var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('COUNT(inv)')
                    .from('Investigation', 'inv')
                    .from('inv.investigationInstruments', 'invins')
                    .from('invins.instrument', 'ins')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                            .and('ins.id = ?', queryParams.instrumentId)
                    );

                var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('inv')
                    .from('Investigation', 'inv')
                    .from('inv.investigationInstruments', 'invins')
                    .from('invins.instrument', 'ins')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                            .and('ins.id = ?', queryParams.instrumentId)
                    );

                if (angular.isDefined(queryParams)) {
                    if (angular.isDefined(queryParams.search) && queryParams.search.trim() !== '') {
                        query.where(
                            squel.expr()
                            .or('UPPER(inv.name) LIKE ?', '%' + queryParams.search.toUpperCase() + '%')
                            //.or('UPPER(inv.title) LIKE ?', '%' + queryParams.search.toUpperCase() + '%')
                            //.or('UPPER(inv.visitId) LIKE ?', '%' + queryParams.search.toUpperCase() + '%')
                        );
                    }

                    //set limit
                    if (angular.isDefined(queryParams.start) && angular.isDefined(queryParams.numRows)) {
                        query.limit(queryParams.start, queryParams.numRows);
                    }

                    //set sort
                    if (angular.isDefined(queryParams.sortField) && angular.isDefined(queryParams.order)) {
                        query.order(queryParams.sortField, sortOrder(queryParams.order));
                    }
                }

                var params = {
                    sessionId: mySessionId,
                    query: query.toString(),
                    countQuery: countQuery.toString(),
                    entity: 'Investigation',
                    server: facility.icatUrl
                };

                if (absUrl === true) {
                    return this.buildUrl(RESTAPIURL, params);
                } else {
                    return this.buildUrl('', params);
                }
            },

            getInvestigationsByInstrumentIdByCycleId: function() {

            },

            getDatasets: function(mySessionId, facility, queryParams, absUrl) {
                validateRequiredArguments(mySessionId, facility, queryParams, absUrl);

                var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('COUNT(ds)')
                    .from('Dataset', 'ds')
                    .from('ds.investigation', 'inv')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                    );

                var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('ds')
                    .from('Dataset', 'ds')
                    .from('ds.investigation', 'inv')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                    );

                if (angular.isDefined(queryParams)) {
                    if (angular.isDefined(queryParams.search) && queryParams.search.trim() !== '') {
                        query.where(
                            squel.expr()
                            .or('UPPER(ds.name) LIKE ?', '%' + queryParams.search.toUpperCase() + '%')
                        );
                    }

                    //set limit
                    if (angular.isDefined(queryParams.start) && angular.isDefined(queryParams.numRows)) {
                        query.limit(queryParams.start, queryParams.numRows);
                    }

                    //set sort
                    if (angular.isDefined(queryParams.sortField) && angular.isDefined(queryParams.order)) {
                        query.order(queryParams.sortField, sortOrder(queryParams.order));
                    }
                }

                var params = {
                    sessionId: mySessionId,
                    query: query.toString(),
                    countQuery: countQuery.toString(),
                    entity: 'Dataset',
                    server: facility.icatUrl
                };

                if (absUrl === true) {
                    return this.buildUrl(RESTAPIURL, params);
                } else {
                    return this.buildUrl('', params);
                }
            },

            getDatasetsByInstrumentId: function(mySessionId, facility, queryParams, absUrl) {
                validateRequiredArguments(mySessionId, facility, queryParams, absUrl);

                var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('COUNT(ds)')
                    .from('Dataset', 'ds')
                    .from('ds.investigation', 'inv')
                    .from('inv.investigationInstruments', 'invins')
                    .from('invins.instrument', 'ins')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                            .and('ins.id = ?', queryParams.instrumentId)
                    );

                var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('ds')
                    .from('Dataset', 'ds')
                    .from('ds.investigation', 'inv')
                    .from('inv.investigationInstruments', 'invins')
                    .from('invins.instrument', 'ins')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                            .and('ins.id = ?', queryParams.instrumentId)
                    );

                if (angular.isDefined(queryParams)) {
                    if (angular.isDefined(queryParams.search) && queryParams.search.trim() !== '') {
                        query.where(
                            squel.expr()
                            .or('UPPER(ds.name) LIKE ?', '%' + queryParams.search.toUpperCase() + '%')
                        );
                    }

                    //set limit
                    if (angular.isDefined(queryParams.start) && angular.isDefined(queryParams.numRows)) {
                        query.limit(queryParams.start, queryParams.numRows);
                    }

                    //set sort
                    if (angular.isDefined(queryParams.sortField) && angular.isDefined(queryParams.order)) {
                        query.order(queryParams.sortField, sortOrder(queryParams.order));
                    }
                }

                var params = {
                    sessionId: mySessionId,
                    query: query.toString(),
                    countQuery: countQuery.toString(),
                    entity: 'Dataset',
                    server: facility.icatUrl
                };

                if (absUrl === true) {
                    return this.buildUrl(RESTAPIURL, params);
                } else {
                    return this.buildUrl('', params);
                }
            },

            getDatasetsByInvestigationId: function(mySessionId, facility, queryParams, absUrl) {
                validateRequiredArguments(mySessionId, facility, queryParams, absUrl);

                var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('COUNT(ds)')
                    .from('Dataset', 'ds')
                    .from('ds.investigation', 'inv')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                            .and('inv.id = ?', queryParams.investigationId)
                    );

                var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('ds')
                    .from('Dataset', 'ds')
                    .from('ds.investigation', 'inv')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                            .and('inv.id = ?', queryParams.investigationId)
                    );

                if (angular.isDefined(queryParams)) {
                    if (angular.isDefined(queryParams.search) && queryParams.search.trim() !== '') {
                        query.where(
                            squel.expr()
                            .or('UPPER(ds.name) LIKE ?', '%' + queryParams.search.toUpperCase() + '%')
                        );
                    }

                    //set limit
                    if (angular.isDefined(queryParams.start) && angular.isDefined(queryParams.numRows)) {
                        query.limit(queryParams.start, queryParams.numRows);
                    }

                    //set sort
                    if (angular.isDefined(queryParams.sortField) && angular.isDefined(queryParams.order)) {
                        query.order(queryParams.sortField, sortOrder(queryParams.order));
                    }
                }

                var params = {
                    sessionId: mySessionId,
                    query: query.toString(),
                    countQuery: countQuery.toString(),
                    entity: 'Dataset',
                    server: facility.icatUrl
                };

                if (absUrl === true) {
                    return this.buildUrl(RESTAPIURL, params);
                } else {
                    return this.buildUrl('', params);
                }
            },

            getDatafiles: function(mySessionId, facility, queryParams, absUrl) {
                validateRequiredArguments(mySessionId, facility, queryParams, absUrl);

                var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('COUNT(df)')
                    .from('Datafile', 'df')
                    .from('df.dataset', 'ds')
                    .from('ds.investigation', 'inv')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                    );

                var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('df')
                    .from('Datafile', 'df')
                    .from('df.dataset', 'ds')
                    .from('ds.investigation', 'inv')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                    );

                if (angular.isDefined(queryParams)) {
                    if (angular.isDefined(queryParams.search) && queryParams.search.trim() !== '') {
                        query.where(
                            squel.expr()
                            .or('UPPER(df.name) LIKE ?', '%' + queryParams.search.toUpperCase() + '%')
                        );
                    }

                    //set limit
                    if (angular.isDefined(queryParams.start) && angular.isDefined(queryParams.numRows)) {
                        query.limit(queryParams.start, queryParams.numRows);
                    }

                    //set sort
                    if (angular.isDefined(queryParams.sortField) && angular.isDefined(queryParams.order)) {
                        query.order(queryParams.sortField, sortOrder(queryParams.order));
                    }
                }

                var params = {
                    sessionId: mySessionId,
                    query: query.toString(),
                    countQuery: countQuery.toString(),
                    entity: 'Datafile',
                    server: facility.icatUrl
                };

                if (absUrl === true) {
                    return this.buildUrl(RESTAPIURL, params);
                } else {
                    return this.buildUrl('', params);
                }
            },

            getDatafilesByInstrumentId: function(mySessionId, facility, queryParams, absUrl) {
                validateRequiredArguments(mySessionId, facility, queryParams, absUrl);

                var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('COUNT(df)')
                    .from('Datafile', 'df')
                    .from('df.dataset', 'ds')
                    .from('ds.investigation', 'inv')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                            .and('ds.id = ?', queryParams.datasetId)
                    );

                var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('df')
                    .from('Datafile', 'df')
                    .from('df.dataset', 'ds')
                    .from('ds.investigation', 'inv')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                            .and('ds.id = ?', queryParams.datasetId)
                    );

                if (angular.isDefined(queryParams)) {
                    if (angular.isDefined(queryParams.search) && queryParams.search.trim() !== '') {
                        query.where(
                            squel.expr()
                            .or('UPPER(df.name) LIKE ?', '%' + queryParams.search.toUpperCase() + '%')
                        );
                    }

                    //set limit
                    if (angular.isDefined(queryParams.start) && angular.isDefined(queryParams.numRows)) {
                        query.limit(queryParams.start, queryParams.numRows);
                    }

                    //set sort
                    if (angular.isDefined(queryParams.sortField) && angular.isDefined(queryParams.order)) {
                        query.order(queryParams.sortField, sortOrder(queryParams.order));
                    }
                }

                var params = {
                    sessionId: mySessionId,
                    query: query.toString(),
                    countQuery: countQuery.toString(),
                    entity: 'Datafile',
                    server: facility.icatUrl
                };

                if (absUrl === true) {
                    return this.buildUrl(RESTAPIURL, params);
                } else {
                    return this.buildUrl('', params);
                }
            },

            getDatafilesByInvestigationId: function(mySessionId, facility, queryParams, absUrl) {
                validateRequiredArguments(mySessionId, facility, queryParams, absUrl);

                var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('COUNT(df)')
                    .from('Datafile', 'df')
                    .from('df.dataset', 'ds')
                    .from('ds.investigation', 'inv')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                            .and('inv.id = ?', queryParams.investigationId)
                    );

                var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('df')
                    .from('Datafile', 'df')
                    .from('df.dataset', 'ds')
                    .from('ds.investigation', 'inv')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                            .and('inv.id = ?', queryParams.investigationId)
                    );

                if (angular.isDefined(queryParams)) {
                    if (angular.isDefined(queryParams.search) && queryParams.search.trim() !== '') {
                        query.where(
                            squel.expr()
                            .or('UPPER(df.name) LIKE ?', '%' + queryParams.search.toUpperCase() + '%')
                        );
                    }

                    //set limit
                    if (angular.isDefined(queryParams.start) && angular.isDefined(queryParams.numRows)) {
                        query.limit(queryParams.start, queryParams.numRows);
                    }

                    //set sort
                    if (angular.isDefined(queryParams.sortField) && angular.isDefined(queryParams.order)) {
                        query.order(queryParams.sortField, sortOrder(queryParams.order));
                    }
                }

                var params = {
                    sessionId: mySessionId,
                    query: query.toString(),
                    countQuery: countQuery.toString(),
                    entity: 'Datafile',
                    server: facility.icatUrl
                };

                if (absUrl === true) {
                    return this.buildUrl(RESTAPIURL, params);
                } else {
                    return this.buildUrl('', params);
                }
            },

            getDatafilesByDatasetId: function(mySessionId, facility, queryParams, absUrl) {
                validateRequiredArguments(mySessionId, facility, queryParams, absUrl);

                var countQuery = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('COUNT(df)')
                    .from('Datafile', 'df')
                    .from('df.dataset', 'ds')
                    .from('ds.investigation', 'inv')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                            .and('ds.id = ?', queryParams.datasetId)
                    );

                var query = squel.ICATSelect({ autoQuoteAliasNames: false })
                    .field('df')
                    .from('Datafile', 'df')
                    .from('df.dataset', 'ds')
                    .from('ds.investigation', 'inv')
                    .from('inv.facility', 'f')
                    .where(
                        squel.expr()
                            .and('f.id = ?', facility.facilityId)
                            .and('ds.id = ?', queryParams.datasetId)
                    );

                if (angular.isDefined(queryParams)) {
                    if (angular.isDefined(queryParams.search) && queryParams.search.trim() !== '') {
                        query.where(
                            squel.expr()
                            .or('UPPER(df.name) LIKE ?', '%' + queryParams.search.toUpperCase() + '%')
                        );
                    }

                    //set limit
                    if (angular.isDefined(queryParams.start) && angular.isDefined(queryParams.numRows)) {
                        query.limit(queryParams.start, queryParams.numRows);
                    }

                    //set sort
                    if (angular.isDefined(queryParams.sortField) && angular.isDefined(queryParams.order)) {
                        query.order(queryParams.sortField, sortOrder(queryParams.order));
                    }
                }

                var params = {
                    sessionId: mySessionId,
                    query: query.toString(),
                    countQuery: countQuery.toString(),
                    entity: 'Datafile',
                    server: facility.icatUrl
                };

                if (absUrl === true) {
                    return this.buildUrl(RESTAPIURL, params);
                } else {
                    return this.buildUrl('', params);
                }
            }
        };
    });