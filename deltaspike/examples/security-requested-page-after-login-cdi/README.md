Making initially requested secured page available for redirect after login with CDI
===================================================================================

The following scenario is commonly seen in web applications:

1. User requests a web page
2. The web page is restricted to logged in users so the browser is redirected to login page
3. After successful login, the user is redirected back to the originally requested page

This example shows pure CDI implementation of this technique.
