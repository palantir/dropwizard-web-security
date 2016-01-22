dropwizard-web-security
=======================
[![Circle CI](https://circleci.com/gh/palantir/dropwizard-web-security.svg?style=svg&circle-token=52b148126fda6cfba213cb832ff733d04d0d7277)](https://circleci.com/gh/palantir/dropwizard-web-security)

A bundle for applying default web security functionality to a dropwizard application. It covers the following areas:

- [Cross-Origin Resource Sharing (CORS)][cors1] [\[2\]][cors2] [\[3\]][cors3]
- [HTTP Strict Transport Security (HSTS)][hsts1] [\[2\]][hsts2] [\[3\]][hsts3]
- Web Application Security Headers ([Content Security Policy][csp], etc.)


Usage
-----
1. Ensure your configuration implements `WebSecurityConfigurable`.
2. Add the bundle to your application:

	```java
	public class ExampleApplication extends Application<ExampleConfiguration> {

	    private final WebSecurityBundle webSecurityBundle = new WebSecurityBundle();

	    @Override
	    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
	        bootstrap.addBundle(webSecurityBundle);
	    }
    }

    ```

3. Add the `AppSecurityFilter` filter to your environment:

    ```java
    @Override
    public void run(ExampleConfiguration configuration, Environment environment) throws Exception {
        // will build a filter from the derived configuration
        AppSecurityFilter filter = new AppSecurityFilter(this.webSecurityBundle.getDerivedConfiguration());

        // add the filter to your environment
        environment.servlets()
                .addFilter("AppSecurityFilter", filter)
                .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
    ```


Configuration
-------------
App Security headers are **not added by default**. You will need to add the `AppSecurityFilter` to your environment
manually. See the example code for specifics.

The following are the default values, only specify values if they differ from the default values shown below.

```yaml
webSecurity:
  contentSecurityPolicy: "default-src 'self'"     # Content-Security-Policy and X-Content-Security-Policy
  contentTypeOptions: "nosniff"                   # X-Content-Type-Options
  frameOptions: "sameorigin"                      # X-Frame-Options
  hsts: ""                                        # Strict-Transport-Security
  xssProtection: "1; mode=block"                  # X-XSS-Protection
```

**NOTE:** To disable a specific header, set the value to `""`.


CORS Configuration
------------------
CORS is **disabled by default**. To enable CORS, set the `allowedOrigins` method to a non-empty string.

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

**NOTE:** The values shown are from [`CrossOriginFilter`][corsfilter], except the following:

- `allowedOrigins` - set to blank instead of `"*"` to require the user to enter the allowed origins
- `allowedMethods` - set to include a default set of commonly used methods


Advanced Usage
--------------
You can customize your application's defaults by defining it inside of your Dropwizard application. Any value not set
will be set to the default values.

**Note:** the application default values will be **overridden by the YAML defined values**.

```java
public static final class ExampleApplication extends Application<ExampleConfiguration> {

    private final WebSecurityConfiguration webSecurityDefaults = new WebSecurityConfiguration.Builder()

            // set app defaults for different header values
            .contentSecurityPolicy(CSP_FROM_APP)
            .contentTypeOptions(CTO_FROM_APP)

            // CORS is still DISABLED, since the allowedOrigins is not set, but the default value will be
            // respected if it's ever turned on
            .cors(new CorsConfiguration.Builder()
                    .preflightMaxAge(60 * 10)
                    .build())

            .build();

    private final WebSecurityBundle webSecurityBundle = new WebSecurityBundle(webSecurityDefaults);

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        bootstrap.addBundle(webSecurityBundle);
    }
}
```


License
-------
This project is made available under the [Apache 2.0 License][license].


[cors1]: https://www.w3.org/TR/cors/
[cors2]: https://www.owasp.org/index.php/CORS_OriginHeaderScrutiny
[cors3]: https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS
[hsts1]: https://tools.ietf.org/id/draft-ietf-websec-strict-transport-sec-14.txt
[hsts2]: https://www.owasp.org/index.php/HTTP_Strict_Transport_Security
[hsts3]: https://developer.mozilla.org/en-US/docs/Web/Security/HTTP_strict_transport_security
[csp]: https://developer.mozilla.org/en-US/docs/Web/Security/CSP

[corsfilter]: http://download.eclipse.org/jetty/9.2.13.v20150730/apidocs/org/eclipse/jetty/servlets/CrossOriginFilter.html

[license]: http://www.apache.org/licenses/LICENSE-2.0
