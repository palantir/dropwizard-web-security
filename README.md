dropwizard-web-security
=======================
[![Circle CI](https://circleci.com/gh/palantir/dropwizard-web-security.svg?style=svg&circle-token=52b148126fda6cfba213cb832ff733d04d0d7277)](https://circleci.com/gh/palantir/dropwizard-web-security)

A bundle for applying default web security functionality to a dropwizard application. It covers the following areas:

- [Cross-Origin Resource Sharing (CORS)](https://www.owasp.org/index.php/CORS_OriginHeaderScrutiny)
- [HTTP Strict Transport Security (HSTS)](https://www.owasp.org/index.php/HTTP_Strict_Transport_Security)
- Web Application Security Headers (Content Security Policy, etc.)

Usage
-----
1. Ensure your configuration implements `WebSecurityConfigurable`.
2. Add the bundle to your application.

	```java
	public class ExampleApplication extends Application<ExampleConfiguration> {
	
	    @Override
	    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
	        bootstrap.addBundle(new WebSecurityBundle());
	    }
	    
	    // ...
	}
	```

Just applying the bundle will add web app security headers to your application by default. **CORS and HSTS are turned
OFF by default.**


CORS Configuration
------------------
CORS is **disabled by default**. To turn on CORS, set the `allowedOrigins` method to a non-empty string.

The following are the default values, only specify values if they differ from the default values shown below.

```yaml
webSecurity:
  cors:
    allowCredentials: true
    allowedHeaders: "X-Requested-With,Content-Type,Accept,Origin"
    allowedMethods: "DELETE,GET,HEAD,POST,PUT"
    allowedOrigins: ""
    exposedHeaders: ""
    preflightMaxAge: 1800
```

**NOTE:** The values shown are from [`CrossOriginFilter`][1], except the following:

- `allowedOrigins` - set to blank instead of `"*"` to require the user to enter the allowed origins
- `allowedMethods` - set to include a default set of commonly used methods


App Security Configuration
--------------------------
App Security is **enabled by default**. It adds common security headers to your responses.

The following are the default values, only specify values if they differ from the default values shown below.

```yaml
webSecurity:
  appSecurity:
    enabled: true
    contentSecurityPolicy: "default-src 'self'"     # Content-Security-Policy and X-Content-Security-Policy
    contentTypeOptions: "nosniff"                   # X-Content-Type-Options
    frameOptions: "sameorigin"                      # X-Frame-Options
    hsts: ""                                        # Strict-Transport-Security
    xssProtection: "1; mode=block"                  # X-XSS-Protection
```

**NOTE:** To disable a specific header, set the value to `""`.


Advanced Usage
--------------
You can customize your application's defaults by defining it inside of your `initialize` method. Any value not set will
be set to the default values.

**Note:** the application default values will be **over-riden by the YAML defined values**.

```java
public class AdvancedApplication extends Application<AdvancedConfiguration> {
    @Override
    public void initialize(Bootstrap<AdvancedConfiguration> bootstrap) {

        // define the application defaults
        WebSecurityConfiguration applicationDefaults = new WebSecurityConfiguration.Builder()

                .cors(new CorsConfiguration.Builder()
                        .allowedOrigins("http://good.origin")
                        .allowedHeaders("Origin,Content-Type,Accept")
                        .preflightMaxAge(60 * 10)
                        .build())

                .appSecurity(AppSecurityConfiguration.DISABLED)

                .build();

        // apply the bundle with the application defaults
        bootstrap.addBundle(new WebSecurityBundle(applicationDefaults));
    }
}
```


License
-------
This project is made available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).

[1]: http://download.eclipse.org/jetty/9.2.13.v20150730/apidocs/org/eclipse/jetty/servlets/CrossOriginFilter.html
