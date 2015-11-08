# Facebook Comment Mention for Android
An android app, showcasing the implemtation of edittext, to mention facebook friends.

## Features
- mention facebook friends using "@"
- select from a list of friends based on the characters you entered after "@"
- generates a string, which can be posted with facebook graph api. Names mentioned will appear as facebook names once you post

## How to use?
1. Register your app at https://developers.facebook.com/apps
2. Replace the app id obtained, in strings.xml
3. Add facebook android sdk to your project https://developers.facebook.com/docs/android
4. Add CustomFacebookMentionEditText to your project
5. Implement CustomFacebookMentionEditText.Interface in your activity
6. Use getStatusString() to get the string to post to graph api. It will look like: Hello @['facebook_id'] , nice to meet you.
7. Upon posting, @['facebook_id'] will be replace by the mentioned facebook friend's name

## Credits
- Facebook

### Contact
For more question, find me at vertigogarden@gmail.com. I shall try to assist as much as possible.
