const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

const admin = require('firebase-admin');
const test_user_ID = 'TQeQuLtjkdWp6kEVxGPhHAPNKuv1'; 
admin.initializeApp();
const CUT_OFF_TIME = 24 * 60 * 60 * 1000; // 24 Hours in milliseconds.
var database = admin.database();

exports.scheduledFunction = functions.pubsub.schedule('every 24 hours').onRun(async context => {
	var users = await listAllUsers();
	users.forEach(function(user){
		var token = '';
		var ref = database.ref('/users/' + user + '/token');
		ref.on("value", function(snapshot) {
			console.log(snapshot.val());
			token = snapshot.val();
		}, function (errorObject) {
			console.log("The read failed: " + errorObject.code);
		});
		var ref = database.ref('/users/' + user + '/appointments/');
		ref.once('value', function(snapshot) {
			snapshot.forEach(function(childSnapshot) {
				var getDate = childSnapshot.key.replace(/-/g, "/");
				var time = childSnapshot.child('time').val();
				var date = new Date(getDate + " " + time);
				var currentDate = new Date();
				var upcoming = date.getTime() - currentDate.getTime() <= CUT_OFF_TIME
				if(upcoming){
					console.log("Upcoming: " + date);
					
					const payload = {
						'notification': {
							title: 'Upcoming appointment!',
							body: 'You have an upcoming appointment on ' + getDate + ' at ' + time + '.'
						},
						'data' : {
							title: 'Upcoming appointment!',
							body: 'You have an upcoming appointment on ' + getDate + ' at ' + time + '.'
						}	
					 };
					 
					 admin.messaging().sendToDevice(token, payload)
					 .then(function (response) {
						console.log("Successfully sent message:", response);
					 })
					 .catch(function (error) {
						console.log("Error sending message:", error);
					 });
				}
				
			});
			
			
		});
	});
	return null;
});

async function listAllUsers(users = [], nextPageToken) {
	const result = await admin.auth().listUsers(1000, nextPageToken);
	result.users.forEach(function(userRecord) {
        console.log('user', userRecord.toJSON());
		users = users.concat(userRecord.uid);
    });
	
	if (result.pageToken) {
		return getInactiveUsers(users, result.pageToken);
	}
/*   // List batch of users, 1000 at a time.
  admin.auth().listUsers(1000, nextPageToken)
    .then(function(listUsersResult) {
      listUsersResult.users.forEach(function(userRecord) {
        //console.log('user', userRecord.toJSON());
		console.log('user', userRecord.uid);
		users.concat(userRecord.uid);
      });
      if (listUsersResult.pageToken) {
        // List next batch of users.
        listAllUsers(listUsersResult.pageToken);
      }
    })
    .catch(function(error) {
      console.log('Error listing users:', error);
    }); */
	//console.log("Users: " + users);
	return users;
}

function getUpcomingAppointments(){
}


 