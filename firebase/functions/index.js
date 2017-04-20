'use strict';

const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const root = '/debug';

exports.mergeTeams = functions.database.ref(root + '/team-indices/{uid}').onWrite(event => {
    const duplicates = findDuplicates(event);
    console.log("Duplicates: " + duplicates);
    if (duplicates.hasDuplicates) {
        return mergeTeams(duplicates);
    }
});

function findDuplicates(event) {
    let teamKey1 = null;
    let teamKey2 = null;

    let hasDuplicates = false;
    let teamKeys = [];
    let teamNumbers = [];

    const teams = event.data.current.toJSON();
    for (const key of Object.keys(teams)) {
        console.log(key, teams[key]);

        const teamNumber = teams[key];
        if (teamNumbers.indexOf(teamNumber) !== -1) {
            teamKey1 = teamKeys[teamNumbers.indexOf(teamNumber)];
            teamKey2 = key;
            hasDuplicates = true;
            break;
        }

        teamKeys.push(key);
        teamNumbers.push(teams[key]);
    }

    return JSON.stringify({
        hasDuplicates: hasDuplicates,
        teamKey1: teamKey1,
        teamKey2: teamKey2
    });
}

function mergeTeams(duplicates) {
    const teamRef = root + '/teams/';
    return Promise.all([
        admin.database().ref(teamRef + duplicates.teamKey1).once('value', (snap) => {
            return snap.val();
        }),
        admin.database().ref(teamRef + duplicates.teamKey2).once('value', (snap) => {
            return snap.val()
        })
    ]).then(values => {
        console.log("Teams: " + values);

        const teamKey1 = values[0];
        const teamKey2 = values[1];

        removeExtraneousValues(teamKey1);
        removeExtraneousValues(teamKey2);
        console.log("First team: " + teamKey1);
        console.log("Second team: " + teamKey2);
    });
}

function removeExtraneousValues(oldestTeam) {
    oldestTeam.timestamp = 0;
}
