dropwizard-web-security
=======================
[![Circle CI](https://circleci.com/gh/palantir/dropwizard-web-security.svg?style=shield&circle-token=52b148126fda6cfba213cb832ff733d04d0d7277)](https://circleci.com/gh/palantir/dropwizard-web-security)
[![Download](https://api.bintray.com/packages/palantir/releases/dropwizard-web-security/images/download.svg) ](https://bintray.com/palantir/releases/dropwizard-web-security/_latestVersion)

A bundle for applying default web security functionality to a dropwizard application. It covers the following areas:

- [Cross-Origin Resource Sharing (CORS)][cors1] [\[2\]][cors2] [\[3\]][cors3]
- Web Application Security Headers ([Content Security Policy][csp], [Strict Transport Security][hsts] etc.)


Usage
-----
1. Add the dependency to your project.

    ```groovy
    repository {
        jcenter()
    }

    dependencies {
        compile 'com.palantir.websecurity:dropwizard-web-security:<latest-version>'
    }
    ```

2. Ensure your configuration implements `WebSecurityConfigurable`.

    ```java
    public static final class ExampleConfiguration extends Configuration implements WebSecurityConfigurable {

        @JsonProperty("webSecurity")
        @NotNull
        @Valid
        private final WebSecurityConfiguration webSecurity = WebSecurityConfiguration.DEFAULT;

        public WebSecurityConfiguration getWebSecurityConfiguration() {
            return this.webSecurity;
        }
    }
    ```

3. Add the bundle to your application.

	```java
	public class ExampleApplication extends Application<ExampleConfiguration> {

	    @Override
	    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
	        bootstrap.addBundle(new WebSecurityBundle());
	    }
    }

    ```


Configuration
-------------
App Security headers are **added by default**. The following are the default values, **only specify values in your
configuration if they differ from the default values shown below**.

```yaml
webSecurity:
  strictTransportSecurity: "max-age=31536000"                                       # Strict-Transport-Security
  contentSecurityPolicy: "default-src 'self'; style-src 'self' 'unsafe-inline'"     # Content-Security-Policy and X-Content-Security-Policy
  contentTypeOptions: "nosniff"                                                     # X-Content-Type-Options
  frameOptions: "sameorigin"                                                        # X-Frame-Options
  xssProtection: "1; mode=block"                                                    # X-XSS-Protection
```

**NOTE:** To disable a specific header, set the value to `""`.


CORS Configuration
------------------
CORS is **disabled by default**. To enable CORS, set the `allowedOrigins` method to a non-empty string.

The following are the default values, only specify values if they differ from the default values shown below.

```yaml
webSecurity:
  cors:
    allowCredentials: false
    allowedHeaders: "Accept,Authorization,Content-Type,Origin,X-Requested-With"
    allowedMethods: "DELETE,GET,HEAD,POST,PUT"
    allowedOrigins: ""
    exposedHeaders: ""
    preflightMaxAge: 1800
```

**NOTE:** The values shown are from [`CrossOriginFilter`][corsfilter], except the following:

- `allowedOrigins` - set to blank instead of `"*"` to require the user to enter the allowed origins
- `allowCredentials` - set to false by default since credentials should be passed via the `Authorization` header
- `allowedHeaders` - set to include the default set of headers and the `Authorization` header
- `allowedMethods` - set to include a default set of commonly used methods


Advanced Usage
--------------

### App-Specific Settings
You can customize your application's defaults by defining it inside of your Dropwizard application. Any value not set
will be set to the default values.

**Note:** the application default values will be **overridden by the YAML defined values**.

```java
public static final class ExampleApplication extends Application<ExampleConfiguration> {

    private final WebSecurityConfiguration webSecurityDefaults = WebSecurityConfiguration.builder()

            // set app defaults for different header values
            .contentSecurityPolicy(CSP_FROM_APP)
            .contentTypeOptions(CTO_FROM_APP)

            // CORS is still DISABLED, since the allowedOrigins is not set, but the default value will be
            // respected if it's ever turned on
            .cors(CorsConfiguration.builder()
                    .preflightMaxAge(60 * 10)
                    .build())

            .build();

    private final WebSecurityBundle webSecurityBundle = new WebSecurityBundle(this.webSecurityDefaults);

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        bootstrap.addBundle(this.webSecurityBundle);
    }
}
```


### Using the Derived Configuration
You can also get the derived configuration to create a matching `WebSecurityHeaderInjector`:

```java
WebSecurityHeaderInjector injector = new WebSecurityHeaderInjector(webSecurityBundle.getDerivedConfiguration());
```


Contributing
------------
Before working on the code, if you plan to contribute changes, please read the [CONTRIBUTING](CONTRIBUTING.md) document.


License
-------
This project is made available under the [Apache 2.0 License][license].


[cors1]: https://www.w3.org/TR/cors/
[cors2]: https://www.owasp.org/index.php/CORS_OriginHeaderScrutiny
[cors3]: https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS
[csp]: https://developer.mozilla.org/en-US/docs/Web/Security/CSP
[hsts]: https://tools.ietf.org/html/rfc6797

[corsfilter]: https://github.com/eclipse/jetty.project/blob/jetty-9.2.13.v20150730/jetty-servlets/src/main/java/org/eclipse/jetty/servlets/CrossOriginFilter.java

[license]: http://www.apache.org/licenses/LICENSE-2.0
