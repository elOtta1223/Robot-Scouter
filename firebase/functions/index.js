'use strict';

const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const root = '/debug';

exports.mergeTeams = functions.database.ref(root + '/team-indices/{uid}').onWrite(event => {
    const duplicates = JSON.parse(findDuplicates(event));
    console.log(duplicates);
    if (duplicates.hasDuplicates) return mergeTeams(duplicates);
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
        console.log(values);

        const team1 = values[0];
        const team2 = values[1];
        console.log(team1); // TODO remove
        console.log(team2);

        const oldestTeam = team1.timestamp < team2.timestamp ? team1 : team2;
        const duplicateTeam = oldestTeam === team1 ? team2 : team1;
        console.log(oldestTeam);
        console.log(duplicateTeam);

        removeExtraneousValues(oldestTeam);
        removeExtraneousValues(duplicateTeam);

        console.log("Removed extraneous values");
        console.log(oldestTeam);
        console.log(duplicateTeam);
    });
}

function removeExtraneousValues(team) {
    team.timestamp = 0;
}
