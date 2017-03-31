'use strict';

const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.mergeTeams = functions.database.ref('/team-indices/{uid}/{pushId}').onWrite(event => {
    const teamNumber = event.data.val();
    return event.data.ref.root.child('teams').child(event.data.key).once('value').then(function (team) {
        return event.data.ref.parent.once('value').then(function (snap) {
            return snap.forEach(function (team) {
            })
        });
    });
});
