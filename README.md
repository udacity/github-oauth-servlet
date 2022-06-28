[![Circle CI](https://circleci.com/gh/udacity/github-oauth-servlet.svg?style=svg)](https://circleci.com/gh/udacity/github-oauth-servlet)
[![Coverage Status](https://coveralls.io/repos/udacity/github-oauth-servlet/badge.svg?branch=master&service=github)](https://coveralls.io/github/udacity/github-oauth-servlet?branch=master)

github-oauth-servlet is a drop-in Servlet that adds GitHub
organization-based authentication to your J2EE project.

## Usage

Set the following configuration, either as envrionment variables or as
Java system properties:

* `GH_CLIENT_ID`: The client ID you received from GitHub when you
  [registered](https://github.com/settings/applications/new)
* `GH_CLIENT_SECRET`: The client secret you received from GitHub when
  you [registered](https://github.com/settings/applications/new).
* `GH_OAUTH_URL`: For production, use *https://github.com/login/oauth*.
  For development and testing, consider using a local mock server
  instead.
* `GH_API_URL`: For production, use *https://api.github.com*.  For
  development and testing, consider using a local mock server instead.
* `GH_ORG`: The GitHub organization that users must be a member of to
  authenticate.

Drop the Servlet into your *web.xml*:

```xml
<servlet>
  <servlet-name>github oauth servlet</servlet-name>
  <servlet-class>com.udacity.github.oauth.GitHubOauthServlet</servlet-class>
</servlet>

<servlet-mapping>
  <servlet-name>github oauth servlet</servlet-name>
  <url-pattern>/oauth/github/*</url-pattern>
</servlet-mapping>
```

To authenticate a user, redirect them to `/oauth/github[/$continue]`,
where `$continue` is a URI-encoded URL to redirect to after successful
authentication.

Upon successful authentication, the `auth-key` cookie will be set, with
which the user can be looked up via `GitHubOauthServlet#get`.
 
## References

* [GitHub API v3](https://developer.github.com/v3/)
* [GitHub OAuth](https://developer.github.com/v3/oauth/)

 # Archival Note 
 This repository is deprecated; therefore, we are going to archive it. However, learners will be able to fork it to their personal Github account but cannot submit PRs to this repository. If you have any issues or suggestions to make, feel free to: 
- Utilize the https://knowledge.udacity.com/ forum to seek help on content-specific issues. 
- Submit a support ticket along with the link to your forked repository if (learners are) blocked for other reasons. Here are the links for the [retail consumers](https://udacity.zendesk.com/hc/en-us/requests/new) and [enterprise learners](https://udacityenterprise.zendesk.com/hc/en-us/requests/new?ticket_form_id=360000279131).